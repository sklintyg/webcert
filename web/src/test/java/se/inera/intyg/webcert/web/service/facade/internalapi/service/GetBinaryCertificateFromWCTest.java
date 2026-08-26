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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetBinaryCertificateResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetBinaryCertificateFromWCTest {

  private static final String CERTIFICATE_ID = "certificateId";

  @InjectMocks private GetBinaryCertificateFromWC getBinaryCertificateFromWC;

  @Test
  void shouldReturnGetBinaryCertificateResponse() {
    final var expectedResponse = GetBinaryCertificateResponseDTO.builder().build();
    final var response = getBinaryCertificateFromWC.get(CERTIFICATE_ID);
    assertEquals(expectedResponse, response);
  }
}
