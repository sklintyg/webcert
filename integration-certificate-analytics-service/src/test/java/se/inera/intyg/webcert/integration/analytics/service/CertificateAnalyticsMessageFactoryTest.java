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

package se.inera.intyg.webcert.integration.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Question.QuestionBuilder;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.dto.IncomingComplementDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO.IncomingMessageRequestDTOBuilder;
import se.inera.intyg.webcert.common.dto.MessageTypeDTO;
import se.inera.intyg.webcert.common.dto.SentByDTO;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUser;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUserService;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@ExtendWith(MockitoExtension.class)
class CertificateAnalyticsMessageFactoryTest {

    @Mock
    private LoggedInWebcertUserService loggedInWebcertUserService;

    @InjectMocks
    private CertificateAnalyticsMessageFactory factory;

    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String CERTIFICATE_TYPE = "certiticate-type";
    private static final String CERTIFICATE_TYPE_VERSION = "certiticate-type-version";
    private static final String CERTIFICATE_PATIENT_ID = "19121212-1212";
    private static final String CERTIFICATE_UNIT_ID = "certificate-unit-id";
    private static final String CERTIFICATE_CARE_PROVIDER_ID = "certificate-care-provider-id";
    private static final String CERTIFICATE_RELATION_PARENT_ID = "certificate-relation-parent-id";
    private static final CertificateRelationType CERTIFICATE_RELATION_PARENT_TYPE = CertificateRelationType.EXTENDED;

    private static final String EVENT_USER_ID = "event-user-id";
    private static final String EVENT_ROLE = "event-role";
    private static final String EVENT_UNIT_ID = "event-unit-id";
    private static final String EVENT_CARE_PROVIDER_ID = "event-care-provider-id";
    private static final String EVENT_ORIGIN = "event.origin";
    private static final String EVENT_SESSION_ID = "event-session-id";

    private static final String MESSAGE_ID = "message-id";
    private static final String MESSAGE_ANSWER_ID = "message-answer-id";
    private static final String MESSAGE_REMINDER_ID = "message-reminder-id";

    private static final String RECIPIENT_ID = "recipient-id";

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AnalyticsMessagesBasedOnCertificate {

        private Certificate certificate;
        private LoggedInWebcertUser loggedInWebcertUser;

        @BeforeEach
        void setUp() {
            certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id(CERTIFICATE_PATIENT_ID)
                                    .build()
                            )
                            .build()
                    )
                    .unit(
                        Unit.builder()
                            .unitId(CERTIFICATE_UNIT_ID)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitId(CERTIFICATE_CARE_PROVIDER_ID)
                            .build()
                    )
                    .recipient(
                        CertificateRecipient.builder()
                            .id(RECIPIENT_ID)
                            .build()
                    )
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .certificateId(CERTIFICATE_RELATION_PARENT_ID)
                                    .type(CERTIFICATE_RELATION_PARENT_TYPE)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .origin(EVENT_ORIGIN)
                .build();

