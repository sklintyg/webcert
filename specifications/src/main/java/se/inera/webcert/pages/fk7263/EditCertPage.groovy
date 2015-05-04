package se.inera.webcert.pages.fk7263

import geb.Module
import se.inera.certificate.page.AbstractPage
import se.inera.webcert.pages.AbstractEditCertPage
import se.inera.webcert.pages.VardenhetModule

class EditCertPage extends AbstractEditCertPage {

    static at = { doneLoading() && $("#edit-fk7263").isDisplayed() }

    static content = {

        // Knappar
        sparaKnapp { $("#spara-utkast") }
        tillbakaButton(required: false) { $("#tillbakaButton") }
        visaVadSomSaknasKnapp { $("#showCompleteButton") }
        doljVadSomSaknasKnapp { $("#hideCompleteButton") }

        // Meddelanden
        intygetSparatMeddelande { $("#intyget-sparat-meddelande") }
        intygetEjKomplettMeddelande { $("#intyget-ej-komplett-meddelande") }
        errorPanel { $("#error-panel") }
        nyttPersonnummer { $("#nyttPersonnummer") }
        signerandeLakare { $("#signingDoctor") }
        integrationBorttaget { $("#integration-deleted") }

        // Intyg
        form { $("form") }
        smittskydd { $("#smittskydd") }
        baserasPa { module BaserasPaModule }
        diagnos { module DiagnosModule }
        sjukdomsforlopp { $("#diseaseCause") }
        funktionsnedsattning { $("#disabilities") }
        aktivitetsbegransning { $("#activityLimitation") }
        arbete { module ArbeteModule }
        arbetsformaga { module ArbetsformagaModule }
        arbetsformagaBeskrivning { $("#capacityForWorkText") }
        prognos { name -> module PrognosModule, form: form }
        atgardSjukvard { $("#measuresCurrent") }
        atgardAnnan { $("#measuresOther") }
        rekommendationer { name -> module RekommendationerModule, form: form }
        kontaktFk { $("#kontaktFk") }
        ovrigt { $("#otherInformation") }
        vardenhet { module VardenhetModule }


        // date picker
        datepicker { $("div[ng-switch='datepickerMode']") }

        // Intygsvalidering
        valideringIntygBaseratPa(required: false)        { $("#validationMessages_intygbaseratpa") }
        valideringDiagnos(required: false)               { $("#validationMessages_diagnos") }
        valideringFunktionsnedsattning(required: false)  { $("#validationMessages_funktionsnedsattning") }
        valideringAktivitetsbegransning(required: false) { $("#validationMessages_aktivitetsbegransning") }
        valideringSysselsattning(required: false)        { $("#validationMessages_sysselsattning") }
        valideringArbetsformaga(required: false)         { $("#validationMessages_arbetsformaga") }
        valideringPrognos(required: false)               { $("#validationMessages_prognos") }
        valideringRekommendationer(required: false)      { $("#validationMessages_rekommendationer") }
        valideringVardperson(required: false)            { $("#validationMessages_vardperson") }

    }
    
    def setSmittskydd(boolean val){
        AbstractPage.scrollIntoView(smittskydd.attr("id"));
        smittskydd.value(val);
        waitFor {
            doneLoading()
        }
    }

}

class BaserasPaModule extends Module {
    static base = { $("#intygetbaseraspa") }
    static content = {
        undersokning { $("#basedOnExamination") }
        undersokningDatum { $("#undersokningAvPatientenDate") }
        undersokningDatumToggle { $("#undersokningAvPatientenDate-toggle") }
        telefonkontakt { $("#basedOnPhoneContact") }
        telefonkontaktDatum { $("#telefonkontaktMedPatientenDate") }
        journal { $("#basedOnJournal") }
        journalDatum { $("#journaluppgifterDate") }
        other { $("#basedOnOther") }
        otherDatum { $("#annanReferensDate") }
        otherText { $("#informationBasedOnOtherText") }
    }

    def setUndersokningCheckBox(value){
        AbstractPage.scrollIntoView(undersokning.attr("id"));
        undersokning = value;
    }

    def setTelefonkontaktCheckBox(value){
        AbstractPage.scrollIntoView(telefonkontakt.attr("id"));
        telefonkontakt = value;
    }

    def setJournalCheckBox(value){
        AbstractPage.scrollIntoView(journal.attr("id"));
        journal = value;
    }

    def setOtherCheckBox(value){
        AbstractPage.scrollIntoView(other.attr("id"));
        other = value;
    }

    def setUndersokning(value){
        undersokning.value(value.toBoolean());
    }
}

class DiagnosModule extends Module {
    static base = { $("#diagnoseForm") }
    static content = {
        diagnoseKodverk_ICD_10_SE { $("#diagnoseKodverk_ICD_10_SE") }
        diagnoseKodverk_KSH_97_P { $("#diagnoseKodverk_KSH_97_P") }
        diagnos1 { $("#diagnoseCode") }
        diagnos1Text { $("#diagnoseDescription") }
        diagnos2 { $("#diagnoseCodeOpt1") }
        diagnos2Text { $("#diagnoseDescriptionOpt1") }
        diagnos3 { $("#diagnoseCodeOpt2") }
        diagnos3Text { $("#diagnoseDescriptionOpt2") }
        fortydligande { $("#diagnoseClarification") }
        samsjuklighet { $("#diagnoseMultipleDiagnoses") }
    }
}

class ArbeteModule extends Module {
    static base = { $("#arbeteForm") }
    static content = {
        nuvarande { $("#arbeteNuvarande") }
        arbetsuppgifter { $("#currentWork") }
        arbetslos { $("#arbeteArbetslos") }
        foraldraledig { $("#arbeteForaldraledig") }
    }
}

