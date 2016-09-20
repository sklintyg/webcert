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

/**
 * @author andreaskaltenbach
 */
class LoggPost extends RestClientFixture {

    def logs
    def counter = -1

    def beginTable() {
        def restClient = createRestClient(logSenderBaseUrl)
        logs = restClient.get(path: "loggtjanst-stub").data
    }

    def execute() {
        counter++
    }

    def systemId() { logs.get(counter).system.systemId }

    def activityType() { logs.get(counter).activity.activityType }

    def purpose() { logs.get(counter).activity.purpose }

    def userId() { logs.get(counter).user.userId }

    def careProvider() { logs.get(counter).user.careProvider.careProviderId }

    def careUnit() { logs.get(counter).user.careUnit.careUnitId }

    def resourceType() { logs.get(counter).resources.resource.get(0).resourceType }

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

    def rensa() {
        def restClient = createRestClient(logSenderBaseUrl)
        restClient.delete(path: "loggtjanst-stub")
    }
}
