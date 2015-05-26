package se.inera.webcert.spec

import se.inera.webcert.spec.util.AsyncUtils

class IntygstjanstAsynk {

    boolean finnsIntygIIntygstjanstenMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtils().intygFinnsIIntygstjansten(intygsId, vantetid)
    }

    boolean finnsIntygIIntygstjanstenSkickadTillSkickadTillMedVantetid(String intygsId, String mottagare, long vantetid = 4000) {
        def result = false
        result = new AsyncUtils().intygFinnsMarkeratSomSkickatIIntygstjansten(intygsId, mottagare, vantetid)
    }

    boolean finnsMakuleratIntygIIntygstjanstenMedVantetid(String intygsId, long vantetid = 4000L) {
        def result = false
        result = new AsyncUtils().makuleratIntygFinnsIIntygstjansten(intygsId, vantetid)
    }
}
