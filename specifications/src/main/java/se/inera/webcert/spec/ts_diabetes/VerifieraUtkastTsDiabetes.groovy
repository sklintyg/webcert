package se.inera.webcert.spec.ts_diabetes

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.ts_diabetes.EditCertPage

class VerifieraUtkastTsDiabetes {
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
                at EditCertPage
            }
            result = page.syn.hogerOgaUtanKorrektion.value()
        }
        result
    }

    String hogerOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            result = page.syn.hogerOgaMedKorrektion.value()
        }
        result
    }

    String vansterOgaUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            result = page.syn.vansterOgaUtanKorrektion.value()
        }
        result
    }

    String vansterOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            result = page.syn.vansterOgaMedKorrektion.value()
        }
        result
    }

    String binokulartUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            result = page.syn.binokulartUtanKorrektion.value()
        }
        result
    }

    String binokulartMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            result = page.syn.binokulartMedKorrektion.value()
        }
        result
    }
}
