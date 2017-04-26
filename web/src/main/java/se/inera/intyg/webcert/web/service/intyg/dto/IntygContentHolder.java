/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

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

    public abstract List<RelationItem> getRelations();

    @Nullable
    public abstract RelationItem getReplacedByRelation();

    @Nullable
    public abstract RelationItem getComplementedByRelation();

    public abstract boolean isDeceased();

    public static Builder builder() {
        return new AutoValue_IntygContentHolder.Builder()
                .setRelations(new ArrayList<>());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract IntygContentHolder build();

        public abstract Builder setContents(String contents);

        public abstract Builder setUtlatande(Utlatande utlatande);

        public abstract Builder setStatuses(List<Status> statuses);

        public abstract Builder setRevoked(boolean revoked);

        public abstract Builder setRelations(List<RelationItem> relations);

        public abstract Builder setReplacedByRelation(RelationItem replacedByRelation);

        public abstract Builder setComplementedByRelation(RelationItem complementedByRelation);

        public abstract Builder setDeceased(boolean deceased);
    }

}
