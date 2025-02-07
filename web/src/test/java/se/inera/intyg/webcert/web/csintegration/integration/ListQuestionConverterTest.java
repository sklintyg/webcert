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
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@ExtendWith(MockitoExtension.class)
class ListQuestionConverterTest {

    @InjectMocks
    ListQuestionConverter listQuestionConverter;

    private static final Optional<CertificateDTO> CERTIFICATE = Optional.of(CertificateDTO.create(CertificateFacadeTestHelper
            .createCertificateTypeWithVersion(
                "type", CertificateStatus.UNSIGNED, true, "typeVersion"),
        Collections.emptyList().toArray(ResourceLinkDTO[]::new)
    ));

    private static Question QUESTION;

    @BeforeEach
    void setUp() {
        QUESTION = buildQuestion(QuestionType.COMPLEMENT);
    }

    @Test
    void shouldConvertCertificateId() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().getId(), response.getIntygId());
    }


    @Test
    void shouldConvertCertificateTestIndicated() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().isTestCertificate(), response.isTestIntyg());
    }

    @Test
    void shouldConvertPatientId() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().getPatient().getPersonId().getId(), response.getPatientId());
    }

    @Test
    void shouldConvertIsDeceased() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().getPatient().isDeceased(), response.isAvliden());
    }

    @Test
    void shouldConvertSignedBy() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().getIssuedBy().getPersonId(), response.getSigneratAv());
    }

    @Test
    void shouldConvertSignedByName() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(CERTIFICATE.get().getMetadata().getIssuedBy().getFullName(), response.getSigneratAvNamn());
    }

    @Test
    void shouldConvertIsReminder() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertTrue(response.isPaminnelse());
    }

    @Test
    void shouldConvertProtectedPerson() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertFalse(response.isSekretessmarkering());
    }

    @Test
    void shouldConvertAuthor() {
        final var response = listQuestionConverter.convert(CERTIFICATE, QUESTION);
        assertEquals(QuestionSenderType.FK.toString(), response.getFragestallare());
    }

    @Nested
    class ConvertStatus {

        @Test
        void shouldConvertStatusClosedIfHandled() {
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion(true));
            assertEquals(Status.CLOSED, response.getStatus());
        }

        @Test
        void shouldConvertStatusAnsweredIfAnswered() {
            final var answer = Answer.builder()
                .sent(LocalDateTime.now())
                .author("FKASSA")
                .message("message")
                .build();
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion(answer));
            assertEquals(Status.ANSWERED, response.getStatus());
        }

        @Test
        void shouldConvertStatusPendingInternalActionIfAuthorIsForsakringskassan() {
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion("Försäkringskassan"));
            assertEquals(Status.PENDING_INTERNAL_ACTION, response.getStatus());
        }

        @Test
        void shouldConvertStatusPendingExternalActionIfAuthorIsNotForskakringskassan() {
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion("WC"));
            assertEquals(Status.PENDING_EXTERNAL_ACTION, response.getStatus());
        }
    }

    @Nested
    class ConvertLinks {

        @Test
        void shouldConvertReadCertificateToLasaFraga() {
            final var link = ResourceLink.builder()
                .type(ResourceLinkTypeEnum.READ_CERTIFICATE)
                .build();
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion(List.of(link)));
            assertEquals(ActionLinkType.LASA_FRAGA, response.getLinks().get(0).getType());
        }

        @Test
        void shouldConvertForwardQuestionToVidarebefordraFraga() {
            final var link = ResourceLink.builder()
                .type(ResourceLinkTypeEnum.FORWARD_QUESTION)
                .build();
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion(List.of(link)));
            assertEquals(ActionLinkType.VIDAREBEFODRA_FRAGA, response.getLinks().get(1).getType());
        }

        @Test
        void shouldNotConvertUnknownResourceLink() {
            final var link = ResourceLink.builder()
                .type(ResourceLinkTypeEnum.COMPLEMENT_CERTIFICATE)
                .build();
            final var response = listQuestionConverter.convert(CERTIFICATE, buildQuestion(List.of(link)));
            assertEquals(List.of(new ActionLink(ActionLinkType.LASA_FRAGA)), response.getLinks());
        }
    }

    @Nested
    class ConvertSubject {

        @Test
        void shouldConvertSubjectCoordination() {
            final Question questionDto = buildQuestion(QuestionType.COORDINATION);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("AVSTMN", response.getAmne());
        }

        @Test
        void shouldConvertSubjectContact() {
            final Question questionDto = buildQuestion(QuestionType.CONTACT);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("KONTKT", response.getAmne());
        }

        @Test
        void shouldConvertSubjectMissing() {
            final Question questionDto = buildQuestion(QuestionType.MISSING);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("OVRIGT", response.getAmne());
        }

        @Test
        void shouldConvertSubjectOther() {
            final Question questionDto = buildQuestion(QuestionType.OTHER);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("OVRIGT", response.getAmne());
        }

        @Test
        void shouldConvertSubjectComplement() {
            final Question questionDto = buildQuestion(QuestionType.COMPLEMENT);
            final var response = listQuestionConverter.convert(CERTIFICATE, questionDto);
            assertEquals("KOMPLT", response.getAmne());
        }
    }

    private Question buildQuestion(List<ResourceLink> links) {
        return buildQuestion(QuestionType.COMPLEMENT, false, null, null, links);
    }

    private Question buildQuestion(String author) {
        return buildQuestion(QuestionType.COMPLEMENT, false, null, author, Collections.emptyList());
    }

    private Question buildQuestion(Answer answer) {
        return buildQuestion(QuestionType.COMPLEMENT, false, answer, null, Collections.emptyList());
    }

    private Question buildQuestion(Boolean isHandled) {
        return buildQuestion(QuestionType.COMPLEMENT, isHandled, null, null, Collections.emptyList());
    }

    private Question buildQuestion(QuestionType questionType) {
        return buildQuestion(questionType, false, null, null, Collections.emptyList());
    }

    private Question buildQuestion(QuestionType questionType, Boolean isHandled, Answer answer, String author, List<ResourceLink> links) {
        return Question.builder()
            .id("id")
            .type(questionType)
            .isForwarded(true)
            .answeredByCertificate(CertificateRelation.builder().build())
            .links(Collections.emptyList())
            .sent(LocalDateTime.now())
            .isHandled(isHandled)
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
            .answer(answer)
            .author(author != null ? author : "Försäkringskassan")
            .links(links)
            .build();
    }

}
