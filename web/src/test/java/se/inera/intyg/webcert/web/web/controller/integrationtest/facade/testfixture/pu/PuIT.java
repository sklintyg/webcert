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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.pu;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BOSTADSLOSE_ANDERSSON;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public abstract class PuIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    @Test
    @DisplayName("Shall include patient address if it's existing in PU")
    void shallIncludePatientAddressIfExistsInPU() {
        final var testSetup = getDraftTestSetup(CreateCertificateFillType.EMPTY, moduleId(), typeVersion(), ATHENA_ANDERSSON);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = getCertificate(testSetup);

        assertAll(
            () -> assertTrue(response.getMetadata().getPatient().isAddressFromPU()),
            () -> assertNotNull(response.getMetadata().getPatient().getStreet(), "Expect draft to include street"),
            () -> assertNotNull(response.getMetadata().getPatient().getCity(), "Expect draft to include city"),
            () -> assertNotNull(response.getMetadata().getPatient().getZipCode(), "Expect draft to include zipCode")
        );
    }

    @Test
    @DisplayName("Shall not include patient address if it's missing in PU")
    void shallExcludePatientAddressIfMissingInPU() {
        final var testSetup = getDraftTestSetup(CreateCertificateFillType.EMPTY, moduleId(), typeVersion(), BOSTADSLOSE_ANDERSSON);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = getCertificate(testSetup);

        assertAll(
            () -> assertFalse(response.getMetadata().getPatient().isAddressFromPU()),
            () -> assertNull(response.getMetadata().getPatient().getStreet(), "Expect draft to exclude street"),
            () -> assertNull(response.getMetadata().getPatient().getCity(), "Expect draft to exclude city"),
            () -> assertNull(response.getMetadata().getPatient().getZipCode(), "Expect draft to exclude zipCode")
        );
    }

    @Test
    @DisplayName("Shall include patient address if it's missing in PU, but entered by user")
    void shallIncludePatientAddressIfMissingInPUAndEnteredByUser() {
        final var testSetup = getDraftTestSetup(CreateCertificateFillType.EMPTY, moduleId(), typeVersion(), BOSTADSLOSE_ANDERSSON);

        final var expectedZipCode = "99999";
        final var expectedStreet = "New Street address";
        final var expectedCity = "New City";

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var originalDraft = getCertificate(testSetup);
        originalDraft.getMetadata().setPatient(
            getPatientWithAddress(expectedZipCode, expectedStreet, expectedCity, originalDraft.getMetadata().getPatient())
        );

        testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(originalDraft)
            .expect().statusCode(200)
            .when()
            .put("api/certificate/{certificateId}")
            .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

        final var updatedDraft = getCertificate(testSetup);

        assertAll(
            () -> assertFalse(updatedDraft.getMetadata().getPatient().isAddressFromPU()),
            () -> assertEquals(expectedStreet, updatedDraft.getMetadata().getPatient().getStreet()),
            () -> assertEquals(expectedCity, updatedDraft.getMetadata().getPatient().getCity()),
            () -> assertEquals(expectedZipCode, updatedDraft.getMetadata().getPatient().getZipCode())
        );
    }
}

