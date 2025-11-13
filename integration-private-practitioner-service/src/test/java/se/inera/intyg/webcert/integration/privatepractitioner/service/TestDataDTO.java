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

package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_ADDRESS;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_CARE_UNIT_NAME;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_CITY;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_COUNTY;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_EMAIL;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_MUNICIPALITY;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_OWNERSHIP_TYPE;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_PHONE_NUMBER;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_POSITION;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_TYPE_OF_CARE;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_WORKPLACE_CODE;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_ZIP_CODE;

import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;

public class TestDataDTO {

    private TestDataDTO() {
        throw new IllegalStateException("Utility class");
    }


    public static RegisterPrivatePractitionerRequest kranstegeRegisterPractitionerRequest() {
        return RegisterPrivatePractitionerRequest.builder()
            .personId(DR_KRANSTEGE_PERSON_ID)
            .name(DR_KRANSTEGE_NAME)
            .position(DR_KRANSTEGE_POSITION)
            .careUnitName(DR_KRANSTEGE_CARE_UNIT_NAME)
            .ownershipType(DR_KRANSTEGE_OWNERSHIP_TYPE)
            .typeOfCare(DR_KRANSTEGE_TYPE_OF_CARE)
            .healthcareServiceType(DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE)
            .workplaceCode(DR_KRANSTEGE_WORKPLACE_CODE)
            .phoneNumber(DR_KRANSTEGE_PHONE_NUMBER)
            .email(DR_KRANSTEGE_EMAIL)
            .address(DR_KRANSTEGE_ADDRESS)
            .zipCode(DR_KRANSTEGE_ZIP_CODE)
            .city(DR_KRANSTEGE_CITY)
            .municipality(DR_KRANSTEGE_MUNICIPALITY)
            .county(DR_KRANSTEGE_COUNTY)
            .build();
    }
}
