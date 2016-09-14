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

package se.inera.intyg.webcert.specifications.pages

class SokSkrivValjIntygTypPage extends AbstractLoggedInPage {

    static at = { doneLoading() && $("#valj-intyg-typ").isDisplayed() }

    static content = {
        patientNamn { $("#patientNamn") }
        intygtypFortsattKnapp { $("#intygTypeFortsatt") }
        intygTyp { $("#intygType") }
        intygLista { $("#intygLista") }
        kopieraKnapp { intygId -> $("#copyBtn-${intygId}") }
        kopieraDialogVisaInteIgen { $("#dontShowAgain") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        fortsattKnapp { $("#intygTypeFortsatt") }
        felmeddelandeRuta { $("#current-list-noResults-error")}
        sekretessmarkering { $("#sekretessmarkering") }
    }

    def copy(String intygId) {
        kopieraKnapp(intygId).click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    void show(String intygId) {
        $("#showBtn-${intygId}").click()
        waitFor {
            doneLoading()
        }
    }

    def valjIntygstypFk7263() {
        intygTyp.value("string:fk7263")
    }
    def valjIntygstypTsBas() {
        intygTyp.value("string:ts-bas")
    }
    def valjIntygstypTsDiabetes() {
        intygTyp.value("string:ts-diabetes")
    }

    def valjIntygsTyp(String typ) {
        if (typ == "FK7263") {
            valjIntygstypFk7263();
        } else if (typ == "ts-bas") {
            valjIntygstypTsBas();
        } else if (typ == "ts-diabetes") {
            valjIntygstypTsDiabetes();
        }
        fortsattKnapp.click();
        waitFor {
            doneLoading()
        }
    }

}
