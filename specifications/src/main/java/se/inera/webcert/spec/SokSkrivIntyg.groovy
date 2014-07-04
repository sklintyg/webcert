package se.inera.webcert.spec

import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.SokSkrivFyllINamnPage
import se.inera.webcert.pages.SokSkrivValjIntygTypPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.VisaIntygPage
import se.inera.webcert.pages.WelcomePage

class SokSkrivIntyg {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    boolean sokSkrivIntygSidanVisas() {
        sokSkrivIntygSidanVisasSaSmaningom()
    }

    boolean sokSkrivIntygSidanVisasSaSmaningom() {
        Browser.drive {
            waitFor (60, 10) {
                at SokSkrivaIntygPage
            }
        }
    }

    def valjPatient(String personNummer) {
        Browser.drive {
            page.personnummer = personNummer
            page.personnummerFortsattKnapp.click()
        }
    }

    boolean fyllINamnSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivFyllINamnPage
            }
        }
    }

    def gePatientFornamnEfternamn(String fornamn, String efternamn) {
        Browser.drive {
            page.fornamn = fornamn
            page.efternamn = efternamn
            page.namnFortsattKnapp.click()
        }
    }

    boolean valjIntygstypSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
        }
    }

    boolean patientensNamnAr(String expected) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                expected == page.patientNamn.text()
            }
        }
        true
    }

    boolean kopieraKnappVisasForIntyg(boolean expected = true, String intygId) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                expected == page.copyBtn(intygId).isDisplayed()
            }
        }
        true
    }

    def kopieraTidigareIntyg(String intygId) {
        Browser.drive {
            waitFor {
                page.copyBtn(intygId).isDisplayed()
            }
            page.copy(intygId)
        }
    }

    def skickaVisatIntyg() {
        Browser.drive {
            page.send()
        }
    }

    def kopieraVisatIntyg() {
        Browser.drive {
            page.copy()
        }
    }

    def visaIntyg(String intygId) {
        Browser.drive {
            waitFor {
                page.intygLista.isDisplayed()
            }

            page.show(intygId)
        }
    }

    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }

            waitFor {
                page.intygVy.isDisplayed()
            }
        }
    }

    public void loggaInIndex() {
        Browser.drive {
            waitFor {
                at IndexPage
            }
            page.startLogin()
        }
    }

    boolean enhetsvaljareVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            waitFor {
                expected == page.careUnitSelector.isDisplayed()
            }
        }
        true
    }

    def valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            waitFor {
                page.careUnitSelector.click()
            }
            waitFor {
                page.selectCareUnit(careUnit);
            }
        }
    }
}
