package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.sessionId;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeDraftEntry;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class ArendeDraftApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testArendeForIntyg() {
        sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygId = "intygId";
        String text = "text";
        String amne = "amne";
        createArendeDraft(intygId, text, amne);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParameter("intygId", intygId)
                .expect()
                    .statusCode(200)
                .when()
                    .get("api/arende/draft/{intygId}")
                .then()
                    .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-draft.json"))
                    .body("text", equalTo(text))
                    .body("intygId", equalTo(intygId))
                    .body("amne", equalTo(amne));

        deleteDraft(intygId);
    }

    private void deleteDraft(String intygId) {

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParameters("intygId", intygId)
                .expect()
                    .statusCode(200)
                .when()
                    .delete("api/arende/draft/{intygId}");
    }

    private void createArendeDraft(String intygId, String text, String amne) {
        ArendeDraftEntry entry = new ArendeDraftEntry();
        entry.setIntygId(intygId);
        entry.setText(text);
        entry.setAmne(amne);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).and().body(entry)
                .expect()
                    .statusCode(200)
                .when()
                    .put("api/arende/draft");
    }
}
