package se.inera.intyg.webcert.specifications.spec.web.ts_bas

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraUtkastTsBas extends ExceptionHandlingFixture {

    boolean kanInteTaStallning() {
        def result
        Browser.drive {
            result = page.behorighetKanInteTaStallning.value()
        }
        result
    }

    String postadress() {
        def result
        Browser.drive {
            result = page.patient.postadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            result = page.patient.postnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            result = page.patient.postort.value()
        }
        result
    }

    String intygetAvser() {
        def result
        Browser.drive {
            result = page.intygetAvser.hamtaBehorigheter()
        }
        result
    }
}
