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
package se.inera.intyg.webcert.web.service.underskrift.grp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
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
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.underskrift.BaseSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.IntygGRPSignature;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class GrpUnderskriftServiceImpl extends BaseSignatureService implements CommonUnderskriftService, GrpUnderskriftService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpUnderskriftServiceImpl.class);

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
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private GrpServicePortType grpService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private GrpCollectPollerFactory grpCollectPollerFactory;

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String intygJson) {
        String hash = createHash(intygJson);

        IntygGRPSignature intygGRPSignature = new IntygGRPSignature(intygJson, hash);

        SignaturBiljett biljett = SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                .withTicketId(UUID.randomUUID().toString())
                .withIntygsId(intygsId)
                .withVersion(version)
                .withIntygSignature(intygGRPSignature)
                .withStatus(SignaturStatus.BEARBETAR)
                .withSkapad(LocalDateTime.now())
                .withHash(intygGRPSignature.getSigningData())
                .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }

    @Override
    public void startGrpCollectPoller(String personId, SignaturBiljett signaturBiljett) {

        AuthenticateRequestType authRequest = buildAuthRequest(personId, signaturBiljett);

        OrderResponseType orderResponse;
        try {
            orderResponse = grpService.authenticate(authRequest);
        } catch (GrpFault grpFault) {
            redisTicketTracker.updateStatus(signaturBiljett.getTicketId(), SignaturStatus.OKAND);

            Optional<FaultStatusType> status = Optional.ofNullable(grpFault.getFaultInfo()).map(GrpFaultType::getFaultStatus);
            if (status.isPresent()) {
                LOG.warn("Fault signing utkast with id {} with GRP. FaultStatus: {}", signaturBiljett.getIntygsId(), status.get().name());
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, status.get().name());
            } else {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, grpFault.getMessage());
            }
        }

        String orderRef = orderResponse.getOrderRef();
        String ticketId = validateOrderResponseTxId(authRequest, orderResponse);

        startAsyncCollectPoller(orderRef, ticketId);
    }

    @Override
    public SignaturBiljett finalizeGrpSignature(SignaturBiljett biljett, byte[] signatur, String certifikat, Utkast utkast,
            WebCertUser user) {
        return finalizePkcs7Signature(user, biljett, new String(signatur, Charset.forName("UTF-8")), utkast);
    }

    // Used for BankID / Mobilt BankID.
    private SignaturBiljett finalizePkcs7Signature(WebCertUser user, SignaturBiljett biljett, String rawSignature, Utkast utkast) {
        final String payloadJson = biljett.getIntygSignature().getIntygJson();
        checkIntysId(utkast, biljett);
        checkVersion(utkast, biljett);

        Signatur signatur = new Signatur(biljett.getSkapad(), user.getHsaId(), biljett.getIntygsId(), payloadJson,
                biljett.getHash(), rawSignature, SignaturTyp.PKCS7);
        Utkast savedUtkast = updateAndSaveUtkast(utkast, payloadJson, signatur, user);

        // Send to Intygstjanst
        intygService.storeIntyg(savedUtkast);

        // If all good, change status of ticket
        biljett.setStatus(SignaturStatus.SIGNERAD);

        return biljett;
    }

    private AuthenticateRequestType buildAuthRequest(String personId, SignaturBiljett signaturBiljett) {
        AuthenticateRequestType authRequest = new AuthenticateRequestType();
        authRequest.setPersonalNumber(personId);
        authRequest.setTransactionId(signaturBiljett.getTicketId());
        authRequest.setPolicy(serviceId);
        authRequest.setProvider(BANK_ID_PROVIDER);
        authRequest.setDisplayName(displayName);
        return authRequest;
    }

    private void startAsyncCollectPoller(String orderRef, String ticketId) {
        GrpCollectPoller collectTask = grpCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTicketId(ticketId);
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

    private String createHash(String payload) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes("UTF-8"));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
