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

package se.inera.intyg.webcert.web.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockOldDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;

@ExtendWith(MockitoExtension.class)
class LockDraftsFromCertificateServiceTest {

    private static final int LOCKED_AFTER_DAY = 5;
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
        final var expectedResult = 3;
        final var lockOldDraftsRequestDTO = LockOldDraftsRequestDTO.builder().build();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(lockOldDraftsRequestDTO).when(csIntegrationRequestFactory).getLockOldDraftsRequestDTO(LOCKED_AFTER_DAY);
        doReturn(expectedResult).when(csIntegrationService).lockOldDrafts(lockOldDraftsRequestDTO);

        assertEquals(expectedResult, lockDraftsFromCertificateService.lock(LOCKED_AFTER_DAY));
    }
}
