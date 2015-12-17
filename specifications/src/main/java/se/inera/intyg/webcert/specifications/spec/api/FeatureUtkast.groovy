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

package se.inera.intyg.webcert.specifications.spec.api

import groovyx.net.http.HttpResponseException
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

class FeatureUtkast extends RestClientFixture{

    boolean ex
    int statusCode

    def execute() {
        statusCode = 0;
        ex = false
        def client = createRestClient(baseUrl)
        def headers = new HashMap<String,String>()
        headers.put("Cookie","JSESSIONID="+Browser.getJSession())
        try {
            client.get(path: "/api/utkast/", headers:headers)
        }
        catch(HttpResponseException e) {
            statusCode = e.statusCode
            ex = true
        }
    }

    def avstangd() {
        ex
    }

    def statusCode() {
        statusCode
    }

}
