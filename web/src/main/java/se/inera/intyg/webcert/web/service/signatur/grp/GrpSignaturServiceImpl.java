/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.signatur.grp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.funktionstjanster.grp.v1.AuthenticateRequestType;
import se.funktionstjanster.grp.v1.FaultStatusType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpFaultType;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.OrderResponseType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Optional;

/**
 *
 *
 * Created by eriklupander on 2015-08-21.
 */
@Service
public class GrpSignaturServiceImpl implements GrpSignaturService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpSignaturServiceImpl.class);

    static final String BANK_ID_PROVIDER = "bankid"; // As specified in CGI GRP docs

    /** Assigned to us by the GRP provider (e.g. CGI). Used in the 'policy' attribute of auth and collect requests. */
    @Value("${cgi.grp.serviceId}")
    private String serviceId;

    /**
     * Note that this value must be fetched from a props file encoded in ISO-8859-1 if it contains non ascii-7 chars.
     */
    @Value("${cgi.grp.displayName}")
    private String displayName;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private GrpServicePortType grpService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private GrpCollectPollerFactory grpCollectPollerFactory;

    @Override
    public SignaturTicket startGrpAuthentication(String intygId, long version) {

        Utkast utkast = utkastRepository.findOne(intygId);
        validateUtkast(intygId, utkast);

        WebCertUser webCertUser = webCertUserService.getUser();
        validateWebCertUser(webCertUser);

        String personId = webCertUser.getPersonId();
        validatePersonId(personId);

        SignaturTicket draftHash = signaturService.createDraftHash(intygId, utkast.getVersion());

        AuthenticateRequestType authRequest = buildAuthRequest(personId, draftHash);

        OrderResponseType orderResponse;
        try {
            orderResponse = grpService.authenticate(authRequest);
        } catch (GrpFault grpFault) {
            signaturTicketTracker.updateStatus(draftHash.getId(), SignaturTicket.Status.OKAND);

            Optional<FaultStatusType> status = Optional.ofNullable(grpFault.getFaultInfo()).map(GrpFaultType::getFaultStatus);
            if (status.isPresent()) {
                LOG.warn("Fault signing utkast with id {} with GRP. FaultStatus: {}", intygId, status.get().name());
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, status.get().name());
            } else {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, grpFault.getMessage());
            }
        }

        // If we could init the authentication, we create a SignaturTicket, reusing the mechanism already present for
        // SITHS
        String orderRef = orderResponse.getOrderRef();

        String transactionId = validateOrderResponseTxId(authRequest, orderResponse);

        startAsyncCollectPoller(webCertUser, orderRef, transactionId);
        return draftHash;
    }

    private void startAsyncCollectPoller(WebCertUser webCertUser, String orderRef, String transactionId) {
        GrpCollectPoller collectTask = grpCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTransactionId(transactionId);
       // collectTask.setWebCertUser(webCertUser);
        collectTask.setSecurityContext(SecurityContextHolder.getContext());
        final long startTimeout = 6000L;
        taskExecutor.execute(collectTask, startTimeout);
    }

    private String validateOrderResponseTxId(AuthenticateRequestType authRequest, OrderResponseType orderResponse) {
        String transactionId = orderResponse.getTransactionId();
        if (!authRequest.getTransactionId().equals(transactionId)) {
            throw new IllegalStateException("OrderResponse transactionId did not match AuthenticateRequest one.");
        }
        return transactionId;
    }

    private void validatePersonId(String personId) {
        if (personId == null) {
            throw new IllegalArgumentException("User principal contained no personId. Cannot issue a GRP auth request "
                    + "without a valid personId. This condition could theoretically occur if a SITHS-logged in lakare "
                    + "accidently managed to init a signing with BankID.");
        }
    }

    private void validateWebCertUser(WebCertUser webCertUser) {
        if (webCertUser == null) {
            throw new IllegalArgumentException("Could not send GRP authenticate request, no user principal found in session.");
        }
    }

    private void validateUtkast(String intygId, Utkast utkast) {
        if (utkast == null) {
            throw new IllegalArgumentException("Could not send GRP authenticate request, no Utkast found for intygId '" + intygId + "'");
        }
    }

    private AuthenticateRequestType buildAuthRequest(String personId, SignaturTicket draftHash) {
        AuthenticateRequestType authRequest = new AuthenticateRequestType();
        authRequest.setPersonalNumber(personId);
        authRequest.setTransactionId(draftHash.getId());
        authRequest.setPolicy(serviceId);
        authRequest.setProvider(BANK_ID_PROVIDER);
        authRequest.setDisplayName(displayName);
        return authRequest;
    }
}
