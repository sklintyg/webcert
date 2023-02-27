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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.complement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class ComplementIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    protected abstract List<String> typeVersionList();

    protected Stream<String> typeVersionStream() {
        return typeVersionList().stream();
    }

    protected abstract Boolean shouldReturnLastestVersion();

    @Test
    @DisplayName("Shall be able to complement current version")
    void shallBeAbleToComplementCurrentVersion() {
        final var testSetup = getCertificateTestSetupWithComplementQuestions(moduleId(), typeVersion());

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        assertEquals(testSetup.certificateId(),
            newCertificate.getCertificate().getMetadata().getRelations().getParent().getCertificateId(),
            () -> String.format("Failed for certificate '%s'", testSetup.certificateId()));
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall return draft when complementing")
    void shallReturnCertificateWhenComplement(String typeVersion) {
        final var testSetup = getCertificateTestSetupWithComplementQuestions(moduleId(), typeVersion);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        if (shouldReturnLastestVersion()) {
            assertEquals(typeVersion(), newCertificate.getCertificate().getMetadata().getTypeVersion());
        } else {
            assertEquals(typeVersion, newCertificate.getCertificate().getMetadata().getTypeVersion());
        }
    }

    @Test
    @DisplayName("Shall pdl log crete activity when complementing a certificate")
    void shallPdlLogCreateActivityWhenComplementCertificate() {
        final var testSetup = getCertificateTestSetupForPdlWithComplementQuestion(moduleId(), typeVersion());

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        assertNumberOfPdlMessages(2);
        assertPdlLogMessage(ActivityType.CREATE, newCertificate.getCertificate().getMetadata().getId());
        assertPdlLogMessage(ActivityType.READ, newCertificate.getCertificate().getMetadata().getId());

    }
}
