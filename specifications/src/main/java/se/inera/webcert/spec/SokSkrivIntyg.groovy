package se.inera.webcert.spec

import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.SokSkrivaIntygPage
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

    boolean enhetsvaljareVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            result = page.careUnitSelector.isDisplayed()
        }
        result == expected
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


    boolean sokSkrivIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
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
}
