/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;

@Value
@Builder
public class IntygContentHolder {

    @JsonRawValue
    String contents;
    @JsonIgnore
    Utlatande utlatande;
    List<Status> statuses;
    boolean revoked;
    Relations relations;
    LocalDateTime created;
    boolean deceased;
    boolean sekretessmarkering;
    boolean patientNameChangedInPU;
    boolean patientAddressChangedInPU;
    boolean testIntyg;
    boolean latestMajorTextVersion;
    List<ActionLink> links = new ArrayList<>();

    public void addLink(ActionLink link) {
        links.add(link);
    }

}
