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

package se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest.PrivatePractitionerRegistrationRequestBuilder;

@Builder
@Value
@JsonDeserialize(builder = PrivatePractitionerRegistrationRequestBuilder.class)
public class PrivatePractitionerRegistrationRequest {

    @NotNull(message = "Position is required")
    String position;
    @NotNull(message = "Care Unit is required")
    String careUnitName;
    @NotNull(message = "Type of Care is required")
    String typeOfCare;
    @NotNull(message = "Healthcare Service Type is required")
    String healthcareServiceType;
    String workplaceCode;

    @NotNull(message = "Phone number is required")
    String phoneNumber;
    @NotNull(message = "Email is required")
    String email;
    @NotNull(message = "Address is required")
    String address;
    @NotNull(message = "Zip code is required")
    String zipCode;
    String city;
    String municipality;
    String county;

    Long consentFormVersion;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PrivatePractitionerRegistrationRequestBuilder {

    }
}
