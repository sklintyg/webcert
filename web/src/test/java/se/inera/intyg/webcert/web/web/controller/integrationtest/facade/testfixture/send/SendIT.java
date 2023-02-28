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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.send;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class SendIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    @Test
    @DisplayName("Shall be able to send certificate")
    void shallBeAbleToSendCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion()).setup();

        final var expectedResult = "OK";
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/send")
            .then().extract().response().as(SendCertificateResponseDTO.class, getObjectMapperForDeserialization()).getResult();

        assertEquals(expectedResult, response);
    }

    @Test
    @DisplayName("Shall pdl log send activity when sending certificate")
    public void shallPdlLogSendActivityWhenSendingCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .clearPdlLogMessages()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .when().post("api/certificate/{certificateId}/send")
            .then()
            .assertThat().statusCode(HttpStatus.OK.value());

        assertNumberOfPdlMessages(1);
        assertPdlLogMessage(ActivityType.SEND, testSetup.certificateId());
    }
}
