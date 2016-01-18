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

package se.inera.intyg.webcert.specifications.spec.notification_sender
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

class SparaUtkast {

    String intygId
    String intygTyp
    long   version

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"

    // hosperson
    String hsaId
    String namn = "enLäkare"

    // Enhet
    String enhetId = "SE4815162344-1A02"
    String enhetnamn = "WebCert-Integration Enhet 1"
    String telefonnummer = "123456789"
    String postadress = "Storgatan 12"
    String postnummer = "12345"
    String postort = "Ankeborg"
    String arbetsplatskod = "arbetsplatskod"
    String epost = "enhet1@webcert.se.invalid"

    // vardgivare
    String vardgivarId = "SE4815162344-1A01"
    String vardgivarnamn = "WebCert-Integration Vårdgivare 1"

    // Diagnos
    String diagnosKod = "S50"
    String diagnosBeskrivning1 = "Kontusion på armbåge"
    String diagnosBeskrivning = "Skada underarm"
    
    String nedsattMed100 = null

    boolean komplett = false
    String kodverk = "ICD_10_SE"

    def response
    def json

    public void execute() {
        WebcertRestUtils.login()
        if (komplett) {
            makeComplete()
        } else {
            makeIncomplete()
        }
        response = WebcertRestUtils.saveUtkast(intygTyp, intygId, version, json)
    }

    public boolean utkastSparat() {
        response.success
    }

    public long version() {
        response.data.version
    }

    // Fill utkast and set it up as 'complete'
    def makeComplete() {
        if ("fk7263" == intygTyp) {
            String diagnos = """"diagnosKodsystem1":"$kodverk","diagnosKod":"$diagnosKod","diagnosBeskrivning1":"$diagnosBeskrivning1","diagnosBeskrivning":"$diagnosBeskrivning"""
            String nedsatt = nedsattMed100 != null ? "${nedsattMed100}," : ""
            json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", $diagnos", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare",${nedsatt}"prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
        }
    }

    def makeIncomplete() {
        if ("fk7263" == intygTyp) {
            json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare","prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
        }
    }
}
