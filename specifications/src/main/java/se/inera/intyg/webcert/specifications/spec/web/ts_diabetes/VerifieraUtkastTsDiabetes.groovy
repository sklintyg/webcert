/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.specifications.spec.web.ts_diabetes

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraUtkastTsDiabetes extends ExceptionHandlingFixture {
    String postadress() {
        def result
        Browser.drive {
            result = page.patient.postadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            result = page.patient.postnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            result = page.patient.postort.value()
        }
        result
    }

    String intygetAvser() {
        def result
        Browser.drive {
            result = page.intygetAvser.hamtaBehorigheter()
        }
        result
    }

    String hogerOgaUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.hogerOgaUtanKorrektion.value()
        }
        result
    }

    String hogerOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.hogerOgaMedKorrektion.value()
        }
        result
    }

    String vansterOgaUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.vansterOgaUtanKorrektion.value()
        }
        result
    }

    String vansterOgaMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.vansterOgaMedKorrektion.value()
        }
        result
    }

    String binokulartUtanKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.binokulartUtanKorrektion.value()
        }
        result
    }

    String binokulartMedKorrektion() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.syn.binokulartMedKorrektion.value()
        }
        result
    }

    boolean bedomning() {
        def result
        Browser.drive {
            waitFor {
                at EditeraTsDiabetesPage
            }
            result = page.bedomning.behorighetBedomning.value().toBoolean()
        }
        result
    }


}
