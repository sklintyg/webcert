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

 package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.candidate;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;

 import io.restassured.http.ContentType;
 import org.junit.jupiter.api.DisplayName;
 import org.junit.jupiter.api.Test;
 import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

 public abstract class CandidateIT extends CommonFacadeITSetup {

     protected abstract String moduleId();

     protected abstract String candidateModuleId();

     protected abstract String candidateTypeVersion();

     @Test
     @DisplayName("Create draft from candidate")
     public void shallCreateDraftFromCandidate() {

         final var testSetup = getCertificateTestSetupBuilder(candidateModuleId(), candidateTypeVersion())
             .useDjupIntegratedOrigin()
             .setup();

         final var draftId = createDraftAndReturnCertificateId(moduleId(), ATHENA_ANDERSSON);

         final var response = testSetup
             .spec()
             .pathParam("certificateId", draftId)
             .contentType(ContentType.JSON)
             .expect().statusCode(200)
             .when()
             .post("api/certificate/{certificateId}/candidate")
             .then().extract().response();

         certificateIdsToCleanAfterTest.add(testSetup.certificateId());
         certificateIdsToCleanAfterTest.add(draftId);

         assertEquals(200, response.getStatusCode());
     }
 }

