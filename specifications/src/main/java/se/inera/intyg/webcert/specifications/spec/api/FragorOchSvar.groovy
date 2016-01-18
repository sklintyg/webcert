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

import groovyx.net.http.RESTClient
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class FragorOchSvar extends RestClientFixture {

    String internReferens
    String externReferens
    def fragaSvar

    public void execute() {
        def restClient = createRestClient("${baseUrl}testability/")

        if (internReferens)
            fragaSvar = restClient.get(path: "questions/${internReferens}").data

        if (externReferens)
            fragaSvar = restClient.get(path: "questions/extern/${externReferens}").data
    }

    def finns() {
        return fragaSvar != null
    }

    def internId() {
        fragaSvar.internReferens
    }

    def fraga() {
        fragaSvar.frageText
    }

    def svar() {
        fragaSvar.svarsText
    }

    def status() {
        fragaSvar.status
    }

    def amne() {
        fragaSvar.amne
    }
}
