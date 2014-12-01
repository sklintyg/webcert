package se.inera.webcert.spec
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

    boolean intygLaddat(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaPage
                page.intygLaddat(expected)
            }
        }
    }
}
