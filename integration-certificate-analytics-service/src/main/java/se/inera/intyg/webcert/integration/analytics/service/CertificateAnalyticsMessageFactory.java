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

import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_COMPLEMENTED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_PRINTED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_RENEWED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_REPLACED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_REVOKED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_SENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.CERTIFICATE_SIGNED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_CREATED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_CREATED_FROM_CERTIFICATE;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_CREATED_WITH_PREFILL;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_DELETED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_DISPOSED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_UPDATED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.DRAFT_UPDATED_FROM_CERTIFICATE;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.LOCKED_DRAFT_REVOKED;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT;
import static se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.common.dto.IncomingComplementDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.common.dto.SentByDTO;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUser;
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
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@Component
@RequiredArgsConstructor
public class CertificateAnalyticsMessageFactory {

    private final LoggedInWebcertUserService loggedInWebcertUserService;

    public CertificateAnalyticsMessage draftCreated(Certificate certificate) {
        return create(certificate, DRAFT_CREATED).build();
    }

    public CertificateAnalyticsMessage draftCreated(Utkast utkast) {
        return create(utkast, DRAFT_CREATED).build();
    }

    public CertificateAnalyticsMessage draftCreated(Certificate certificate, LoggedInWebcertUser loggedInWebcertUser) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(DRAFT_CREATED, loggedInWebcertUser)
            )
            .certificate(
                createCertificate(certificate)
            )
            .build();
    }

    public CertificateAnalyticsMessage draftCreated(Utkast utkast, LoggedInWebcertUser loggedInWebcertUser) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(DRAFT_CREATED, loggedInWebcertUser)
            )
            .certificate(
                createCertificate(utkast)
            )
            .build();
    }

    public CertificateAnalyticsMessage draftCreatedFromCertificate(Utkast utkast) {
        return create(utkast, DRAFT_CREATED_FROM_CERTIFICATE).build();
    }

    public CertificateAnalyticsMessage draftCreatedFromCertificate(Certificate certificate) {
        return create(certificate, DRAFT_CREATED_FROM_CERTIFICATE).build();
    }

    public CertificateAnalyticsMessage draftCreatedWithPrefill(Certificate certificate, LoggedInWebcertUser loggedInWebcertUser) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(DRAFT_CREATED_WITH_PREFILL, loggedInWebcertUser)
            )
            .certificate(
                createCertificate(certificate)
            )
            .build();
    }

    public CertificateAnalyticsMessage draftCreatedWithPrefill(Utkast utkast, LoggedInWebcertUser loggedInWebcertUser) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(DRAFT_CREATED_WITH_PREFILL, loggedInWebcertUser)
            )
            .certificate(
                createCertificate(utkast)
            )
            .build();
    }

    public CertificateAnalyticsMessage draftDeleted(Certificate certificate) {
        return create(certificate, DRAFT_DELETED).build();
    }

    public CertificateAnalyticsMessage draftDisposed(Certificate certificate) {
        return create(certificate, DRAFT_DISPOSED).build();
    }

    public CertificateAnalyticsMessage draftDeleted(Utkast utkast) {
        return create(utkast, DRAFT_DELETED).build();
    }

    public CertificateAnalyticsMessage draftDisposed(Utkast utkast) {
        return create(utkast, DRAFT_DISPOSED).build();
    }

    public CertificateAnalyticsMessage draftUpdated(Utkast utkast) {
        return create(utkast, DRAFT_UPDATED).build();
    }

    public CertificateAnalyticsMessage draftUpdated(Certificate certificate) {
        return create(certificate, DRAFT_UPDATED).build();
    }

    public CertificateAnalyticsMessage draftUpdatedFromCertificate(Certificate certificate) {
        return create(certificate, DRAFT_UPDATED_FROM_CERTIFICATE).build();
    }

    public CertificateAnalyticsMessage draftUpdatedFromCertificate(Utkast utkast) {
        return create(utkast, DRAFT_UPDATED_FROM_CERTIFICATE).build();
    }

    public CertificateAnalyticsMessage draftReadyForSign(Certificate certificate) {
        return create(certificate, DRAFT_READY_FOR_SIGN).build();
    }

    public CertificateAnalyticsMessage draftReadyForSign(Utkast utkast) {
        return create(utkast, DRAFT_READY_FOR_SIGN).build();
    }

    public CertificateAnalyticsMessage lockedDraftRevoked(Utkast utkast) {
        return create(utkast, LOCKED_DRAFT_REVOKED).build();
    }

    public CertificateAnalyticsMessage certificateSigned(Certificate certificate) {
        return create(certificate, CERTIFICATE_SIGNED).build();
    }

    public CertificateAnalyticsMessage certificateSigned(Utkast utkast) {
        return create(utkast, CERTIFICATE_SIGNED).build();
    }

    public CertificateAnalyticsMessage certificateSent(Certificate certificate, String recipientId) {
        return create(certificate, CERTIFICATE_SENT)
            .recipient(
                AnalyticsRecipient.builder()
                    .id(recipientId)
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage certificateSent(Utkast utkast, String recipientId) {
        return create(utkast, CERTIFICATE_SENT)
            .recipient(
                AnalyticsRecipient.builder()
                    .id(recipientId)
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage certificateRenewed(Certificate certificate) {
        return create(certificate, CERTIFICATE_RENEWED).build();
    }

    public CertificateAnalyticsMessage certificateRenewed(Utkast utkast) {
        return create(utkast, CERTIFICATE_RENEWED).build();
    }

    public CertificateAnalyticsMessage certificateReplace(Certificate certificate) {
        return create(certificate, CERTIFICATE_REPLACED).build();
    }

    public CertificateAnalyticsMessage certificateReplace(Utkast utkast) {
        return create(utkast, CERTIFICATE_REPLACED).build();
    }

    public CertificateAnalyticsMessage certificateComplemented(Certificate certificate) {
        return create(certificate, CERTIFICATE_COMPLEMENTED).build();
    }

    public CertificateAnalyticsMessage certificateComplemented(Utkast certificate) {
        return create(certificate, CERTIFICATE_COMPLEMENTED).build();
    }

    public CertificateAnalyticsMessage certificateRevoked(Certificate certificate) {
        return create(certificate, CERTIFICATE_REVOKED).build();
    }

    public CertificateAnalyticsMessage certificateRevoked(Utlatande utlatande) {
        return create(utlatande, CERTIFICATE_REVOKED);
    }

    public CertificateAnalyticsMessage certificatePrinted(Certificate certificate) {
        return create(certificate, CERTIFICATE_PRINTED).build();
    }

    public CertificateAnalyticsMessage certificatePrinted(Utlatande utlatande) {
        return create(utlatande, CERTIFICATE_PRINTED);
    }

    public CertificateAnalyticsMessage receivedMessage(Certificate certificate, IncomingMessageRequestDTO incomingMessageRequest) {
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

    public CertificateAnalyticsMessage receivedMessage(Utkast utkast, Arende arende) {
        return create(utkast, messageTypeForArende(arende))
            .message(
                AnalyticsMessage.builder()
                    .id(arende.getMeddelandeId())
                    .answerId(arende.getSvarPaId())
                    .reminderId(arende.getPaminnelseMeddelandeId())
                    .type(arende.getAmne().name())
                    .questionIds(
                        arende.getKomplettering() == null ? null :
                            arende.getKomplettering().stream()
                                .map(MedicinsktArende::getFrageId)
                                .toList()
                    )
                    .sender(SentByDTO.FK.getCode())
                    .recipient(SentByDTO.WC.getCode())
                    .sent(arende.getSkickatTidpunkt())
                    .lastDateToAnswer(arende.getSistaDatumForSvar())
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage sentMessage(Certificate certificate, Question question) {
        return create(certificate, isAnswer(question) ? ANSWER_TO_RECIPIENT : QUESTION_TO_RECIPIENT)
            .message(
                AnalyticsMessage.builder()
                    .id(isAnswer(question) ? question.getAnswer().getId() : question.getId())
                    .answerId(isAnswer(question) ? question.getId() : null)
                    .type(
                        switch (question.getType()) {
                            case COMPLEMENT -> ArendeAmne.KOMPLT.name();
                            case COORDINATION -> ArendeAmne.AVSTMN.name();
                            case CONTACT -> ArendeAmne.KONTKT.name();
                            case OTHER, MISSING -> ArendeAmne.OVRIGT.name();
                        }
                    )
                    .sender(SentByDTO.WC.getCode())
                    .recipient(SentByDTO.FK.getCode())
                    .sent(question.getSent())
                    .lastDateToAnswer(isAnswer(question) ? null : question.getLastDateToReply())
                    .build()
            )
            .build();
    }

    public CertificateAnalyticsMessage sentMessage(Utkast utkast, Arende arende) {
        return create(utkast, isAnswer(arende) ? ANSWER_TO_RECIPIENT : QUESTION_TO_RECIPIENT)
            .message(
                AnalyticsMessage.builder()
                    .id(arende.getMeddelandeId())
                    .answerId(arende.getSvarPaId())
                    .type(arende.getAmne().name())
                    .sender(SentByDTO.WC.getCode())
                    .recipient(SentByDTO.FK.getCode())
                    .sent(arende.getSkickatTidpunkt())
                    .lastDateToAnswer(arende.getSistaDatumForSvar())
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
                createCertificate(certificate)
            );
    }

    private CertificateAnalyticsMessageBuilder create(Utkast utkast, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                createCertificate(utkast)
            );
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
        return createAnalyticsEvent(type, loggedInWebcertUser);
    }

    private AnalyticsEvent createAnalyticsEvent(CertificateAnalyticsMessageType type, LoggedInWebcertUser loggedInWebcertUser) {
        return AnalyticsEvent.builder()
            .timestamp(LocalDateTime.now())
            .messageType(type)
            .userId(loggedInWebcertUser.getStaffId())
            .role(loggedInWebcertUser.getRole())
            .unitId(loggedInWebcertUser.getUnitId())
            .careProviderId(loggedInWebcertUser.getCareProviderId())
            .origin(loggedInWebcertUser.getOrigin())
            .sessionId(sessionId())
            .build();
    }

    private static String sessionId() {
        final var sessionId = MDC.get(MdcLogConstants.SESSION_ID_KEY);
        if (sessionId == null || sessionId.isBlank() || sessionId.equals("-")) {
            return null;
        }
        return sessionId;
    }

    private AnalyticsCertificate createCertificate(Certificate certificate) {
        return AnalyticsCertificate.builder()
            .id(certificate.getMetadata().getId())
            .type(certificate.getMetadata().getType())
            .typeVersion(certificate.getMetadata().getTypeVersion())
            .patientId(certificate.getMetadata().getPatient().getPersonId().getId())
            .unitId(certificate.getMetadata().getUnit().getUnitId())
            .careProviderId(certificate.getMetadata().getCareProvider().getUnitId())
            .parent(
                createCertificateAnalyticsRelation(certificate)
            )
            .build();
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

    private AnalyticsCertificate createCertificate(Utkast utkast) {
        return AnalyticsCertificate.builder()
            .id(utkast.getIntygsId())
            .type(utkast.getIntygsTyp())
            .typeVersion(utkast.getIntygTypeVersion())
            .patientId(utkast.getPatientPersonnummer().getPersonnummerWithDash())
            .unitId(utkast.getEnhetsId())
            .careProviderId(utkast.getVardgivarId())
            .parent(
                createCertificateAnalyticsRelation(utkast)
            )
            .build();
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

    private static CertificateAnalyticsMessageType messageTypeForIncomingMessage(IncomingMessageRequestDTO incomingMessageRequest) {
        if (isAnswer(incomingMessageRequest)) {
            return switch (incomingMessageRequest.getType()) {
                case AVSTMN, KONTKT, OVRIGT -> ANSWER_FROM_RECIPIENT;
                case KOMPLT -> COMPLEMENT_FROM_RECIPIENT;
                case PAMINN -> REMINDER_FROM_RECIPIENT;
            };
        }

        return switch (incomingMessageRequest.getType()) {
            case AVSTMN, KONTKT, OVRIGT -> QUESTION_FROM_RECIPIENT;
            case KOMPLT -> COMPLEMENT_FROM_RECIPIENT;
            case PAMINN -> REMINDER_FROM_RECIPIENT;
        };
    }

    private static CertificateAnalyticsMessageType messageTypeForArende(Arende arende) {
        if (isAnswer(arende)) {
            return switch (arende.getAmne()) {
                case AVSTMN, KONTKT, OVRIGT -> ANSWER_FROM_RECIPIENT;
                case KOMPLT -> COMPLEMENT_FROM_RECIPIENT;
                case PAMINN -> REMINDER_FROM_RECIPIENT;
            };
        }

        return switch (arende.getAmne()) {
            case AVSTMN, KONTKT, OVRIGT -> QUESTION_FROM_RECIPIENT;
            case KOMPLT -> COMPLEMENT_FROM_RECIPIENT;
            case PAMINN -> REMINDER_FROM_RECIPIENT;
        };
    }

    private static boolean isAnswer(IncomingMessageRequestDTO incomingMessageRequest) {
        return incomingMessageRequest.getAnswerMessageId() != null && !incomingMessageRequest.getAnswerMessageId().isEmpty();
    }

    private static boolean isAnswer(Question question) {
        return question.getAnswer() != null && question.getAnswer().getId() != null && !question.getAnswer().getId().isEmpty();
    }

    private static boolean isAnswer(Arende arende) {
        return arende.getSvarPaId() != null && !arende.getSvarPaId().isEmpty();
    }
}