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
package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetBinaryCertificate;

@ExtendWith(MockitoExtension.class)
class GetBinaryCertificateInternalAggregatorTest {

  private static final String CERTIFICATE_ID = "certificateId";

  @Mock GetBinaryCertificate getBinaryCertificateFromCS;

  @Mock GetBinaryCertificate getBinaryCertificateFromWC;

  private GetBinaryCertificateInternalAggregator aggregator;

  @BeforeEach
  void setUp() {
    aggregator =
        new GetBinaryCertificateInternalAggregator(
            getBinaryCertificateFromCS, getBinaryCertificateFromWC);
  }

  @Test
  void shouldReturnResponseFromCS() {
    final var expectedResponse = GetBinaryCertificateResponseDTO.builder().build();
    when(getBinaryCertificateFromCS.get(CERTIFICATE_ID)).thenReturn(expectedResponse);

    final var actualResponse = aggregator.get(CERTIFICATE_ID);

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void shouldNotGetResponseFromWCWhenCSReturnsResponse() {
    when(getBinaryCertificateFromCS.get(CERTIFICATE_ID))
        .thenReturn(GetBinaryCertificateResponseDTO.builder().build());

    aggregator.get(CERTIFICATE_ID);

    verifyNoInteractions(getBinaryCertificateFromWC);
  }

  @Test
  void shouldUseCertificateIdWhenGettingResponseFromCS() {
    when(getBinaryCertificateFromCS.get(CERTIFICATE_ID))
        .thenReturn(GetBinaryCertificateResponseDTO.builder().build());

    aggregator.get(CERTIFICATE_ID);

    verify(getBinaryCertificateFromCS).get(CERTIFICATE_ID);
  }

  @Test
  void shouldReturnResponseFromWC() {
    final var expectedResponse = GetBinaryCertificateResponseDTO.builder().build();
    when(getBinaryCertificateFromCS.get(CERTIFICATE_ID)).thenReturn(null);
    when(getBinaryCertificateFromWC.get(CERTIFICATE_ID)).thenReturn(expectedResponse);

    final var actualResponse = aggregator.get(CERTIFICATE_ID);

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void shouldUseCertificateIdWhenGettingResponseFromWC() {
    when(getBinaryCertificateFromCS.get(CERTIFICATE_ID)).thenReturn(null);

    aggregator.get(CERTIFICATE_ID);

    verify(getBinaryCertificateFromWC).get(CERTIFICATE_ID);
  }
}
