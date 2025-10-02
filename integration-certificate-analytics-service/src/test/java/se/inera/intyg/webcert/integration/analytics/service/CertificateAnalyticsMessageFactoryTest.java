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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUser;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUserService;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@ExtendWith(MockitoExtension.class)
class CertificateAnalyticsMessageFactoryTest {

    @Mock
    private LoggedInWebcertUserService loggedInWebcertUserService;

    @InjectMocks
    private static CertificateAnalyticsMessageFactory factory;

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

    private static final String RECIPIENT_ID = "recipient-id";

    @Nested
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

        static Stream<Arguments> analyticsMessagesBasedOnCertificate() {
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
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificateRevoked(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_REVOKED
                ),
                Arguments.of(
                    (Function<Certificate, CertificateAnalyticsMessage>) certificate -> factory.certificatePrinted(certificate),
                    CertificateAnalyticsMessageType.CERTIFICATE_PRINTED
                )
            );
        }
    }

    @Nested
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

        static Stream<Arguments> analyticsMessagesBasedOnUtkast() {
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
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftReadyForSign(utkast),
                    CertificateAnalyticsMessageType.DRAFT_READY_FOR_SIGN
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.lockedDraftRevoked(utkast),
                    CertificateAnalyticsMessageType.LOCKED_DRAFT_REVOKED
                ),
                Arguments.of(
                    (Function<Utkast, CertificateAnalyticsMessage>) utkast -> factory.draftCreateFromTemplate(utkast),
                    CertificateAnalyticsMessageType.DRAFT_CREATED_FROM_TEMPLATE
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
                )
            );
        }
    }

    @Nested
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

        static Stream<Arguments> analyticsMessagesBasedOnUtlatande() {
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
}