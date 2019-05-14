/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/**
 * Defines some constant strings related to Security, SAML or other auth mechanisms.
 *
 * Created by eriklupander on 2015-10-13.
 */
public final class AuthConstants {
    // CHECKSTYLE:OFF LineLength
    public static final String ALIAS_SITHS = "defaultAlias";
    public static final String ALIAS_ELEG = "eleg";

    public static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    public static final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";

    public static final String FAKE_AUTHENTICATION_SITHS_CONTEXT_REF = "urn:inera:webcert:siths:fake";
    public static final String FAKE_AUTHENTICATION_ELEG_CONTEXT_REF = "urn:inera:webcert:eleg:fake";

    public static final String HTTP_ID_SAMBI_SE_LOA_LOA3 = "http://id.sambi.se/loa/loa3";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwofactorContract";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED = "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";

    // CHECKSTYLE:ON LineLength
    private AuthConstants() {
    }

}
