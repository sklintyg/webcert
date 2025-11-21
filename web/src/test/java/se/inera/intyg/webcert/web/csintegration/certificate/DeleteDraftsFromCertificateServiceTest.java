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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class DeleteDraftsFromCertificateServiceTest {

    private static final LocalDateTime CUTOFF_DATE = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final long PERIOD = ChronoUnit.DAYS.between(CUTOFF_DATE.toLocalDate(), LocalDate.now());
    private static final String CERTIFICATE_ID_1 = "cert-id-1";
    private static final String CERTIFICATE_ID_2 = "cert-id-2";
    private static final String CERTIFICATE_ID_3 = "cert-id-3";
    private static final String TYPE = "lisjp";

    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    MonitoringLogService monitoringLogService;
    @InjectMocks
    DeleteDraftsFromCertificateService deleteDraftsFromCertificateService;

    @Test
    void shouldReturnZeroIfCertificateServiceProfileIsNotActive() {
        doReturn(false).when(certificateServiceProfile).active();

        final var result = deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        assertEquals(0, result);
        verifyNoInteractions(csIntegrationService);
        verifyNoInteractions(csIntegrationRequestFactory);
        verifyNoInteractions(publishCertificateStatusUpdateService);
        verifyNoInteractions(monitoringLogService);
    }

    @Test
    void shouldReturnZeroIfNoStaleDraftsFound() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(Collections.emptyList()).when(csIntegrationService).listStaleDrafts(any());

        final var result = deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        assertEquals(0, result);
        verify(csIntegrationService).listStaleDrafts(any());
        verify(csIntegrationService, never()).deleteStaleDraft(any());
        verifyNoInteractions(publishCertificateStatusUpdateService);
        verifyNoInteractions(monitoringLogService);
    }

    @Test
    void shouldDeleteEachDraftIndividually() {
        final var staleDraftIds = List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2, CERTIFICATE_ID_3);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(staleDraftIds).when(csIntegrationService).listStaleDrafts(any());

        doReturn("xml1").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_1);
        doReturn("xml2").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_2);
        doReturn("xml3").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_3);

        doReturn(getCertificate(CERTIFICATE_ID_1)).when(csIntegrationService).deleteStaleDraft(any());

        final var result = deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        assertEquals(3, result);
        verify(csIntegrationService, times(3)).getInternalCertificateXml(any());
        verify(csIntegrationService, times(3)).deleteStaleDraft(any());
    }

    @Test
    void shouldPublishStatusUpdateWithXmlForEachDeletedDraft() {
        final var staleDraftIds = List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2);
        final var certificate1 = getCertificate(CERTIFICATE_ID_1);
        final var certificate2 = getCertificate(CERTIFICATE_ID_2);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(staleDraftIds).when(csIntegrationService).listStaleDrafts(any());

        doReturn("xml1").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_1);
        doReturn("xml2").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_2);

        doReturn(certificate1, certificate2).when(csIntegrationService).deleteStaleDraft(any());

        deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        verify(publishCertificateStatusUpdateService, times(2))
            .publish(any(Certificate.class), any(HandelsekodEnum.class), any(String.class));
    }

    @Test
    void shouldLogMonitoringForEachDeletedDraft() {
        final var staleDraftIds = List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(staleDraftIds).when(csIntegrationService).listStaleDrafts(any());

        doReturn("xml1").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_1);
        doReturn("xml2").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_2);

        doReturn(getCertificate(CERTIFICATE_ID_1), getCertificate(CERTIFICATE_ID_2))
            .when(csIntegrationService).deleteStaleDraft(any());

        deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        verify(monitoringLogService).logUtkastDisposed(CERTIFICATE_ID_1, TYPE, PERIOD);
        verify(monitoringLogService).logUtkastDisposed(CERTIFICATE_ID_2, TYPE, PERIOD);
    }

    @Test
    void shouldContinueProcessingWhenOneDraftFailsToDelete() {
        final var staleDraftIds = List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2, CERTIFICATE_ID_3);
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(staleDraftIds).when(csIntegrationService).listStaleDrafts(any());

        doReturn("xml1").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_1);
        doThrow(new RuntimeException("Failed to get XML")).when(csIntegrationService)
            .getInternalCertificateXml(CERTIFICATE_ID_2);
        doReturn("xml3").when(csIntegrationService).getInternalCertificateXml(CERTIFICATE_ID_3);

        doReturn(getCertificate(CERTIFICATE_ID_1), getCertificate(CERTIFICATE_ID_3))
            .when(csIntegrationService).deleteStaleDraft(any());

        final var result = deleteDraftsFromCertificateService.delete(CUTOFF_DATE);

        verify(csIntegrationService, times(3)).getInternalCertificateXml(any());
        assertEquals(2, result);
        verify(csIntegrationService, times(2)).deleteStaleDraft(any());
    }

    private static Certificate getCertificate(String id) {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(id)
                .type(TYPE)
                .build()
        );
        return certificate;
    }
}