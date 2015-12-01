package se.inera.webcert.spec.web.ts_diabetes

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.webcert.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraUtkastTsDiabetes extends ExceptionHandlingFixture {
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

    String hogerOgaUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.hogerOgaUtanKorrektion.value()
        }
        result
    }

    String hogerOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.hogerOgaMedKorrektion.value()
        }
        result
    }

    String vansterOgaUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.vansterOgaUtanKorrektion.value()
        }
        result
    }

    String vansterOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.vansterOgaMedKorrektion.value()
        }
        result
    }

    String binokulartUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.binokulartUtanKorrektion.value()
        }
        result
    }

    String binokulartMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.binokulartMedKorrektion.value()
        }
        result
    }

    boolean bedomning() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.bedomning.behorighetBedomning.value().toBoolean()
        }
        result
    }


}
