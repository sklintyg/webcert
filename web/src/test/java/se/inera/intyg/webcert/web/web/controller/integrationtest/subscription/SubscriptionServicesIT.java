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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class SubscriptionServicesIT extends BaseRestIntegrationTest {

    private static final int OK = 200;
    private static final int FORBIDDEN = 403;

    private static final String USER_API_URI = "api/anvandare";
    private static final String SUBSCRIPTION_API_URI = "/api/subscription";
    private static final String TESTABILITY_CONFIG_URI = "/testability/config";
    private static final String KUNDPORTALEN_STUB_SETTINGS_URI = "/services/stubs/kundportalenstub/settings";

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @After
    public void resetStubAndFeatures() {
        given().when().get(TESTABILITY_CONFIG_URI + "/resetfeatures").then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/set/true");
    }

    @Test
    public void shouldSetSubscriptionActionNoneAndEmptyMissingSubscriptionListIfNotSubscriptionAdaptationOrRequired()
        throws JsonProcessingException {
        final var testFeatures = createSubscriptionFeatures(false, false);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/set/false");
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        final var response = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().asString();

        final var subscriptionInfo = objectMapper.readValue(response, WebCertUser.class).getSubscriptionInfo();
        assertEquals(SubscriptionAction.NONE, subscriptionInfo.getSubscriptionAction());
        assertTrue(subscriptionInfo.getCareProvidersMissingSubscription().isEmpty());
    }

    @Test
    public void shouldSetWarnOnOrgMissingSubscriptionIfAdaptation() throws JsonProcessingException {
        final var testFeatures = createSubscriptionFeatures(true, false);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(STAFFAN_STAFETT);

        final var response = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().asString();

        final var subscriptionInfo = objectMapper.readValue(response, WebCertUser.class).getSubscriptionInfo();
        assertEquals(SubscriptionAction.WARN, subscriptionInfo.getSubscriptionAction());
        assertEquals(1, subscriptionInfo.getCareProvidersMissingSubscription().size());
        assertTrue(subscriptionInfo.getCareProvidersMissingSubscription().contains("ostergotland"));
        assertFalse(subscriptionInfo.getCareProvidersMissingSubscription().contains("vastmanland"));
    }

    @Test
    public void shouldRemoveCareProviderFromMissingSubscriptionListWhenWarningAcknowledged() throws JsonProcessingException {
        final var testFeatures = createSubscriptionFeatures(true, false);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        final var responseBefore = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().asString();

        final var webCertUserBefore = objectMapper.readValue(responseBefore, WebCertUser.class);
        final var selectedCareProviderBefore = ((Vardgivare) webCertUserBefore.getValdVardgivare());
        final var subscriptionInfoBefore = webCertUserBefore.getSubscriptionInfo();
        assertEquals(SubscriptionAction.WARN, subscriptionInfoBefore.getSubscriptionAction());
        assertTrue(subscriptionInfoBefore.getCareProvidersMissingSubscription().contains(selectedCareProviderBefore.getId()));

        given().expect().statusCode(OK).when().get(SUBSCRIPTION_API_URI + "/acknowledgeWarning");

        final var responseAfter = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().asString();

        final var webCertUserAfter = objectMapper.readValue(responseAfter, WebCertUser.class);
        final var selectedCareProviderAfter = ((Vardgivare) webCertUserAfter.getValdVardgivare());
        final var subscriptionInfoAfter = webCertUserAfter.getSubscriptionInfo();
        assertEquals(SubscriptionAction.WARN, subscriptionInfoAfter.getSubscriptionAction());
        assertFalse(subscriptionInfoAfter.getCareProvidersMissingSubscription().contains(selectedCareProviderAfter.getId()));
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
    public void shouldSetBlockOnOrgMissingSubscriptionIfRequired() throws JsonProcessingException {
        final var testFeatures = createSubscriptionFeatures(false, true);
        given().contentType(ContentType.JSON).body(testFeatures).when().post(TESTABILITY_CONFIG_URI + "/setfeatures")
            .then().statusCode(OK);
        given().expect().statusCode(OK).when().get(KUNDPORTALEN_STUB_SETTINGS_URI + "/setactive/2-orgnr-vastmanland/Webcert");

        RestAssured.sessionId = getAuthSession(STAFFAN_STAFETT);

        final var response = given()
            .when().get(USER_API_URI)
            .then().statusCode(OK)
            .extract().response().getBody().asString();

        final var subscriptionInfo = objectMapper.readValue(response, WebCertUser.class).getSubscriptionInfo();
        assertEquals(SubscriptionAction.BLOCK, subscriptionInfo.getSubscriptionAction());
        assertEquals(1, subscriptionInfo.getCareProvidersMissingSubscription().size());
        assertTrue(subscriptionInfo.getCareProvidersMissingSubscription().contains("ostergotland"));
        assertFalse(subscriptionInfo.getCareProvidersMissingSubscription().contains("vastmanland"));
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
        feature.setDesc("Test feature " + name + " set by SubscriptionServicesIT.");
        feature.setIntygstyper(Collections.emptyList());
        return feature;
    }
}
