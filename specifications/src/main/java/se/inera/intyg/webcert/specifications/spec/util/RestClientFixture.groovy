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

package se.inera.intyg.webcert.specifications.spec.util

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture extends se.inera.intyg.common.specifications.spec.util.RestClientFixture {

    String baseUrl = System.getProperty("webcert.baseUrl")
    String logSenderBaseUrl = System.getProperty("logsender.baseUrl")

    protected void awaitResult(Callable<Boolean> callable, long timeout) {
        await().atMost(timeout, TimeUnit.MILLISECONDS).until(callable)
    }
}
