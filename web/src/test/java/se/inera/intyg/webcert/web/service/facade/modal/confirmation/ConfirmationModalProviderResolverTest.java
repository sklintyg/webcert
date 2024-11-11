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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class ConfirmationModalProviderResolverTest {

    @Test
    void shouldReturnProviderForDBIfNormalOriginAndCreatedFromList() {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("NORMAL");
        assertEquals(DbConfirmationModalProvider.class,
            ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, user, true, true).getClass());
    }

    @Test
    void shouldReturnNullIfUnsupportedType() {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("NORMAL");
        assertNull(ConfirmationModalProviderResolver.getConfirmation("doi", CertificateStatus.UNSIGNED, user, true, true));
    }

    @ParameterizedTest
    @EnumSource(CertificateStatus.class)
    void testStatusShouldReturnNullOrProvider(CertificateStatus status) {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("NORMAL");
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", status, user, true, true);
        if (status == CertificateStatus.UNSIGNED) {
            assertNotNull(response);
        } else {
            assertNull(response);
        }
    }

    @Test
    void shouldReturnNullIfNormalAndNotNewDraft() {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("NORMAL");
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, user, false, true);
        assertNull(response);
    }

    @Test
    void shouldReturnProviderIfIntegratedAndNotCreatedFromList() {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("DJUPINTEGRATION");
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, user, false, true);
        assertEquals(DbConfirmationModalProvider.class, response.getClass());
    }

    @Test
    void shouldNotReturnProviderIfIntegratedAndNotAllowedToEdit() {
        final var user = mock(WebCertUser.class);
        when(user.getOrigin())
            .thenReturn("DJUPINTEGRATION");
        when(user.isSjfActive())
            .thenReturn(true);
        final var response = ConfirmationModalProviderResolver.getConfirmation("db", CertificateStatus.UNSIGNED, user, false, false);
        assertNull(response);
    }
}