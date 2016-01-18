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

package se.inera.intyg.webcert.specifications.pages.ts_bas
import se.inera.intyg.webcert.specifications.pages.AbstractViewCertPage

class VisaTsBasPage extends AbstractViewCertPage {

    static content = {

        skickaDialogBodyTsBas { $("span[key=\"ts-bas.label.send.body\"]") }

        patientpostadress { $("#patientpostadress") }
        patientpostnummer { $("#patientpostnummer") }
        patientpostort { $("#patientpostort") }
        intygAvser { $("#intygAvser") }
        identitet { $("#identitet ") }
        synfaltsdefekter { $("#synfaltsdefekter") }
        nattblindhet { $("#nattblindhet ") }
        diplopi { $("#diplopi") }
        nystagmus { $("#nystagmus") }
        hogerOgautanKorrektion { $("#hogerOgautanKorrektion") }
        hogerOgamedKorrektion { $("#hogerOgamedKorrektion") }
        hogerOgakontaktlins { $("#hogerOgakontaktlins") }
        vansterOgautanKorrektion { $("#vansterOgautanKorrektion") }
        vansterOgamedKorrektion { $("#vansterOgamedKorrektion") }
        vansterOgakontaktlins { $("#vansterOgakontaktlins") }
        binokulartutanKorrektion { $("#binokulartutanKorrektion") }
        binokulartmedKorrektion { $("#binokulartmedKorrektion") }
        korrektionsglasensStyrka { $("#korrektionsglasensStyrka") }
        horselBalansbalansrubbningar { $("#horselBalansbalansrubbningar") }
        horselBalanssvartUppfattaSamtal4Meter { $("#horselBalanssvartUppfattaSamtal4Meter") }
        funktionsnedsattning { $("#funktionsnedsattning ") }
        funktionsnedsattningbeskrivning { $("#funktionsnedsattningbeskrivning") }
        funktionsnedsattningotillrackligRorelseformaga { $("#funktionsnedsattningotillrackligRorelseformaga") }
        hjartKarlSjukdom { $("#hjartKarlSjukdom") }
        hjarnskadaEfterTrauma { $("#hjarnskadaEfterTrauma") }
        riskfaktorerStroke { $("#riskfaktorerStroke") }
        beskrivningRiskfaktorer { $("#beskrivningRiskfaktorer") }
        harDiabetes { $("#harDiabetes") }
        diabetesTyp { $("#diabetesTyp") }
        kost { $("#kost") }
        tabletter { $("#tabletter") }
        insulin { $("#insulin") }
        neurologiskSjukdom { $("#neurologiskSjukdom") }
        medvetandestorning { $("#medvetandestorning") }
        medvetandestorningbeskrivning { $("#medvetandestorningbeskrivning") }
        nedsattNjurfunktion { $("#nedsattNjurfunktion") }
        sviktandeKognitivFunktion { $("#sviktandeKognitivFunktion") }
        teckenSomnstorningar { $("#teckenSomnstorningar") }
        teckenMissbruk { $("#teckenMissbruk") }
        foremalForVardinsats { $("#foremalForVardinsats") }
        provtagningBehovs { $("#provtagningBehovs") }
        lakarordineratLakemedelsbruk { $("#lakarordineratLakemedelsbruk") }
        lakemedelOchDos { $("#lakemedelOchDos") }
        psykiskSjukdom { $("#psykiskSjukdom") }
        psykiskUtvecklingsstorning { $("#psykiskUtvecklingsstorning") }
        harSyndrom { $("#harSyndrom") }
        stadigvarandeMedicinering { $("#stadigvarandeMedicinering") }
        medicineringbeskrivning { $("#medicineringbeskrivning") }
        kommentar { $("#kommentar") }
        kommentarEjAngivet { $("#kommentarEjAngivet") }
        bedomning { $("#bedomning") }
        bedomningKanInteTaStallning { $("#bedomningKanInteTaStallning") }
        lakareSpecialKompetens { $("#lakareSpecialKompetens") }
        lakareSpecialKompetensEjAngivet { $("#lakareSpecialKompetensEjAngivet") }
        signeringsdatum { $("#signeringsdatum") }
        vardperson_namn { $("#vardperson_namn") }
        vardperson_enhetsnamn { $("#vardperson_enhetsnamn") }
        vardenhet_postadress { $("#vardenhet_postadress") }
        vardenhet_postnummer { $("#vardenhet_postnummer") }
        vardenhet_postort { $("#vardenhet_postort") }
        vardenhet_telefonnummer { $("#vardenhet_telefonnummer") }
    }

    def sendWithValidation() {
        skickaKnapp.click()
        waitFor {
            doneLoading()
            skickaDialogBodyTsBas.text().trim().equals("")
            skickaDialogCheck.isDisplayed()
        }
        skickaDialogCheck.click()
        waitFor {
            skickaDialogSkickaKnapp.isEnabled()
        }
        skickaDialogSkickaKnapp.click()
    }
}
