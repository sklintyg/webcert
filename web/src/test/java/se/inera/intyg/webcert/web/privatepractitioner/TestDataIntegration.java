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

import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_CITY;

import java.util.List;
import se.inera.intyg.webcert.integration.privatepractitioner.model.Code;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerConfig;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest.RegisterPrivatePractitionerRequestBuilder;

public class TestDataIntegration {

    public static final List<Code> POSITIONS = List.of(new Code("203090", "Läkare legitimerad, annan"),
        new Code("201010", "Överläkare"));

    public static final List<Code> HEALTHCARE_SERVICE_TYPES = List.of(
        new Code("11", "Medicinsk verksamhet"),
        new Code("13", "Opererande verksamhet")
    );

    public static final List<Code> TYPE_OF_CARE = List.of(
        new Code("01", "Öppenvård"),
        new Code("02", "Slutenvård")
    );

    public static final RegisterPrivatePractitionerRequest KRANSTEGE_REGISTREATION_REQUEST = registerPrivatePractitionerRequest().build();
    public static final PrivatePractitionerConfig PRIVATE_PRACTITIONER_CONFIG = PrivatePractitionerConfig
        .builder()
        .positionCodes(POSITIONS)
        .healthcareServiceTypeCodes(HEALTHCARE_SERVICE_TYPES)
        .typeOfCareCodes(TYPE_OF_CARE)
        .build();

//    public static final PrivatePractitionerConfig PRIVATE_PRACTITIONER_CONFIG = PrivatePractitionerConfig
//        .builder()
//        .positionCodes(POSITIONS)
//        .healthcareServiceTypeCodes(HEALTHCARE_SERVICE_TYPES)
//        .typeOfCareCodes(TYPE_OF_CARE)
//        .build();

    public static RegisterPrivatePractitionerRequestBuilder registerPrivatePractitionerRequest() {

        return RegisterPrivatePractitionerRequest.builder()
            .personId(TestDataConstants.DR_KRANSTEGE_PERSON_ID)
            .name(TestDataConstants.DR_KRANSTEGE_NAME)
            .position(TestDataConstants.DR_KRANSTEGE_POSITION)
            .careUnitName(TestDataConstants.DR_KRANSTEGE_CARE_UNIT_NAME)
            .ownershipType(TestDataConstants.DR_KRANSTEGE_OWNERSHIP_TYPE)
            .typeOfCare(TestDataConstants.DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(TestDataConstants.DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(TestDataConstants.DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(TestDataConstants.DR_KRANSTEGE_PHONE_NUMBER)
            .email(TestDataConstants.DR_KRANSTEGE_EMAIL)
            .address(TestDataConstants.DR_KRANSTEGE_ADDRESS)
            .zipCode(TestDataConstants.DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(TestDataConstants.DR_KRANSTEGE_MUNICIPALITY)
            .county(TestDataConstants.DR_KRANSTEGE_COUNTY);
    }


}
