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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class PrintCertificateFromCertificateServiceTest {

    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final boolean EMPLOYER_COPY = true;
    private static final String PATIENT_ID = "191212121212";
    private static final PrintCertificateRequestDTO REQUEST = PrintCertificateRequestDTO.builder().build();
    private static final GetCertificateRequestDTO GET_REQUEST = GetCertificateRequestDTO.builder().build();
    private static final Certificate certificate = new Certificate();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    IntygPdf responseFromCS;

    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    PrintCertificateFromCertificateService printCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesntExistInCS() {
        final var response = printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

        assertNull(response);
    }

    @Test
    void shouldPerformPDLLogIfCertificateDoesntExistInCS() {
        printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

        verify(pdlLogService, times(0)).logPrinted(certificate);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);
            when(csIntegrationService.getCertificate(ID, GET_REQUEST))
                .thenReturn(certificate);

            when(csIntegrationRequestFactory.getPrintCertificateRequest("Intyget är utskrivet från Webcert.", PATIENT_ID))
                .thenReturn(REQUEST);
            when(csIntegrationRequestFactory.getCertificateRequest())
                .thenReturn(GET_REQUEST);
            when(csIntegrationService.printCertificate(ID, REQUEST))
                .thenReturn(responseFromCS);
        }

        @Test
        void shouldReturnResponseFromCSIfCertificateExistsInCS() {
            certificate.setMetadata(getMetadata(CertificateStatus.UNSIGNED));
            final var response = printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

            assertEquals(responseFromCS, response);
        }

        @Test
        void shouldPerformPDLLog() {
            certificate.setMetadata(getMetadata(CertificateStatus.REVOKED));
            printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

            verify(pdlLogService, times(1)).logPrinted(certificate);
        }

        @Test
        void shouldPublishAnalyticsMessageWhenCertificateIsPrinted() {
            certificate.setMetadata(getMetadata(CertificateStatus.UNSIGNED));
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.certificatePrinted(certificate)).thenReturn(analyticsMessage);

            printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

            verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
        }

        @Nested
        class TestMonitoringLogService {

            @Test
            void shouldLogPrintedDraft() {
                certificate.setMetadata(getMetadata(CertificateStatus.UNSIGNED));
                printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

                verify(monitoringLogService, times(1)).logUtkastPrint(ID, TYPE);
            }

            @Test
            void shouldLogPrintedLocked() {
                certificate.setMetadata(getMetadata(CertificateStatus.LOCKED));
                printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

                verify(monitoringLogService, times(1)).logUtkastPrint(ID, TYPE);
            }

            @Test
            void shouldLogPrintedRevoked() {
                certificate.setMetadata(getMetadata(CertificateStatus.REVOKED));
                printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

                verify(monitoringLogService, times(1)).logRevokedPrint(ID, TYPE);
            }

            @Test
            void shouldLogPrintedSigned() {
                certificate.setMetadata(getMetadata(CertificateStatus.SIGNED));
                printCertificateFromCertificateService.print(ID, TYPE, EMPLOYER_COPY);

                verify(monitoringLogService, times(1)).logIntygPrintPdf(ID, TYPE, EMPLOYER_COPY);
            }
        }
    }

    private CertificateMetadata getMetadata(CertificateStatus status) {
        return CertificateMetadata.builder()
            .status(status)
            .type(TYPE)
            .patient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id(PATIENT_ID)
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
