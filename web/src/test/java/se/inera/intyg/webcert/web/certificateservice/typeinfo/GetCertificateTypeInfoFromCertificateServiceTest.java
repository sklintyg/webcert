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

package se.inera.intyg.webcert.web.certificateservice.typeinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.certificate.GetCertificateTypeInfoFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateTypeInfoFromCertificateServiceTest {

    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("191212121212").get();
    private static final List<CertificateTypeInfoDTO> types = List.of(new CertificateTypeInfoDTO());
    private static final CertificateTypeInfoResponseDTO RESPONSE = new CertificateTypeInfoResponseDTO(types);
    private static final CertificateServiceUserDTO USER = new CertificateServiceUserDTO();

    private static final CertificateServicePatientDTO PATIENT = new CertificateServicePatientDTO();

    private static final CertificateServiceUnitDTO UNIT = new CertificateServiceUnitDTO();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CertificateServiceUserHelper certificateServiceUserHelper;

    @Mock
    CertificateServicePatientHelper certificateServicePatientHelper;

    @Mock
    CertificateServiceUnitHelper certificateServiceUnitHelper;

    @InjectMocks
    private GetCertificateTypeInfoFromCertificateService getCertificateTypeInfoFromCertificateService;

    @BeforeEach
    void setup() {
        when(csIntegrationService.getTypeInfo(any()))
            .thenReturn(RESPONSE);
    }

    @Nested
    class Request {

        @Test
        void shouldSetUser() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);

            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);
            verify(csIntegrationService).getTypeInfo(captor.capture());

            assertEquals(USER, captor.getValue().getUser());
        }

        @Test
        void shouldSetPatient() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            when(certificateServicePatientHelper.get(PATIENT_ID))
                .thenReturn(PATIENT);

            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);
            verify(csIntegrationService).getTypeInfo(captor.capture());

            assertEquals(PATIENT, captor.getValue().getPatient());
        }

        @Test
        void shouldSetUnit() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);

            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);
            verify(csIntegrationService).getTypeInfo(captor.capture());

            assertEquals(UNIT, captor.getValue().getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(UNIT);

            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);
            verify(csIntegrationService).getTypeInfo(captor.capture());

            assertEquals(UNIT, captor.getValue().getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(UNIT);

            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);
            verify(csIntegrationService).getTypeInfo(captor.capture());

            assertEquals(UNIT, captor.getValue().getCareProvider());
        }
    }

    @Test
    void shouldReturnListOfTypesFromCSInternalApi() {
        final var response = getCertificateTypeInfoFromCertificateService.get(PATIENT_ID);

        assertEquals(types, response);
    }
}