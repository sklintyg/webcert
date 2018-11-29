/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;

import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by marced on 2016-06-01.
 */
public class GetPrivatePractitionerTermsIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.GetPrivatePractitionerTermsResponse.";
    private static final String GET_PRIVATE_PRACTITIONER_TERMS_V1_0 = "services/get-private-practitioner-terms/v1.0";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/getPrivatePractitionerTerms.v1.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver
                .load("interactions/GetPrivatePractitionerTermsInteraction/GetPrivatePractitionerTermsResponder_1.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:infrastructure:directory:privatepractitioner:GetPrivatePractitionerTermsResponder:1"),
                "soap:Envelope/soap:Body/lc:GetPrivatePractitionerTermsResponse");
    }

    @Test
    @Ignore
    public void testGetTerms() throws Exception {

        given().body(requestTemplate.render())
                .when()
                .post(GET_PRIVATE_PRACTITIONER_TERMS_V1_0)
                .then().statusCode(200)
                .rootPath(BASE)
                .body("resultCode", is(ResultCodeEnum.OK.value()))
                .body("avtal.avtalVersion", not(isEmptyString()))
                .body("avtal.avtalText", not(isEmptyString()));

    }

    @Test
    @Ignore
    public void testResponseRespectsSchema() throws Exception {

        given().filter(responseBodyExtractorFilter).body(requestTemplate.render())
                .when()
                .post(GET_PRIVATE_PRACTITIONER_TERMS_V1_0)
                .then()
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));
    }

    /**
     * Check that even when sending invalid request, Soap faults should get transformed to a valid error response
     */
    @Test
    @Ignore
    public void testMessageWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(GET_PRIVATE_PRACTITIONER_TERMS_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("resultCode", is(ResultCodeEnum.ERROR.value()))
                .body("resultText", is("APPLICATION_ERROR"));
    }

    @Test
    @Ignore
    public void testErronousRequestResponseRespectsSchema() throws Exception {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().filter(responseBodyExtractorFilter).body(brokenTemplate.render())
                .when()
                .post(GET_PRIVATE_PRACTITIONER_TERMS_V1_0)
                .then()
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));
    }
}
