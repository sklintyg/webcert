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
import se.inera.intyg.webcert.integration.privatepractitioner.dto.Code;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.HospInformation;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerConfiguration;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.RegisterPrivatePractitionerRequest.RegisterPrivatePractitionerRequestBuilder;

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
    public static final List<Code> DR_KRANSTEGE_SPECIALITIES = List.of(
        new Code("32", "Klinisk fysiologi"),
        new Code("74", "Nukleärmedicin")
    );
    public static final List<Code> DR_KRANSTEGE_LICENSED_HEALTHCARE_PROFESSIONS =
        List.of(new Code("LK", "Läkare"));

    public static final RegisterPrivatePractitionerRequest DR_KRANSTEGE_REGISTREATION_REQUEST = registerPrivatePractitionerRequest().build();
    public static final PrivatePractitionerConfiguration PRIVATE_PRACTITIONER_CONFIG = PrivatePractitionerConfiguration
        .builder()
        .positionCodes(POSITIONS)
        .healthcareServiceTypeCodes(HEALTHCARE_SERVICE_TYPES)
        .typeOfCareCodes(TYPE_OF_CARE)
        .build();

    public static final HospInformation DR_KRANSTEGE_HOSP_INFO = HospInformation.builder()
        .personId(DR_KRANSTEGE_PERSON_ID)
        .personalPrescriptionCode(DR_KRANSTEGE_PRESCRIPTION_CODE)
        .licensedHealthcareProfessions(DR_KRANSTEGE_LICENSED_HEALTHCARE_PROFESSIONS)
        .specialities(DR_KRANSTEGE_SPECIALITIES)
        .build();

    public static RegisterPrivatePractitionerRequestBuilder registerPrivatePractitionerRequest() {

        return RegisterPrivatePractitionerRequest.builder()
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
