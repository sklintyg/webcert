/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import com.mobilityguard.grp.service.v2.AuthenticateRequestType;
import com.mobilityguard.grp.service.v2.FaultStatusType;
import com.mobilityguard.grp.service.v2.GrpFaultType;
import com.mobilityguard.grp.service.v2.OrderResponseType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.funktionstjanster.grp.v2.AuthenticateRequestTypeV23;
import se.funktionstjanster.grp.v2.GrpException;
import se.funktionstjanster.grp.v2.GrpServicePortType;
import se.funktionstjanster.grp.v2.OrderResponseTypeV23;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.underskrift.BaseSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.IntygGRPSignature;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@Profile("!grp-rest-api")
public class GrpUnderskriftServiceImpl extends BaseSignatureService implements GrpSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpUnderskriftServiceImpl.class);

    static final String BANK_ID_PROVIDER = "bankid"; // As specified in CGI GRP docs

    /**
     * Assigned to us by the GRP provider (e.g. CGI). Used in the 'policy' attribute of auth and collect requests.
     */
    @Value("${cgi.grp.serviceId}")
    private String serviceId;

    /**
     * Note that this value must be fetched from a props file encoded in ISO-8859-1 if it contains non ascii-7 chars.
     */
    @Value("${cgi.grp.displayName}")
    private String displayName;

    private final GrpServicePortType grpService;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final GrpCollectPollerFactory grpCollectPollerFactory;
    private final SignCertificateService signCertificateService;

    public GrpUnderskriftServiceImpl(GrpServicePortType grpService, ThreadPoolTaskExecutor taskExecutor,
        GrpCollectPollerFactory grpCollectPollerFactory, SignCertificateService signCertificateService) {
        this.grpService = grpService;
        this.taskExecutor = taskExecutor;
        this.grpCollectPollerFactory = grpCollectPollerFactory;
        this.signCertificateService = signCertificateService;
    }

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, Optional<String> intygJson,
        SignMethod signMethod, String ticketId, String userIpAddress, String certificateXml) {
        final var jsonData = intygJson.orElse(null);
        final var hash = intygJson.map(this::createHash).orElse(null);
        final var intygGRPSignature = new IntygGRPSignature(jsonData, hash);
        final var biljett = SignaturBiljett.SignaturBiljettBuilder
            .aSignaturBiljett(UUID.randomUUID().toString(), SignaturTyp.PKCS7, signMethod)
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
        final var authRequest = buildAuthRequest(personId, signaturBiljett);

        OrderResponseTypeV23 orderResponse;
        try {
            orderResponse = grpService.authenticate(authRequest);
            updateTicketProperties(signaturBiljett, orderResponse);
            updateTicketTracker(signaturBiljett, orderResponse);
        } catch (GrpException grpException) {
            redisTicketTracker.updateStatus(signaturBiljett.getTicketId(), SignaturStatus.OKAND);

            Optional<FaultStatusType> status = Optional.ofNullable(grpException.getFaultInfo()).map(GrpFaultType::getFaultStatus);
            if (status.isPresent()) {
                LOG.warn("Fault signing utkast with id {} with GRP. FaultStatus: {}", signaturBiljett.getIntygsId(), status.get().name());
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, status.get().name());
            } else {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                    grpException.getFaultInfo().getDetailedDescription());
            }
        }

        final var orderRef = orderResponse.getOrderRef();
        final var ticketId = validateOrderResponseTxId(authRequest, orderResponse);
        startAsyncCollectPoller(orderRef, ticketId);
    }

    @Override
    public SignaturBiljett finalizeSignature(SignaturBiljett biljett, byte[] signatur, String certifikat, Utkast utkast,
        WebCertUser user) {
        SignaturBiljett sb = finalizePkcs7Signature(user, biljett, new String(signatur, StandardCharsets.UTF_8), utkast);
        return redisTicketTracker.updateStatus(sb.getTicketId(), sb.getStatus());
    }

    @Override
    public FinalizedCertificateSignature finalizeSignatureForCS(SignaturBiljett ticket, byte[] signatur, String certifikat) {
        final var certificate = signCertificateService.signWithoutSignature(ticket.getIntygsId(), ticket.getVersion());
        ticket.setStatus(SignaturStatus.SIGNERAD);

        redisTicketTracker.updateStatus(
            ticket.getTicketId(),
            ticket.getStatus()
        );

        return FinalizedCertificateSignature.builder()
            .certificate(certificate)
            .signaturBiljett(ticket)
            .build();
    }

    private void updateTicketProperties(SignaturBiljett ticket, OrderResponseTypeV23 response) {
        ticket.setAutoStartToken(response.getAutoStartToken());
        ticket.setQrStartToken(response.getQrStartToken());
        ticket.setQrStartSecret(response.getQrStartSecret());
    }

    private void updateTicketTracker(SignaturBiljett ticket, OrderResponseTypeV23 response) {
        final var ticketId = ticket.getTicketId();
        redisTicketTracker.updateAutoStartToken(ticketId, response.getAutoStartToken());
        redisTicketTracker.updateQrCodeProperties(ticketId, response.getQrStartToken(), response.getQrStartSecret());
    }

    // Used for BankID / Mobilt BankID.
    private SignaturBiljett finalizePkcs7Signature(WebCertUser user, SignaturBiljett biljett, String rawSignature, Utkast utkast) {
        final var payloadJson = biljett.getIntygSignature().getIntygJson();
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

    private AuthenticateRequestTypeV23 buildAuthRequest(String personId, SignaturBiljett signaturBiljett) {
        final var authRequest = new AuthenticateRequestTypeV23();
        authRequest.setSubjectIdentifier(personId);
        authRequest.setTransactionId(signaturBiljett.getTicketId());
        authRequest.setPolicy(serviceId);
        authRequest.setProvider(BANK_ID_PROVIDER);
        authRequest.setRpDisplayName(displayName);
        return authRequest;
    }

    private void startAsyncCollectPoller(String orderRef, String ticketId) {
        final var collectTask = grpCollectPollerFactory.getInstance();
        collectTask.setRefId(orderRef);
        collectTask.setTransactionId(ticketId);
        collectTask.setMdcContextMap(MDC.getCopyOfContextMap());
        collectTask.setSecurityContext(SecurityContextHolder.getContext());
        taskExecutor.execute(collectTask);
    }

    private String validateOrderResponseTxId(AuthenticateRequestType authRequest, OrderResponseType orderResponse) {
        final var transactionId = orderResponse.getTransactionId();
        if (!authRequest.getTransactionId().equals(transactionId)) {
            throw new IllegalStateException("OrderResponse transactionId did not match AuthenticateRequest one.");
        }
        return transactionId;
    }

    private String createHash(String payload) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
