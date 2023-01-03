/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.grp;

import java.nio.charset.Charset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.Property;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Runnable implementation / spring prototype bean responsible for performing once GRP collect lifecycle for a single
 * signerings attempt over CGI's GRP API.
 *
 * Will for up to {@link GrpCollectPollerImpl#TIMEOUT} milliseconds issue a GRP "collect" every 3 seconds and act on the
 * response.
 *
 * The typical flow of a GRP authentication/collect is that a parent thread issues an AuthenticationRequest to the
 * GRP API and stores the AuthenticationResponse orderRef. An instance of this class then goes into a loop and
 * issues a "collect" request until either the may loop times out, a GrpFault is thrown from the API or a terminating
 * ProgressStatusType is returned. On {@link se.funktionstjanster.grp.v1.ProgressStatusType#COMPLETE} the operation has
 * successfully finished (e.g. the user has used BankID or Mobilt BankID and successfully authenticated themselves) and
 * we can notify waiting parties about the success.
 *
 * Note that we set the copied {@link SecurityContext} onto the ThreadLocal since the "skickaIntyg" requires the
 * Principal to be
 * available on the {@link SecurityContextHolder}.
 */
@Component(value = "grpCollectPoller")
@Scope(value = "prototype")
public class GrpCollectPollerImpl implements GrpCollectPoller {

    private static final Logger LOG = LoggerFactory.getLogger(GrpCollectPollerImpl.class);

    private static final long TIMEOUT = 240000L; // 4 minutes, normally an EXPIRED_TRANSACTION will be returned after 3.

    private String orderRef;
    private String ticketId;

    @Value("${cgi.grp.serviceId}")
    private String serviceId;

    @Value("${cgi.grp.displayName}")
    private String displayName;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private UnderskriftService underskriftService;

    @Autowired
    private GrpServicePortType grpService;

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

                CollectRequestType req = buildCollectRequest();
                try {

                    CollectResponseType resp = grpService.collect(req);
                    LOG.info("GRP collect returned ProgressStatusType: {}", resp.getProgressStatus());
                    switch (resp.getProgressStatus()) {
                        case COMPLETE:
                            String subjectSerialNumber = getCollectResponseAttribute(resp.getAttributes());
                            if (!subjectSerialNumber.replaceAll("\\-", "").equals(webCertUser.getPersonId().replaceAll("\\-", ""))) {
                                throw new IllegalStateException(
                                    "Could not process GRP Collect COMPLETE response, subject serialNumber did not match "
                                        + "issuing WebCertUser.");
                            }

                            String signature = resp.getSignature();
                            underskriftService.grpSignature(ticketId, signature.getBytes(Charset.forName("UTF-8")));
                            LOG.info("Signature was successfully persisted and ticket updated.");
                            return;
                        case USER_SIGN:
                            redisTicketTracker.updateStatus(ticketId, SignaturStatus.VANTA_SIGN);
                            break;
                        case OUTSTANDING_TRANSACTION:
                        case STARTED:
                        case USER_REQ:
                            break;
                        case NO_CLIENT:
                            redisTicketTracker.updateStatus(ticketId, SignaturStatus.NO_CLIENT);
                            LOG.info("GRP collect returned ProgressStatusType: {}, "
                                    + "has the user started their BankID or Mobilt BankID application?",
                                resp.getProgressStatus());
                            break;
                    }

                } catch (GrpFault grpFault) {
                    handleGrpFault(grpFault);
                    // Always terminate loop after a GrpFault has been encountered
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
                "Cannot start GRP poller thread, no securityContext was bound to the GrpCollectPollerImpl instance.");
        }
        SecurityContextHolder.setContext(securityContext);
    }

    private String getCollectResponseAttribute(List<Property> attributes) {
        for (Property p : attributes) {
            if ("Subject.SerialNumber".equals(p.getName())) {
                return p.getValue();
            }
        }
        throw new IllegalStateException(
            "Cannot use GRP collect to sign certificate, the signing identity is not the same as the user who initiated "
                + "the GRP authentication request");
    }

    private void handleGrpFault(GrpFault grpFault) {
        redisTicketTracker.updateStatus(ticketId, SignaturStatus.OKAND);
        switch (grpFault.getFaultInfo().getFaultStatus()) {
            case CLIENT_ERR:
                LOG.error("GRP collect failed with CLIENT_ERR, message: {}", grpFault.getFaultInfo().getDetailedDescription());
                break;
            case USER_CANCEL:
                LOG.info("User cancelled BankID signing.");
                break;
            case ALREADY_COLLECTED:
            case EXPIRED_TRANSACTION:
                LOG.info("GRP collect failed with status {}, this is expected "
                        + "when the user doesn't start their BankID client and transaction times out after ~3 minutes.",
                    grpFault.getFaultInfo().getFaultStatus());
                break;
            default:
                LOG.error("Unexpected GrpFault thrown when performing GRP collect: {}. Message: {}",
                    grpFault.getFaultInfo().getFaultStatus().toString(),
                    grpFault.getFaultInfo().getDetailedDescription());
                break;
        }
    }

    private CollectRequestType buildCollectRequest() {
        CollectRequestType req = new CollectRequestType();
        req.setOrderRef(orderRef);
        req.setTransactionId(ticketId);
        req.setPolicy(serviceId);
        req.setDisplayName(displayName);
        req.setProvider(GrpUnderskriftServiceImpl.BANK_ID_PROVIDER);
        return req;
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
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    @Override
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }
}
