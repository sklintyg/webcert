/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserPreferenceStorageRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Created by marced on 17/11/15.
 */
public class UserApiControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_LAKARE_NAME = "Jan Nilsson";

    @Test
    public void testGetAnvandare() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .expect().statusCode(200).when().get("api/anvandare")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-response-schema.json"))
            .body("hsaId", equalTo(DEFAULT_LAKARE.getHsaId()))
            .body("valdVardenhet.id", equalTo(DEFAULT_LAKARE.getEnhetId()))
            .body("namn", equalTo(DEFAULT_LAKARE_NAME));
    }

    @Test
    public void testGetAnvandareNotLoggedIn() {

        RestAssured.sessionId = null;

        given().expect().statusCode(403).when().get("api/anvandare");
    }

    @Test
    public void testAndraValdEnhet() {

        // Log in as user having medarbetaruppdrag at several vardenheter.
        FakeCredentials user = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-104B",
            "IFV1239877878-1042").legitimeradeYrkesgrupper(asList("Läkare")).build();
        RestAssured.sessionId = getAuthSession(user);

        // An improvement of this would be to call hsaStub rest api to add testa data as we want it to
        // avoid "magic" ids and the dependency to bootstrapped data?
        final String vardEnhetToChangeTo = "IFV1239877878-1045";
        ChangeSelectedUnitRequest changeRequest = new ChangeSelectedUnitRequest();
        changeRequest.setId(vardEnhetToChangeTo);

        spec()
            .and().body(changeRequest)
            .when().post("api/anvandare/andraenhet")
            .then().statusCode(200).body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-response-schema.json"))
            .body("valdVardenhet.id", equalTo(vardEnhetToChangeTo));
    }

    /**
     * Verify that trying to change vardEnhet to an invalid one gives an error response.
     */
    @Test
    public void testAndraValdEnhetMedOgiltigEnhetsId() {

        // Log in as user having medarbetaruppdrag at several vardenheter.
        FakeCredentials user = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-104B",
            "IFV1239877878-1042").legitimeradeYrkesgrupper(asList("Läkare")).build();
        RestAssured.sessionId = getAuthSession(user);

        // An improvement of this would be to call hsaStub rest api to add testa data as we want it to
        // avoid "magic" ids and the dependency to bootstrapped data?
        final String vardEnhetToChangeTo = "non-existing-vardenehet-id";
        ChangeSelectedUnitRequest changeRequest = new ChangeSelectedUnitRequest();
        changeRequest.setId(vardEnhetToChangeTo);

        spec()
            .and().body(changeRequest)
            .expect().statusCode(400)
            .when().post("api/anvandare/andraenhet");
    }

    @Test
    public void testGodkannAvtal() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .when().put("api/anvandare/godkannavtal")
            .then().statusCode(200);
    }

    @Test
    public void testAtertaAvtalGodkannande() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .when().delete("api/anvandare/privatlakaravtal")
            .then().statusCode(200);
    }

    @Test
    public void testHamtaSenasteAvtal() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .when().get("api/anvandare/latestavtal")
            .then().statusCode(200).body(matchesJsonSchemaInClasspath("jsonschema/webcert-avtal-response-schema.json"));
    }

    @Test
    public void testPing() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .expect().statusCode(200)
            .when().get("api/anvandare/ping");
    }

    @Test
    public void testStoreAnvandarPreference() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        WebUserPreferenceStorageRequest storeRequest = new WebUserPreferenceStorageRequest();
        storeRequest.setKey("key1");
        storeRequest.setValue("value1");
        spec()
            .when().delete("api/anvandare/preferences/key1").then().statusCode(200);

        spec()
            .and().body(storeRequest)
            .when().put("api/anvandare/preferences")
            .then().statusCode(200);

        spec()
            .expect().statusCode(200).when().get("api/anvandare")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-response-schema.json"))
            .body("hsaId", equalTo(DEFAULT_LAKARE.getHsaId()))
            .body("anvandarPreference.key1", equalTo("value1"));

        spec()
            .when().delete("api/anvandare/preferences/key1").then().statusCode(200);
    }

    @Test
    public void testDeleteAnvandarPreference() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        WebUserPreferenceStorageRequest storeRequest = new WebUserPreferenceStorageRequest();
        storeRequest.setKey("key1");
        storeRequest.setValue("value1");

        spec()
            .and()
            .body(storeRequest)
            .when().put("api/anvandare/preferences").then().statusCode(200);

        spec()
            .when().delete("api/anvandare/preferences/key1").then().statusCode(200);

        spec()
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-response-schema.json"))
            .body("hsaId", equalTo(DEFAULT_LAKARE.getHsaId()))
            .body("anvandarPreference.key1", nullValue());
    }
}
