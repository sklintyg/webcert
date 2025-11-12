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

package se.inera.intyg.webcert.web.privatepractitioner;

import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PRESCRIPTION_CODE;

import java.util.List;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse.HospInformationResponseBuilder;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest.PrivatePractitionerRegistrationRequestBuilder;

public class TestDataDTO {

    public static final List<CodeDTO> POSITIONS_DTO = List.of(new CodeDTO("203090", "Läkare legitimerad, annan"),
        new CodeDTO("201010", "Överläkare"));

    public static final List<CodeDTO> HEALTHCARE_SERVICE_TYPES_DTO = List.of(
        new CodeDTO("11", "Medicinsk verksamhet"),
        new CodeDTO("13", "Opererande verksamhet")
    );

    public static final List<CodeDTO> TYPE_OF_CARE_DTO = List.of(
        new CodeDTO("01", "Öppenvård"),
        new CodeDTO("02", "Slutenvård")
    );

    public static final List<CodeDTO> DR_KRANSTEGE_SPECIALITIES = List.of(
        new CodeDTO("32", "Klinisk fysiologi"),
        new CodeDTO("74", "Nukleärmedicin")
    );
    public static final PrivatePractitionerRegistrationRequest KRANSTEGE_REGISTREATION_REQUEST_DTO = kranstegeRegistrationRequest().build();

    public static final PrivatePractitionerConfigResponse PRIVATE_PRACTITIONER_CONFIG_DTO = PrivatePractitionerConfigResponse
        .builder()
        .positions(POSITIONS_DTO)
        .healthcareServiceTypes(HEALTHCARE_SERVICE_TYPES_DTO)
        .typeOfCare(TYPE_OF_CARE_DTO)
        .build();

    public static PrivatePractitionerRegistrationRequestBuilder kranstegeRegistrationRequest() {
        return PrivatePractitionerRegistrationRequest.builder()
            .position("Överläkare")
            .careUnitName("Kransteges specialistmottagning")
            .typeOfCare("01")
            .healthcareServiceType("11")
            .workplaceCode("555")
            .phoneNumber("0123-456789")
            .email("frida@kranstege.se")
            .address("Addressgatan 1")
            .zipCode("12345")
            .city("Stad")
            .municipality("Kommun")
            .county("Län");
    }

    public static HospInformationResponseBuilder kranstegeHospInformationResponse() {
        return HospInformationResponse.builder()
            .personalPrescriptionCode(DR_KRANSTEGE_PRESCRIPTION_CODE)
            .specialities(DR_KRANSTEGE_SPECIALITIES);
    }


}
