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

package se.inera.intyg.webcert.web.service.underskrift.grp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpAttributes.GrpAttributesBuilder;

@JsonDeserialize(builder = GrpAttributesBuilder.class)
@Value
@Builder
public class GrpAttributes {

    @JsonProperty("urn:oid:2.16.840.1.113730.3.1.241")
    String fullName;
    @JsonProperty("urn:oid:2.5.4.42")
    String firstName;
    @JsonProperty("urn:oid:1.2.752.29.4.13")
    String personId;
    @JsonProperty("urn:oid:2.5.4.4")
    String lastName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GrpAttributesBuilder {

    }

}