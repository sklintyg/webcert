/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.common.FakeCredential;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.CreateCertificateRequestDTO;

public class TestSetup {

    private String certificateId;
    private final Certificate certificate;
    private final String routeId;
    private final String csrfToken;

    public static TestSetupBuilder create() {
        return new TestSetupBuilder();
    }

    private TestSetup(String certificateId, Certificate certificate, String routeId,
        String csrfToken) {
        this.certificateId = certificateId;
        this.certificate = certificate;
        this.routeId = routeId;
        this.csrfToken = csrfToken;
    }

    public String certificateId() {
        return certificateId;
    }

    public Certificate certificate() {
        return certificate;
    }

    public String routeId() {
        return routeId;
    }

    public String csrfToken() {
        return csrfToken;
    }

    public static class TestSetupBuilder {

        private String certificateType;
        private String certificateTypeVersion;
        private String patientId;
        private String personId;
        private String unitId;
        private CertificateStatus status;
        private boolean clearPdlLogMessages;
        private boolean createCertificate;
        private FakeCredential credentials;
        private String origin;
        private boolean sjf;

        private String certificateId;
        private Certificate certificate;
        private String routeId;
        private String csrfToken;

        private CustomObjectMapper objectMapper = new CustomObjectMapper();
        private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
        private static final String FAKE_LOGIN_URI = "/fake";

        private TestSetupBuilder() {

        }

        public TestSetupBuilder draft(String certificateType, String certificateTypeVersion, String patientId, String personId,
            String unitId) {
            this.createCertificate = true;
            this.status = CertificateStatus.UNSIGNED;
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            return this;
        }

        public TestSetupBuilder certificate(String certificateType, String certificateTypeVersion, String patientId, String personId,
            String unitId) {
            this.createCertificate = true;
            this.status = CertificateStatus.SIGNED;
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            return this;
        }

        public TestSetupBuilder clearPdlLogMessages() {
            this.clearPdlLogMessages = true;
            return this;
        }

        public TestSetupBuilder login(FakeCredential credentials) {
            this.credentials = credentials;
            return this;
        }
        
        public TestSetupBuilder useDjupIntegratedOrigin() {
            this.origin = "DJUPINTEGRATION";
            return this;
        }

        public TestSetupBuilder sjf() {
            this.sjf = true;
            return this;
        }

        public TestSetup setup() {
            if (createCertificate) {
                certificateId = createCertificate();
            }

            if (credentials != null) {
                RestAssured.sessionId = getAuthSession(credentials);
            }

            if (origin != null) {
                changeOriginTo(origin);
            }

            if (sjf) {
                setSjf();
            }

            if (certificateId != null) {
                certificate = getCertificate(certificateId);
            }

            if (clearPdlLogMessages) {
                deletePdlLogMessagesFromQueue();
            }

            return new TestSetup(certificateId, certificate, routeId, csrfToken);
        }

        private void deletePdlLogMessagesFromQueue() {
            given().when().delete("testability/logMessages");
        }

        private String createCertificate() {
            final var values = new HashMap<String, CertificateDataValue>();

            final CertificateDataValueBoolean avstangningSmittskydd = CertificateDataValueBoolean.builder()
                .id(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27)
                .selected(true)
                .build();
            values.put(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_ID_27, avstangningSmittskydd);

            final CertificateDataValueDiagnosisList diagnos = CertificateDataValueDiagnosisList.builder()
                .list(
                    Collections.singletonList(
                        CertificateDataValueDiagnosis.builder()
                            .id("1")
                            .terminology("ICD_10_SE")
                            .code("A01")
                            .description("Tyfoidfeber och paratyfoidfeber")
                            .build()
                    )
                )
                .build();
            values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

            final var bedomning = CertificateDataValueDateRangeList.builder()
                .list(
                    Collections.singletonList(
                        CertificateDataValueDateRange.builder()
                            .id("HELT_NEDSATT")
                            .from(LocalDate.now())
                            .to(LocalDate.now().plusDays(14))
                            .build()
                    )
                )
                .build();
            values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

            final var certificateRequest = new CreateCertificateRequestDTO();
            certificateRequest.setCertificateType(certificateType);
            certificateRequest.setCertificateTypeVersion(certificateTypeVersion);
            certificateRequest.setPatientId(patientId);
            certificateRequest.setPersonId(personId);
            certificateRequest.setUnitId(unitId);
            certificateRequest.setStatus(status);
            certificateRequest.setValues(values);

            return given()
                .contentType(ContentType.JSON)
                .body(certificateRequest)
                .expect().statusCode(200)
                .when().post("testability/certificate")
                .then().extract().response().body().asString();
        }

        protected String getAuthSession(FakeCredential fakeCredential) {
            try {
                return getAuthSession(objectMapper.writeValueAsString(fakeCredential));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        private String getAuthSession(String credentialsJson) {
            Response response = given().contentType(ContentType.URLENC).and().redirects().follow(false).and()
                .formParam(USER_JSON_FORM_PARAMETER, credentialsJson)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().post(FAKE_LOGIN_URI)
                .then().extract().response();

            assertNotNull(response.sessionId());
            routeId = response.getCookie("ROUTEID") != null ? response.getCookie("ROUTEID") : "nah";
            csrfToken = response.getCookie("XSRF-TOKEN");

            return response.sessionId();
        }

        private ObjectMapper getObjectMapperForDeserialization() {
            return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
        }

        private Certificate getCertificate(String certificateId) {
            return given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }

        protected void setSjf() {
            given()
                .expect()
                .statusCode(200)
                .when()
                .post("authtestability/user/parameters/sjf");
        }

        private void changeOriginTo(String newOrigin) {
            given()
                .pathParam("origin", newOrigin)
                .expect().statusCode(200)
                .when().get("authtestability/user/origin/{origin}");

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//            }
        }
    }
}
