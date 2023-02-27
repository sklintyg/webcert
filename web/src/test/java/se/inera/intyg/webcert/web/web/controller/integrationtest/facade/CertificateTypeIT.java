/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.BaseFacadeIT;

/**
 * This needs webcert to be up and running.
 */
public class CertificateTypeIT extends BaseFacadeIT {

    private static final String PATIENT_ID = "19121212-1212";

    @Test
    @DisplayName("Shall return a list of CertificateTypeInfoDTO's")
    void shallGetCertificateTypes() {
        final var testSetup = TestSetup.create()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

        final var certificateTypeInfoDTOS = testSetup
            .spec()
            .pathParam("patientId", PATIENT_ID)
            .expect().statusCode(200)
            .when()
            .get("api/certificate/type/{patientId}")
            .then().extract().response().as(List.class, getObjectMapperForDeserialization());

        assertFalse(certificateTypeInfoDTOS.isEmpty());
    }
}
