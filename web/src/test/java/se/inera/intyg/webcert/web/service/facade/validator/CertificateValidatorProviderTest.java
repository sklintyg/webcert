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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;

class CertificateValidatorProviderTest {

    private CertificateValidatorProvider certificateValidatorProvider;

    @BeforeEach
    void setUp() {
        final var typeAheadProvider = mock(DefaultTypeAheadProvider.class);
        certificateValidatorProvider = new CertificateValidatorProvider(typeAheadProvider);
    }

    @Test
    void shouldReturnSosParentValidatorForDbCertificateType() {
        final var certificateType = DbModuleEntryPoint.MODULE_ID;

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isPresent());
        assertInstanceOf(SosParentCertificateValidator.class, validator.get());
    }

    @Test
    void shouldReturnSosParentValidatorForDoiCertificateType() {
        final var certificateType = DoiModuleEntryPoint.MODULE_ID;

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isPresent());
        assertInstanceOf(SosParentCertificateValidator.class, validator.get());
    }

    @Test
    void shouldReturnEmptyForUnknownCertificateType() {
        final var certificateType = "unknown_type";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isEmpty());
    }

    @Test
    void shouldReturnEmptyForLisjpCertificateType() {
        final var certificateType = "lisjp";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isEmpty());
    }

    @Test
    void shouldReturnEmptyForNullCertificateType() {
        final var validator = certificateValidatorProvider.get(null);

        assertTrue(validator.isEmpty());
    }

    @Test
    void shouldBeCaseInsensitiveForDbCertificateType() {
        final var certificateType = "DB";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isPresent());
        assertInstanceOf(SosParentCertificateValidator.class, validator.get());
    }

    @Test
    void shouldBeCaseInsensitiveForDoiCertificateType() {
        final var certificateType = "DOI";

        final var validator = certificateValidatorProvider.get(certificateType);

        assertTrue(validator.isPresent());
        assertInstanceOf(SosParentCertificateValidator.class, validator.get());
    }
}


