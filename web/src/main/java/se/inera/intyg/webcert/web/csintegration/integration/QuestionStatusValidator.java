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

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@Component
public class QuestionStatusValidator {

    public Boolean validate(ArendeListItem question, QuestionStatusType statusToFilterOn) {
        switch (statusToFilterOn) {
            case NOT_HANDLED:
                return question.getStatus() != Status.CLOSED;
            case HANDLED:
                return question.getStatus() == Status.CLOSED;
            case COMPLEMENT:
                return question.getStatus() == Status.PENDING_INTERNAL_ACTION
                    && Objects.equals(question.getAmne(), ArendeAmne.KOMPLT.name());
            case ANSWER:
                return question.getStatus() == Status.PENDING_INTERNAL_ACTION
                    && List.of(ArendeAmne.KONTKT.name(), ArendeAmne.OVRIGT.name(), ArendeAmne.AVSTMN.name()).contains(question.getAmne());
            case READ_ANSWER:
                return
                    (question.getStatus() == Status.PENDING_INTERNAL_ACTION
                        && Objects.equals(question.getAmne(), ArendeAmne.PAMINN.name()))
                        || question.getStatus() == Status.ANSWERED;
            case WAIT:
                return question.getStatus() == Status.PENDING_EXTERNAL_ACTION;
            case SHOW_ALL:
                return true;
        }
        return false;
    }
}
