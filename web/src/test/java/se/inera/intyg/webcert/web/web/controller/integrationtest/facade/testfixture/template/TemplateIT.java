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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class TemplateIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String expectedVersion();

    protected abstract List<String> typeVersionList();

    protected Stream<String> typeVersionStream() {
        return typeVersionList().stream();
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall return certificate of current version when creating certificate from template")
    void shallReturnCertificateOfCurrentVersionWhenCreatingFromTemplate(String typeVersion) {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion).setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var certificateId = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/template")
            .then().extract().path("certificateId").toString();

        certificateIdsToCleanAfterTest.add(certificateId);

        final var response = testSetup
            .spec()
            .pathParam("certificateId", certificateId)
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

        assertEquals(expectedVersion(), response.getMetadata().getTypeVersion());
    }
}
