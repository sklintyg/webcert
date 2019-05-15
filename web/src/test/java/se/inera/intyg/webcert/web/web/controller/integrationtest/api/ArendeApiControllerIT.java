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
package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.sessionId;

public class ArendeApiControllerIT extends BaseRestIntegrationTest {

    private static final String INTYGSTYP = "lisjp";

    @Test
    public void testKompletteringarForIntyg() throws JsonProcessingException {
        sessionId = getAuthSession(DEFAULT_LAKARE);

        List<String> intygsIds = new ArrayList<String>() {{
            add(createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER));
            add(createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER));
            add(createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER));
        }};

        intygsIds.forEach(id -> createArendeQuestion(INTYGSTYP, id, DEFAULT_PATIENT_PERSONNUMMER, ArendeAmne.KOMPLT));

        GetCertificateAdditionsType request = new GetCertificateAdditionsType();
        intygsIds.forEach(id -> {
            IntygId iid = new IntygId();
            iid.setRoot("some-root-value");
            iid.setExtension(id);
            request.getIntygsId().add(iid);
        });

        spec()
                .body(objectMapper.writeValueAsString(request))
                .expect()
                    .statusCode(200)
                .when()
                    .post("api/arende/kompletteringar")
                .then()
                    .body("additions", Matchers.notNullValue())
                    .body("additions", Matchers.hasSize(request.getIntygsId().size()));

        // {"additions":[],"result":null,"any":[]}
    }

}
