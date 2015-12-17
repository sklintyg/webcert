/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.signatur.asn1;

/**
 * Defines a limited set of ASN.1 byte identifiers.
 *
 * Created by eriklupander on 2015-09-01.
 */
public final class ASN1Type {

    public static final byte SET = 0x31;
    public static final byte SEQUENCE = 0x30;
    public static final byte OBJECT_IDENTIFIER = 0x06;
    public static final byte UTF8_STRING = 0x0C;
    public static final byte PRINTABLE_STRING = 0x13;

    private ASN1Type() {
    }

}
