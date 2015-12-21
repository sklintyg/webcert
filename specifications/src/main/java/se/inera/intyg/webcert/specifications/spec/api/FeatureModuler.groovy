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

import static groovyx.net.http.ContentType.JSON

class FeatureModuler extends RestClientFixture{

    String typ
    String intygStatus
    String verb
    String metod

    boolean ex
    int status

    def execute() {
        status = 0;
        ex = false

        def client = createRestClient(baseUrl)

        def headers = new HashMap<String,String>()
        headers.put("Cookie","JSESSIONID="+Browser.getJSession())

        def intygsId = "webcert-fitnesse-features-1"
        if (intygStatus.equals("intyg")) {
            if (typ.equals("fk7263"))
                intygsId = "intyg-fit-1"
            else if (typ.equals("ts-bas"))
                intygsId = "intyg-fit-4"
            else if (typ.equals("ts-diabetes"))
                intygsId = "intyg-fit-5"
        }

        def path = "/moduleapi/"+intygStatus+"/"+typ+"/"+intygsId+(metod?"/"+metod:"");

        try {
            if (verb.equals("PUT"))
                client.put(path: path, requestContentType: JSON, headers: headers, body: "{}")
            else if (verb.equals("POST"))
                client.post(path: path, requestContentType: JSON, headers: headers, body: "{}")
            else if (verb.equals("DELETE"))
                client.delete(path: path, headers: headers)
            else
                client.get(path: path, headers: headers)
        }
        catch(HttpResponseException e) {
            status = e.statusCode
            if (status == 403) {
                ex = true
            }
        }
    }

    def avstangd() {
        ex
    }

    def status() {
        status
    }

}
