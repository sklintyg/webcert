/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.model.Status;

@Component
public class FragaSvarToQuestionConverterImpl implements FragaSvarToQuestionConverter {

    @Override
    public Question convert(FragaSvar fragaSvar) {
        if (fragaSvar == null) {
            return null;
        }
        return Question.builder()
            .id(String.valueOf(fragaSvar.getInternReferens()))
            .author(getAuthor(fragaSvar.getFrageStallare(), fragaSvar.getVardperson().getNamn()))
            .sent(fragaSvar.getFrageSkickadDatum())
            .lastUpdate(getLastUpdate(fragaSvar))
            .lastDateToReply(fragaSvar.getSistaDatumForSvar())
            .message(fragaSvar.getFrageText())
            .subject(getSubject(fragaSvar))
            .type(getType(fragaSvar.getAmne()))
            .isHandled(fragaSvar.getStatus() == Status.CLOSED)
            .isForwarded(fragaSvar.getVidarebefordrad())
            .complements(getComplements(fragaSvar))
            .reminders(new Reminder[0])
            .build();
    }

    private LocalDateTime getLastUpdate(FragaSvar fragaSvar) {
        if (fragaSvar.getSvarSkickadDatum() != null) {
            return fragaSvar.getSvarSkickadDatum();
        } else {
            return fragaSvar.getFrageSkickadDatum();
        }
    }

    private String getAuthor(String frageStallare, String vardpersonn) {
        if (frageStallare == null) {
            return null;
        }
        switch (frageStallare) {
            case "FK":
                return "Försäkringskassan";
            case "WC":
                return vardpersonn;
            default:
                return null;
        }
    }

    private Complement[] getComplements(FragaSvar fragaSvar) {
        final var kompletteringar = fragaSvar.getKompletteringar();

        if (kompletteringar == null || kompletteringar.isEmpty()) {
            return new Complement[0];
        }
        return kompletteringar.stream().map(this::convertToComplement).toArray(Complement[]::new);
    }

    private Complement convertToComplement(Komplettering komplettering) {
        return Complement.builder()
            .questionText(komplettering.getFalt())
            .message(komplettering.getText())
            .build();
    }
}
