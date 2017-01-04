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

package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class TaBortIntyg extends RestClientFixture {

    def restClient = createRestClient()

    String personnr
    String id
    String idTemplate
    int from
    int to

	private String template
	
	public void reset() {
		template = null
	}
	
    public void execute() {
		if (from && to && personnr && !idTemplate) {
			template = "test-${personnr}-intyg-%1\$s"
		} else if (idTemplate) {
			template = idTemplate
		}
        Exception pendingException
        String failedIds = ""
        for (i in from..to) {
            if (template) {
                id = String.format(template, i)
            }
            try {
            restClient.delete(
                    path: 'certificate/' + id,
                    requestContentType: JSON
            )
            } catch(e) {
                failedIds += id + ","
                if (!pendingException) {
                    pendingException = e
                }
            }
        }
        if (pendingException) {
            throw new Exception("Kunde inte ta bort " + failedIds, pendingException)
        }
    }

    public void taBortAllaIntyg() {
        restClient.delete(
            path: 'certificate/',
            requestContentType: JSON
        )
    }
}
