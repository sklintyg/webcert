package se.inera.webcert.spec

import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditCertPage
import se.inera.webcert.pages.fk7263.VisaFk7263Page

class IntegrationMedJournalsystem {

    boolean exists(content) {
        content
    }

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    def visaIntygViaIntegration(String intygId) {
        Browser.drive {
            go "/integration/" + intygId
            waitFor {
                at VisaFk7263Page
            }
        }
    }

    def visaUtkastViaIntegration(String intygId) {
        Browser.drive {
            go "/integration/" + intygId
            waitFor {
                at EditCertPage
            }
        }
    }

    boolean verifieraTillbakaknappBorta() {
        Browser.drive {
            waitFor() {
                !exists(page.tillbakaButton)
            }
        }
        true
    }

    boolean raderaUtkastFelmeddelandeVisas() {
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            waitFor() {
                page.errorPanel.isDisplayed()
            }
        }

        true
    }

    boolean vidarebefordraEjHanteradFragaEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.vidarebefordraEjHanterad)
            }
        }
        true
    }

    boolean webcertLogoEjKlickbar() {
        Browser.drive {
            waitFor() {
                !exists(page.webcertLogoLink)
            }
        }
        true
    }

    boolean huvudmenyEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.huvudmeny)
            }
        }
        true
    }

    boolean bytVardenhetEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.bytVardenhetLink)
            }
        }
        true
    }

    boolean loggaUtEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.loggaUtLink)
            }
        }
        true
    }


    boolean omWebcertKanOppnasViaLank() {
        Browser.drive {
            page.omWebcertLink.click()
            waitFor() {
                page.omWebcertDialog.isDisplayed()
            }
        }
        true
    }
}
