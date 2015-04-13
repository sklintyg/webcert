package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.VisaPage

class VisaIntyg {

    def visaIntygMedIdOchTyp(String id, String intygstyp) {
        Browser.drive {
            go "/web/dashboard#/intyg/${intygstyp}/${id}"
            waitFor {
                at VisaPage
            }
        }
    }


    def skickaVisatIntyg() {
        Browser.drive {
            waitFor {
                at VisaPage
                page.skickKnapp.click()
            }
        }
    }

    boolean intygLaddat(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaPage
                page.intygLaddat(expected)
            }
        }
    }

    boolean skickaKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.skickaKnapp.isDisplayed() == expected
            }
        }
        true
    }

    boolean skrivUtKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.skrivUtKnapp.isDisplayed() == expected
            }
        }
        true
    }

    boolean kopieraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.kopieraKnapp.isDisplayed() == expected
            }
        }
    }

    boolean makuleraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.makuleraKnapp.isDisplayed() == expected
            }
        }
    }
}
