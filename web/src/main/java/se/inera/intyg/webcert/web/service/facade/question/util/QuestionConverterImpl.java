/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.question.util;

import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getSubject;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getType;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getTypeFromAmneAsString;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.model.Status;

@Component
public class QuestionConverterImpl implements QuestionConverter {

    @Override
    public Question convert(ArendeDraft arendeDraft) {
        return Question.builder()
            .id(Long.toString(arendeDraft.getId()))
            .type(getTypeFromAmneAsString(arendeDraft.getAmne()))
            .message(arendeDraft.getText())
            .build();
    }

    @Override
    public Question convert(Arende arende) {
        return startConvert(arende, new Complement[0]).build();
    }

    @Override
    public Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate) {
        return startConvert(arende, complements).build();
    }

    @Override
    public Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, List<Arende> reminders) {
        return startConvert(arende, complements, answeredByCertificate, reminders).build();
    }

    @Override
    public Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, Arende answer,
        List<Arende> reminders) {
        if (answer == null) {
            return convert(arende, complements, answeredByCertificate, reminders);
        }

        return startConvert(arende, complements, answeredByCertificate, reminders)
            .answer(
                Answer.builder()
                    .id(answer.getMeddelandeId())
                    .message(answer.getMeddelande())
                    .author(answer.getVardaktorName())
                    .sent(answer.getSkickatTidpunkt())
                    .build()
            )
            .build();
    }

    @Override
    public Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, ArendeDraft answerDraft,
        List<Arende> reminders) {
        if (answerDraft == null) {
            return convert(arende, complements, answeredByCertificate, reminders);
        }

        return startConvert(arende, complements, answeredByCertificate, reminders)
            .answer(
                Answer.builder()
                    .message(answerDraft.getText())
                    .build()
            )
            .build();
    }

    private Question.QuestionBuilder startConvert(Arende arende, Complement[] complements) {
        return startConvert(arende, complements, null, Collections.emptyList());
    }

    private Question.QuestionBuilder startConvert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate,
        List<Arende> reminders) {
        final var remindersToAdd = reminders.stream()
            .map(reminder ->
                Reminder.builder()
                    .id(reminder.getMeddelandeId())
                    .author(getAuthor(reminder))
                    .message(reminder.getMeddelande())
                    .sent(reminder.getSkickatTidpunkt())
                    .build()
            )
            .toArray(Reminder[]::new);

        return Question.builder()
            .id(arende.getMeddelandeId())
            .type(getType(arende.getAmne()))
            .author(getAuthor(arende))
            .subject(getSubject(arende))
            .sent(arende.getSkickatTidpunkt())
            .isHandled(arende.getStatus() == Status.CLOSED)
            .isForwarded(arende.getVidarebefordrad())
            .message(arende.getMeddelande())
            .lastUpdate(arende.getSenasteHandelse())
            .reminders(remindersToAdd)
            .complements(complements)
            .answeredByCertificate(answeredByCertificate);
    }

    private String getAuthor(Arende arende) {
        if (arende.getSkickatAv().equalsIgnoreCase("FK")) {
            return "Försäkringskassan";
        }
        return arende.getVardaktorName();
    }
}