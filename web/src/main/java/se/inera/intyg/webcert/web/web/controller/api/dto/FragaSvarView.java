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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * Wrapper class for adding some extra data to the FragaSvar database entities, before sending them to frontend. Due to
 * the legacy status of FragaSvar code, it is not done in the same manner as with the Arende entities, but instead
 * quicker solution.
 */
@Value
@Builder
public class FragaSvarView {

    FragaSvar fragaSvar;
    AnsweredWithIntyg answeredWithIntyg;
    String answerDraft;

}
