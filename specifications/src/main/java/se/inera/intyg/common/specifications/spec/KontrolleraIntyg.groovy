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

package se.inera.intyg.common.specifications.spec

import static groovyx.net.http.ContentType.JSON
import se.inera.intyg.common.specifications.spec.util.RestClientFixture

/**
 * Läs upp ett intyg via intygsId, för att verifiera vårdEnhet, vårdGivare och wireTap-status.
 *
 */
public class KontrolleraIntyg extends RestClientFixture {

    String intyg
    private def certificate
    
    public void execute() {
        def restClient = createRestClient()
        def response = restClient.get(
                path: "certificate/${intyg}",
                requestContentType: JSON
                )
        certificate = response.data
    }
    
    public String personNr() {
        certificate.civicRegistrationNumber
    }

    public String vårdEnhet() {
        certificate.careUnitId
    }

    public String vårdGivare() {
        certificate.careGiverId
    }

    public boolean wiretappat() {
        certificate.wireTapped
    }
}