            // Make this lenient to enable mocking to work in parameterized tests
            lenient().when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(loggedInWebcertUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, EVENT_SESSION_ID);
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventTimestamp(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventMessageType(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(messageType, actual.getEvent().getMessageType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventStaffId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventRole(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventUnitId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventCareProviderId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventOrigin(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_ORIGIN, actual.getEvent().getOrigin());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectEventSessionId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(EVENT_SESSION_ID, actual.getEvent().getSessionId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateType(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateTypeVersion(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificatePatientId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateUnitId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateCareProviderId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateRelationParentId(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificate")
        void shallReturnCorrectCertificateRelationParentType(Function<Certificate, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(certificate);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectRecipientIdForCertificateSent() {
            final var actual = factory.certificateSent(certificate, certificate.getMetadata().getRecipient().getId());
            assertEquals(RECIPIENT_ID, actual.getRecipient().getId());
        }

        Stream<Arguments> analyticsMessagesBasedOnCertificate() {
            return Stream.of(
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftCreated(certificate),
                    CertificateAnalyticsMessageType.DRAFT_CREATED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftDeleted(certificate),
                    CertificateAnalyticsMessageType.DRAFT_DELETED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftDisposed(certificate),
                    CertificateAnalyticsMessageType.DRAFT_DISPOSED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftUpdated(certificate),
                    CertificateAnalyticsMessageType.DRAFT_UPDATED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftReadyForSign(certificate),
                    CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateSigned(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_SIGNED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateSent(certificate,
                        certificate.getMetadata().getRecipient().getId()),
                    CertificateAnalyticsMessageType.CERTIFICATE_SENT
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateRenewed(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_RENEWED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateReplace(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_REPLACED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateComplemented(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_COMPLEMENTED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateRevoked(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_REVOKED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificatePrinted(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_PRINTED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftCreatedFromCertificate(certificate),
                    CertificateAnalyticsMessageType.DRAFT_CREATED_FROM_CERTIFICATE
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.draftUpdatedFromCertificate(certificate),
                    CertificateAnalyticsMessageType.DRAFT_UPDATED_FROM_CERTIFICATE
                )
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AnalyticsMessagesBasedOnCertificateWithoutLoggedInUser {

        private Certificate certificate;
        private LoggedInWebcertUser loggedInWebcertUser;

        @BeforeEach
        void setUp() {
            certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id(CERTIFICATE_PATIENT_ID)
                                    .build()
                            )
                            .build()
                    )
                    .unit(
                        Unit.builder()
                            .unitId(CERTIFICATE_UNIT_ID)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitId(CERTIFICATE_CARE_PROVIDER_ID)
                            .build()
                    )
                    .recipient(
                        CertificateRecipient.builder()
                            .id(RECIPIENT_ID)
                            .build()
                    )
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .certificateId(CERTIFICATE_RELATION_PARENT_ID)
                                    .type(CERTIFICATE_RELATION_PARENT_TYPE)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .build();

            MDC.put(MdcLogConstants.SESSION_ID_KEY, "-");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventTimestamp(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventMessageType(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(messageType, actual.getEvent().getMessageType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventStaffId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventRole(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventUnitId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventCareProviderId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventOrigin(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertNull(actual.getEvent().getOrigin(), "Origin should be null when no user logged in");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventSessionId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertNull(actual.getEvent().getSessionId(), "SessionId should be null when no user logged in");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateType(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateTypeVersion(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificatePatientId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateUnitId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateCareProviderId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateRelationParentId(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateRelationParentType(Function<CertificateAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(CertificateAndUser.create(certificate, loggedInWebcertUser));
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        Stream<Arguments> analyticsMessagesBasedOnCertificateWithoutLoggedInUser() {
            return Stream.of(
                Arguments.of(
                    (Function<CertificateAndUser, CertificateAnalyticsMessage>) certificateAndUser ->
                        factory.draftCreated(certificateAndUser.certificate(), certificateAndUser.user()),
                    CertificateAnalyticsMessageType.DRAFT_CREATED
                ),
                Arguments.of(
                    (Function<CertificateAndUser, CertificateAnalyticsMessage>) certificateAndUser ->
                        factory.draftCreatedWithPrefill(certificateAndUser.certificate(), certificateAndUser.user()),
                    CertificateAnalyticsMessageType.DRAFT_CREATED_WITH_PREFILL
                )
            );
        }

        record CertificateAndUser(Certificate certificate, LoggedInWebcertUser user) {

            public static CertificateAndUser create(Certificate certificate, LoggedInWebcertUser user) {
                return new CertificateAndUser(certificate, user);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AnalyticsMessagesBasedOnUtkast {

        private Utkast utkast;
        private LoggedInWebcertUser loggedInWebcertUser;

        @BeforeEach
        void setUp() {
            utkast = new Utkast();
            utkast.setIntygsId(CERTIFICATE_ID);
            utkast.setIntygsTyp(CERTIFICATE_TYPE);
            utkast.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION);
            utkast.setPatientPersonnummer(Personnummer.createPersonnummer(CERTIFICATE_PATIENT_ID).orElseThrow());
            utkast.setEnhetsId(CERTIFICATE_UNIT_ID);
            utkast.setVardgivarId(CERTIFICATE_CARE_PROVIDER_ID);
            utkast.setSkickadTillMottagare(RECIPIENT_ID);
            utkast.setRelationIntygsId(CERTIFICATE_RELATION_PARENT_ID);
            utkast.setRelationKod(RelationKod.FRLANG);

            loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .origin(EVENT_ORIGIN)
                .build();

            // Make this lenient to enable mocking to work in parameterized tests
            lenient().when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(loggedInWebcertUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, EVENT_SESSION_ID);
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventTimestamp(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventMessageType(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(messageType, actual.getEvent().getMessageType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventStaffId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventRole(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventUnitId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventCareProviderId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventOrigin(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_ORIGIN, actual.getEvent().getOrigin());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectEventSessionId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(EVENT_SESSION_ID, actual.getEvent().getSessionId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateType(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateTypeVersion(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificatePatientId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateUnitId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateCareProviderId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateRelationParentId(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtkast")
        void shallReturnCorrectCertificateRelationParentType(Function<Utkast, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utkast);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectRecipientIdForCertificateSent() {
            final var actual = factory.certificateSent(utkast, utkast.getSkickadTillMottagare());
            assertEquals(RECIPIENT_ID, actual.getRecipient().getId());
        }

        Stream<Arguments> analyticsMessagesBasedOnUtkast() {
            return Stream.of(
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftCreated(utkast),
                    CertificateAnalyticsMessageType.DRAFT_CREATED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftDeleted(utkast),
                    CertificateAnalyticsMessageType.DRAFT_DELETED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftDisposed(utkast),
                    CertificateAnalyticsMessageType.DRAFT_DISPOSED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftUpdated(utkast),
                    CertificateAnalyticsMessageType.DRAFT_UPDATED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftUpdatedFromCertificate(utkast),
                    CertificateAnalyticsMessageType.DRAFT_UPDATED_FROM_CERTIFICATE
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftReadyForSign(utkast),
                    CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.lockedDraftRevoked(utkast),
                    CertificateAnalyticsMessageType.LOCKED_DRAFT_REVOKED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftCreatedFromCertificate(utkast),
                    CertificateAnalyticsMessageType.DRAFT_CREATED_FROM_CERTIFICATE
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.certificateSigned(utkast),
                    CertificateAnalyticsMessageType.CERTIFICATE_SIGNED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.certificateSent(utkast,
                        utkast.getSkickadTillMottagare()),
                    CertificateAnalyticsMessageType.CERTIFICATE_SENT
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.certificateRenewed(utkast),
                    CertificateAnalyticsMessageType.CERTIFICATE_RENEWED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.certificateReplace(utkast),
                    CertificateAnalyticsMessageType.CERTIFICATE_REPLACED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.certificateComplemented(utkast),
                    CertificateAnalyticsMessageType.CERTIFICATE_COMPLEMENTED
                )
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AnalyticsMessagesBasedOnUtkastWithoutLoggedInUser {

        private Utkast utkast;
        private LoggedInWebcertUser loggedInWebcertUser;

        @BeforeEach
        void setUp() {
            utkast = new Utkast();
            utkast.setIntygsId(CERTIFICATE_ID);
            utkast.setIntygsTyp(CERTIFICATE_TYPE);
            utkast.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION);
            utkast.setPatientPersonnummer(Personnummer.createPersonnummer(CERTIFICATE_PATIENT_ID).orElseThrow());
            utkast.setEnhetsId(CERTIFICATE_UNIT_ID);
            utkast.setVardgivarId(CERTIFICATE_CARE_PROVIDER_ID);
            utkast.setSkickadTillMottagare(RECIPIENT_ID);
            utkast.setRelationIntygsId(CERTIFICATE_RELATION_PARENT_ID);
            utkast.setRelationKod(RelationKod.FRLANG);

            loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .build();

            MDC.put(MdcLogConstants.SESSION_ID_KEY, "-");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventTimestamp(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventMessageType(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(messageType, actual.getEvent().getMessageType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventStaffId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventRole(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventUnitId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventCareProviderId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventOrigin(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertNull(actual.getEvent().getOrigin(), "Origin should be null when no user logged in");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectEventSessionId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertNull(actual.getEvent().getSessionId(), "SessionId should be null when no user logged in");
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateType(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateTypeVersion(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificatePatientId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateUnitId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateCareProviderId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateRelationParentId(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnCertificateWithoutLoggedInUser")
        void shallReturnCorrectCertificateRelationParentType(Function<UtkastAndUser, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(UtkastAndUser.create(utkast, loggedInWebcertUser));
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        Stream<Arguments> analyticsMessagesBasedOnCertificateWithoutLoggedInUser() {
            return Stream.of(
                Arguments.of(
                    (Function<UtkastAndUser, CertificateAnalyticsMessage>) utkastAndUser ->
                        factory.draftCreated(utkastAndUser.certificate(), utkastAndUser.user()),
                    CertificateAnalyticsMessageType.DRAFT_CREATED
                ),
                Arguments.of(
                    (Function<UtkastAndUser, CertificateAnalyticsMessage>) utkastAndUser ->
                        factory.draftCreatedWithPrefill(utkastAndUser.certificate(), utkastAndUser.user()),
                    CertificateAnalyticsMessageType.DRAFT_CREATED_WITH_PREFILL
                )
            );
        }

        record UtkastAndUser(Utkast certificate, LoggedInWebcertUser user) {

            public static UtkastAndUser create(Utkast utkast, LoggedInWebcertUser user) {
                return new UtkastAndUser(utkast, user);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AnalyticsMessagesBasedOnUtlatande {

        private Utlatande utlatande;
        private LoggedInWebcertUser loggedInWebcertUser;

        @BeforeEach
        void setUp() {
            utlatande = mock(Utlatande.class);

            final var grundData = mock(GrundData.class);
            final var hosPersonal = mock(HoSPersonal.class);
            final var vardenhet = mock(Vardenhet.class);
            final var vardgivare = mock(Vardgivare.class);
            final var patient = mock(se.inera.intyg.common.support.model.common.internal.Patient.class);

            when(utlatande.getId()).thenReturn(CERTIFICATE_ID);
            when(utlatande.getTyp()).thenReturn(CERTIFICATE_TYPE);
            when(utlatande.getTextVersion()).thenReturn(CERTIFICATE_TYPE_VERSION);
            when(utlatande.getGrundData()).thenReturn(grundData);

            when(grundData.getPatient()).thenReturn(patient);
            when(grundData.getSkapadAv()).thenReturn(hosPersonal);
            when(patient.getPersonId()).thenReturn(Personnummer.createPersonnummer(CERTIFICATE_PATIENT_ID).orElseThrow());

            when(hosPersonal.getVardenhet()).thenReturn(vardenhet);
            when(vardenhet.getEnhetsid()).thenReturn(CERTIFICATE_UNIT_ID);
            when(vardenhet.getVardgivare()).thenReturn(vardgivare);
            when(vardgivare.getVardgivarid()).thenReturn(CERTIFICATE_CARE_PROVIDER_ID);

            loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .origin(EVENT_ORIGIN)
                .build();

            // Make this lenient to enable mocking to work in parameterized tests
            lenient().when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(loggedInWebcertUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, EVENT_SESSION_ID);
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventTimestamp(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventMessageType(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(messageType, actual.getEvent().getMessageType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventStaffId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventRole(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventUnitId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventCareProviderId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventOrigin(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_ORIGIN, actual.getEvent().getOrigin());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectEventSessionId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(EVENT_SESSION_ID, actual.getEvent().getSessionId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificateId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificateType(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificateTypeVersion(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificatePatientId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificateUnitId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @ParameterizedTest(name = "{index} => {1}")
        @MethodSource("analyticsMessagesBasedOnUtlatande")
        void shallReturnCorrectCertificateCareProviderId(Function<Utlatande, CertificateAnalyticsMessage> test,
            CertificateAnalyticsMessageType messageType) {
            final var actual = test.apply(utlatande);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        Stream<Arguments> analyticsMessagesBasedOnUtlatande() {
            return Stream.of(
                Arguments.of(
                    (Function<Utlatande, CertificateAnalyticsMessage>) utlatande -> factory.certificatePrinted(utlatande),
                    CertificateAnalyticsMessageType.CERTIFICATE_PRINTED
                ),
                Arguments.of(
                    (Function<Utlatande, CertificateAnalyticsMessage>) utlatande -> factory.certificateRevoked(utlatande),
                    CertificateAnalyticsMessageType.CERTIFICATE_REVOKED
                )
            );
        }
    }

    @Nested
    class AnalyticsMessagesBasedOnIncomingMessages {

        private Certificate certificate;
        private IncomingMessageRequestDTOBuilder incomingMessageBuilder;
        private IncomingMessageRequestDTO incomingMessage;

        @BeforeEach
        void setUp() {
            certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id(CERTIFICATE_PATIENT_ID)
                                    .build()
                            )
                            .build()
                    )
                    .unit(
                        Unit.builder()
                            .unitId(CERTIFICATE_UNIT_ID)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitId(CERTIFICATE_CARE_PROVIDER_ID)
                            .build()
                    )
                    .recipient(
                        CertificateRecipient.builder()
                            .id(RECIPIENT_ID)
                            .build()
                    )
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .certificateId(CERTIFICATE_RELATION_PARENT_ID)
                                    .type(CERTIFICATE_RELATION_PARENT_TYPE)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            incomingMessageBuilder = IncomingMessageRequestDTO.builder()
                .id(MESSAGE_ID)
                .type(MessageTypeDTO.KOMPLT)
                .sent(LocalDateTime.now())
                .sentBy(SentByDTO.FK)
                .lastDateToAnswer(LocalDate.now().plusWeeks(1));

            incomingMessage = incomingMessageBuilder.build();

            final var noLoggedInUser = LoggedInWebcertUser.builder()
                .build();

            when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(noLoggedInUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, "-");
        }

        @Test
        void shallReturnCorrectEventTimestamp() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMN() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .type(MessageTypeDTO.AVSTMN)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGT() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .type(MessageTypeDTO.OVRIGT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKT() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .type(MessageTypeDTO.KONTKT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLT() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .type(MessageTypeDTO.KOMPLT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenPAMINN() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .type(MessageTypeDTO.PAMINN)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMNAndAnAnswer() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .type(MessageTypeDTO.AVSTMN)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGTAndAnAnswer() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .type(MessageTypeDTO.OVRIGT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKTAndAnAnswer() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .type(MessageTypeDTO.KONTKT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLTAndAnAnswer() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .type(MessageTypeDTO.KOMPLT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenPAMINNAndAnAnswer() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .type(MessageTypeDTO.PAMINN)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventStaffId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getUserId(), "Event user id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventRole() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getRole(), "Event role shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventUnitId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getUnitId(), "Event unit id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventCareProviderId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getCareProviderId(),
                "Event care provider id shall be null when received message since no user is logged in"
            );
        }

        @Test
        void shallReturnCorrectEventOrigin() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getOrigin(), "Event origin shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventSessionId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertNull(actual.getEvent().getSessionId(), "Event session id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectCertificateId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @Test
        void shallReturnCorrectCertificateType() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @Test
        void shallReturnCorrectCertificateTypeVersion() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @Test
        void shallReturnCorrectCertificatePatientId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @Test
        void shallReturnCorrectCertificateUnitId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @Test
        void shallReturnCorrectCertificateCareProviderId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentType() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectMessageId() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(MESSAGE_ID, actual.getMessage().getId());
        }

        @Test
        void shallReturnCorrectAnswerId() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .answerMessageId(MESSAGE_ANSWER_ID)
                    .build()
            );
            assertEquals(MESSAGE_ANSWER_ID, actual.getMessage().getAnswerId());
        }

        @Test
        void shallReturnCorrectReminderId() {
            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder
                    .reminderMessageId(MESSAGE_REMINDER_ID)
                    .build()
            );
            assertEquals(MESSAGE_REMINDER_ID, actual.getMessage().getReminderId());
        }

        @Test
        void shallReturnCorrectMessageType() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(incomingMessage.getType().name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageSender() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(incomingMessage.getSentBy().getCode(), actual.getMessage().getSender());
        }

        @Test
        void shallReturnCorrectMessageRecipient() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(SentByDTO.WC.getCode(), actual.getMessage().getRecipient());
        }

        @Test
        void shallReturnCorrectMessageSent() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(incomingMessage.getSent(), actual.getMessage().getSent());
        }

        @Test
        void shallReturnCorrectMessageLastDayAnswer() {
            final var actual = factory.receivedMessage(certificate, incomingMessage);
            assertEquals(incomingMessage.getLastDateToAnswer(), actual.getMessage().getLastDateToAnswer());
        }

        @Test
        void shallReturnCorrectQuestions() {
            final var expected = List.of("question-1", "question-2", "question-3");

            final var actual = factory.receivedMessage(certificate,
                incomingMessageBuilder.complements(
                        expected.stream()
                            .map(id ->
                                IncomingComplementDTO.builder()
                                    .questionId(id)
                                    .build()
                            )
                            .toList()
                    )
                    .build()
            );
            assertEquals(expected, actual.getMessage().getQuestionIds());
        }
    }

    @Nested
    class AnalyticsMessagesBasedOnSentQuestions {

        private Certificate certificate;
        private QuestionBuilder questionBuilder;
        private Question question;

        @BeforeEach
        void setUp() {
            certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id(CERTIFICATE_PATIENT_ID)
                                    .build()
                            )
                            .build()
                    )
                    .unit(
                        Unit.builder()
                            .unitId(CERTIFICATE_UNIT_ID)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitId(CERTIFICATE_CARE_PROVIDER_ID)
                            .build()
                    )
                    .recipient(
                        CertificateRecipient.builder()
                            .id(RECIPIENT_ID)
                            .build()
                    )
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .certificateId(CERTIFICATE_RELATION_PARENT_ID)
                                    .type(CERTIFICATE_RELATION_PARENT_TYPE)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            questionBuilder = Question.builder()
                .id(MESSAGE_ID)
                .type(QuestionType.COMPLEMENT)
                .author(EVENT_USER_ID)
                .sent(LocalDateTime.now())
                .lastDateToReply(LocalDate.now().plusWeeks(1));

            question = questionBuilder.build();

            final var loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .origin(EVENT_ORIGIN)
                .build();

            when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(loggedInWebcertUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, EVENT_SESSION_ID);
        }

        @Test
        void shallReturnCorrectEventTimestamp() {
            final var actual = factory.sentMessage(certificate, question);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMN() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.COORDINATION)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGT() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.OTHER)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKT() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.CONTACT)
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMNAndAnAnswer() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .id(MESSAGE_ANSWER_ID)
                    .type(QuestionType.COORDINATION)
                    .answer(
                        Answer.builder()
                            .id(MESSAGE_ID)
                            .build()
                    )
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGTAndAnAnswer() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .id(MESSAGE_ANSWER_ID)
                    .type(QuestionType.OTHER)
                    .answer(
                        Answer.builder()
                            .id(MESSAGE_ID)
                            .build()
                    )
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKTAndAnAnswer() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .id(MESSAGE_ANSWER_ID)
                    .type(QuestionType.CONTACT)
                    .answer(
                        Answer.builder()
                            .id(MESSAGE_ID)
                            .build()
                    )
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLTAndAnAnswer() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .id(MESSAGE_ANSWER_ID)
                    .type(QuestionType.COMPLEMENT)
                    .answer(
                        Answer.builder()
                            .id(MESSAGE_ID)
                            .build()
                    )
                    .build()
            );
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventStaffId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @Test
        void shallReturnCorrectEventRole() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @Test
        void shallReturnCorrectEventUnitId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @Test
        void shallReturnCorrectEventCareProviderId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @Test
        void shallReturnCorrectEventOrigin() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_ORIGIN, actual.getEvent().getOrigin());
        }

        @Test
        void shallReturnCorrectEventSessionId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(EVENT_SESSION_ID, actual.getEvent().getSessionId());
        }

        @Test
        void shallReturnCorrectCertificateId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @Test
        void shallReturnCorrectCertificateType() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @Test
        void shallReturnCorrectCertificateTypeVersion() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @Test
        void shallReturnCorrectCertificatePatientId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @Test
        void shallReturnCorrectCertificateUnitId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @Test
        void shallReturnCorrectCertificateCareProviderId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentType() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectMessageId() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(MESSAGE_ID, actual.getMessage().getId());
        }

        @Test
        void shallReturnCorrectAnswerId() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .id(MESSAGE_ANSWER_ID)
                    .answer(
                        Answer.builder()
                            .id(MESSAGE_ID)
                            .build()
                    )
                    .build()
            );
            assertEquals(MESSAGE_ANSWER_ID, actual.getMessage().getAnswerId());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenComplement() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.COMPLEMENT)
                    .build()
            );
            assertEquals(ArendeAmne.KOMPLT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenContact() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.CONTACT)
                    .build()
            );
            assertEquals(ArendeAmne.KONTKT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenCoordination() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.COORDINATION)
                    .build()
            );
            assertEquals(ArendeAmne.AVSTMN.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenOther() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder
                    .type(QuestionType.OTHER)
                    .build()
            );
            assertEquals(ArendeAmne.OVRIGT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageSender() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(SentByDTO.WC.getCode(), actual.getMessage().getSender());
        }

        @Test
        void shallReturnCorrectMessageRecipient() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(SentByDTO.FK.getCode(), actual.getMessage().getRecipient());
        }

        @Test
        void shallReturnCorrectMessageSent() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(question.getSent(), actual.getMessage().getSent());
        }

        @Test
        void shallReturnCorrectMessageLastDayAnswer() {
            final var actual = factory.sentMessage(certificate, question);
            assertEquals(question.getLastDateToReply(), actual.getMessage().getLastDateToAnswer());
        }

        @Test
        void shallReturnCorrectQuestions() {
            final var actual = factory.sentMessage(certificate,
                questionBuilder.complements(
                        new Complement[]{
                            Complement.builder()
                                .questionId("complement-question-id")
                                .build()
                        }
                    )
                    .build()
            );
            assertNull(actual.getMessage().getQuestionIds(),
                "Question ids shall be null since it will never be sent to a certificate recipient"
            );
        }
    }

    @Nested
    class AnalyticsMessagesBasedOnArende {

        private Utkast utkast;
        private Arende arende;

        @BeforeEach
        void setUp() {
            utkast = new Utkast();
            utkast.setIntygsId(CERTIFICATE_ID);
            utkast.setIntygsTyp(CERTIFICATE_TYPE);
            utkast.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION);
            utkast.setPatientPersonnummer(Personnummer.createPersonnummer(CERTIFICATE_PATIENT_ID).orElseThrow());
            utkast.setEnhetsId(CERTIFICATE_UNIT_ID);
            utkast.setVardgivarId(CERTIFICATE_CARE_PROVIDER_ID);
            utkast.setSkickadTillMottagare(RECIPIENT_ID);
            utkast.setRelationIntygsId(CERTIFICATE_RELATION_PARENT_ID);
            utkast.setRelationKod(RelationKod.FRLANG);

            final var noLoggedInUser = LoggedInWebcertUser.builder()
                .build();

            arende = new Arende();
            arende.setMeddelandeId(MESSAGE_ID);
            arende.setAmne(ArendeAmne.AVSTMN);

            // Make this lenient to enable mocking to work in parameterized tests
            lenient().when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(noLoggedInUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, "-");
        }

        @Test
        void shallReturnCorrectEventTimestamp() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMN() {
            arende.setAmne(ArendeAmne.AVSTMN);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGT() {
            arende.setAmne(ArendeAmne.OVRIGT);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKT() {
            arende.setAmne(ArendeAmne.KONTKT);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLT() {
            arende.setAmne(ArendeAmne.KOMPLT);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenPAMINN() {
            arende.setAmne(ArendeAmne.PAMINN);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMNAndAnAnswer() {
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGTAndAnAnswer() {
            arende.setAmne(ArendeAmne.OVRIGT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKTAndAnAnswer() {
            arende.setAmne(ArendeAmne.KONTKT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLTAndAnAnswer() {
            arende.setAmne(ArendeAmne.KOMPLT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.COMPLEMENT_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenPAMINNAndAnAnswer() {
            arende.setAmne(ArendeAmne.PAMINN);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.REMINDER_FROM_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventStaffId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getUserId(), "Event user id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventRole() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getRole(), "Event role shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventUnitId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getUnitId(), "Event unit id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventCareProviderId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getCareProviderId(),
                "Event care provider id shall be null when received message since no user is logged in"
            );
        }

        @Test
        void shallReturnCorrectEventOrigin() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getOrigin(), "Event origin shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectEventSessionId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertNull(actual.getEvent().getSessionId(), "Event session id shall be null when received message since no user is logged in");
        }

        @Test
        void shallReturnCorrectCertificateId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @Test
        void shallReturnCorrectCertificateType() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @Test
        void shallReturnCorrectCertificateTypeVersion() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @Test
        void shallReturnCorrectCertificatePatientId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @Test
        void shallReturnCorrectCertificateUnitId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @Test
        void shallReturnCorrectCertificateCareProviderId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentType() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectMessageId() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(MESSAGE_ID, actual.getMessage().getId());
        }

        @Test
        void shallReturnCorrectAnswerId() {
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(MESSAGE_ANSWER_ID, actual.getMessage().getAnswerId());
        }

        @Test
        void shallReturnCorrectReminderId() {
            arende.setPaminnelseMeddelandeId(MESSAGE_REMINDER_ID);
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(MESSAGE_REMINDER_ID, actual.getMessage().getReminderId());
        }

        @Test
        void shallReturnCorrectMessageType() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(arende.getAmne().name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageSender() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(SentByDTO.FK.getCode(), actual.getMessage().getSender());
        }

        @Test
        void shallReturnCorrectMessageRecipient() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(SentByDTO.WC.getCode(), actual.getMessage().getRecipient());
        }

        @Test
        void shallReturnCorrectMessageSent() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(arende.getSkickatTidpunkt(), actual.getMessage().getSent());
        }

        @Test
        void shallReturnCorrectMessageLastDayAnswer() {
            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(arende.getSistaDatumForSvar(), actual.getMessage().getLastDateToAnswer());
        }

        @Test
        void shallReturnCorrectQuestions() {
            final var expected = List.of("question-1", "question-2", "question-3");
            arende.setKomplettering(
                expected.stream()
                    .map(id -> {
                            final var medicinsktArende = new MedicinsktArende();
                            medicinsktArende.setFrageId(id);
                            return medicinsktArende;
                        }
                    )
                    .toList()
            );

            final var actual = factory.receivedMessage(utkast, arende);
            assertEquals(expected, actual.getMessage().getQuestionIds());
        }
    }

    @Nested
    class AnalyticsMessagesBasedOnSentArende {

        private Utkast utkast;
        private Arende arende;

        @BeforeEach
        void setUp() {
            utkast = new Utkast();
            utkast.setIntygsId(CERTIFICATE_ID);
            utkast.setIntygsTyp(CERTIFICATE_TYPE);
            utkast.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION);
            utkast.setPatientPersonnummer(Personnummer.createPersonnummer(CERTIFICATE_PATIENT_ID).orElseThrow());
            utkast.setEnhetsId(CERTIFICATE_UNIT_ID);
            utkast.setVardgivarId(CERTIFICATE_CARE_PROVIDER_ID);
            utkast.setSkickadTillMottagare(RECIPIENT_ID);
            utkast.setRelationIntygsId(CERTIFICATE_RELATION_PARENT_ID);
            utkast.setRelationKod(RelationKod.FRLANG);

            arende = new Arende();
            arende.setMeddelandeId(MESSAGE_ID);
            arende.setAmne(ArendeAmne.AVSTMN);

            final var loggedInWebcertUser = LoggedInWebcertUser.builder()
                .staffId(EVENT_USER_ID)
                .role(EVENT_ROLE)
                .unitId(EVENT_UNIT_ID)
                .careProviderId(EVENT_CARE_PROVIDER_ID)
                .origin(EVENT_ORIGIN)
                .build();

            when(loggedInWebcertUserService.getLoggedInWebcertUser()).thenReturn(loggedInWebcertUser);

            MDC.put(MdcLogConstants.SESSION_ID_KEY, EVENT_SESSION_ID);
        }

        @Test
        void shallReturnCorrectEventTimestamp() {
            final var actual = factory.sentMessage(utkast, arende);
            assertNotNull(actual.getEvent().getTimestamp());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMN() {
            arende.setAmne(ArendeAmne.AVSTMN);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGT() {
            arende.setAmne(ArendeAmne.OVRIGT);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKT() {
            arende.setAmne(ArendeAmne.KONTKT);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.QUESTION_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenAVSTMNAndAnAnswer() {
            arende.setAmne(ArendeAmne.AVSTMN);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenOVRIGTAndAnAnswer() {
            arende.setAmne(ArendeAmne.OVRIGT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKONTKTAndAnAnswer() {
            arende.setAmne(ArendeAmne.KONTKT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventMessageTypeWhenKOMPLTAndAnAnswer() {
            arende.setAmne(ArendeAmne.KOMPLT);
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CertificateAnalyticsMessageType.ANSWER_TO_RECIPIENT, actual.getEvent().getMessageType());
        }

        @Test
        void shallReturnCorrectEventStaffId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_USER_ID, actual.getEvent().getUserId());
        }

        @Test
        void shallReturnCorrectEventRole() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_ROLE, actual.getEvent().getRole());
        }

        @Test
        void shallReturnCorrectEventUnitId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_UNIT_ID, actual.getEvent().getUnitId());
        }

        @Test
        void shallReturnCorrectEventCareProviderId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_CARE_PROVIDER_ID, actual.getEvent().getCareProviderId());
        }

        @Test
        void shallReturnCorrectEventOrigin() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_ORIGIN, actual.getEvent().getOrigin());
        }

        @Test
        void shallReturnCorrectEventSessionId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(EVENT_SESSION_ID, actual.getEvent().getSessionId());
        }

        @Test
        void shallReturnCorrectCertificateId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_ID, actual.getCertificate().getId());
        }

        @Test
        void shallReturnCorrectCertificateType() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_TYPE, actual.getCertificate().getType());
        }

        @Test
        void shallReturnCorrectCertificateTypeVersion() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_TYPE_VERSION, actual.getCertificate().getTypeVersion());
        }

        @Test
        void shallReturnCorrectCertificatePatientId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_PATIENT_ID, actual.getCertificate().getPatientId());
        }

        @Test
        void shallReturnCorrectCertificateUnitId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_UNIT_ID, actual.getCertificate().getUnitId());
        }

        @Test
        void shallReturnCorrectCertificateCareProviderId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_CARE_PROVIDER_ID, actual.getCertificate().getCareProviderId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_RELATION_PARENT_ID, actual.getCertificate().getParent().getId());
        }

        @Test
        void shallReturnCorrectCertificateRelationParentType() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(CERTIFICATE_RELATION_PARENT_TYPE.name(), actual.getCertificate().getParent().getType());
        }

        @Test
        void shallReturnCorrectMessageId() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(MESSAGE_ID, actual.getMessage().getId());
        }

        @Test
        void shallReturnCorrectAnswerId() {
            arende.setSvarPaId(MESSAGE_ANSWER_ID);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(MESSAGE_ANSWER_ID, actual.getMessage().getAnswerId());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenKOMPLT() {
            arende.setAmne(ArendeAmne.KOMPLT);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(ArendeAmne.KOMPLT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenKONTKT() {
            arende.setAmne(ArendeAmne.KONTKT);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(ArendeAmne.KONTKT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenAVSTMN() {
            arende.setAmne(ArendeAmne.AVSTMN);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(ArendeAmne.AVSTMN.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageTypeWhenOVRIGT() {
            arende.setAmne(ArendeAmne.OVRIGT);
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(ArendeAmne.OVRIGT.name(), actual.getMessage().getType());
        }

        @Test
        void shallReturnCorrectMessageSender() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(SentByDTO.WC.getCode(), actual.getMessage().getSender());
        }

        @Test
        void shallReturnCorrectMessageRecipient() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(SentByDTO.FK.getCode(), actual.getMessage().getRecipient());
        }

        @Test
        void shallReturnCorrectMessageSent() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(arende.getSkickatTidpunkt(), actual.getMessage().getSent());
        }

        @Test
        void shallReturnCorrectMessageLastDateToAnswer() {
            final var actual = factory.sentMessage(utkast, arende);
            assertEquals(arende.getSistaDatumForSvar(), actual.getMessage().getLastDateToAnswer());
        }

        @Test
        void shallReturnCorrectQuestions() {
            arende.setKomplettering(
                List.of(
                    new MedicinsktArende() {{
                        setFrageId("question-1");
                    }}
                )
            );
            final var actual = factory.sentMessage(utkast, arende);
            assertNull(actual.getMessage().getQuestionIds(),
                "Question ids shall be null since it will never be sent to a certificate recipient"
            );
        }
    }
}