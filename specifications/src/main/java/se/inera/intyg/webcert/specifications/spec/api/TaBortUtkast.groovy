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

class TaBortUtkast extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}testability/")
    
    String utkastId
    String enhetsId
	def response;
	
	public String respons(){
		return response.status;
	}
    
    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (utkastId) taBortUtkast(utkastId)
        if (enhetsId) taBortUtkastForEnhet(enhetsId)
    }

    def taBortAllaUtkast() {
        response = restClient.delete(path: "intyg/")
    }
    
    def taBortUtkast(String utkastId) {
        response = restClient.delete(path: "intyg/${utkastId}")
    }
    
    def taBortUtkastForEnhet(String enhetsId) {
        response = restClient.delete(path: "intyg/enhet/${enhetsId}")
    }
    
}
