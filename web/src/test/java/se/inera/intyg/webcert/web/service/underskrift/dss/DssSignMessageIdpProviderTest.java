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

package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class DssSignMessageIdpProviderTest {

    public static final String IDENTITY_PROVIDER_FOR_SIGN = "https://idp.url.se/saml/sign/siths-eid-same";
    public static final String IDENTITY_PROVIDER_FOR_SIGN_WHEN_MTLS = "https://idp.url.se/saml/sign";
    public static final String DEFAULT_IDP_URL = "DEFAULT_IDP_URL";
    private DssSignMessageIdpProvider dssSignMessageIdpProvider;

    @Nested
    class UseSameAsAuthTest {

        @BeforeEach
        void setUp() {
            dssSignMessageIdpProvider = new DssSignMessageIdpProvider(
                DEFAULT_IDP_URL,
                true,
                "/saml/sign");
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

        @Test
        void shallLogWarningIfIdentityProviderForSignIsNull(CapturedOutput uatLog) {
            dssSignMessageIdpProvider.get(null);
            assertTrue(
                uatLog
                    .getOut()
                    .contains("IdentityProviderForSign-attribute is missing! Default idp is used instead."),
                () -> String.format("Incorrect log-message: '%s'", uatLog.getOut())
            );
        }

        @Test
        void shallLogWarningIfIdentityProviderForSignIsEmpty(CapturedOutput uatLog) {
            dssSignMessageIdpProvider.get("");
            assertTrue(
                uatLog
                    .getOut()
                    .contains("IdentityProviderForSign-attribute is missing! Default idp is used instead."),
                () -> String.format("Incorrect log-message: '%s'", uatLog.getOut())
            );
        }
    }

    @Nested
    class DontUseSameAsAuthTest {

        @BeforeEach
        void setUp() {
            dssSignMessageIdpProvider = new DssSignMessageIdpProvider(
                DEFAULT_IDP_URL,
                false,
                "/saml/sign");
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

    @Nested
    class UseSameDeviceWhenMtlsTest {

        @BeforeEach
        void setUp() {
            dssSignMessageIdpProvider = new DssSignMessageIdpProvider(
                DEFAULT_IDP_URL,
                true,
                "/saml/sign");
        }

        @Test
        void shallUseDefaultIdpIfIdentityProviderForSignIsMtls() {
            assertEquals(DEFAULT_IDP_URL,
                dssSignMessageIdpProvider.get(IDENTITY_PROVIDER_FOR_SIGN_WHEN_MTLS)
            );
        }

        @Test
        void shallUseIdentityProviderForSignIfNotMtls() {
            assertEquals(IDENTITY_PROVIDER_FOR_SIGN,
                dssSignMessageIdpProvider.get(IDENTITY_PROVIDER_FOR_SIGN)
            );
        }
    }
}