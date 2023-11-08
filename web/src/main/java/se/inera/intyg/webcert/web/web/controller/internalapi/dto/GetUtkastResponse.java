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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.Objects;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

public class GetUtkastResponse {

    private Utkast draft;

    private IntygContentHolder intygContentHolder;

    public static GetUtkastResponse create(Utkast draft) {
        final var getUtkastResponse = new GetUtkastResponse();
        getUtkastResponse.setDraft(draft);
        return getUtkastResponse;
    }

    public static GetUtkastResponse create(IntygContentHolder intygContentHolder) {
        final var getUtkastResponse = new GetUtkastResponse();
        getUtkastResponse.setIntygContentHolder(intygContentHolder);
        return getUtkastResponse;
    }

    public Utkast getDraft() {
        return draft;
    }

    public void setDraft(Utkast draft) {
        this.draft = draft;
    }

    public IntygContentHolder getIntygContentHolder() {
        return intygContentHolder;
    }

    public void setIntygContentHolder(IntygContentHolder intygContentHolder) {
        this.intygContentHolder = intygContentHolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GetUtkastResponse that = (GetUtkastResponse) o;
        return Objects.equals(draft, that.draft) && Objects.equals(intygContentHolder, that.intygContentHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(draft, intygContentHolder);
    }

    @Override
    public String toString() {
        return "GetUtkastResponse{"
            + "draft=" + draft
            + ", intygContentHolder=" + intygContentHolder
            + '}';
    }
}
