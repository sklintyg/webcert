/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import com.jayway.restassured.RestAssured;

import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

/**
 * Created by eriklupander, marced on 2016-05-10.
 */
public class ReceiveMedicalCertificateQuestionIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.ReceiveMedicalCertificateQuestionResponse.";
    private static final String RECEIVE_QUESTION_V1_0 = "services/receive-question/v1.0";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/receiveMedicalCertificateQuestion.v1.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver
                .load("interactions/ReceiveMedicalCertificateQuestionInteraction/ReceiveMedicalCertificateQuestionResponder_1.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1"),
                "soap:Envelope/soap:Body/lc:ReceiveMedicalCertificateQuestionResponse");
    }

    private String createRequestBody(String amne) {
        requestTemplate.add("data", new QuestionData(amne));
        return requestTemplate.render();
    }

    @Test
    public void testReceiveQuestion() throws IOException {

        given().body(createRequestBody("Komplettering_av_lakarintyg"))
                .when()
                .post(RestAssured.baseURI + RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()));
    }

    @Test
    public void testResponseMatchesSchema() throws IOException {
        given().filter(
                responseBodyExtractorFilter)
                .body(createRequestBody("Komplettering_av_lakarintyg"))
                .when()
                .post(RestAssured.baseURI + RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));

    }

    @Test
    public void testCreateQuestionForUnknownAmneFailsWithValidationError() {

        given().body(createRequestBody("NON_EXISTING_AMNE"))
                .when()
                .post(RestAssured.baseURI + RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Check that even when sending invalid request, Soap faults should get transformed to a valid error response
     */
    @Test
    public void testCreateQuestionWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(RestAssured.baseURI + RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    // String Template Data object
    private static final class QuestionData {
        public final String amne;

        public QuestionData(String amne) {
            this.amne = amne;
        }
    }
}
