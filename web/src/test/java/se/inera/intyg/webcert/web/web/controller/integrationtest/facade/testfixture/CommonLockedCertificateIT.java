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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class CommonLockedCertificateIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    protected abstract List<String> typeVersionList();

    protected abstract Boolean shouldReturnLatestVersion();

    protected Stream<String> typeVersionStream() {
        return typeVersionList().stream();
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall return certificate of correct version when copy")
    void shallReturnCertificateOfCurrentVersionWhenCopy(String typeVersion) {
        final var testSetup = getLockedCertificateTestSetup(moduleId(), typeVersion);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var newCertificateRequestDTO = new NewCertificateRequestDTO();
        newCertificateRequestDTO.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
        newCertificateRequestDTO.setCertificateType(testSetup.certificate().getMetadata().getType());

        final var certificateId = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(newCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/copy")
            .then().extract().path("certificateId").toString();

        certificateIdsToCleanAfterTest.add(certificateId);

        final var response = testSetup
            .spec()
            .pathParam("certificateId", certificateId)
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

        if (shouldReturnLatestVersion()) {
            assertEquals(typeVersion(), response.getMetadata().getTypeVersion());

        } else {
            assertEquals(typeVersion, response.getMetadata().getTypeVersion());
        }
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall be able to revoke locked certificate of version")
    public void shallBeAbleToRevokeLockedCertificateOfVersion(String typeVersion) {
        final var testSetup = getLockedCertificateTestSetup(moduleId(), typeVersion);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
        revokeCertificateRequest.setReason("Reason");
        revokeCertificateRequest.setMessage("Message");

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(revokeCertificateRequest)
            .when().post("api/certificate/{certificateId}/revoke")
            .then().extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @Nested
    class PdlTestRelatedToLockedDraft {

        @Test
        @DisplayName("Shall pdl log read activity when fetching locked drafts")
        public void shallPdlLogReadActivityWhenFetchingLockedDraft() {
            final var testSetup = getLockedCertificateTestSetupForPdlWithSjf(moduleId(), typeVersion());

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ, testSetup.certificateId());
        }

        @Test
        @DisplayName("Shall pdl log create activity when copying locked drafts")
        public void shallPdlLogCreateActivityWhenCopyLockedDraft() {
            final var testSetup = getLockedCertificateTestSetupForPdl(moduleId(), typeVersion());

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var newCertificateRequestDTO = new NewCertificateRequestDTO();
            newCertificateRequestDTO.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
            newCertificateRequestDTO.setCertificateType(testSetup.certificate().getMetadata().getType());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/copy")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE, certificateId);
        }

        @Test
        @DisplayName("Shall pdl log revoke activity when revoking locked drafts")
        public void shallPdlLogRevokeActivityWhenRevokeLockedDraft() {
            final var testSetup = getLockedCertificateTestSetupForPdl(moduleId(), typeVersion());

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
            revokeCertificateRequest.setReason("Reason");
            revokeCertificateRequest.setMessage("Message");

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.REVOKE, testSetup.certificateId());
        }
    }
}
