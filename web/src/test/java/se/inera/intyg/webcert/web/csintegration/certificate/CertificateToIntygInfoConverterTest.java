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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.webcert.web.service.intyginfo.GetIntygInfoEventsService;

@ExtendWith(MockitoExtension.class)
class CertificateToIntygInfoConverterTest {

    @Mock
    private GetIntygInfoEventsService getIntygInfoEventsService;
    @Mock
    private CertificateRelationsToIntygInfoEventsConverter certificateRelationsToIntygInfoEventsConverter;
    @InjectMocks
    private CertificateToIntygInfoConverter certificateToIntygInfoConverter;

    private Certificate certificate;
    private CertificateMetadata metadata;

    @Test
    void shouldSetSentToNullIfNotSent() {
        metadata = CertificateMetadata.builder()
            .id("123")
            .type("TypeA")
            .typeVersion("1.0")
            .signed(LocalDateTime.now())
            .issuedBy(Staff.builder().fullName("Dr. John Doe").personId("1234567890").build())
            .careUnit(Unit.builder().unitName("Care Unit A").unitId("CU123").build())
            .careProvider(Unit.builder().unitName("Care Provider A").unitId("CP123").build())
            .sent(false)
            .recipient(
                CertificateRecipient.builder()
                    .sent(LocalDateTime.now())
                    .build()
            )
            .testCertificate(true)
            .created(LocalDateTime.now())
            .createdBy(
                Staff.builder()
                    .fullName("Dr. Jane Test")
                    .personId("0987654321")
                    .build()
            )
            .readyForSign(LocalDateTime.now().minusMonths(1))
            .revokedAt(LocalDateTime.now().minusDays(1))
            .revokedBy(
                Staff.builder()
                    .fullName("NAME")
                    .personId("HSA_ID")
                    .build()
            )
            .build();
        certificate = new Certificate();
        certificate.setMetadata(metadata);

        final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
        assertNull(result.getSentToRecipient());
    }

    @Test
    void shouldSetNumberOfRecipientsTo0IfNoRecipient() {
        metadata = CertificateMetadata.builder()
            .id("123")
            .type("TypeA")
            .typeVersion("1.0")
            .signed(LocalDateTime.now())
            .issuedBy(Staff.builder().fullName("Dr. John Doe").personId("1234567890").build())
            .careUnit(Unit.builder().unitName("Care Unit A").unitId("CU123").build())
            .careProvider(Unit.builder().unitName("Care Provider A").unitId("CP123").build())
            .sent(true)
            .testCertificate(true)
            .created(LocalDateTime.now())
            .createdBy(
                Staff.builder()
                    .fullName("Dr. Jane Test")
                    .personId("0987654321")
                    .build()
            )
            .build();
        certificate = new Certificate();
        certificate.setMetadata(metadata);

        final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
        assertEquals(0, result.getNumberOfRecipients());
    }

    @Nested
    class Prefilled {


        @BeforeEach
        void setUp() {
            metadata = CertificateMetadata.builder()
                .id("123")
                .type("TypeA")
                .typeVersion("1.0")
                .signed(LocalDateTime.now())
                .issuedBy(Staff.builder().fullName("Dr. John Doe").personId("1234567890").build())
                .careUnit(Unit.builder().unitName("Care Unit A").unitId("CU123").build())
                .careProvider(Unit.builder().unitName("Care Provider A").unitId("CP123").build())
                .sent(true)
                .recipient(
                    CertificateRecipient.builder()
                        .sent(LocalDateTime.now())
                        .build()
                )
                .testCertificate(true)
                .created(LocalDateTime.now())
                .createdBy(
                    Staff.builder()
                        .fullName("Dr. Jane Test")
                        .personId("0987654321")
                        .build()
                )
                .readyForSign(LocalDateTime.now().minusMonths(1))
                .revokedAt(LocalDateTime.now().minusDays(1))
                .revokedBy(
                    Staff.builder()
                        .fullName("NAME")
                        .personId("HSA_ID")
                        .build()
                )
                .build();
            certificate = new Certificate();
            certificate.setMetadata(metadata);
        }

        @Nested
        class Questions {

            @Test
            void shouldSetComplements() {
                final var question = Question.builder()
                    .type(QuestionType.COMPLEMENT)
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);
                assertEquals(1, result.getKompletteringar());
            }

            @Test
            void shouldSetComplementsAnsweredIfQuestionIsHandled() {
                final var question = Question.builder()
                    .type(QuestionType.COMPLEMENT)
                    .isHandled(true)
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);
                assertEquals(1, result.getKompletteringarAnswered());
            }

