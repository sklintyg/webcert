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

class TaBortFraga extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}testability/")

    String internReferens
    String externReferens
    String frageText
    String svarsText
    def response;


    public String respons() {
        return response.status;
    }

    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (internReferens) taBortFragaMedInternReferens(internReferens)
        if (externReferens) taBortFragaMedExternReferens(externReferens)
        if (frageText) taBortFragaMedFrageText(frageText)
        if (svarsText) taBortFragaMedSvarsText(svarsText)
    }

    def taBortFragaMedExternReferens(String externReferens) {
        response = restClient.delete(path: "fragasvar/extern/${externReferens}")
    }

    def taBortFragaMedInternReferens(String internReferens) {
        response = restClient.delete(path: "fragasvar/${internReferens}")
    }

    def taBortFragaMedFrageText(String frageText) {
        response = restClient.delete(path: "fragasvar/frageText/${frageText}")
    }

    def taBortFragaMedSvarsText(String svarsText) {
        response = restClient.delete(path: "fragasvar/svarsText/${svarsText}")
    }

    def taBortFragorForEnhet(String enhetsId) {
        response = restClient.delete(path: "fragasvar/enhet/${enhetsId}")
    }

    def taBortAllaFragor() {
        response = restClient.delete(path: "fragasvar/")
    }
}
