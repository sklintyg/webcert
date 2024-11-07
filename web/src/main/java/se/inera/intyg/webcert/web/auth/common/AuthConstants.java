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
package se.inera.intyg.webcert.web.auth.common;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Defines some constant strings related to Security, SAML or other auth mechanisms.
 */
public final class AuthConstants {

    public static final String REGISTRATION_ID_ELEG = "eleg";
    public static final String REGISTRATION_ID_SITHS = "siths";
    public static final String REGISTRATION_ID_SITHS_NORMAL = "sithsNormal";

    public static final String ATTRIBUTE_EMPLOYEE_HSA_ID = "http://sambi.se/attributes/1/employeeHsaId";
    public static final String ATTRIBUTE_SUBJECT_SERIAL_NUMBER = "Subject_SerialNumber";
    public static final String ATTRIBUTE_LOGIN_METHOD = "LoginMethod";
    public static final String ATTRIBUTE_IDENTITY_PROVIDER_FOR_SIGN = "urn:identityProviderForSign";
    public static final String ATTRIBUTE_SECURITY_LEVEL_DESCRIPTION = "SecurityLevelDescription";

    public static final String AUTHN_METHOD = "urn:sambi:names:attribute:authnMethod";
    public static final String SAML_2_0_NAMEID_FORMAT_TRANSIENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    public static final String SAML_2_0_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    public static final String NAMESPACE_PREFIX_SAML2P = "saml2p";
    public static final String ELEMENT_LOCAL_NAME_SESSION_INDEX = "SessionIndex";

    public static final String METADATA_LOCATION_STRING_TEMPLATE = "https://%s/saml2/service-provider-metadata/%s";
    public static final String AUTHN_CONTEXT_CLASS_REF_REGEX = "(?<=<saml2:AuthnContextClassRef>).*(?=</saml2:AuthnContextClassRef>)";
    public static final Pattern AUTHN_CONTEXT_CLASS_REF_PATTERN = Pattern.compile(AUTHN_CONTEXT_CLASS_REF_REGEX);

    public static final String FAKE_AUTHENTICATION_SITHS_CONTEXT_REF = "urn:inera:webcert:siths:fake";
    public static final String FAKE_AUTHENTICATION_ELEG_CONTEXT_REF = "urn:inera:webcert:eleg:fake";

    public static final List<String> SITHS_AUTHN_CLASSES = List.of(
        "http://id.sambi.se/loa/loa2",
        "http://id.sambi.se/loa/loa3");

    public static final List<String> ELEG_AUTHN_CLASSES = List.of(
        "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI",
        "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI",
        "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwofactorContract",
        "http://id.elegnamnden.se/loa/1.0/loa3");

    private AuthConstants() {
    }

}
