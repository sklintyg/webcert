package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.AccessDeniedPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.UnsignedIntygPage

class Features {
    def gaTillSvaraOchFraga(boolean wait = true) {
        Browser.drive {
            go "/web/dashboard#/unhandled-qa"
            if (wait) {
                waitFor {
                    at UnhandledQAPage
                }
            }
        }
    }

    def gaTillSokSkrivIntyg(boolean wait = true) {
        Browser.drive {
            go "/web/dashboard#/create/choose-patient/index"
            if (wait) {
                waitFor {
                    at SokSkrivaIntygPage
                }
            }
        }
    }

    def gaTillUnsignedIntyg(boolean wait = true) {
        Browser.drive {
            go "/web/dashboard#/unsigned"
            if (wait) {
                waitFor {
                    at UnsignedIntygPage
                }
            }
        }
    }

    boolean accessDeniedSidanVisas() {
        Browser.drive {
            waitFor {
                at AccessDeniedPage
            }
        }
    }

    def fragaSvarSynsIMenyn() {
        Browser.drive {
            waitFor {
                page.$('.navbar-nav li a[href="/web/dashboard#/unhandled-qa"]').isDisplayed()
            }
        }
        true
    }

    def omWebcertSynsIMenyn() {
        Browser.drive {
            waitFor {
                page.$('.navbar-nav li a[href="/web/dashboard#/webcert/about"]').isDisplayed()
            }
        }
        true
    }

    def sokSkrivIntygSynsIMenyn() {
        Browser.drive {
            waitFor {
                page.$('.navbar-nav li a[href="/web/dashboard#/create/index"]').isDisplayed()
            }
        }
        true
    }

    def ejSigneradeUtkastSynsIMenyn() {
        Browser.drive {
            waitFor {
                page.$('.navbar-nav li a[href="/web/dashboard#/unsigned"]').isDisplayed()
            }
        }
        true
    }

    def sokSkrivIntygSynsEjIMenyn() {
        Browser.drive {
            waitFor {
                !page.$('.navbar-nav li a[href="/web/dashboard#/create/index"]').isDisplayed()
            }
        }
        true
    }

    def ejSigneradeUtkastSynsEjIMenyn() {
        Browser.drive {
            waitFor {
                !page.$('.navbar-nav li a[href="/web/dashboard#/unsigned"]').isDisplayed()
            }
        }
        true
    }

    def personnummerSynsEj() {
        Browser.drive {
            waitFor {
                !page.$('#pnr').isDisplayed()
            }
        }
        true
    }

    def restUtkast() {
        Browser.drive {
            go "/api/utkast"
            waitFor {
                page.$('body').text() == 'Not available since feature is not active'
            }
        }
        true
    }

    def restModuleUtkastGet() {
        Browser.drive {
            go "/moduleapi/utkast/fk7263/webcert-fitnesse-features-1"
            waitFor {
                page.$('body').text() == 'Not available since feature is not active'
            }
        }
        true
    }

}