            @Test
            void shouldSetAdminQuestionsSent() {
                final var question = Question.builder()
                    .author("WC")
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);
                assertEquals(1, result.getAdministrativaFragorSent());
            }

            @Test
            void shouldSetAdminQuestionsReceived() {
                final var question = Question.builder()
                    .author("FK")
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);
                assertEquals(1, result.getAdministrativaFragorReceived());
            }

            @Test
            void shouldSetAdminQuestionsSentAnswered() {
                final var question = Question.builder()
                    .author("WC")
                    .answer(
                        Answer.builder()
                            .sent(LocalDateTime.now())
                            .build()
                    )
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);
                assertEquals(1, result.getAdministrativaFragorSentAnswered());
            }

            @Test
            void shouldSetAdminQuestionsReceivedAnswered() {
                final var question = Question.builder()
                    .author("FK")
                    .answer(
                        Answer.builder()
                            .sent(LocalDateTime.now())
                            .build()
                    )
                    .build();
                final var questions = Collections.singletonList(question);

                final var result = certificateToIntygInfoConverter.convert(certificate, questions);

                assertEquals(1, result.getAdministrativaFragorReceivedAnswered());
            }
        }

        @Test
        void shouldSetNumberOfRecipientsTo1IfRecipient() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals(1, result.getNumberOfRecipients());
        }

        @Test
        void shouldSetIntygId() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("123", result.getIntygId());
        }

        @Test
        void shouldSetIntygType() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("TypeA", result.getIntygType());
        }

        @Test
        void shouldSetIntygVersion() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("1.0", result.getIntygVersion());
        }

        @Test
        void shouldSetSignedDate() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals(metadata.getSigned(), result.getSignedDate());
        }

        @Test
        void shouldSetSentToRecipientIfSent() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals(metadata.getRecipient().getSent(), result.getSentToRecipient());
        }

        @Test
        void shouldSetSignedByName() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("Dr. John Doe", result.getSignedByName());
        }

        @Test
        void shouldSetSignedByHsaId() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("1234567890", result.getSignedByHsaId());
        }

        @Test
        void shouldSetCareUnitName() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("Care Unit A", result.getCareUnitName());
        }

        @Test
        void shouldSetCareUnitHsaId() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("CU123", result.getCareUnitHsaId());
        }

        @Test
        void shouldSetCareGiverName() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("Care Provider A", result.getCareGiverName());
        }

        @Test
        void shouldSetCareGiverHsaId() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals("CP123", result.getCareGiverHsaId());
        }

        @Test
        void shouldSetTestCertificate() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertTrue(result.isTestCertificate());
        }

        @Test
        void shouldSetCreatedInWC() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertTrue(result.isCreatedInWC());
        }

        @Test
        void shouldSetDraftCreated() {
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertEquals(metadata.getCreated(), result.getDraftCreated());
        }

        @Test
        void shouldSetEvents() {
            final var event1 = new IntygInfoEvent(Source.WEBCERT);
            final var event2 = new IntygInfoEvent(Source.INTYGSTJANSTEN);
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.singletonList(event1));
            when(certificateRelationsToIntygInfoEventsConverter.convert(certificate)).thenReturn(Collections.singletonList(event2));

            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            assertAll(
                () -> assertEquals(event1, result.getEvents().getFirst())
            );
        }

        @Test
        void shouldSetCreatedEvent() {
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.emptyList());
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            final var createdEvent = result.getEvents().stream()
                .filter(event -> event.getType() == IntygInfoEventType.IS001)
                .findFirst()
                .orElseThrow();
            assertAll(
                () -> assertEquals(
                    metadata.getCreated(),
                    createdEvent.getDate()
                )
            );
        }

        @Test
        void shouldSetRevokedEvent() {
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.emptyList());

            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());

            final var revokedEvent = result.getEvents().stream()
                .filter(event -> event.getType() == IntygInfoEventType.IS009)
                .findFirst()
                .orElseThrow();
            assertEquals(
                metadata.getRevokedAt(),
                revokedEvent.getDate()
            );
        }

        @Test
        void shouldSetReadyForSignEvent() {
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.emptyList());

            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());

            final var readyForSignEvent = result.getEvents().stream()
                .filter(event -> event.getType() == IntygInfoEventType.IS018)
                .findFirst()
                .orElseThrow();
            assertEquals(
                metadata.getReadyForSign(),
                readyForSignEvent.getDate()

            );
        }

        @Test
        void shouldSetSentEvent() {
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.emptyList());

            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());

            final var sentEvent = result.getEvents().stream()
                .filter(event -> event.getType() == IntygInfoEventType.IS006)
                .findFirst()
                .orElseThrow();
            assertAll(
                () -> assertEquals(
                    metadata.getRecipient().getSent(),
                    sentEvent.getDate()

                ),
                () -> assertEquals(
                    certificate.getMetadata().getRecipient().getName(),
                    sentEvent.getData().get("intygsmottagare")
                )
            );
        }

        @Test
        void shouldSetSignedEvent() {
            when(getIntygInfoEventsService.get(metadata.getId())).thenReturn(Collections.emptyList());
            final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
            final var signedEvent = result.getEvents().stream()
                .filter(event -> event.getType() == IntygInfoEventType.IS004)
                .findFirst()
                .orElseThrow();
            assertAll(
                () -> assertEquals(
                    metadata.getSigned(),
                    signedEvent.getDate()
                ),
                () -> assertEquals(
                    metadata.getIssuedBy().getPersonId(),
                    signedEvent.getData().get("hsaId")
                ),
                () -> assertEquals(
                    metadata.getIssuedBy().getFullName(),
                    signedEvent.getData().get("name")
                )
            );
        }
    }
}