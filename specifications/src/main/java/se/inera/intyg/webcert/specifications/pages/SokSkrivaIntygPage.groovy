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

package se.inera.intyg.webcert.specifications.pages

class SokSkrivaIntygPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/create/choose-patient/index"

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer { $("#pnr") }
        personnummerFortsattKnapp { $("#skapapersonnummerfortsatt") }
        puFelmeddelande { $("#puerror") }

        sokSkrivIntygLink(required: false) { $("#menu-skrivintyg") }

        valjIntygTyp(required: false) { $("#valj-intyg-typ") }
    }

    def angePatient(String patient) {
        personnummer = patient
        personnummerFortsattKnapp.click()
        waitFor {
            doneLoading()
        }
    }

}
