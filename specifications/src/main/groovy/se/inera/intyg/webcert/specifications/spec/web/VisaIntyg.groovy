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

import se.inera.intyg.webcert.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.VisaIntygPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VisaIntyg extends ExceptionHandlingFixture {

    def visaIntygMedIdOchTyp(String id, String intygstyp) {
        Browser.drive {
            go "/web/dashboard#/intyg/${intygstyp}/${id}"
            waitFor {
                at VisaIntygPage
            }
        }
    }

    boolean intygLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        return result
    }

    boolean intygInteLaddat() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        return !result
    }

    boolean skickaKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.skickaKnapp.isDisplayed()
        }
        return result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = (page.skrivUtKnapp?.present && page.skrivUtKnapp.isDisplayed()) || (page.skrivUtKnappEmployer?.present && page.skrivUtKnappEmployer.isDisplayed())

        }
        return result
    }

    boolean skrivUtVanligKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.skrivUtKnapp.isDisplayed()
        }
        return result
    }

    boolean kopieraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.kopieraKnapp.isDisplayed()

        }
        return result
    }

    boolean kopieraKnappEjVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = !page.kopieraKnappNoWait?.present || !page.kopieraKnappNoWait?.isDisplayed()

        }
        return result
    }

    boolean makuleraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaIntygPage
            }
            result = page.makuleraKnapp.isDisplayed()

        }
        return result
    }

    boolean sekretessmarkeringVisas() {
        def result
        Browser.drive {
            result = page.sekretessmarkering.isDisplayed()
        }
        result
    }

}
