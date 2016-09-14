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

package se.inera.intyg.webcert.specifications.spec.web.fk7263

import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class VerifieraSigneratFk7263 extends ExceptionHandlingFixture {

    public VerifieraSigneratFk7263() {
    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        def result = false
        Browser.drive {
            result = page."$field".isDisplayed() && page."$field" != "Ej angivet"
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

    boolean smittskydd() {
        getBooleanResult(getCurrentMethodName())
    }

    String diagnosKod() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosKod2() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning2() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosKod3() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning3() {
        getStringResult(getCurrentMethodName())
    }

    boolean samsjuklighet() {
        getBooleanResult(getCurrentMethodName())
    }

    String sjukdomsforlopp() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattning() {
        getStringResult(getCurrentMethodName())
    }

    boolean baseratPaList() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean undersokningAvPatienten() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean telefonkontaktMedPatienten() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean journaluppgifter() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean annanReferens() {
        getBooleanResult(getCurrentMethodName())
    }

    String annanReferensBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String aktivitetsbegransning() {
        getStringResult(getCurrentMethodName())
    }

    boolean rekommendationKontaktArbetsformedlingen() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean rekommendationKontaktForetagshalsovarden() {
        getBooleanResult(getCurrentMethodName())
    }

    String rekommendationOvrigt() {
        getStringResult(getCurrentMethodName())
    }

    String atgardInomSjukvarden() {
        getStringResult(getCurrentMethodName())
    }

    String annanAtgard() {
        getStringResult(getCurrentMethodName())
    }

    String rehabilitering() {
        def result = ''
        Browser.drive {
            if (page.rehabiliteringAktuell.isDisplayed()) result = "AKTUELL"
            if (page.rehabiliteringEjAktuell.isDisplayed()) result = "EJAKTUELL"
            if (page.rehabiliteringGarInteAttBedoma.isDisplayed()) result = "GARINTEATTBEDOMA"
        }
        result
    }

    String nuvarandeArbetsuppgifter() {
        getStringResult(getCurrentMethodName())
    }

    boolean arbetsloshet() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean foraldrarledighet() {
        getBooleanResult(getCurrentMethodName())
    }

    String nedsattMed25from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed25tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed25Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed100from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed100tom() {
        getStringResult(getCurrentMethodName())
    }

    String prognosBedomning() {
        getStringResult(getCurrentMethodName())
    }

    String arbetsformagaPrognos() {
        getStringResult(getCurrentMethodName())
    }

    String prognos10() {
        def result = ''
        Browser.drive {
            if (page.arbetsformagaPrognosJa.isDisplayed()) result = "JA"
            if (page.arbetsformagaPrognosJaDelvis.isDisplayed()) result = "JADELVIS"
            if (page.arbetsformagaPrognosNej.isDisplayed()) result = "NEJ"
            if (page.arbetsformagaPrognosGarInteAttBedoma.isDisplayed()) result = "GARINTEATTBEDOMA"
        }
        result
    }

    String arbetsformagaPrognosGarInteAttBedomaBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String ressattTillArbeteAktuellt() {
        def result = null
        Browser.drive {
            if (page.ressattTillArbeteAktuellt.isDisplayed()) result = true
            if (page.ressattTillArbeteEjAktuellt.isDisplayed()) result = false
        }
        result
    }

    boolean kontaktMedFk() {
        getBooleanResult(getCurrentMethodName())
    }

    String kommentar() {
        getStringResult(getCurrentMethodName())
    }

    String forskrivarkodOchArbetsplatskod() {
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

    String vardperson_postadress() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_postnummer() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_postort() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_telefonnummer() {
        getStringResult(getCurrentMethodName())
    }
}
