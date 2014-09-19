package se.inera.webcert.spec

import se.inera.webcert.pages.SokSkrivValjIntygTypPage
import se.inera.webcert.pages.SokSkrivaIntygPage

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
                if (typ == "FK7263") {
                    at se.inera.webcert.pages.fk7263.EditCertPage
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditCertPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditCertPage
                }
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
