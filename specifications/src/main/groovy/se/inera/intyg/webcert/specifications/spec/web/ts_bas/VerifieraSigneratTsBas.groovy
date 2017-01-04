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

package se.inera.intyg.webcert.specifications.spec.web.ts_bas

import se.inera.intyg.webcert.specifications.pages.ts_bas.VisaTsBasPage
import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.intyg.webcert.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraSigneratTsBas extends ExceptionHandlingFixture {

    public VerifieraSigneratTsBas() {

    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        boolean result
        Browser.drive {
            result = page."$field".isDisplayed()
        }
        result
    }

    String getStringResult(field) {
        String result = ''
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

    String synfaltsdefekter() {
        getStringResult(getCurrentMethodName())
    }

    String nattblindhet() {
        getStringResult(getCurrentMethodName())
    }

    String diplopi() {
        getStringResult(getCurrentMethodName())
    }

    String nystagmus() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgautanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgamedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgakontaktlins() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgautanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgamedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgakontaktlins() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartmedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String korrektionsglasensStyrka() {
        getStringResult(getCurrentMethodName())
    }

    String horselBalansbalansrubbningar() {
        getStringResult(getCurrentMethodName())
    }

    String horselBalanssvartUppfattaSamtal4Meter() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattning() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattningbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattningotillrackligRorelseformaga() {
        getStringResult(getCurrentMethodName())
    }

    String hjartKarlSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String hjarnskadaEfterTrauma() {
        getStringResult(getCurrentMethodName())
    }

    String riskfaktorerStroke() {
        getStringResult(getCurrentMethodName())
    }

    String beskrivningRiskfaktorer() {
        getStringResult(getCurrentMethodName())
    }

    String harDiabetes() {
        getStringResult(getCurrentMethodName())
    }

    String diabetesTyp() {
        getStringResult(getCurrentMethodName())
    }

    String kost() {
        getStringResult(getCurrentMethodName())
    }

    String tabletter() {
        getStringResult(getCurrentMethodName())
    }

    String insulin() {
        getStringResult(getCurrentMethodName())
    }

    String neurologiskSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String medvetandestorning() {
        getStringResult(getCurrentMethodName())
    }

    String medvetandestorningbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattNjurfunktion() {
        getStringResult(getCurrentMethodName())
    }

    String sviktandeKognitivFunktion() {
        getStringResult(getCurrentMethodName())
    }

    String teckenSomnstorningar() {
        getStringResult(getCurrentMethodName())
    }

    String teckenMissbruk() {
        getStringResult(getCurrentMethodName())
    }

    String foremalForVardinsats() {
        getStringResult(getCurrentMethodName())
    }

    String provtagningBehovs() {
        getStringResult(getCurrentMethodName())
    }

    String lakarordineratLakemedelsbruk() {
        getStringResult(getCurrentMethodName())
    }

    String lakemedelOchDos() {
        getStringResult(getCurrentMethodName())
    }

    String psykiskSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String psykiskUtvecklingsstorning() {
        getStringResult(getCurrentMethodName())
    }

    String harSyndrom() {
        getStringResult(getCurrentMethodName())
    }

    String stadigvarandeMedicinering() {
        getStringResult(getCurrentMethodName())
    }

    String medicineringbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String kommentar() {
        getStringResult(getCurrentMethodName())
    }

    String kommentarEjAngivet() {
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

    String lakareSpecialKompetensEjAngivet() {
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
