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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
            .build();
        certificate = new Certificate();
        certificate.setMetadata(metadata);

        final var result = certificateToIntygInfoConverter.convert(certificate, Collections.emptyList());
        assertNull(result.getSentToRecipient());
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
            void shouldSetComplementsAnswered() {
                final var question = Question.builder()
                    .type(QuestionType.COMPLEMENT)
                    .answer(
                        Answer.builder()
                            .sent(LocalDateTime.now())
                            .build()
                    )
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
            assertEquals(List.of(event1, event2), result.getEvents());
        }
    }
}