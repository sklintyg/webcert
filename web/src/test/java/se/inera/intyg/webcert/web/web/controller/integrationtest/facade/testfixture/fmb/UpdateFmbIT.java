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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.fmb;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOSES_LIST_ITEM_1_ID;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;

import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class UpdateFmbIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    @Test
    @DisplayName("Shall return icf request with icf codes")
    void shallReturnIcfRequestWithIcfCodes() {
        final var testSetup = getLoginTestSetup();

        final var icd10Codes = new String[]{"A02"};
        final IcfRequestDTO icfRequestDTO = new IcfRequestDTO();
        icfRequestDTO.setIcdCodes(icd10Codes);

        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        final var response = testSetup
            .spec()
            .contentType(ContentType.JSON)
            .body(icfRequestDTO)
            .expect().statusCode(200)
            .when()
            .post("api/icf")
            .then().extract().response().as(IcfResponseDTO.class, getObjectMapperForDeserialization());

        assertAll(
            () -> assertTrue(response.getActivityLimitation().getUniqueCodes().size() > 0),
            () -> assertTrue(response.getDisability().getUniqueCodes().size() > 0)
        );
    }

    @Test
    @DisplayName("Shall return draft with FMB warning")
    void shallReturnDraftWithFMBWarning() {
        final var icd10Codes = new String[]{"F500"};
        final var diagnosisValue = getValueDiagnosisList();
        final var sickLeaveValue = getValueDateRangeList();
        final var valueMap = getValueMap(diagnosisValue, sickLeaveValue);
        final ValidateSickLeavePeriodRequestDTO validateSickLeavePeriodRequest = getValidateSickLeavePeriodRequest(
            icd10Codes, sickLeaveValue);

        final var testSetup = getDraftWithValuesTestSetup(moduleId(), typeVersion(), valueMap);

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        testSetup
            .spec().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        final var response = testSetup
            .spec()
            .contentType(ContentType.JSON)
            .body(validateSickLeavePeriodRequest)
            .expect().statusCode(200)
            .when()
            .post("api/fmb/validateSickLeavePeriod")
            .then().extract().response().as(ValidateSickLeavePeriodResponseDTO.class, getObjectMapperForDeserialization());

        assertTrue(response.getMessage().length() > 0);
    }

    private ValidateSickLeavePeriodRequestDTO getValidateSickLeavePeriodRequest(String[] icd10Codes,
        CertificateDataValueDateRangeList sickLeaveValue) {
        final var validateSickLeavePeriodRequest = new ValidateSickLeavePeriodRequestDTO();
        validateSickLeavePeriodRequest.setPersonId(ATHENA_ANDERSSON.getPersonId().getId());
        validateSickLeavePeriodRequest.setIcd10Codes(icd10Codes);
        validateSickLeavePeriodRequest.setDateRangeList(sickLeaveValue);
        return validateSickLeavePeriodRequest;
    }

    private CertificateDataValueDiagnosisList getValueDiagnosisList() {
        return CertificateDataValueDiagnosisList.builder()
            .list(Collections.singletonList(
                    CertificateDataValueDiagnosis.builder()
                        .code("F500")
                        .id(DIAGNOSES_LIST_ITEM_1_ID)
                        .build()
                )
            )
            .build();
    }

    private CertificateDataValueDateRangeList getValueDateRangeList() {
        return CertificateDataValueDateRangeList.builder()
            .list(Collections.singletonList(
                    CertificateDataValueDateRange.builder()
                        .id("HALFTEN")
                        .from(LocalDate.now())
                        .to(LocalDate.now().plusYears(5))
                        .build()
                )
            )
            .build();
    }

    private Map<String, CertificateDataValue> getValueMap(
        CertificateDataValue diagnosisValue, CertificateDataValue sickLeaveValue) {
        Map<String, CertificateDataValue> valueMap = new HashMap<>();
        valueMap.put("6", diagnosisValue);
        valueMap.put("32", sickLeaveValue);
        return valueMap;
    }
}
