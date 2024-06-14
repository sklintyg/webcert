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

import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@Component
public class QuestionIsHandledValidator {

    private static final String MAKULERING = "MAKULERING";
    private static final String PAMINNELSE = "PAMINNELSE";
    private static final String PAMINN = "PAMINN";

    public Boolean validate(ArendeListItem arendeListItem) {
        return !isUnhandled(arendeListItem);
    }

    private boolean isUnhandled(ArendeListItem item) {
        return !((item.getStatus() == Status.PENDING_INTERNAL_ACTION && isReminder(item) && item.getFragestallare().equals("FK"))
            || item.getStatus() == Status.ANSWERED
            || item.getStatus() == Status.CLOSED
            || item.getAmne().equals(MAKULERING));
    }

    private boolean isReminder(ArendeListItem item) {
        return item.getAmne().equals(PAMINNELSE) || item.getAmne().equals(PAMINN);
    }
}
