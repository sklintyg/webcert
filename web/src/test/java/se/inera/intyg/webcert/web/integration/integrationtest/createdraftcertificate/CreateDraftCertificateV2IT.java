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
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;

import se.inera.intyg.webcert.web.integration.integrationtest.BaseWSIntegrationTest;
import se.inera.intyg.webcert.web.integration.integrationtest.BodyExtractorFilter;
import se.inera.intyg.webcert.web.integration.integrationtest.ClasspathResourceResolver;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

/**
 * Created by eriklupander, marced on 2016-05-10.
 */
public class CreateDraftCertificateV2IT extends BaseWSIntegrationTest {

    private static final String LUAE_FS = "luae_fs";

    private static final String LUAE_NA = "luae_na";

    private static final String LISU = "lisu";

    private static final String LUSE = "luse";

    private static final String BASE = "Envelope.Body.CreateDraftCertificateResponse.";
    private static final String CREATE_DRAFT_CERTIFICATE_V2_0 = "services/create-draft-certificate/v2.0";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/createDraftCertificate.v2.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathResourceResolver.load(null,
                "interactions/CreateDraftCertificateInteraction/CreateDraftCertificateResponder_2.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:2"),
                "soap:Envelope/soap:Body/lc:CreateDraftCertificateResponse");
    }

    private String createRequestBody(String intygstyp) {
        requestTemplate.add("data", new IntygsData(intygstyp));
        return requestTemplate.render();
    }

    @Test
    public void testCreateLuaefsDraft() throws IOException {

        given().body(createRequestBody(LUAE_FS))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("OK"))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUAE_FS);

    }

    @Test
    public void testCreateLuaenaDraft() throws IOException {

        given().body(createRequestBody(LUAE_NA))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("OK"))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUAE_NA);
    }

    @Test
    public void testCreateLuseDraft() throws IOException {

        given().body(createRequestBody(LUSE))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("OK"))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LUSE);
    }

    @Test
    public void testCreateLisuDraft() throws IOException {

        given().body(createRequestBody(LISU))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("OK"))
                .body("intygs-id.extension.size()", is(1));

        testMatchesSchemaForType(LISU);
    }

    private void testMatchesSchemaForType(String type) throws IOException {
        given().filter(
                responseBodyExtractorFilter)
                .body(createRequestBody(type))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .body(matchesXsd(IOUtils.toString(xsdInputstream)).with(new ClasspathResourceResolver()));

    }

    @Test
    public void testCreateDraftForUnknownTypeFailsWithValidationError() {

        given().body(createRequestBody("NON_EXISTING_TYPE"))
                .when()
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("ERROR"))
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
                .post(RestAssured.baseURI + CREATE_DRAFT_CERTIFICATE_V2_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is("ERROR"))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    // String Template Data object
    private static final class IntygsData {
        public final String intygstyp;

        public IntygsData(String intygstyp) {
            this.intygstyp = intygstyp;
        }
    }
}
