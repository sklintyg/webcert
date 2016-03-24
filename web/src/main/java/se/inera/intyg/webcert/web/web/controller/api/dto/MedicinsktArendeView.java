/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import com.google.auto.value.AutoValue;

import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.Builder;

@AutoValue
public abstract class MedicinsktArendeView {

    public abstract Integer getPosition();

    public abstract Integer getInstans();

    public abstract String getFrageId();

    public abstract String getText();

    public abstract String getJsonPropertyHandle();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_MedicinsktArendeView.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setPosition(Integer position);

        public abstract Builder setInstans(Integer instans);

        public abstract Builder setText(String text);

        public abstract Builder setFrageId(String frageId);

        public abstract Builder setJsonPropertyHandle(String jsonPropertyHandle);

        public abstract MedicinsktArendeView build();
    }

}
