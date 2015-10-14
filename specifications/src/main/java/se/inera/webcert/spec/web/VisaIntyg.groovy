package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.VisaIntygPage

class VisaIntyg {

    def visaIntygMedIdOchTyp(String id, String intygstyp) {
        Browser.drive {
            go "/web/dashboard#/intyg/${intygstyp}/${id}"
            waitFor {
                at VisaIntygPage
            }
        }
    }


    boolean intygLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        return result
    }

    boolean intygInteLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        return !result
    }

    boolean skickaKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.skickaKnapp.isDisplayed()
        }
        return result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = (page.skrivUtKnapp?.present && page.skrivUtKnapp.isDisplayed()) || (page.skrivUtKnappEmployer?.present && page.skrivUtKnappEmployer.isDisplayed())

        }
        return result
    }

    boolean skrivUtVanligKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.skrivUtKnapp.isDisplayed()
        }
        return result
    }

    boolean skrivUtArbetsgivarutskriftKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.skrivUtKnappEmployer.isDisplayed()
        }
        return result
    }

    boolean kopieraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.kopieraKnapp.isDisplayed()

        }
        return result
    }

    boolean kopieraKnappEjVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = !page.kopieraKnappNoWait?.present || !page.kopieraKnappNoWait?.isDisplayed()

        }
        return result
    }

    boolean makuleraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
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
