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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.underskrift.BaseSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpOrderResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.IntygGRPSignature;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("grp-rest-api")
public class GrpSignatureServiceImpl extends BaseSignatureService implements GrpSignatureService {

    private final ThreadPoolTaskExecutor taskExecutor;
    private final GrpCollectPollerFactory grpCollectPollerFactory;
    private final SignCertificateService signCertificateService;
    private final GrpRestClient grpRestClient;

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
            .withUserIpAddress(userIpAddress)
            .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }

    @Override
    public void startGrpCollectPoller(String personId, SignaturBiljett signaturBiljett) {
        final var orderResponse = grpRestClient.init(personId, signaturBiljett);
        log.info("Grp sign initiated for certificateId '{}' with transactionId: '{}'.", signaturBiljett.getIntygsId(),
            orderResponse.getTransactionId());
        updateTicketProperties(signaturBiljett, orderResponse);
        updateTicketTracker(signaturBiljett, orderResponse);
        validateOrderResponseId(signaturBiljett.getTicketId(), orderResponse.getTransactionId());
        startAsyncCollectPoller(orderResponse.getRefId(), orderResponse.getTransactionId());
    }

    private void validateOrderResponseId(String requestId, String responseId) {
        if (!requestId.equals(responseId)) {
            throw new IllegalStateException("GrpOrderResponse transactionId did not match GrpOrderRequest transactionId.");
        }
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

    private void updateTicketProperties(SignaturBiljett ticket, GrpOrderResponse response) {
        ticket.setAutoStartToken(response.getAutoStartToken());
        ticket.setQrStartToken(response.getQrStartToken());
        ticket.setQrStartSecret(response.getQrStartSecret());
    }

    private void updateTicketTracker(SignaturBiljett ticket, GrpOrderResponse response) {
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

    private void startAsyncCollectPoller(String refId, String transactionId) {
        final var collectTask = grpCollectPollerFactory.getInstance();
        collectTask.setRefId(refId);
        collectTask.setTransactionId(transactionId);
        collectTask.setSecurityContext(SecurityContextHolder.getContext());
        taskExecutor.execute(collectTask);
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
