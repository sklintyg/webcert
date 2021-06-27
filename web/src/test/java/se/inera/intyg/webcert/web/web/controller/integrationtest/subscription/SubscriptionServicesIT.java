/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integrationtest.subscription;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SubscriptionAction;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class SubscriptionServicesIT extends BaseRestIntegrationTest {

    private static final int OK = 200;
    private static final int FORBIDDEN = 403;

    private static final String USER_API_URI = "api/anvandare";
    private static final String SUBSCRIPTION_API_URI = "/api/subscription";
    private static final String TESTABILITY_CONFIG_URI = "/testability/config";
    private static final String KUNDPORTALEN_STUB_SETTINGS_URI = "/services/stubs/kundportalenstub/settings";

    @After
    public void resetStubAndFeatures() {
        given().when().get(TESTABILITY_CONFIG_URI + "/resetfeatures").then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/set/true");
    }

    @Test
    public void shouldSetNoneOnCareProviderIfNotSubscriptionAdaptationOrRequired() {
        final var testFeatures = createSubscriptionFeatures(false, false);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/set/false");
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        final var webcertUser = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().as(WebCertUser.class);

        final var careProviders = webcertUser.getVardgivare();
        assertEquals(SubscriptionAction.NONE, careProviders.get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, careProviders.get(0).getVardenheter().get(0).getSubscriptionAction());
    }

    @Test
    public void shouldSetWarnOnOrgMissingAndNoneOnOrgHavingSubscriptionIfAdaptation() {
        final var testFeatures = createSubscriptionFeatures(true, false);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(STAFFAN_STAFETT);

        final var webcertUser = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().as(WebCertUser.class);

        final var hasSubscription = webcertUser.getVardgivare().stream().filter(cp -> cp.getId().equals("vastmanland"))
            .findFirst().orElseThrow();
        final var noSubscription = webcertUser.getVardgivare().stream().filter(cp -> !cp.getId().equals("vastmanland"))
            .findFirst().orElseThrow();
        assertEquals(SubscriptionAction.WARN, noSubscription.getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, noSubscription.getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, noSubscription.getVardenheter().get(0).getMottagningar().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getVardenheter().get(0).getMottagningar().get(0).getSubscriptionAction());
    }

    @Test
    public void shouldReplaceWarnWithNoneWhenWarningAcknowledged() {
        final var testFeatures = createSubscriptionFeatures(true, false);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        final var webcertUserBefore = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().as(WebCertUser.class);

        assertEquals(SubscriptionAction.WARN, webcertUserBefore.getVardgivare().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.WARN, ((Vardgivare) webcertUserBefore.getValdVardgivare()).getSubscriptionAction());

        given().expect().statusCode(OK).when().get(SUBSCRIPTION_API_URI + "/acknowledgeWarning");

        final var webcertUserAfter = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().as(WebCertUser.class);

        assertEquals(SubscriptionAction.NONE, webcertUserAfter.getVardgivare().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, ((Vardgivare) webcertUserAfter.getValdVardgivare()).getSubscriptionAction());
    }

    @Test
    public void shouldFailWithForbiddenIfNoSubscriptionsWhenRequired() {
        final var testFeatures = createSubscriptionFeatures(false, true);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/set/false");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given()
            .when().get(USER_API_URI)
            .then().statusCode(FORBIDDEN);
    }

    @Test
    public void shouldSetBlockOnOrgMissingAndNoneOnOrgHavingSubscriptionIfRequired() {
        final var testFeatures = createSubscriptionFeatures(false, true);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(STAFFAN_STAFETT);

        final var webcertUser = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().as(WebCertUser.class);

        final var hasSubscription = webcertUser.getVardgivare().stream().filter(cp -> cp.getId().equals("vastmanland"))
            .findFirst().orElseThrow();
        final var noSubscription = webcertUser.getVardgivare().stream().filter(cp -> !cp.getId().equals("vastmanland"))
            .findFirst().orElseThrow();
        assertEquals(SubscriptionAction.BLOCK, noSubscription.getSubscriptionAction());
        assertEquals(SubscriptionAction.BLOCK, noSubscription.getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.BLOCK, noSubscription.getVardenheter().get(0).getMottagningar().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getVardenheter().get(0).getSubscriptionAction());
        assertEquals(SubscriptionAction.NONE, hasSubscription.getVardenheter().get(0).getMottagningar().get(0).getSubscriptionAction());
    }

    private List<Feature> createSubscriptionFeatures(boolean adjustment, boolean required) {
        final var features = new ArrayList<Feature>();
        features.add(createFeature("SUBSCRIPTION_ADAPTATION_PERIOD", adjustment));
        features.add(createFeature("SUBSCRIPTION_REQUIRED", required));
        return features;
    }

    private Feature createFeature(String name, boolean global) {
        final var feature = new Feature();
        feature.setName(name);
        feature.setGlobal(global);
        feature.setDesc("Test feature" + name + "set by SubscriptionServicesIT.");
        feature.setIntygstyper(Collections.emptyList());
        return feature;
    }
}
