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
package se.inera.intyg.webcert.web.integration.integrationtest;

import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

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
            .load("interactions/ListCertificatesForCareWithQAInteraction/ListCertificatesForCareWithQAResponder_3.3.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
            ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCareWithQAResponder:3"),
            "soap:Envelope/soap:Body/lc:ListCertificatesForCareWithQAResponse");
    }

    @Test
    public void testResponseRespectsSchema() {

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
    public void messageNotFollowingXSDFails() {
        String personnummer = "<root>123456</root>";
        String enhetsid = "enhetsid";
        requestTemplate.add("data", new ListCertificatesForCareWithQARequestParameters(personnummer, enhetsid));

        given().body(requestTemplate.render()).when()
            .post(LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL)
            .then().statusCode(500);
    }

    @Test
    public void testMessageWithInvalidXMLFails() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
            .when()
            .post(LIST_CERTIFICATES_FOR_CARE_WITH_QA_URL)
            .then()
            .statusCode(500);
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
