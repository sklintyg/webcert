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
package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class DisposeObsoleteDraftsServiceTest {

  private static final int PAGE_SIZE = 10;
  private static final LocalDateTime OBSOLETE_DRAFTS_DATE = LocalDateTime.now();
  @Mock UtkastService utkastService;
  @Mock DisposeObsoleteDraftsFromCertificateService disposeObsoleteDraftsFromCertificateService;
  @InjectMocks DisposeObsoleteDraftsService disposeObsoleteDraftsService;

  @Test
  void shouldDisposeObsoleteDraftsInWC() {
    disposeObsoleteDraftsService.dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
    verify(utkastService).dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
  }

  @Test
  void shouldDisposeObsoleteDraftsInCS() {
    disposeObsoleteDraftsService.dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
    verify(disposeObsoleteDraftsFromCertificateService).dispose(OBSOLETE_DRAFTS_DATE);
  }
}
