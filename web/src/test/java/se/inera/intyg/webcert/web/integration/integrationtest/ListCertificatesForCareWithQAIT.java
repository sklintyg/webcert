/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.google.common.collect.ImmutableMap;

import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

public class ListCertificatesForCareWithQAIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.ListCertificatesForCareWithQAResponse.";
    private static final String LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL = "services/list-certificates-for-care-with-qa/v3.0";
    BodyExtractorFilter responseBodyExtractorFilter;
    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/listCertificatesForCareWithQA.v3.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver
                .load("interactions/ListCertificatesForCareWithQAInteraction/ListCertificatesForCareWithQAResponder_3.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCareWithQAResponder:3"),
                "soap:Envelope/soap:Body/lc:ListCertificatesForCareWithQAResponse");
    }

    @Test
    public void testResponseRespectsSchema() throws Exception {

        String enhetsId = "123456";
        String personnummer = "191212121212";

        requestTemplate.add("data", new ListCertificatesForCareWithQARequestParameters(personnummer, enhetsId));

        given().filter(responseBodyExtractorFilter).body(requestTemplate.render())
                .when()
                .post(LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL)
                .then()
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));
    }

    @Test
    public void messageNotFollowingXSDFailsWithValidationError() throws Exception {
        String personnummer = "<root>123456</root>";
        String enhetsid = "enhetsid";
        requestTemplate.add("data", new ListCertificatesForCareWithQARequestParameters(personnummer, enhetsid));

        given().body(requestTemplate.render()).when()
                .post(LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL)
                .then().statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Check that even when sending invalid request, Soap faults should get transformed to a valid error response.
     */
    @Test
    public void testMessageWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    private static class ListCertificatesForCareWithQARequestParameters {
        public final String personnummer;
        public final String enhetsId;

        public ListCertificatesForCareWithQARequestParameters(String personnummer, String enhetsId) {
            this.personnummer = personnummer;
            this.enhetsId = enhetsId;
        }
    }
}
