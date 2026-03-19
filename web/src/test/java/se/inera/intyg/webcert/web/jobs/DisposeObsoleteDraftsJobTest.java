/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.web.csintegration.certificate.DisposeObsoleteDraftsService;

@ExtendWith(MockitoExtension.class)
class DisposeObsoleteDraftsJobTest {

  @Mock MdcHelper mdcHelper;
  @Mock DisposeObsoleteDraftsService disposeObsoleteDraftsService;
  @InjectMocks DisposeObsoleteDraftsJob disposeObsoleteDraftsJob;

  @Test
  void shouldExecuteJob() {
    ReflectionTestUtils.setField(disposeObsoleteDraftsJob, "obsoleteDraftsPeriod", "P3M");
    ReflectionTestUtils.setField(disposeObsoleteDraftsJob, "obsoleteDraftsPageSize", 10);

    disposeObsoleteDraftsJob.run();

    verify(disposeObsoleteDraftsService).dispose(any(LocalDateTime.class), eq(10));
  }
}
