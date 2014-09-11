package se.inera.webcert.spec

import se.inera.webcert.pages.SokSkrivValjIntygTypPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.ts_bas.EditCertPage

class SkrivIntyg {

    def intygsid

    def skapaNyttIntygsutkastForPatientAvTyp(String patient, String typ) {
        Browser.drive {

            waitFor {
                at SokSkrivaIntygPage
            }

            page.personnummer = patient
            page.personnummerFortsattKnapp.click()

            waitFor {
                at SokSkrivValjIntygTypPage
            }

            if (typ == "FK7263") {
                page.valjIntygstypFk7263();
            } else if (typ == "ts-bas") {
                page.valjIntygstypTsBas();
            } else if (typ == "ts-diabetes") {
                page.valjIntygstypTsDiabetes();
            }

            page.fortsattKnapp.click();

            waitFor {
                at EditCertPage
            }

            intygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
        }
    }

    String intygsid() {
        intygsid
    }

    def sparaUtkast() {
        Browser.drive {
            page.sparaKnapp.click()
        }
    }

    boolean intygSparatVisas() {
        Browser.drive {
            waitFor {
                page.intygetSparatMeddelande.isDisplayed()
            }
        }
        true
    }
}
