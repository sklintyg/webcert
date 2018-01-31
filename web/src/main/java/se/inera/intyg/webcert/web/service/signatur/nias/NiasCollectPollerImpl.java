/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.signatur.nias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;
import com.secmaker.netid.nias.v1.ResultCollect;

import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Runnable implementation / spring prototype bean responsible for performing once NIAP collect lifecycle for a single
 * signerings attempt over SecMaker's NetiD Access Server API.
 *
 * Will for up to {@link NiasCollectPollerImpl#TIMEOUT} milliseconds issue a NIAS "collect" every 3 seconds and act on
 * the
 * response.
 *
 * The typical flow of a NIAS authentication/collect is that a parent thread issues a Sign request to the
 * NIAS API and stores the returned orderRef. An instance of this class then goes into a loop and
 * issues a "collect" request until either the may loop times out, an Exception is thrown from the API or a terminating
 * progressStatus is returned. On COMPLETE the operation has
 * successfully finished (e.g. the user has used NetiD compatible device for signing) and
 * we can notify waiting parties about the success.
 *
 * Note that we set the copied {@link SecurityContext} onto the ThreadLocal since the "skickaIntyg" requires the
 * Principal to be available on the {@link SecurityContextHolder}.
 */
@Component(value = "niasCollectPoller")
@Scope(value = "prototype")
public class NiasCollectPollerImpl implements NiasCollectPoller {

    private static final Logger LOG = LoggerFactory.getLogger(NiasCollectPollerImpl.class);

    private static final long TIMEOUT = 240000L; // 4 minutes, normally an EXPIRED_TRANSACTION will be returned after 3.

    private String orderRef;
    private String transactionId;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private NetiDAccessServerSoap netiDAccessServerSoap;

    private final long defaultSleepMs = 3000L;
    private long ms = defaultSleepMs;
    private SecurityContext securityContext;

    @Override
    public void run() {
        try {
            applySecurityContextToThreadLocal();
            WebCertUser webCertUser = (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            long startTimeMs = System.currentTimeMillis();

            while ((startTimeMs + TIMEOUT) > System.currentTimeMillis()) {

                try {

                    ResultCollect resp = netiDAccessServerSoap.collect(orderRef);
                    LOG.info("NIAS collect for '{}' returned progressStatus: '{}'", orderRef, resp.getProgressStatus());
                    switch (resp.getProgressStatus()) {
                    case "COMPLETE":
                        String subjectSerialNumber = resp.getUserInfo().getPersonalNumber();
                        if (!subjectSerialNumber.replaceAll("\\-", "").equals(webCertUser.getPersonId().replaceAll("\\-", ""))) {
                            throw new IllegalStateException(
                                    "Could not process NIAS Collect COMPLETE response, subject serialNumber did not match "
                                            + "issuing WebCertUser.");
                        }

                        String signature = resp.getSignature();
                        signaturService.clientGrpSignature(transactionId, signature, webCertUser);
                        LOG.info("NetiD Access Server Signature was successfully persisted and ticket updated.");
                        return;
                    case "USER_SIGN":
                        signaturTicketTracker.updateStatus(transactionId, SignaturTicket.Status.VANTA_SIGN);
                        break;
                    case "OUTSTANDING_TRANSACTION":
                    case "STARTED":
                    case "USER_REQ":
                        break;
                    case "NO_CLIENT":
                        signaturTicketTracker.updateStatus(transactionId, SignaturTicket.Status.NO_CLIENT);
                        LOG.info("NIAS collect returned ProgressStatusType: {}, "
                                + "has the user started their NetID Access application?",
                                resp.getProgressStatus());
                        break;
                    }

                } catch (Exception ex) {
                    LOG.error("Error occurred handling collect response: " + ex.getMessage());
                    // Always terminate loop after an Exception has been encountered
                    return;
                }

                sleepMs(ms);
            }
        } finally {
            // Since this poller thread will be returned to its thread pool, we make sure we clean up the security
            // context we bound to this runnable's threadlocal.
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Since Spring doesn't (and shouldn't!) copy the threadlocal context from the thread that issued this worker
     * thread,
     * we manually set the {@link SecurityContext} set on this instance on the currently executing thread using the
     * Spring static {@link SecurityContextHolder} mechanism.
     *
     * Make sure we clean up when the Runnable exits.
     */
    private void applySecurityContextToThreadLocal() {
        if (securityContext == null) {
            throw new IllegalStateException(
                    "Cannot start NIAS poller thread, no securityContext was bound to the NiasCollectPollerImpl instance.");
        }
        SecurityContextHolder.setContext(securityContext);
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOG.warn("Sleep was interrupted: " + e.getMessage());
        }
    }

    /**
     * Use this for unit-testing purposes only.
     */
    void setMs(long ms) {
        this.ms = ms;
    }

    @Override
    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }

    @Override
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }
}
