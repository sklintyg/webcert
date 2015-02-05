package se.inera.webcert.spec
import se.inera.webcert.pages.fk7263.VisaFk7263Page

import se.inera.webcert.pages.*

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

    boolean editeraFk7263SidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.fk7263.EditCertPage
            }
        }
    }

    boolean visaIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }

            waitFor {
                page.intygLaddat.isDisplayed()
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
            page.signeraBtn.click()
            sleep(300)
            page.konfirmeraSignera.click()
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

    boolean intygetKomplettMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.intygetSparatMeddelande.isDisplayed()
            }
        }
        true
    }

    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
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
        sleep(300)
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
        sleep(300)
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
        sleep(300)
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
        sleep(300)
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
        sleep(300)
        result
    }

    boolean andraPostadress(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostadress = value
        }
        sleep(300)
        true
    }

    boolean andraPostnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostnummer = value
        }
        sleep(300)
        true
    }

    boolean andraPostort(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostort = value
        }
        sleep(300)
        true
    }

    boolean andraTelefonnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsTelefonnummer = value
        }
        sleep(300)
        true
    }

    boolean andraEpost(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsEpost = value
        }
        sleep(300)
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

    def gaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
            waitFor {
                at VisaFk7263Page
            }
        }
    }
}
