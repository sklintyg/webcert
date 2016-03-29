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

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;

@AutoValue
public abstract class ArendeView {

    public enum ArendeType {
        FRAGA,
        SVAR,
        PAMINNELSE
    }

    public abstract ImmutableList<MedicinsktArendeView> getKompletteringar();

    public abstract String getInternReferens();

    public abstract Status getStatus();

    @Nullable
    public abstract ArendeAmne getAmne();

    @Nullable
    public abstract String getMeddelandeRubrik();

    @Nullable
    public abstract LocalDate getSistaDatumForSvar();

    @Nullable
    public abstract Boolean getVidarebefordrad();

    @Nullable
    public abstract String getFrageStallare();

    @Nullable
    public abstract ImmutableList<String> getExternaKontakter();

    @Nullable
    public abstract String getMeddelande();

    @Nullable
    public abstract LocalDateTime getSvarSkickadDatum();

    public abstract String getIntygId();

    @Nullable
    public abstract String getEnhetsnamn();

    @Nullable
    public abstract String getVardgivarnamn();

    public abstract LocalDateTime getTimestamp();

    @Nullable
    public abstract ArendeType getArendeType();

    @Nullable
    public abstract String getSvarPaId();

    @Nullable
    public abstract String getPaminnelseMeddelandeId();

    /*
     * Retrieve a builder from an existing SjukersattningUtlatande object. The builder can then be used
     * to create a new copy with modified attributes.
     */
    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_ArendeView.Builder()
                .setKompletteringar(ImmutableList.<MedicinsktArendeView> of())
                .setExternaKontakter(ImmutableList.<String> of());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract ArendeView build();

        public abstract Builder setInternReferens(String internReferens);

        public abstract Builder setStatus(Status status);

        public abstract Builder setAmne(ArendeAmne arendeAmne);

        public abstract Builder setMeddelandeRubrik(String meddelandeRubrik);

        public abstract Builder setSistaDatumForSvar(LocalDate sistaDatumForSvar);

        public abstract Builder setEnhetsnamn(String enhetsnamn);

        public abstract Builder setIntygId(String intygId);

        public abstract Builder setVardgivarnamn(String vardgivarNamn);

        public abstract Builder setSvarSkickadDatum(LocalDateTime svarSkickadDatum);

        public abstract Builder setMeddelande(String meddelande);

        public abstract Builder setFrageStallare(String frageStallare);

        public abstract Builder setVidarebefordrad(Boolean vidarebefordrad);

        public abstract Builder setTimestamp(LocalDateTime timestamp);

        public abstract Builder setArendeType(ArendeType arendeType);

        public abstract Builder setSvarPaId(String svarPaId);

        public abstract Builder setPaminnelseMeddelandeId(String paminnelseMeddelandeId);

        public Builder setKompletteringar(List<MedicinsktArendeView> kompletteringar) {
            return setKompletteringar(ImmutableList.copyOf(kompletteringar));
        }

        public Builder setExternaKontakter(List<String> externaKontakter) {
            return setExternaKontakter(ImmutableList.copyOf(externaKontakter));
        }

        /* package private */
        abstract Builder setKompletteringar(ImmutableList<MedicinsktArendeView> kompletteringar);

        abstract Builder setExternaKontakter(ImmutableList<String> kompletteringar);
    }

}
