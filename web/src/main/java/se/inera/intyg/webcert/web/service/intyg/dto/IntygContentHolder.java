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
package se.inera.intyg.webcert.web.service.intyg.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;

@AutoValue
@JsonDeserialize(builder = AutoValue_IntygContentHolder.Builder.class)
public abstract class IntygContentHolder {

    @Nullable
    @JsonRawValue
    public abstract String getContents();

    @Nullable
    @JsonIgnore
    public abstract Utlatande getUtlatande();

    @Nullable
    public abstract List<Status> getStatuses();

    public abstract boolean isRevoked();

    public abstract Relations getRelations();

    @Nullable
    public abstract LocalDateTime getCreated();

    public abstract boolean isDeceased();

    public abstract boolean isSekretessmarkering();

    public abstract boolean isPatientNameChangedInPU();

    public abstract boolean isPatientAddressChangedInPU();

    public abstract boolean isTestIntyg();

    public abstract boolean isLatestMajorTextVersion();

    private List<ActionLink> links = new ArrayList<>();

    public List<ActionLink> getLinks() {
        return links;
    }

    public void addLink(ActionLink link) {
        links.add(link);
    }

    public static Builder builder() {
        return new AutoValue_IntygContentHolder.Builder()
            .setRelations(new Relations())
            .setLatestMajorTextVersion(true);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract IntygContentHolder build();

        public abstract Builder setContents(String contents);

        public abstract Builder setUtlatande(Utlatande utlatande);

        public abstract Builder setStatuses(List<Status> statuses);

        public abstract Builder setRevoked(boolean revoked);

        public abstract Builder setRelations(Relations relations);

        public abstract Builder setCreated(LocalDateTime relations);

        public abstract Builder setDeceased(boolean deceased);

        public abstract Builder setSekretessmarkering(boolean sekretessmarkering);

        public abstract Builder setPatientNameChangedInPU(boolean patientNameChangedInPU);

        public abstract Builder setPatientAddressChangedInPU(boolean patientAddressChangedInPU);

        public abstract Builder setTestIntyg(boolean isTestIntyg);

        public abstract Builder setLatestMajorTextVersion(boolean isLatestMajorTextVersion);
    }

}
