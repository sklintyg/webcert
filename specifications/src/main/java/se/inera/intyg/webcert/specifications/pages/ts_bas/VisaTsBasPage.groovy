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
