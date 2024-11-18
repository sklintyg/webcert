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

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class UtkastLockJobTest {

    private static final int LOCKED_AFTER_DAY = 5;
    @Mock
    private UtkastService utkastService;
    @Mock
    private LockDraftsFromCertificateService lockDraftsFromCertificateService;
    @Spy
    private MdcHelper mdcHelper;
    @InjectMocks
    private UtkastLockJob utkastLockJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(utkastLockJob, "lockedAfterDay", LOCKED_AFTER_DAY);
    }

    @Test
    void shallLockOldDraftsFromWCAndCertificateService() {
        utkastLockJob.run();
        verify(utkastService).lockOldDrafts(LOCKED_AFTER_DAY, LocalDate.now());
        verify(lockDraftsFromCertificateService).lock(LOCKED_AFTER_DAY);
    }
}
