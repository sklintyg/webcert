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
package se.inera.intyg.webcert.web.service.facade.modal.typeinfo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CertificateTypeInfoModalProviderResolverTest {

    @Test
    void shouldReturnDbProviderForDbCertificateType() {
        final var certificateType = "db";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertAll(
            () -> assertNotNull(provider),
            () -> assertInstanceOf(DbTypeInfoModalProvider.class, provider)
        );
    }

    @Test
    void shouldReturnDbProviderForDbCertificateTypeUpperCase() {
        final var certificateType = "DB";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertAll(
            () -> assertNotNull(provider),
            () -> assertInstanceOf(DbTypeInfoModalProvider.class, provider)
        );
    }

    @Test
    void shouldReturnDoiProviderForDoiCertificateType() {
        final var certificateType = "doi";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertAll(
            () -> assertNotNull(provider),
            () -> assertInstanceOf(DoiTypeInfoModalProvider.class, provider)
        );
    }

    @Test
    void shouldReturnDoiProviderForDoiCertificateTypeUpperCase() {
        final var certificateType = "DOI";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertAll(
            () -> assertNotNull(provider),
            () -> assertInstanceOf(DoiTypeInfoModalProvider.class, provider)
        );
    }

    @Test
    void shouldReturnNullForUnknownCertificateType() {
        final var certificateType = "ag114";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertNull(provider);
    }

    @Test
    void shouldReturnNullForNullCertificateType() {
        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(null);

        assertNull(provider);
    }

    @Test
    void shouldReturnNullForEmptyCertificateType() {
        final var certificateType = "";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertNull(provider);
    }

    @Test
    void shouldReturnNullForLuaenaCertificateType() {
        final var certificateType = "luae_na";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertNull(provider);
    }

    @Test
    void shouldReturnNullForLisjpCertificateType() {
        final var certificateType = "lisjp";

        final var provider = CertificateTypeInfoModalProviderResolver.getModalProvider(certificateType);

        assertNull(provider);
    }
}

