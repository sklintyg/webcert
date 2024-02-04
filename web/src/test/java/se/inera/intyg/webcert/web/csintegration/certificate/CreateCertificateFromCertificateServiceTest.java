/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromCertificateServiceTest {

    private static final CertificateServiceUserDTO USER = new CertificateServiceUserDTO();
    private static final CertificateServiceUnitDTO UNIT = new CertificateServiceUnitDTO();
    private static final String TYPE = "TYPE";
    private static final String VERSION = "VERSION";
    private static final CertificateServicePatientDTO PATIENT = new CertificateServicePatientDTO();
    private static final CertificateModelIdDTO EXISTS_RESPONSE = new CertificateModelIdDTO(TYPE, VERSION);
    private static final String PATIENT_ID = "191212121212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).get();
    private static final String ID = "ID";

    @Mock
    CertificateServiceUnitHelper certificateServiceUnitHelper;

    @Mock
    CertificateServicePatientHelper certificateServicePatientHelper;

    @Mock
    CertificateServiceUserHelper certificateServiceUserHelper;

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    PDLLogService pdlLogService;

    @InjectMocks
    CreateCertificateFromCertificateService createCertificateFromCertificateService;

    @Test
    void shouldThrowErrorIfIntegrationAPIReturnsNull() {
        when(csIntegrationService.certificateTypeExists(TYPE))
            .thenReturn(Optional.of(EXISTS_RESPONSE));

        assertThrows(CreateCertificateException.class, () -> createCertificateFromCertificateService.create(TYPE, "wrongFormat"));
    }

    @Test
    void shouldThrowErrorIfPatientIdHasWrongFormat() {
        when(csIntegrationService.certificateTypeExists(TYPE))
            .thenReturn(Optional.of(EXISTS_RESPONSE));

        assertThrows(CreateCertificateException.class, () -> createCertificateFromCertificateService.create(TYPE, "wrongFormat"));
    }

    @Test
    void shouldReturnNullIfCertificateIntegrationResponseIsNull() throws CreateCertificateException {
        final var response = createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

        assertNull(response);
    }

    @Test
    void shouldReturnNullIfCertificateTypeDoesNotExistInCS() throws CreateCertificateException {
        final var response = createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

        assertNull(response);
    }

    @Test
    void shouldNotPerformPDLLogIfTypeWasNotCreatedFromCS() throws CreateCertificateException {
        createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

        verify(pdlLogService, times(0)).logCreated(PATIENT_ID, ID);
    }

    @Nested
    class HasCertificate {

        @BeforeEach
        void setup() throws CreateCertificateException {
            Certificate certificate = new Certificate();
            final var metadata = CertificateMetadata.builder().id(ID).build();
            certificate.setMetadata(metadata);

            when(csIntegrationService.certificateTypeExists(TYPE))
                .thenReturn(Optional.of(EXISTS_RESPONSE));
            when(csIntegrationService.createCertificate(any()))
                .thenReturn(certificate);
        }

        @Test
        void shouldReturnIdOfCertificate() throws CreateCertificateException {
            final var response = createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            assertEquals(ID, response);
        }

        @Test
        void shouldPerformPDLForCreateCertificate() throws CreateCertificateException {
            createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            verify(pdlLogService, times(1)).logCreated(PATIENT_ID, ID);
        }

        @Nested
        class Request {

            @BeforeEach
            void setup() {
                when(certificateServiceUserHelper.get())
                    .thenReturn(USER);
                when(certificateServicePatientHelper.get(PERSONNUMMER))
                    .thenReturn(PATIENT);
            }

            @Test
            void shouldSetUser() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(USER, captor.getValue().getUser());
            }

            @Test
            void shouldSetUnit() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

                when(certificateServiceUnitHelper.getUnit())
                    .thenReturn(UNIT);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(UNIT, captor.getValue().getUnit());
            }

            @Test
            void shouldSetCareUnit() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);
                when(certificateServiceUnitHelper.getCareUnit())
                    .thenReturn(UNIT);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(UNIT, captor.getValue().getCareUnit());
            }

            @Test
            void shouldSetCareProvider() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);
                when(certificateServiceUnitHelper.getCareProvider())
                    .thenReturn(UNIT);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(UNIT, captor.getValue().getCareProvider());
            }

            @Test
            void shouldSetPatient() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(PATIENT, captor.getValue().getPatient());
            }

            @Test
            void shouldSetType() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(TYPE, captor.getValue().getCertificateModelId().getType());
            }

            @Test
            void shouldSetTypeVersion() throws CreateCertificateException {
                final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

                createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
                verify(csIntegrationService).createCertificate(captor.capture());

                assertEquals(VERSION, captor.getValue().getCertificateModelId().getVersion());
            }
        }
    }

}