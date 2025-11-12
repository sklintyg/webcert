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

package se.inera.intyg.webcert.web.service.facade.validator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;

class CertificateValidatorProviderTest {

    private CertificateValidatorProvider certificateValidatorProvider;

    @BeforeEach
    void setUp() {
        DefaultTypeAheadProvider typeAheadProvider = mock(DefaultTypeAheadProvider.class);
        certificateValidatorProvider = new CertificateValidatorProvider(typeAheadProvider);
    }

    @Test
    void shouldReturnDbValidatorForDbCertificateType() {
        final var certificateType = DbModuleEntryPoint.MODULE_ID;

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNotNull(validator);
        assertInstanceOf(SosParentCertificateValidator.class, validator);
    }

    @Test
    void shouldReturnNullForUnknownCertificateType() {
        final var certificateType = "unknown_type";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNull(validator);
    }

    @Test
    void shouldReturnNullForLisjpCertificateType() {
        final var certificateType = "lisjp";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNull(validator);
    }

    @Test
    void shouldReturnNullForNullCertificateType() {
        final var validator = certificateValidatorProvider.get(null);

        assertNull(validator);
    }

    @Test
    void shouldBeCaseInsensitiveForDbCertificateType() {
        final var certificateType = "DB";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNotNull(validator);
        assertInstanceOf(SosParentCertificateValidator.class, validator);
    }

    @Test
    void shouldReturnDoiValidatorForDoiCertificateType() {
        final var certificateType = "doi";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNotNull(validator);
        assertInstanceOf(SosParentCertificateValidator.class, validator);
    }

    @Test
    void shouldBeCaseInsensitiveForDoiCertificateType() {
        final var certificateType = "DOI";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertNotNull(validator);
        assertInstanceOf(SosParentCertificateValidator.class, validator);
    }
}

