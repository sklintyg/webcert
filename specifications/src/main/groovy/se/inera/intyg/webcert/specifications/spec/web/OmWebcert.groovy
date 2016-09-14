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
import se.inera.intyg.webcert.specifications.pages.OmWebcertCookiesPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertFAQPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertIntygPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertSupportPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class OmWebcert extends ExceptionHandlingFixture {

    def gaTillOmWebcert() {
        Browser.drive {
            go "/web/dashboard#/webcert/about"
            waitFor {
                at OmWebcertPage
            }
        }
    }

    boolean valjSupport() {
        Browser.drive {
            waitFor {
                page.supportLink.click()
            }
            waitFor {
                at OmWebcertSupportPage
            }
        }
    }

    boolean valjIntygSomStods() {
        Browser.drive {
            waitFor {
                page.intygLink.click()
            }
            waitFor {
                at OmWebcertIntygPage
            }
        }
    }

    boolean valjVanligaFragor() {
        Browser.drive {
            waitFor {
                page.faqLink.click()
            }
            waitFor {
                at OmWebcertFAQPage
            }
        }
    }

    boolean valjOmKakor() {
        Browser.drive {
            waitFor {
                page.cookiesLink.click()
            }
            waitFor {
                at OmWebcertCookiesPage
            }
        }
    }

    boolean avtalsvillkorSynligIMenyn() {
        boolean result = false
        Browser.drive {
            result = page.ppTermsLink?.isDisplayed()
        }
        result
    }
}
