package se.inera.webcert.spec

import se.inera.webcert.pages.UnsignedIntygPage
import se.inera.webcert.pages.WelcomePage

class OmWebcert {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    def gaTillEjSigneradeIntyg() {
        Browser.drive {
            go "/web/dashboard#/unsigned"
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean ejSigneradeIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

}
