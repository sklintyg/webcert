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

package se.inera.intyg.webcert.specifications.spec

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.VisaFragaSvarPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class IntegrationViaUthoppslank extends ExceptionHandlingFixture {

    boolean exists(content) {
        content
    }

    def gaTillIntygsvyViaUthoppMedIntygsId(String id) {
        Browser.drive {
            go "/webcert/web/user/certificate/${id}/questions"
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    boolean meddelandeIntygetInteSkickatVisas() {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                result = page.certificateIsNotSentToFkMessage.isDisplayed()
            }
        }
        return result
    }

    boolean lamnaFragaSvarVarningVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.qaOnlyDialog.isDisplayed()
            }
        }
        return result
    }

    boolean svaraEjMojligtDialogVisasInte() {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = !page.svaraEjMojligtDialog.isDisplayed()
        }
        return result
    }

    def gaTillEjSigneradeUtkast() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.linkEjSigneradeUtkast.click()
        }
    }

    def gaTillSokSkrivaIntyg() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
                page.linkSokSkrivIntyg.click()
        }
    }

    def avbrytLamnaFragaOchSvar() {
        Browser.drive {
            page.qaOnlyDialogCancel.click()
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

}
