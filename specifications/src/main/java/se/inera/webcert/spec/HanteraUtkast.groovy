package se.inera.webcert.spec
import se.inera.webcert.pages.EditeraIntygPage
import se.inera.webcert.pages.SokSkrivaIntygPage
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
            Thread.sleep(300);
            waitFor {
                page.konfirmeraRadera.click()
            }
        }
    }

    boolean signeraKnappAktiverad(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected == page.signeraBtn.isEnabled()
            }
        }
        true
    }

    boolean signeraKnappVisas(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected == page.signeraBtn.isDisplayed()
            }
        }
        true
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
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.signRequiresDoctorMessage.isDisplayed()
            }
        }
        true
    }

    boolean intygetSigneratMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.certificateIsSentToITMessage.isDisplayed()
            }
        }
        true
    }

    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
        }
    }

    boolean sokSkrivIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    String postadress() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostort.value()
        }
        result
    }

    String telefonnummer() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsTelefonnummer.value()
        }
        result
    }

    String epost() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsEpost.value()
        }
        result
    }

    boolean andraPostadress(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostadress = value
        }
        true
    }

    boolean andraPostnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostnummer = value
        }
        true
    }

    boolean andraPostort(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostort = value
        }
        true
    }

    boolean andraTelefonnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsTelefonnummer = value
        }
        true
    }

    boolean andraEpost(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsEpost = value
        }
        true
    }

    boolean sparaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                page.sparaBtn.click()
            }
        }
    }

    boolean wait4it() {
        Thread.sleep(5000)
        true
    }
}
