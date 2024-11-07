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

package se.inera.intyg.webcert.web.service.facade.modal.confirmation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.inera.intyg.common.support.facade.model.CertificateStatus;

class ConfirmationModalProviderResolverTest {

    @Test
    void shouldReturnProviderForDBIfNormalOriginAndCreatedFromList() {
        assertEquals(DbConfirmationModalProvider.class,
            ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, "NORMAL", true).getClass());
    }

    @Test
    void shouldReturnNullIfUnsupportedType() {
        assertNull(ConfirmationModalProviderResolver.getConfirmation("doi", CertificateStatus.UNSIGNED, "NORMAL", true));
    }

    @ParameterizedTest
    @EnumSource(CertificateStatus.class)
    void testStatusShouldReturnNullOrProvider(CertificateStatus status) {
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", status, "NORMAL", true);
        if (status == CertificateStatus.UNSIGNED) {
            assertNotNull(response);
        } else {
            assertNull(response);
        }
    }

    @Test
    void shouldReturnNullIfNormalAndNotNewDraft() {
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, "NORMAL", false);
        assertNull(response);
    }

    @Test
    void shouldReturnProviderIfIntegratedAndNotCreatedFromList() {
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, "DJUPINTEGRATION", false);
        assertEquals(DbConfirmationModalProvider.class, response.getClass());
    }
}