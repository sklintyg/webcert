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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class TextVersionIT extends BaseFacadeIT {

    private static final String PATIENT_ATHENA = ATHENA_ANDERSSON.getPersonId().getId();

    private String latestLisjpVersion;
    private String previousLisjpVersion;
    private String latestTsDiabetesVersion;
    private String previousTsDiabetesVersion;

    @BeforeEach
    void setup() {
        latestLisjpVersion = getTextVersionForType(LisjpEntryPoint.MODULE_ID, "1", 0);
        previousLisjpVersion = getTextVersionForType(LisjpEntryPoint.MODULE_ID, "1", 1);
        latestTsDiabetesVersion = getTextVersionForType(TsDiabetesEntryPoint.MODULE_ID, "4", 0);
        previousTsDiabetesVersion = getTextVersionForType(TsDiabetesEntryPoint.MODULE_ID, "4", 1);
    }

    @Test
    public void shouldOpenSavedDraftWithLatestTextVersionForLisjp() {
        final var testSetup = initDraftTestSetup(LisjpEntryPoint.MODULE_ID, previousLisjpVersion);
        final var certificateResponse = getCertificate(testSetup);

        assertEquals(latestLisjpVersion, certificateResponse.getMetadata().getTypeVersion());
    }

    @Test
    public void shouldOpenSignedCertificateWithOriginalTextVersionForLisjp() {
        final var testSetup = initCertificateTestSetup(LisjpEntryPoint.MODULE_ID, previousLisjpVersion);
        final var certificateResponse = getCertificate(testSetup);

        assertEquals(previousLisjpVersion, certificateResponse.getMetadata().getTypeVersion());
    }

    @Test
    public void shouldOpenSavedDraftWithLatestTextVersionForTsDiabetes() {
        final var testSetup = initDraftTestSetup(TsDiabetesEntryPoint.MODULE_ID, previousTsDiabetesVersion);
        final var certificateResponse = getCertificate(testSetup);

        assertEquals(latestTsDiabetesVersion, certificateResponse.getMetadata().getTypeVersion());
    }

    @Test
    public void shouldOpenSignedCertificateWithOriginalTextVersionForTsDiabetes() {
        final var testSetup = initCertificateTestSetup(TsDiabetesEntryPoint.MODULE_ID, previousTsDiabetesVersion);
        final var certificateResponse = getCertificate(testSetup);

        assertEquals(previousTsDiabetesVersion, certificateResponse.getMetadata().getTypeVersion());
    }

    private TestSetup initDraftTestSetup(String certificateType, String previousVersion) {
        final var testSetup = TestSetup.create()
            .draft(certificateType, previousVersion, CreateCertificateFillType.MINIMAL, DR_AJLA, ALFA_VARDCENTRAL, PATIENT_ATHENA)
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());
        return testSetup;
    }

    private TestSetup initCertificateTestSetup(String certificateType, String previousVersion) {
        final var testSetup = TestSetup.create()
            .certificate(certificateType, previousVersion, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());
        return testSetup;
    }

    private CertificateDTO getCertificate(TestSetup testSetup) {
        return testSetup.spec()
            .pathParam("certificateId", testSetup.certificateId())
            .when().get("api/certificate/{certificateId}")
            .then().statusCode(200)
            .extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
    }

    private String getTextVersionForType(String certificateType, String majorVersion, int skip) {
        final var supportedCertificateTypes = given()
            .when().get("/testability/certificate/types")
            .then().statusCode(HttpStatus.OK.value())
            .extract().body().jsonPath().getList("certificateTypes", CertificateType.class);

        return supportedCertificateTypes.stream()
            .filter(t -> t.getInternalType().equals(certificateType))
            .limit(1)
            .flatMap(f -> f.getVersions().stream())
            .filter(v -> v.split("\\.")[0].equals(majorVersion))
            .map(Double::parseDouble)
            .sorted(Comparator.reverseOrder())
            .skip(skip)
            .max(Comparator.naturalOrder()).orElseThrow().toString();
    }
}
