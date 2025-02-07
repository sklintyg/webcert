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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionDTO.QuestionDTOBuilder;

@JsonDeserialize(builder = QuestionDTOBuilder.class)
@Value
@Builder
public class QuestionDTO {

    String id;
    QuestionType type;
    String subject;
    String message;
    String author;
    LocalDateTime sent;
    Complement[] complements;
    boolean isHandled;
    boolean isForwarded;
    Answer answer;
    CertificateRelation answeredByCertificate;
    Reminder[] reminders;
    LocalDateTime lastUpdate;
    List<ResourceLinkDTO> links;
    LocalDate lastDateToReply;
    String[] contactInfo;
    String certificateId;

    public static QuestionDTO create(Question question, List<ResourceLinkDTO> links) {
        return QuestionDTO.builder()
            .id(question.getId())
            .type(question.getType())
            .subject(question.getSubject())
            .message(question.getMessage())
            .author(question.getAuthor())
            .sent(question.getSent())
            .complements(question.getComplements())
            .isHandled(question.isHandled())
            .isForwarded(question.isForwarded())
            .answer(question.getAnswer())
            .answeredByCertificate(question.getAnsweredByCertificate())
            .reminders(question.getReminders())
            .lastUpdate(question.getLastUpdate())
            .links(links)
            .lastDateToReply(question.getLastDateToReply())
            .contactInfo(question.getContactInfo())
            .certificateId(question.getCertificateId())
            .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class QuestionDTOBuilder {

    }
}
