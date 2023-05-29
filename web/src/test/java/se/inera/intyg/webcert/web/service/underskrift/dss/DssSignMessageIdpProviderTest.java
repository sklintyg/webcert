/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DssSignMessageIdpProviderTest {

    public static final String IDENTITY_PROVIDER_FOR_SIGN = "IDENTITY_PROVIDER_FOR_SIGN";
    public static final String DEFAULT_IDP_URL = "DEFAULT_IDP_URL";
    private DssSignMessageIdpProvider dssSignMessageIdpProvider;

    @Nested
    class UseSameAsAuthTest {

        @BeforeEach
        void setUp() {
            dssSignMessageIdpProvider = new DssSignMessageIdpProvider(
                DEFAULT_IDP_URL,
                true
            );
        }

        @Test
        void shallUseIdentityProviderForSignIfExists() {
            assertEquals(IDENTITY_PROVIDER_FOR_SIGN,
                dssSignMessageIdpProvider.get(IDENTITY_PROVIDER_FOR_SIGN)
            );
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIsNull() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get(null)
            );
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIsEmpty() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get("")
            );
        }
    }

    @Nested
    class DontUseSameAsAuthTest {

        @BeforeEach
        void setUp() {
            dssSignMessageIdpProvider = new DssSignMessageIdpProvider(
                DEFAULT_IDP_URL,
                false
            );
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIfExists() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get(IDENTITY_PROVIDER_FOR_SIGN)
            );
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIsNull() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get(null)
            );
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIsEmpty() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get("")
            );
        }
    }
}