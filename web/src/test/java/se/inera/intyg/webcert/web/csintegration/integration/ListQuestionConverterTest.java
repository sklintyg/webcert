/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

@ExtendWith(MockitoExtension.class)
class ListQuestionConverterTest {

    @InjectMocks
    ListQuestionConverter listQuestionConverter;

    private static final Optional<CertificateDTO> CERTIFICATE = Optional.of(CertificateDTO.create(CertificateFacadeTestHelper
            .createCertificateTypeWithVersion(
                "type", CertificateStatus.UNSIGNED, true, "typeVersion"),
        Collections.emptyList().toArray(ResourceLinkDTO[]::new)
    ));

    private static QuestionDTO QUESTION_DTO;

    @BeforeEach
    void setUp() {
        QUESTION_DTO = buildQuestionDTO(QuestionType.COMPLEMENT);
    }

    @Test
    void shouldConvertCertificateId() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().getId(), response.get(0).getIntygId());
    }


    @Test
    void shouldConvertCertificateTestIndicated() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().isTestCertificate(), response.get(0).isTestIntyg());
    }

    @Test
    void shouldConvertPatientId() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().getPatient().getPersonId().getId(), response.get(0).getPatientId());
    }

    @Test
    void shouldConvertIsDeceased() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().getPatient().isDeceased(), response.get(0).isAvliden());
    }

    @Test
    void shouldConvertSignedBy() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().getIssuedBy().getPersonId(), response.get(0).getSigneratAv());
    }

    @Test
    void shouldConvertSignedByName() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(CERTIFICATE.get().getMetadata().getIssuedBy().getFullName(), response.get(0).getSigneratAvNamn());
    }

    @Test
    void shouldConvertIsReminder() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertTrue(response.get(0).isPaminnelse());
    }

    @Test
    void shouldConvertProtectedPerson() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertFalse(response.get(0).isSekretessmarkering());
    }


    @Test
    void shouldConvertAuthor() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION_DTO);
        assertEquals(QUESTION_DTO.getAuthor(), response.get(0).getFragestallare());
    }

    @Nested
    class ConvertSubject {

        @Test
        void shouldConvertSubjectToReminderIfIsReminder() {
            final var questionDto = buildQuestionDTO(QuestionType.COORDINATION);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("PAMINN", response.get(0).getAmne());
        }

        @Test
        void shouldConvertSubjectCoordination() {
            final var questionDto = buildQuestionDTO(QuestionType.COORDINATION);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("AVSTMN", response.get(1).getAmne());
        }

        @Test
        void shouldConvertSubjectContact() {
            final var questionDto = buildQuestionDTO(QuestionType.CONTACT);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("KONTKT", response.get(1).getAmne());
        }

        @Test
        void shouldConvertSubjectMissing() {
            final var questionDto = buildQuestionDTO(QuestionType.MISSING);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("OVRIGT", response.get(1).getAmne());
        }

        @Test
        void shouldConvertSubjectOther() {
            final var questionDto = buildQuestionDTO(QuestionType.OTHER);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("OVRIGT", response.get(1).getAmne());
        }

        @Test
        void shouldConvertSubjectComplement() {
            final var questionDto = buildQuestionDTO(QuestionType.COMPLEMENT);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("KOMPLT", response.get(1).getAmne());
        }
    }

    private QuestionDTO buildQuestionDTO(QuestionType questionType) {
        return QuestionDTO.builder()
            .id("id")
            .type(questionType)
            .isForwarded(true)
            .answeredByCertificate(CertificateRelation.builder().build())
            .links(Collections.emptyList())
            .sent(LocalDateTime.now())
            .isHandled(false)
            .lastDateToReply(LocalDate.now())
            .lastUpdate(LocalDateTime.now())
            .message("message")
            .reminders(List.of(
                Reminder.builder()
                    .author("author")
                    .id("id")
                    .message("message")
                    .sent(LocalDateTime.now())
                    .build()).toArray(Reminder[]::new))
            .subject("subject")
            .certificateId("certificateId")
            .complements(List.of(Complement.builder().build()).toArray(Complement[]::new))
            .contactInfo(List.of("contactinfo1", "contactinfo2").toArray(String[]::new))
            .answer(Answer.builder().build())
            .author("author")
            .build();
    }

}
