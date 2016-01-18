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

package se.inera.intyg.webcert.specifications.spec.certificate_sender

import se.inera.intyg.webcert.specifications.spec.util.AsyncUtils
import se.inera.intyg.webcert.specifications.spec.util.AsyncUtilsITStub
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

/**
 * Specifically used for asserting that messages sent asynchronously from Webcert has (or has not) been processed
 * by Intygstjansten.
 */
class IntygstjanstAsynk {

    /**
     * Returns true if the Intyg identified by the specified ID exists in Intygstjänsten. Will wait up to ${timeout} ms.
     */
    boolean finnsIntygIIntygstjanstenMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtils().intygFinnsIIntygstjansten(intygsId, vantetid)
    }

    /**
     * Returns true if the Intyg identified by the specified ID exists in Intygstjänsten with state SENT targeting the
     * specified mottagare. Will wait up to ${timeout} ms.
     */
    boolean finnsIntygIIntygstjanstenSkickadTillMedVantetid(String intygsId, String mottagare, long vantetid = 4000) {
        def result = false
        result = new AsyncUtils().intygFinnsMarkeratSomSkickatIIntygstjansten(intygsId, mottagare, vantetid)
    }

    /**
     * Returns true if the Intyg identified by the specified ID exists in Intygstjänsten with makulerat == true.
     * Will wait up to ${timeout} ms.
     */
    boolean finnsMakuleratIntygIIntygstjanstenMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtils().makuleratIntygFinnsIIntygstjansten(intygsId, vantetid)
    }



    boolean finnsIntygIStubMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtilsITStub().intygFinnsIStub(intygsId, vantetid)
    }


    boolean finnsIntygIStubSkickadTillMedVantetid(String intygsId, String mottagare, long vantetid = 4000) {
        def result = false
        result = new AsyncUtilsITStub().intygFinnsMarkeratSomSkickatIStub(intygsId, mottagare, vantetid)
    }


    boolean finnsMakuleratIntygIStubMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtilsITStub().makuleratIntygFinnsIStub(intygsId, vantetid)
    }



    boolean resetIntygtjanstStub() {
        def result = false
        result = WebcertRestUtils.resetIntygtjanstStub()
    }
}
