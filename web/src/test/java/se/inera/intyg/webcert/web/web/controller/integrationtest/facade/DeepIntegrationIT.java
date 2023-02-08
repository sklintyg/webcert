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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BETA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_BEATA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_BEATA_BETA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.LAUNCH_ID;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class DeepIntegrationIT extends BaseFacadeIT {

    @Test
    @DisplayName("Shall be redirected to React web client if part of pilot")
    void shallBeRedirectedToReactWebclientIfPartOfPilot() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                DR_AJLA,
                ALFA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var queryParams = Map.of("enhet", ALFA_VARDCENTRAL);

        final var response = testSetup
            .spec()
            .redirects().follow(false)
            .pathParam("certificateId", testSetup.certificateId())
            .queryParams(queryParams)
            .when().get("/visa/intyg/{certificateId}")
            .then().extract().response();

        assertAll(
            () -> assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatusCode()),
            () -> assertTrue(response.getHeader(HttpHeaders.LOCATION).contains("/certificate/"),
                () -> "Expect '" + response.getHeader(HttpHeaders.LOCATION) + "' header to refer to route used in the React client")
        );
    }

    @Test
    @DisplayName("Shall be redirected to Angular web client if not part of pilot")
    void shallBeRedirectedToAngularWebclientIfNotPartOfPilot() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                DR_BEATA,
                BETA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_BEATA_BETA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var queryParams = Map.of("enhet", BETA_VARDCENTRAL);

        final var response = testSetup
            .spec()
            .redirects().follow(false)
            .pathParam("certificateId", testSetup.certificateId())
            .queryParams(queryParams)
            .when().get("/visa/intyg/{certificateId}")
            .then().extract().response();

        assertAll(
            () -> assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatusCode()),
            () -> assertTrue(response.getHeader(HttpHeaders.LOCATION).contains("/lisjp/1.2/edit/"),
                () -> "Expect '" + response.getHeader(HttpHeaders.LOCATION) + "' header to refer to route used in the Angular client")
        );
    }

    @Test
    @DisplayName("Shall return response with status code 403 when launchId is not matching")
    void shallReturnResponseWith403StatusCode() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                DR_BEATA,
                BETA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_BEATA_BETA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .useLaunchId(LAUNCH_ID)
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = testSetup
            .spec()
            .redirects().follow(false)
            .header("launchId", "WRONG-LAUNCH_ID")
            .when().get("/api/session-auth-check/ping")
            .then().extract().response();

        assertAll(
            () -> assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode())
        );
    }

    @Test
    @DisplayName("Shall return response with status code 200 when launchId is matching")
    void shallReturnResponseWith200StatusCode() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                DR_BEATA,
                BETA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_BEATA_BETA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .useLaunchId(LAUNCH_ID)
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = testSetup
            .spec()
            .redirects().follow(false)
            .header("launchId", LAUNCH_ID)
            .when().get("/api/session-auth-check/ping")
            .then().extract().response();

        assertAll(
            () -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode())
        );
    }
}
