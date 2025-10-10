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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromTemplateAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String NEW_CERTIFICATE_ID_FROM_WC = "newCertificateIdFromWC";
    private static final String NEW_CERTIFICATE_ID_FROM_CS = "newCertificateIdFromCS";

    CreateCertificateFromTemplateFacadeService createCertificateFromTemplateFromWC;
    CreateCertificateFromTemplateFacadeService createCertificateFromTemplateFromCS;
    CreateCertificateFromTemplateFacadeService aggregator;

    @BeforeEach
    void setUp() {
        createCertificateFromTemplateFromWC = mock(CreateCertificateFromTemplateFacadeService.class);
        createCertificateFromTemplateFromCS = mock(CreateCertificateFromTemplateFacadeService.class);

        aggregator = new CreateCertificateFromTemplateAggregator(
            createCertificateFromTemplateFromWC,
            createCertificateFromTemplateFromCS
        );
    }

  @Test
  void shouldReturnCertificateIdFromCSIfCSReturnsResponse() {
    when(createCertificateFromTemplateFromCS.createCertificateFromTemplate(CERTIFICATE_ID))
        .thenReturn(NEW_CERTIFICATE_ID_FROM_CS);

    final var result = aggregator.createCertificateFromTemplate(CERTIFICATE_ID);

    verify(createCertificateFromTemplateFromCS, times(1)).createCertificateFromTemplate(CERTIFICATE_ID);
    verifyNoInteractions(createCertificateFromTemplateFromWC);
    assertEquals(NEW_CERTIFICATE_ID_FROM_CS, result);
  }

    @Test
    void shouldReturnCertificateIdFromWCIfCSReturnsNull() {
        when(createCertificateFromTemplateFromCS.createCertificateFromTemplate(CERTIFICATE_ID))
            .thenReturn(null);
        when(createCertificateFromTemplateFromWC.createCertificateFromTemplate(CERTIFICATE_ID))
            .thenReturn(NEW_CERTIFICATE_ID_FROM_WC);

        final var result = aggregator.createCertificateFromTemplate(CERTIFICATE_ID);

        verify(createCertificateFromTemplateFromCS, times(1)).createCertificateFromTemplate(CERTIFICATE_ID);
        verify(createCertificateFromTemplateFromWC, times(1)).createCertificateFromTemplate(CERTIFICATE_ID);
        assertEquals(NEW_CERTIFICATE_ID_FROM_WC, result);
    }

    @Test
    void shouldReturnNullAndBothCSAndWCReturnNull() {
        when(createCertificateFromTemplateFromCS.createCertificateFromTemplate(CERTIFICATE_ID))
            .thenReturn(null);
        when(createCertificateFromTemplateFromWC.createCertificateFromTemplate(CERTIFICATE_ID))
            .thenReturn(null);

        final var result = aggregator.createCertificateFromTemplate(CERTIFICATE_ID);

        verify(createCertificateFromTemplateFromCS, times(1)).createCertificateFromTemplate(CERTIFICATE_ID);
        verify(createCertificateFromTemplateFromWC, times(1)).createCertificateFromTemplate(CERTIFICATE_ID);
      assertNull(result);
    }
}
