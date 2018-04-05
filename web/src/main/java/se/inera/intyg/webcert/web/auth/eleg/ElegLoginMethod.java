/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.eleg;

/**
 * Defines enumerations for a number of known  (and undocumented)
 *
 * <saml2:Attribute Name="LoginMethod">
 *
 * from the CGI funktionstjänster SAML IdP documentation.
 *
 * Created by eriklupander on 2015-09-23.
 */
public enum ElegLoginMethod {
    /** Legacy NetID production. */
    CCP1,

    /** Legacy NetID test. */
    CCP2,

    /** NetID. */
    CCP8,

    /** BankID. */
    CCP10,

    /** Mobilt BankID. */
    CCP11,

    /** BankID  (Telia, future). */
    CCP12,

    /** Mobilt BankID (Telia, future). */
    CCP13
}
