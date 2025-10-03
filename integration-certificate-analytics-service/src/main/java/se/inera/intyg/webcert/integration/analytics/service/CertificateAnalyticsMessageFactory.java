/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.analytics.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.common.dto.IncomingComplementDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.common.dto.SentByDTO;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUserService;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsCertificate;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsCertificateRelation;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsEvent;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsRecipient;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage.CertificateAnalyticsMessageBuilder;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@Component
@RequiredArgsConstructor
public class CertificateAnalyticsMessageFactory {

    private final LoggedInWebcertUserService loggedInWebcertUserService;

    public CertificateAnalyticsMessage draftCreated(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.DRAFT_CREATED).build();
    }

    public CertificateAnalyticsMessage draftCreated(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_CREATED).build();
    }

    public CertificateAnalyticsMessage draftDeleted(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.DRAFT_DELETED).build();
    }

    public CertificateAnalyticsMessage draftDeleted(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_DELETED).build();
    }

    public CertificateAnalyticsMessage draftUpdated(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_UPDATED).build();
    }

    public CertificateAnalyticsMessage draftUpdated(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.DRAFT_UPDATED).build();
    }

    public CertificateAnalyticsMessage draftReadyForSign(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN).build();
    }

    public CertificateAnalyticsMessage draftReadyForSign(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN).build();
    }

    public CertificateAnalyticsMessage lockedDraftRevoked(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.LOCKED_DRAFT_REVOKED).build();
    }

    public CertificateAnalyticsMessage draftCreateFromTemplate(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_CREATED_FROM_TEMPLATE).build();
    }

    public CertificateAnalyticsMessage certificateSigned(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_SIGNED).build();
    }

    public CertificateAnalyticsMessage certificateSigned(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_SIGNED).build();
    }

    public CertificateAnalyticsMessage certificateSent(Certificate certificate, String recipientId) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_SENT)
            .recipient(
                AnalyticsRecipient.builder()
                    .id(recipientId)
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage certificateSent(Utkast utkast, String recipientId) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_SENT)
            .recipient(
                AnalyticsRecipient.builder()
                    .id(recipientId)
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage certificateRenewed(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_RENEWED).build();
    }

    public CertificateAnalyticsMessage certificateRenewed(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_RENEWED).build();
    }

    public CertificateAnalyticsMessage certificateReplace(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_REPLACED).build();
    }

    public CertificateAnalyticsMessage certificateReplace(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_REPLACED).build();
    }

    public CertificateAnalyticsMessage certificateComplemented(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_COMPLEMENTED).build();
    }

    public CertificateAnalyticsMessage certificateComplemented(Utkast certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_COMPLEMENTED).build();
    }

    public CertificateAnalyticsMessage certificateRevoked(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_REVOKED).build();
    }

    public CertificateAnalyticsMessage certificateRevoked(Utlatande utlatande) {
        return create(utlatande, CertificateAnalyticsMessageType.CERTIFICATE_REVOKED);
    }

    public CertificateAnalyticsMessage certificatePrinted(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_PRINTED).build();
    }

    public CertificateAnalyticsMessage certificatePrinted(Utlatande utlatande) {
        return create(utlatande, CertificateAnalyticsMessageType.CERTIFICATE_PRINTED);
    }

    public CertificateAnalyticsMessage receivedMesssage(Certificate certificate, IncomingMessageRequestDTO incomingMessageRequest) {
        return create(certificate, messageTypeForIncomingMessage(incomingMessageRequest))
            .message(
                AnalyticsMessage.builder()
                    .id(incomingMessageRequest.getId())
                    .answerId(incomingMessageRequest.getAnswerMessageId())
                    .reminderId(incomingMessageRequest.getReminderMessageId())
                    .type(incomingMessageRequest.getType().name())
                    .questionIds(
                        incomingMessageRequest.getComplements() == null ? null :
                            incomingMessageRequest.getComplements().stream()
                                .map(IncomingComplementDTO::getQuestionId)
                                .toList()
                    )
                    .sender(incomingMessageRequest.getSentBy().getCode())
                    .recipient(SentByDTO.WC.getCode())
                    .sent(incomingMessageRequest.getSent())
                    .lastDateToAnswer(incomingMessageRequest.getLastDateToAnswer())
                    .build()
            )
            .build();
    }

    private CertificateAnalyticsMessageBuilder create(Certificate certificate, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                AnalyticsCertificate.builder()
                    .id(certificate.getMetadata().getId())
                    .type(certificate.getMetadata().getType())
                    .typeVersion(certificate.getMetadata().getTypeVersion())
                    .patientId(certificate.getMetadata().getPatient().getPersonId().getId())
                    .unitId(certificate.getMetadata().getUnit().getUnitId())
                    .careProviderId(certificate.getMetadata().getCareProvider().getUnitId())
                    .parent(
                        createCertificateAnalyticsRelation(certificate)
                    )
                    .build()
            );
    }

    private AnalyticsCertificateRelation createCertificateAnalyticsRelation(Certificate certificate) {
        if (certificate.getMetadata().getRelations() == null || certificate.getMetadata().getRelations().getParent() == null) {
            return null;
        }
        return AnalyticsCertificateRelation.builder()
            .id(certificate.getMetadata().getRelations().getParent().getCertificateId())
            .type(certificate.getMetadata().getRelations().getParent().getType().name())
            .build();
    }

    private CertificateAnalyticsMessageBuilder create(Utkast utkast, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                AnalyticsCertificate.builder()
                    .id(utkast.getIntygsId())
                    .type(utkast.getIntygsTyp())
                    .typeVersion(utkast.getIntygTypeVersion())
                    .patientId(utkast.getPatientPersonnummer().getPersonnummerWithDash())
                    .unitId(utkast.getEnhetsId())
                    .careProviderId(utkast.getVardgivarId())
                    .parent(
                        createCertificateAnalyticsRelation(utkast)
                    )
                    .build()
            );
    }

    private AnalyticsCertificateRelation createCertificateAnalyticsRelation(Utkast utkast) {
        if (utkast.getRelationIntygsId() == null) {
            return null;
        }
        return AnalyticsCertificateRelation.builder()
            .id(utkast.getRelationIntygsId())
            .type(
                switch (utkast.getRelationKod()) {
                    case ERSATT -> CertificateRelationType.REPLACED.name();
                    case KOMPLT -> CertificateRelationType.COMPLEMENTED.name();
                    case FRLANG -> CertificateRelationType.EXTENDED.name();
                    case KOPIA -> CertificateRelationType.COPIED.name();
                }
            )
            .build();
    }

    private CertificateAnalyticsMessage create(Utlatande utlatande, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                AnalyticsCertificate.builder()
                    .id(utlatande.getId())
                    .type(utlatande.getTyp())
                    .typeVersion(utlatande.getTextVersion())
                    .patientId(utlatande.getGrundData().getPatient().getPersonId().getPersonnummerWithDash())
                    .unitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid())
                    .careProviderId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid())
                    .build()
            )
            .build();
    }

    private AnalyticsEvent createAnalyticsEvent(CertificateAnalyticsMessageType type) {
        final var loggedInWebcertUser = loggedInWebcertUserService.getLoggedInWebcertUser();
        return AnalyticsEvent.builder()
            .timestamp(LocalDateTime.now())
            .messageType(type)
            .userId(loggedInWebcertUser.getStaffId())
            .role(loggedInWebcertUser.getRole())
            .unitId(loggedInWebcertUser.getUnitId())
            .careProviderId(loggedInWebcertUser.getCareProviderId())
            .origin(loggedInWebcertUser.getOrigin())
            .sessionId(MDC.get(MdcLogConstants.SESSION_ID_KEY))
            .build();
    }

    private static CertificateAnalyticsMessageType messageTypeForIncomingMessage(IncomingMessageRequestDTO incomingMessageRequest) {
        return switch (incomingMessageRequest.getType()) {
            case AVSTMN, KONTKT, OVRIGT -> CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT;
            case KOMPLT -> CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT;
            case PAMINN -> CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT;
        };
    }
}
