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
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

/**
 * Created by eriklupander, marced on 2016-05-10.
 */
public class ReceiveMedicalCertificateAnswerIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.ReceiveMedicalCertificateAnswerResponse.";
    private static final String RECEIVE_QUESTION_V1_0 = "services/receive-answer/v1.0";
    private static final String INTYGSID = "1234-456";
    private static final String PATIENT_PERSONNR = "19520614-2597";
    private static final String SVAR_MEDDELANDE_TEXT = "Här är ett svar från FK";
    private static final String HOS_PERSONAL_ID = "SE4815162344-1B01";
    private static final String SIGNERINGS_TIDPUNKT = "2014-12-09T11:00:00.000";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/receiveMedicalCertificateAnswer.v1.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver
                .load("interactions/ReceiveMedicalCertificateAnswerInteraction/ReceiveMedicalCertificateAnswerResponder_1.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1"),
                "soap:Envelope/soap:Body/lc:ReceiveMedicalCertificateAnswerResponse");
    }

    @Test
    public void testReceiveAnswer() throws IOException {
        final int internalReferens = createQuestion(Fk7263EntryPoint.MODULE_ID, INTYGSID, PATIENT_PERSONNR);
        given().body(createRequestBody(internalReferens, HOS_PERSONAL_ID, SVAR_MEDDELANDE_TEXT, SIGNERINGS_TIDPUNKT))
                .when()
                .post(RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.OK.value()));
    }

    @Test
    public void testResponseMatchesSchema() throws IOException {
        final int internalReferens = createQuestion(Fk7263EntryPoint.MODULE_ID, INTYGSID, PATIENT_PERSONNR);
        given().filter(
                responseBodyExtractorFilter)
                .body(createRequestBody(internalReferens, HOS_PERSONAL_ID, "Här är ett svar från FK", SIGNERINGS_TIDPUNKT))
                .when()
                .post(RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));

    }

    /**
     * Send a request intentionally breaking the request schema (in this case, omitting the mandatory
     * lakarutlatade.signeringsTidpunkt). Since we have SchemaValidation enabled, this should be detected and given an
     * appropriately wrapped validation error response)
     */
    @Test
    public void testRequestSchemaValidationError() {
        given().body(createRequestBody(1, HOS_PERSONAL_ID, "Här är ett svar från FK", ""))
                .when()
                .post(RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Send an request that will fail programmatic validation. It should respond with correct error code.
     */
    @Test
    public void testEmptyHosPersonalIdFailsWithValidationError() {
        final int internalReferens = createQuestion(Fk7263EntryPoint.MODULE_ID, INTYGSID, PATIENT_PERSONNR);
        given().body(createRequestBody(internalReferens, "", "", SIGNERINGS_TIDPUNKT))
                .when()
                .post(RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Trigger a soapfault by sending a totally nonsense request - Soap faults should get transformed to a valid
     * (application) error response
     */
    @Test
    public void testCreateAnswerWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(RECEIVE_QUESTION_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    private String createRequestBody(int internalReferens, String hosPersonalId, String meddelandeText, String signeringsTidpunkt) {
        requestTemplate.add("data", new AnswerData(internalReferens, hosPersonalId, meddelandeText, signeringsTidpunkt));
        return requestTemplate.render();
    }

    private int createQuestion(String typ, String intygId, String personnummer) {
        LocalDateTime now = LocalDateTime.now();
        FragaSvar fs = new FragaSvar();
        fs.setAmne(Amne.ARBETSTIDSFORLAGGNING);
        fs.setFrageText("Frågetext");
        fs.setIntygsReferens(new IntygsReferens(intygId, typ, new Personnummer(personnummer), "Api Restman", now));
        fs.setStatus(Status.PENDING_INTERNAL_ACTION);
        fs.setFrageSkickadDatum(now);
        fs.setMeddelandeRubrik("Meddelanderubrik");
        fs.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fs.setFrageSigneringsDatum(now);
        fs.setVardAktorNamn("Vardaktor");
        fs.setVardAktorHsaId("Test-hsa-id");
        fs.setExternReferens("FK-REF-1");

        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId("IFV1239877878-1042");
        vardperson.setArbetsplatsKod("0000000");
        vardperson.setEnhetsnamn("blub");
        vardperson.setHsaId("IFV1239877878-1049");
        vardperson.setVardgivarId("TESTVG");
        vardperson.setVardgivarnamn("VG TEST SYD");
        vardperson.setNamn("Hr Doktor");
        fs.setVardperson(vardperson);

        Response response = given().log().all().contentType(ContentType.JSON)
                .body(fs).expect().statusCode(200).when()
                .post("testability/questions").then().extract().response();

        JsonPath model = new JsonPath(response.body().asString());
        return model.get("internReferens");
    }

    // String Template Data object
    private static final class AnswerData {
        public final int internalReferens;
        public final String hosPersonalId;
        public final String meddelandeText;
        public final String signeringsTidpunkt;

        public AnswerData(int internalReferens, String hosPersonalId, String meddelandeText, String signeringsTidpunkt) {
            this.internalReferens = internalReferens;
            this.hosPersonalId = hosPersonalId;
            this.meddelandeText = meddelandeText;
            this.signeringsTidpunkt = signeringsTidpunkt;

        }
    }
}
