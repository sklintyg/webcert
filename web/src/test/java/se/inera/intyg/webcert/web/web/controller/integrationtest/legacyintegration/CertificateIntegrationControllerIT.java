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
package se.inera.intyg.webcert.web.web.controller.integrationtest.legacyintegration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

import io.restassured.RestAssured;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Check that basic-certificate-links are redirected correctly.
 */
public class CertificateIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_INTYGSID = "abcd123-abcd123-abcd123";
    private static final String INTYGSTYP = "luse";


    @Test
    public void testRedirectSuccess() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
            expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
            when().get("webcert/web/user/basic-certificate/{intygsId}/questions?enhet=IFV1239877878-1042").
            then().
            header(HttpHeaders.LOCATION, endsWith("/fragasvar/fk7263/" + FK7263_BASE_INTYG_TYPE_VERSION + "/" + DEFAULT_INTYGSID));
    }

    @Test
    public void testRedirectFailsWithInvalidRole() {

        FakeElegCredentials fakeElegCredentials = new FakeElegCredentials();
        fakeElegCredentials.setPersonId("19121212-1212");
        fakeElegCredentials.setPrivatLakare(true);
        fakeElegCredentials.setOrigin(UserOriginType.NORMAL.name());

        RestAssured.sessionId = getAuthSession(fakeElegCredentials);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
            expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
            when().get("webcert/web/user/basic-certificate/{intygsId}/questions").
            then().header(HttpHeaders.LOCATION, endsWith("/error.jsp?reason=auth-exception"));
    }

    @Test
    public void testRedirectWithTypeSuccess() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        //Redirect handling now always needs to look up the version of the intyg, so we must have one waiting when testing
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .redirects().follow(false).and().pathParams("intygsId", intygId, "intygsTyp", INTYGSTYP).
            expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
            when().get("webcert/web/user/basic-certificate/{intygsTyp}/{intygsId}/questions?enhet=IFV1239877878-1042").
            then().
            header(HttpHeaders.LOCATION, endsWith("/fragasvar/" + INTYGSTYP + "/" + LUSE_BASE_INTYG_TYPE_VERSION + "/" + intygId));
    }

    @Test
    public void testRedirectWithTypeFailsWithInvalidRole() {

        FakeElegCredentials fakeElegCredentials = new FakeElegCredentials();
        fakeElegCredentials.setPersonId("19121212-1212");
        fakeElegCredentials.setPrivatLakare(true);
        fakeElegCredentials.setOrigin(UserOriginType.NORMAL.name());

        RestAssured.sessionId = getAuthSession(fakeElegCredentials);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .redirects().follow(false).and().pathParams("intygsId", DEFAULT_INTYGSID, "intygsTyp", INTYGSTYP).
            expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
            when().get("webcert/web/user/basic-certificate/{intygsTyp}/{intygsId}/questions").
            then().header(HttpHeaders.LOCATION, endsWith("/error.jsp?reason=auth-exception"));
    }
}
