/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.AccessDeniedPage
import se.inera.intyg.webcert.specifications.pages.SokSkrivaIntygPage
import se.inera.intyg.webcert.specifications.pages.UnhandledQAPage
import se.inera.intyg.webcert.specifications.pages.UnsignedIntygPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class Features extends ExceptionHandlingFixture {

    void gaTillSvaraOchFraga() {
        Browser.drive {
            to UnhandledQAPage
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    void gaTillSokSkrivIntyg() {
        Browser.drive {
            to SokSkrivaIntygPage
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    void gaTillUnsignedIntyg() {
        Browser.drive {
            to UnsignedIntygPage
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean accessDeniedSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt AccessDeniedPage
            }
        }
        result
    }

    boolean fragaSvarSynsIMenyn() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt(AbstractLoggedInPage) && page.unhandledQa.isDisplayed()
            }
        }
        result
    }

    boolean omWebcertSynsIMenyn() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt(AbstractLoggedInPage) && page.omWebcert.isDisplayed()
            }
        }
        result
    }

    boolean sokSkrivIntygSynsIMenyn() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt(AbstractLoggedInPage) && page.skrivIntyg.isDisplayed()
            }
        }
        result
    }

    boolean ejSigneradeUtkastSynsIMenyn() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt(AbstractLoggedInPage) && page.unsigned.isDisplayed()
            }
        }
        result
    }

    boolean personnummerSyns() {
        boolean result
        Browser.drive {
            result = page.$('#pnr')?.isDisplayed()
        }
        result
    }

    /*
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
    */

}
