package se.inera.webcert.spec
import se.inera.webcert.pages.EditeraIntygPage
import se.inera.webcert.pages.UnsignedIntygPage
import se.inera.webcert.pages.VisaIntygPage
import se.inera.webcert.pages.WelcomePage

class HanteraUtkast {

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

    boolean ingaEjSigneradeIntygVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            waitFor {
                page.ingaEjSigneradeIntyg.isDisplayed()
            }
        }
    }

    def gaTillEditeraIntygMedTypOchIntygid(String typ, String intygid) {
        Browser.drive {
            go "/web/dashboard#/$typ/edit/$intygid"
        }
    }

    boolean editeraSidanVisas() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
        }
    }

    boolean raderaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                page.radera.click()
            }
            waitFor {
                page.konfirmeraRadera.click()
            }
        }
    }

    boolean signeraKnappAktiverad(boolean expected = true) {
        def result = false
        Browser.drive {
            at EditeraIntygPage
            result = page.signeraBtn.isEnabled()
        }
        result == expected
    }

    boolean signeraKnappVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            at EditeraIntygPage
            result = page.signeraBtn.isDisplayed()
        }
        result == expected
    }

    boolean signeraUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                page.signeraBtn.click()
            }
            waitFor {
                page.konfirmeraSignera.click()
            }
        }
    }

    boolean signeringKraverLakareVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            asType EditeraIntygPage
            result = page.signRequiresDoctorMessage.isDisplayed()
        }
        result == expected
    }

    boolean intygetSigneratMeddelandeVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            asType EditeraIntygPage
            result = page.certificateIsSentToITMessage.isDisplayed()
        }
        result == expected
    }

    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
        }
    }

}