class ArbetsformagaModule extends Module {
    static base = { $("#arbetsformagaForm") }
    static content = {
        tjanstgoringstid { $("#capacityForWorkActualWorkingHoursPerWeek") }
        nedsattMed25 { $("#nedsattMed25") }
        nedsattMed25start { $("#nedsattMed25from") }
        nedsattMed25slut { $("#nedsattMed25tom") }
        nedsattMed25beskrivning { $("#nedsattMed25beskrivning") }
        nedsattMed50 { $("#nedsattMed50") }
        nedsattMed50start { $("#nedsattMed50from") }
        nedsattMed50slut { $("#nedsattMed50tom") }
        nedsattMed50beskrivning { $("#nedsattMed50beskrivning") }
        nedsattMed75 { $("#nedsattMed75") }
        nedsattMed75start { $("#nedsattMed75from") }
        nedsattMed75slut { $("#nedsattMed75tom") }
        nedsattMed75beskrivning { $("#nedsattMed75beskrivning") }
        nedsattMed100 { $("#nedsattMed100") }
        nedsattMed100start { $("#nedsattMed100from") }
        nedsattMed100slut { $("#nedsattMed100tom") }

        period { $("#totalCertDays") }
        arbetstid25 { $("#arbetstid25") }
        arbetstid50 { $("#arbetstid50") }
        arbetstid75 { $("#arbetstid75") }
    }
}

class PrognosModule extends Module {
    def form;
    static base = { $("#prognosForm") }
    static content = {

        radioGroup { form.capacityForWorkForecast }

        beskrivning { $("#capacityForWorkForecastText") }
        prognos { $("input", name: "capacityForWorkForecast") }

    }

    def valjPrognos(String valdPrognos) {
        if (valdPrognos != null) {
            AbstractPage.scrollIntoView("capacityForWork4");
            def validTypes = ["ja", "delvis", "nej", "?"];
            assert validTypes.contains(valdPrognos),
                    "Fältet 'prognos' kan endast innehålla något av följande värden: ${validTypes}"

            if ("ja" == valdPrognos) {
                prognos = "YES"
            } else if ("delvis" == valdPrognos) {
                prognos = "PARTLY"
            } else if ("nej" == valdPrognos) {
                prognos = "NO"
            } else if ("?" == valdPrognos) {
                prognos = "UNKNOWN"
            }
        }
    }

    def prognosValue(){
        AbstractPage.scrollIntoView("capacityForWork4");
        return prognos.value();
    }
}

class RekommendationerModule extends Module {
    // Toplevel form, directly accessible without locator expression
    def form
    static base = { $("#rekommendationerForm") }
    static content = {

        // Access radio groups directly from form, since locator expressions
        // don't seem to work when multiple radio groups are found inside base scope
        // although according to http://www.gebish.org/manual/current/navigator.html#radio
        // we should be able to directly set a value on the radio group which will change the selection
        // this doesn't seem to work with the form.<group name> syntax
        // as a result I needed to use the input query for arbetslivsinriktadRehabilitering
        // when selecting the radio button group
        arbetslivsinriktadRehabilitering { $("input", name:"recommendationsToFkReabInQuestion") }
        recommendationsToFkTravel { $("input", name:"recommendationsToFkTravel") }
        radioGroupResor { form.recommendationsToFkTravel }
        radioGroupRehab {  form.recommendationsToFkReabInQuestion }

        ressattJa { $("#rekommendationRessatt") }
        ressattNej { $("#rekommendationRessattEj") }
        kontaktAf { $("#rekommendationKontaktAf") }
        kontaktForetagshalsovard { $("#rekommendationKontaktForetagshalsovard") }
        ovrigt { $("#rekommendationOvrigt") }
        ovrigtBeskrivning { $("#rekommendationOvrigtBeskrivning") }
        rehabYes { $("#rehabYes") }
        rehabNo { $("#rehabNo") }
    }

    def valjArbetslivsinriktadRehabilitering(String arAktuell) {
        if (arAktuell != null) {
            AbstractPage.scrollIntoView("rehabYes");
            def validTypes = ["ja", "nej", "?"];
            assert validTypes.contains(arAktuell),
                    "Fältet 'arbetslivsinriktadRehabilitering' kan endast innehålla något av följande värden: ${validTypes}"

            if ("ja" == arAktuell) {
                arbetslivsinriktadRehabilitering = "JA"
            } else if ("nej" == arAktuell) {
                arbetslivsinriktadRehabilitering = "NEJ"
            } else if ("?" == arAktuell) {
                arbetslivsinriktadRehabilitering = "GAREJ"
            }
        }
    }

    def arbetslivsinriktadRehabiliteringValue(){
        return arbetslivsinriktadRehabilitering.value();
    }

    def valjRecommendationsToFkTravel(String arAktuell) {
        if (arAktuell != null) {
            AbstractPage.scrollIntoView("rekommendationRessatt");
            def validTypes = ["ja", "nej"];
            assert validTypes.contains(arAktuell),
                    "Fältet 'RecommendationsToFkTravel' kan endast innehålla något av följande värden: ${validTypes}"

            if ("ja" == arAktuell) {
                recommendationsToFkTravel = "JA"
            } else if ("nej" == arAktuell) {
                recommendationsToFkTravel = "NEJ"
            }
        }
    }

    def radioGroupRehabValue(){
        AbstractPage.scrollIntoView("rehabYes");
        return radioGroupRehab;
    }

    def radioGroupResorValue(){
        AbstractPage.scrollIntoView("rekommendationRessatt");
        return radioGroupResor;
    }

    def recommendationsToFkTravelValue(){
        return recommendationsToFkTravel.value();
    }
}
