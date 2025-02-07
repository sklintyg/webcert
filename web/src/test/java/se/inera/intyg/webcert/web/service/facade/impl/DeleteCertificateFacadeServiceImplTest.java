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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class DeleteCertificateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private DeleteCertificateFacadeServiceImpl deleteCertificateFacadeService;

    @Test
    void shallDeleteDraft() {
        final var expectedCertificateId = "certificateId";
        final var expectedVersion = 100L;

        deleteCertificateFacadeService.deleteCertificate(expectedCertificateId, expectedVersion);

        final var actualCertificateIdCaptor = ArgumentCaptor.forClass(String.class);
        final var actualVersionCaptor = ArgumentCaptor.forClass(Long.class);

        verify(utkastService)
            .deleteUnsignedDraft(
                actualCertificateIdCaptor.capture(),
                actualVersionCaptor.capture()
            );

        assertAll(
            () -> assertEquals(expectedCertificateId, actualCertificateIdCaptor.getValue()),
            () -> assertEquals(expectedVersion, actualVersionCaptor.getValue())
        );
    }
}
