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
package se.inera.intyg.webcert.web.service.signatur.asn1;

import java.io.InputStream;

/**
 * Interface for accessing data from an ASN.1 container.
 *
 * Created by eriklupander on 2015-09-04, revamped 2017-06-02.
 */
public interface ASN1Util {

    /**
     * Tries to find the value identified by the supplied identifier.
     *
     * @param identifier
     *      X.520 identifier to get value for, typically '2.5.4.5' i.e. serialNumber.
     * @param asn1Signature
     *      Base64-encoded (implementor must decode if necessary) stream of a signature in ASN1.
     * @return
     *      A string value or null if not found within the container.
     */
    String getValue(String identifier, InputStream asn1Signature);
}
