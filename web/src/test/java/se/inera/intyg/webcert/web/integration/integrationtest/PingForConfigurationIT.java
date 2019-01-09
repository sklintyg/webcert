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
package se.inera.intyg.webcert.web.integration.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;

/**
 * Created by marced on 2016-06-02.
 */
public class PingForConfigurationIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.PingForConfigurationResponse.";
    private static final String PING_FOR_CONFIGURATION_V1_0 = "monitoring/ping-for-configuration/v1.0";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/pingForConfiguration.v1.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver.load("interactions/PingForConfigurationInteraction/PingForConfigurationResponder_1.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1"),
                "soap:Envelope/soap:Body/lc:PingForConfigurationResponse");
    }

    @Test
    public void testResponseRespectsSchema() throws Exception {

        given().filter(responseBodyExtractorFilter).body(requestTemplate.render())
                .when()
                .post(PING_FOR_CONFIGURATION_V1_0)
                .then()
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));
    }

    @Test
    public void testPingForConfigurationBasic() throws Exception {

        given().body(requestTemplate.render()).when()
                .post(PING_FOR_CONFIGURATION_V1_0)
                .then().statusCode(200)
                .rootPath(BASE)
                .body("version", not(isEmptyString()))
                .body("pingDateTime", not(isEmptyString()))
                .body("configuration.size()", is(greaterThan(0)));

    }

}
