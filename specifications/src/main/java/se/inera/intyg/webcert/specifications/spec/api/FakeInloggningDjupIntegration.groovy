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
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.URLENC

class FakeInloggningDjupIntegration extends RestClientFixture {
    def resp

    public void execute() {
        def restClient = createRestClient(baseUrl)
        def postBody = 'userJsonDisplay={"fornamn" : "Ivar", "efternamn" : "Integration", "hsaId" : "SE4815162344-1B01", "enhetId" : "SE4815162344-1A02", "lakare" : true,"forskrivarKod": "2481632"}'
        resp = restClient.post(path: "fake", requestContentType: URLENC, body: postBody)
    }

    public String status() {
        if (resp.success) {
            "OK" + " " + header
        } else {
            "FAIL"
        }
    }

}
