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

package se.inera.intyg.webcert.specifications.spec.util

import groovyx.net.http.HttpResponseDecorator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

/**
 * Created by eriklupander on 2015-05-26.
 *
 * Use this to perform asynchronous assertions vs external message receivers.
 *
 * Currently used only for async assertions vs Intygstjänsten but should be general-purpose.
 */
class AsyncUtils extends RestClientFixture {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncUtils.class)

    static final String intygstjanstBaseUrl = System.getProperty("certificate.baseUrl")
    static final def intygstjanst = createRestClient(intygstjanstBaseUrl)

    boolean result

    // INTYGSTJANST
    boolean intygFinnsIIntygstjansten(String intygsId, long timeout = 4000L) {
        result = false
        awaitResult(buildIntygExistsConditionCallable(intygsId), timeout)
        result
    }

    boolean intygFinnsMarkeratSomSkickatIIntygstjansten(String intygsId, String recipient, long timeout = 4000L) {
        result = false
        awaitResult(buildIntygExistsAsSentConditionCallable(intygsId, recipient), timeout)
        result
    }

    boolean makuleratIntygFinnsIIntygstjansten(String intygsId, long timeout = 4000L) {
        result = false
        awaitResult(buildMakuleratIntygExistsConditionCallable(intygsId), timeout)
        result
    }



    /** Private scope */
    private Callable<Boolean> buildIntygExistsConditionCallable(String intygsId) {

        return {
            try {
                HttpResponseDecorator response = intygstjanst.get(path : "resources/certificate/${intygsId}")
                result = response.status == 200
            } catch (Exception e) {
                result = false
            }
        }
    }

    private Callable<Boolean> buildMakuleratIntygExistsConditionCallable(String intygsId) {

        return {
            try {
                HttpResponseDecorator response = intygstjanst.get(path : "resources/certificate/${intygsId}")
                result = response.data.revoked
            } catch (Exception e) {
                result = false
            }
        }
    }

    private Callable<Boolean> buildIntygExistsAsSentConditionCallable(String intygsId, String recipient) {
        return {
            try {
                HttpResponseDecorator response = intygstjanst.get(path : "resources/certificate/${intygsId}")
                result = isSentToRecipient(recipient, response.data.states)
                result
            } catch (Exception e) {
                result = false
            }
        }
    }

    private boolean isSentToRecipient(recipient, states) {
        boolean sent = false
        for(state in states) {
            if (state.target.equals(recipient) && state.state.equals('SENT')) {
                sent = true
                break
            }
        }
        sent
    }

}
