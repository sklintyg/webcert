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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;

@ExtendWith(MockitoExtension.class)
class ListIntygEntryConverterTest {

    @InjectMocks
    private ListIntygEntryConverter listIntygEntryConverter;
    private static final Certificate CERTIFICATE = CertificateFacadeTestHelper.createCertificateTypeWithVersion(
        "type", CertificateStatus.UNSIGNED, true, "typeVersion");

    @Test
    void shouldConvertCertificateId() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(response.getIntygId(), CERTIFICATE.getMetadata().getId());
    }

    @Test
    void shouldConvertCertificateType() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(response.getIntygType(), CERTIFICATE.getMetadata().getType());
    }

    @Test
    void shouldConvertCertificateTypeName() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(response.getIntygTypeName(), CERTIFICATE.getMetadata().getTypeName());
    }

    @Test
    void shouldConvertCertificateTypeVersion() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(response.getIntygTypeVersion(), CERTIFICATE.getMetadata().getTypeVersion());
    }

    @Test
    void shouldConvertCertificateVersion() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(response.getVersion(), CERTIFICATE.getMetadata().getVersion());
    }


}