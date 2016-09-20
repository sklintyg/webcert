/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.specifications.spec.api

import static groovyx.net.http.ContentType.JSON
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

public class HamtaIntygSomPdf extends RestClientFixture {

    String intygsId
    String intygsTyp

    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"

    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.get(
                path: "moduleapi/intyg/${intygsTyp}/${intygsId}/pdf",
                requestContentType: JSON
        )
    }
}
