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

package se.inera.intyg.webcert.specifications.spec.web.ts_diabetes

import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.intyg.webcert.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraSigneratTsDiabetes extends ExceptionHandlingFixture {

    public VerifieraSigneratTsDiabetes() {

    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        def result = false
        Browser.drive {
            result = page."$field".isDisplayed()
        }
        result
    }

    String getStringResult(field) {
        def result = ''
        Browser.drive {
            if (!page."$field".isDisplayed()) {
                result = "notshown"
            } else {
                result = page."$field".text()
            }
        }
        result
    }

    String patientpostadress() {
        getStringResult(getCurrentMethodName())
    }

    String patientpostnummer() {
        getStringResult(getCurrentMethodName())
    }

    String patientpostort() {
        getStringResult(getCurrentMethodName())
    }

    String intygAvser() {
        getStringResult(getCurrentMethodName())
    }

    String identitet() {
        getStringResult(getCurrentMethodName())
    }

    String observationsperiod() {
        getStringResult(getCurrentMethodName())
    }

    String diabetestyp() {
        getStringResult(getCurrentMethodName())
    }

    String endastKost() {
        getStringResult(getCurrentMethodName())
    }

    String tabletter() {
        getStringResult(getCurrentMethodName())
    }

    String insulin() {
        getStringResult(getCurrentMethodName())
    }

    String insulinBehandlingsperiod() {
        getStringResult(getCurrentMethodName())
    }

    String annanBehandlingBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String kunskapOmAtgarder() {
        getStringResult(getCurrentMethodName())
    }

    String teckenNedsattHjarnfunktion() {
        getStringResult(getCurrentMethodName())
    }

    String saknarFormagaKannaVarningstecken() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomst() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstTrafiken() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstTrafikBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String egenkontrollBlodsocker() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstVakenTid() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstVakenTidObservationstid() {
        getStringResult(getCurrentMethodName())
    }

    String separatOgonlakarintyg() {
        getStringResult(getCurrentMethodName())
    }

    String synfaltsprovningUtanAnmarkning() {
        getStringResult(getCurrentMethodName())
    }

    String hogerutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogermedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vanstermedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartmedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String diplopi() {
        getStringResult(getCurrentMethodName())
    }

    String lamplighetInnehaBehorighet() {
        getStringResult(getCurrentMethodName())
    }

    String kommentar() {
        getStringResult(getCurrentMethodName())
    }

    String bedomning() {
        getStringResult(getCurrentMethodName())
    }

    String bedomningKanInteTaStallning() {
        getStringResult(getCurrentMethodName())
    }

    String lakareSpecialKompetens() {
        getStringResult(getCurrentMethodName())
    }

    String signeringsdatum() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_namn() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_enhetsnamn() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_postadress() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_postnummer() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_postort() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_telefonnummer() {
        getStringResult(getCurrentMethodName())
    }
}
