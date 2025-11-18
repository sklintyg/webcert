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
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitioner;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse.PrivatePractitionerResponseBuilder;

@Value
@Builder
@JsonDeserialize(builder = PrivatePractitionerResponseBuilder.class)
public class PrivatePractitionerResponse {

    String personId;
    String name;

    String position;
    String careUnitName;
    String typeOfCare;
    String healthcareServiceType;
    String workplaceCode;

    String phoneNumber;
    String email;
    String address;
    String zipCode;
    String city;
    String municipality;
    String county;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PrivatePractitionerResponseBuilder {

    }

    public static PrivatePractitionerResponse convert(PrivatePractitioner privatePractitioner) {
        return PrivatePractitionerResponse.builder()
            .personId(privatePractitioner.getPersonId())
            .name(privatePractitioner.getName())
            .position(privatePractitioner.getPosition())
            .careUnitName(privatePractitioner.getCareUnitName())
            .typeOfCare(privatePractitioner.getTypeOfCare())
            .healthcareServiceType(privatePractitioner.getHealthcareServiceType())
            .workplaceCode(privatePractitioner.getWorkplaceCode())
            .phoneNumber(privatePractitioner.getPhoneNumber())
            .email(privatePractitioner.getEmail())
            .address(privatePractitioner.getAddress())
            .zipCode(privatePractitioner.getZipCode())
            .city(privatePractitioner.getCity())
            .municipality(privatePractitioner.getMunicipality())
            .county(privatePractitioner.getCounty())
            .build();
    }
}
