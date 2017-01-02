/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
 * Convenience helper for checking if a given bit is set on a byte, or unsetting a bit at a given position.
 *
 * Used by {@link ASN1StreamParser} to parse the length octet.
 *
 * Created by eriklupander on 2015-10-15.
 */
public final class BitHelper {

    private BitHelper() {
    }

    public static boolean isSet(byte value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    public static byte unset(byte value, int pos) {
        return (byte) (value & ~(1 << pos));
    }

}
