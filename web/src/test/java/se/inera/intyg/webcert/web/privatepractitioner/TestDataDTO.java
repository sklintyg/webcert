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

import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_ADDRESS;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_CARE_UNIT_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_CITY;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_COUNTY;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_EMAIL;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_MUNICIPALITY;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PHONE_NUMBER;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_POSITION;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PRESCRIPTION_CODE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_TYPE_OF_CARE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_WORKPLACE_CODE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_ZIP_CODE;

import java.util.List;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails.PrivatePractitionerDetailsBuilder;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse.PrivatePractitionerResponseBuilder;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerUpdateRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerUpdateRequest.PrivatePractitionerUpdateRequestBuilder;

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

    public static final List<CodeDTO> DR_KRANSTEGE_LICENSED_HEALTHCARE_PROFESSIONS =
        List.of(new CodeDTO("LK", "Läkare"));

    public static final PrivatePractitionerDetails DR_KRANSTEGE_REGISTREATION_REQUEST_DTO = kranstegeRegistrationRequest().build();
    public static final PrivatePractitionerUpdateRequest DR_KRANSTEGE_UPDATE_REQUEST_DTO = kranstegeUpdateRequest().build();
    public static final UpdatePrivatePractitionerRequest DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO = kranstegeRequestUpdate().build();

    private static UpdatePrivatePractitionerRequest.UpdatePrivatePractitionerRequestBuilder kranstegeRequestUpdate() {
        return UpdatePrivatePractitionerRequest.builder()
            .personId(DR_KRANSTEGE_PERSON_ID)
            .position(DR_KRANSTEGE_POSITION)
            .careUnitName(DR_KRANSTEGE_CARE_UNIT_NAME)
            .typeOfCare(DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(DR_KRANSTEGE_PHONE_NUMBER)
            .email(DR_KRANSTEGE_EMAIL)
            .address(DR_KRANSTEGE_ADDRESS)
            .zipCode(DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(DR_KRANSTEGE_MUNICIPALITY)
            .county(DR_KRANSTEGE_COUNTY);
    }

    public static final PrivatePractitionerConfigResponse PRIVATE_PRACTITIONER_CONFIG_DTO = PrivatePractitionerConfigResponse
        .builder()
        .positions(POSITIONS_DTO)
        .healthcareServiceTypes(HEALTHCARE_SERVICE_TYPES_DTO)
        .typeOfCare(TYPE_OF_CARE_DTO)
        .build();

    public static HospInformationResponse DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO = HospInformationResponse.builder()
        .personalPrescriptionCode(DR_KRANSTEGE_PRESCRIPTION_CODE)
        .licensedHealthcareProfessions(DR_KRANSTEGE_LICENSED_HEALTHCARE_PROFESSIONS)
        .specialities(DR_KRANSTEGE_SPECIALITIES)
        .build();

    public static PrivatePractitionerResponse DR_KRANSTEGE_RESPONSE_DTO = kranstegeResponse().build();

    public static PrivatePractitionerDetailsBuilder kranstegeRegistrationRequest() {
        return PrivatePractitionerDetails.builder()
            .position(DR_KRANSTEGE_POSITION)
            .careUnitName(DR_KRANSTEGE_CARE_UNIT_NAME)
            .typeOfCare(DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(DR_KRANSTEGE_PHONE_NUMBER)
            .email(DR_KRANSTEGE_EMAIL)
            .address(DR_KRANSTEGE_ADDRESS)
            .zipCode(DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(DR_KRANSTEGE_MUNICIPALITY)
            .county(DR_KRANSTEGE_COUNTY);
    }

    private static PrivatePractitionerUpdateRequestBuilder kranstegeUpdateRequest() {
        return PrivatePractitionerUpdateRequest.builder()
            .personId(DR_KRANSTEGE_PERSON_ID)
            .position(DR_KRANSTEGE_POSITION)
            .careUnitName(DR_KRANSTEGE_CARE_UNIT_NAME)
            .typeOfCare(DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(DR_KRANSTEGE_PHONE_NUMBER)
            .email(DR_KRANSTEGE_EMAIL)
            .address(DR_KRANSTEGE_ADDRESS)
            .zipCode(DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(DR_KRANSTEGE_MUNICIPALITY)
            .county(DR_KRANSTEGE_COUNTY);
    }

    public static PrivatePractitionerResponseBuilder kranstegeResponse() {
        return PrivatePractitionerResponse.builder()
            .personId(DR_KRANSTEGE_PERSON_ID)
            .name(DR_KRANSTEGE_NAME)
            .position(DR_KRANSTEGE_POSITION)
            .careUnitName(DR_KRANSTEGE_CARE_UNIT_NAME)
            .typeOfCare(DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(DR_KRANSTEGE_PHONE_NUMBER)
            .email(DR_KRANSTEGE_EMAIL)
            .address(DR_KRANSTEGE_ADDRESS)
            .zipCode(DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(DR_KRANSTEGE_MUNICIPALITY)
            .county(DR_KRANSTEGE_COUNTY);
    }


}
