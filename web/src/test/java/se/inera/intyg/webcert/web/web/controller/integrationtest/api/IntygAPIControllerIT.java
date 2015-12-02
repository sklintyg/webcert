package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by marced on 01/12/15.
 */
public class IntygAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetListDraftsAndIntygForPersonWithEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when().get("api/intyg/person/{personNummer}").
                then().
                body(equalTo("[]"));
    }


    @Test
    public void testCreateUtkastCopy() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(200).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").
                then().
                body("intygsUtkastId", not(isEmptyString())).
                body("intygsTyp", equalTo("fk7263"));
    }

    @Test
    public void testGetListDraftsAndIntygForPersonWithNonEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        ListIntygEntry[] intygArray =
                given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when().get("api/intyg/person/{personNummer}").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-list-response-schema.json")).extract().response().as(ListIntygEntry[].class);

        assertEquals(1, intygArray.length);

        assertEquals(utkastId, intygArray[0].getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, intygArray[0].getPatientId().getPersonnummer());
        assertEquals("fk7263", intygArray[0].getIntygType());
    }
}
