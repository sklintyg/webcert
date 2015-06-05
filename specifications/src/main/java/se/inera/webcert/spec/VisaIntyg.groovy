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


    boolean intygLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        return result
    }

    boolean intygInteLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.intygLaddatNoWait.isDisplayed()
        }
        return !result
    }

    boolean skickaKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.skickaKnapp.isDisplayed()
        }
        return result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.skrivUtKnapp.isDisplayed()

        }
        return result
    }

    boolean kopieraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.kopieraKnapp.isDisplayed()

        }
        return result
    }

    boolean kopieraKnappEjVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = !page.kopieraKnappNoWait?.present || !page.kopieraKnappNoWait?.isDisplayed()

        }
        return result
    }

    boolean makuleraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaPage
            }
            result = page.makuleraKnapp.isDisplayed()

        }
        return result
    }

    boolean sekretessmarkeringVisas() {
        def result
        Browser.drive {
            result = page.sekretessmarkering.isDisplayed()
        }
        result
    }

}
