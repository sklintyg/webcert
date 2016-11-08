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

package se.inera.intyg.webcert.web.integration.integrationtest.createdraftcertificate;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;

import se.inera.intyg.webcert.web.integration.integrationtest.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

/**
 * Created by eriklupander, marced on 2016-05-10.
 */
public class CreateDraftCertificateV2IT extends BaseWSIntegrationTest {

    private static final String LUAE_FS = "LUAE_FS";
    private static final String LUAE_NA = "LUAE_NA";
    private static final String LISJP = "LISJP";
    private static final String LUSE = "LUSE";
    private static final String TS_BAS = "TSTRK1007";
    private static final String TS_DIABETES = "TSTRK1031";

    private static final String BASE = "Envelope.Body.CreateDraftCertificateResponse.";
    private static final String CREATE_DRAFT_CERTIFICATE_V2_0 = "services/create-draft-certificate/v2.0";

    private static final String DEFAULT_LAKARE_HSAID = "IFV1239877878-1049";
    private static final String OTHER_LAKARE_HSAID = "SE4815162344-1B01";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/createDraftCertificate.v2.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver
                .load("interactions/CreateDraftCertificateInteraction/CreateDraftCertificateResponder_2.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:2"),
                "soap:Envelope/soap:Body/lc:CreateDraftCertificateResponse");
    }

    private String createRequestBody(String intygstyp, String lakareHsaId) {
        requestTemplate.add("data", new IntygsData(intygstyp, lakareHsaId));
        return requestTemplate.render();
    }

    @Test
    public void testCreateLuaefsDraft() throws IOException {

        given().body(createRequestBody(LUAE_FS, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUAE_FS);

    }

    @Test
    public void testCreateLuaenaDraft() throws IOException {

        given().body(createRequestBody(LUAE_NA, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUAE_NA);
    }

    @Test
    public void testCreateLuseDraft() throws IOException {

        given().body(createRequestBody(LUSE, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUSE);
    }

    @Test
    public void testCreateTsBasDraft() throws IOException {

        given().body(createRequestBody(TS_BAS, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(TS_BAS);
    }

    @Test
    public void testCreateTsDiabetesDraft() throws IOException {

        given().body(createRequestBody(TS_DIABETES, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(TS_DIABETES);
    }

    @Test
    public void testCreateLisjpDraft() throws IOException {

        given().body(createRequestBody(LISJP, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LISJP);
    }

    private void testMatchesSchemaForType(String type) throws IOException {
        given().filter(
                responseBodyExtractorFilter)
                .body(createRequestBody(type, DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .body(matchesXsd(IOUtils.toString(xsdInputstream)).with(new ClasspathSchemaResourceResolver()));

    }

    @Test
    public void testCreateDraftForUnknownTypeFailsWithValidationError() {

        given().body(createRequestBody("NON_EXISTING_TYPE", DEFAULT_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Check that even when sending invalid request, Soap faults gets transformed to a valid error response
     */
    @Test
    public void testCreateDraftWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    /**
     * Check that VALIDATION_ERROR is found when utfärdare does not have medarbetaruppdrag on the specified Vårdenhet.
     */
    @Test
    public void testCreateDraftFailsWithValidationErrorWhenIssuerHasNoMiUOnUnit() {

        given().body(createRequestBody(LISJP, OTHER_LAKARE_HSAID))
                .when()
                .post(CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    // String Template Data object
    private static final class IntygsData {
        public final String intygstyp;
        public final String lakareHsaId;

        public IntygsData(String intygstyp, String lakareHsaId) {
            this.intygstyp = intygstyp;
            this.lakareHsaId = lakareHsaId;
        }
    }
}
