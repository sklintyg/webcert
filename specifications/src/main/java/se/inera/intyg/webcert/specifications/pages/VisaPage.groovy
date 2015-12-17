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

import se.inera.intyg.common.specifications.page.AbstractPage

class VisaPage extends AbstractPage {

    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }

    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat(required: false) { $('#intyg-vy-laddad') }
        intygLaddatNoWait { $('#intyg-vy-laddad') }

        skickaDialogCheck(required: false) { $("#patientSamtycke") }
        skickaDialogSkickaKnapp(required: false) { $("#button1send-dialog") }

        skickaKnapp(required: false) { $("#sendBtn") }
        skrivUtKnapp(required: false) { $("#downloadprint") }
        skrivUtKnappEmployer(required: false){ $("#downloadprintemployer") }
        kopieraKnapp(required: false) { $("#copyBtn") }
        makuleraKnapp(required: false) { $("#makuleraBtn") }
        sekretessmarkering { $("#sekretessmarkering") }

        skickaKnappNoWait { $("#sendBtn") }
        skrivUtKnappNoWait { $("#downloadprint") }
        kopieraKnappNoWait(required: false) { $("#copyBtn") }
        makuleraKnappNoWait { $("#makuleraBtn") }

        kopieraDialogMsgForlangningSjukskrivning(required:false, wait: true) { $("#msgForlangningSjukskrivning") }
        kopieraDialogMsgForlangningSjukskrivningNoWait(required:false) { $("#msgForlangningSjukskrivning") }

    }

    /**
     * Generic send, does not validate content of send dialog body text
     * @return
     */
    def send() {
        skickaKnapp.click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
    }
}
