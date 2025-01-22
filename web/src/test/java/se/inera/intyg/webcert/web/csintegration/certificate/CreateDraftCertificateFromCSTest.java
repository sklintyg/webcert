/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.APPLICATION_ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.VALIDATION_ERROR;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.exception.HandleApiErrorService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@ExtendWith(MockitoExtension.class)
class CreateDraftCertificateFromCSTest {

    private static final String INVALID_PERSON_ID = "INVALID";
    private static final String HSA_ID = "HSA_ID";
    private static final IntygUser USER = new IntygUser(HSA_ID);
    private static final String VALID_PERSON_ID = "191212121212";
    private static final String CODE = "code";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String EXPECTED_ID = "EXPECTED_ID";
    private static final String EXPECTED_UNIT_ID = "EXPECTED_UNIT_ID";
    private static final String CODE_SYSTEM = "codeSystem";
    @Mock
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    private HandleApiErrorService handleApiErrorService;
    @Mock
    private PDLLogService pdlLogService;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private IntegratedUnitRegistryHelper integratedUnitRegistryHelper;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private CreateDraftCertificateFromCS createDraftCertificateFromCS;

    @BeforeEach
    void setUp() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(EXPECTED_ID)
                .type(CODE)
                .unit(
                    Unit.builder()
                        .unitId(EXPECTED_UNIT_ID)
                        .build()
                )
                .issuedBy(
                    Staff.builder()
                        .personId(HSA_ID)
                        .build()
                )
                .build()
        );
    }

    @Test
    void shouldThrowIfPersonIdCouldNotBeCreated() {
        final var certificate = getIntyg(INVALID_PERSON_ID);
        final var user = new IntygUser(HSA_ID);
        final var modelIdDTO = Optional.of(CertificateModelIdDTO.builder().build());

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(modelIdDTO);
        final var webCertServiceException = assertThrows(WebCertServiceException.class,
            () -> createDraftCertificateFromCS.create(certificate, user));

        assertTrue(webCertServiceException.getMessage().contains("Cannot create Personnummer object with invalid personId"));
    }

    @Test
    void shouldReturnErrorResponseIfSekretessStatusIsUndefined() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var modelIdDTO = Optional.of(CertificateModelIdDTO.builder().build());
        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(modelIdDTO);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);

        final var result = createDraftCertificateFromCS.create(certificate, USER);

        assertTrue(result.getResult().getResultText().contains("Information om skyddade personuppgifter kunde inte hämtas."));
        assertEquals(ErrorIdType.APPLICATION_ERROR, result.getResult().getErrorId());
    }

    @Test
    void shouldReturnNullIfCertificateTypeDontExists() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.empty());
        assertNull(createDraftCertificateFromCS.create(certificate, USER));
    }

    @Test
    void shouldReturnSuccessResponseWithCertificateIdIfNoErrorsFromCertificateService() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenReturn(CERTIFICATE);

        final var result = createDraftCertificateFromCS.create(certificate, user);
        assertEquals(EXPECTED_ID, result.getIntygsId().getExtension());
    }

    @Test
    void shouldReturnApplicationErrorIfNotHttpServerErrorException() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenThrow(IllegalArgumentException.class);

        final var result = createDraftCertificateFromCS.create(certificate, user);
        assertEquals(APPLICATION_ERROR, result.getResult().getErrorId());
    }

    @Test
    void shouldReturnValidationErrorIfHttpServerErrorException() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenThrow(
            HttpClientErrorException.create("", HttpStatus.FORBIDDEN, "", HttpHeaders.EMPTY, null, null)
        );

        final var result = createDraftCertificateFromCS.create(certificate, user);
        assertEquals(VALIDATION_ERROR, result.getResult().getErrorId());
    }

    @Test
    void shouldPutIntegreradEnhetToRegistry() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenReturn(CERTIFICATE);

        createDraftCertificateFromCS.create(certificate, user);

        verify(integratedUnitRegistryHelper).addUnit(user);
    }

    @Test
    void shouldPdlLogIfNoErrorsFromCertificateService() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenReturn(CERTIFICATE);

        createDraftCertificateFromCS.create(certificate, user);

        verify(pdlLogService).logCreatedWithIntygUser(CERTIFICATE, user);
    }

    @Test
    void shouldMonitorLogIfNoErrorsFromCertificateService() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenReturn(CERTIFICATE);

        createDraftCertificateFromCS.create(certificate, user);

        verify(monitoringLogService).logUtkastCreated(
            CERTIFICATE.getMetadata().getId(),
            CERTIFICATE.getMetadata().getType(),
            CERTIFICATE.getMetadata().getUnit().getUnitId(),
            CERTIFICATE.getMetadata().getIssuedBy().getPersonId(),
            0
        );
    }

    @Test
    void shouldPublishCertificateStatusUpdate() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenReturn(CERTIFICATE);

        createDraftCertificateFromCS.create(certificate, user);

        verify(publishCertificateStatusUpdateService).publish(
            CERTIFICATE,
            HandelsekodEnum.SKAPAT,
            Optional.of(user),
            Optional.empty());
    }

    @Test
    void shouldNotLogIfCertificateServiceThrows() {
        final var certificate = getIntyg(VALID_PERSON_ID);
        final var user = mock(IntygUser.class);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE)).thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createCertificate(request)).thenThrow(IllegalArgumentException.class);

        createDraftCertificateFromCS.create(certificate, user);

        verifyNoInteractions(pdlLogService);
        verifyNoInteractions(monitoringLogService);
    }


    private static Intyg getIntyg(String extension) {
        final var intyg = new Intyg();
        intyg.setPatient(new Patient());
        intyg.getPatient().setPersonId(new PersonId());
        intyg.getPatient().getPersonId().setExtension(extension);
        intyg.setTypAvIntyg(new TypAvIntyg());
        intyg.getTypAvIntyg().setCode(CODE);
        intyg.getTypAvIntyg().setCodeSystem(CODE_SYSTEM);
        return intyg;
    }
}
