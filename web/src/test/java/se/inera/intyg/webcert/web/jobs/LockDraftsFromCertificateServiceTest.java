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

package se.inera.intyg.webcert.web.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class LockDraftsFromCertificateServiceTest {

    private static final int LOCKED_AFTER_DAY = 5;
    private static final String ID = "id";
    private static final String TYPE = "type";

    @Mock
    MonitoringLogService monitoringService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @InjectMocks
    LockDraftsFromCertificateService lockDraftsFromCertificateService;

    @Test
    void shallReturnZeroIfCertificateServiceProfileIsNotActive() {
        doReturn(false).when(certificateServiceProfile).active();
        assertEquals(0, lockDraftsFromCertificateService.lock(LOCKED_AFTER_DAY));
    }

    @Test
    void shallReturnNumberOfLockedDraftsFromCertificateServiceIsProfileIsActive() {
        final var expectedResult = List.of(getCertificate(), getCertificate(), getCertificate());
        final var lockOldDraftsRequestDTO = LockDraftsRequestDTO.builder().build();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(lockOldDraftsRequestDTO).when(csIntegrationRequestFactory).getLockDraftsRequestDTO(LOCKED_AFTER_DAY);
        doReturn(expectedResult).when(csIntegrationService).lockDrafts(lockOldDraftsRequestDTO);

        assertEquals(expectedResult.size(), lockDraftsFromCertificateService.lock(LOCKED_AFTER_DAY));
    }

    @Test
    void shallMonitorLogLockedDraftsIdsAndType() {
        final var certificates = List.of(getCertificate(), getCertificate(), getCertificate());
        final var lockOldDraftsRequestDTO = LockDraftsRequestDTO.builder().build();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(lockOldDraftsRequestDTO).when(csIntegrationRequestFactory).getLockDraftsRequestDTO(LOCKED_AFTER_DAY);
        doReturn(certificates).when(csIntegrationService).lockDrafts(lockOldDraftsRequestDTO);

        lockDraftsFromCertificateService.lock(LOCKED_AFTER_DAY);

        verify(monitoringService, times(3)).logUtkastLocked(ID, TYPE);
    }

    private static Certificate getCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build()
        );
        return certificate;
    }
}
