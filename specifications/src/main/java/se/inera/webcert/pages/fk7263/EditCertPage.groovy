package se.inera.webcert.pages.fk7263

import geb.Module
import geb.Page

class EditCertPage extends Page {

    static at = { $("#edit-fk7263").isDisplayed() }

    static content = {

        // Knappar
        sparaKnapp { $("#spara-utkast") }

        // Meddelanden
        intygetSparatMeddelande { $("#intyget-sparat-meddelande") }

        // Formulärfält
        smittskydd { $("#smittskydd") }
        baserasPa { module BaserasPaModule }
        diagnos { module DiagnosModule }
        sjukdomsforlopp { $("#diseaseCause") }
        funktionsnedsattning { $("#disabilities") }
        aktivitetsbegransning { $("#activityLimitation") }
        arbete { module ArbeteModule }
        arbetsformaga { module ArbetsformagaModule }
        arbetsformagaBeskrivning { $("#capacityForWorkText") }
        prognos { module PrognosModule }
        atgardSjukvard { $("#measuresCurrent") }
        atgardAnnan { $("#measuresOther") }
        rekommendationer { module RekommendationerModule }
        kontaktFk { $("#kontaktFk") }
        ovrigt { $("#otherInformation") }
        vardenhet { module VardenhetModule }
    }
}

class BaserasPaModule extends Module {
    static base = { $("#intygetbaseraspa") }
    static content = {
        undersokning { $("#basedOnExamination") }
        undersokningDatum { $("#undersokningAvPatienten-date") }
        telefonkontakt { $("#basedOnPhoneContact") }
        telefonkontaktDatum { $("#telefonkontaktMedPatienten-date") }
        journal { $("#basedOnJournal") }
        journalDatum { $("#journaluppgifter-date") }
        other { $("#basedOnOther") }
        otherDatum { $("#annanReferens-date") }
        otherText { $("#informationBasedOnOtherText") }
    }
}

class DiagnosModule extends Module {
    static base = { $("#diagnoseForm") }
    static content = {
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
        nedsattMed25start { $("#nedsattMed25startdate") }
        nedsattMed25slut { $("#nedsattMed25enddate") }
        nedsattMed25beskrivning { $("#nedsattMed25beskrivning") }
        nedsattMed50 { $("#nedsattMed50") }
        nedsattMed50start { $("#nedsattMed50startdate") }
        nedsattMed50slut { $("#nedsattMed50enddate") }
        nedsattMed50beskrivning { $("#nedsattMed50beskrivning") }
        nedsattMed75 { $("#nedsattMed75") }
        nedsattMed75start { $("#nedsattMed75startdate") }
        nedsattMed75slut { $("#nedsattMed75enddate") }
        nedsattMed75beskrivning { $("#nedsattMed75beskrivning") }
        nedsattMed100 { $("#nedsattMed100") }
        nedsattMed100start { $("#nedsattMed100startdate") }
        nedsattMed100slut { $("#nedsattMed100enddate") }
    }
}

class PrognosModule extends Module {
    static base = { $("#prognosForm") }
    static content = {
        prognos { $("input", name: "capacityForWorkForecast") }
        beskrivning { $("#capacityForWorkForecastText") }
    }

    def valjPrognos(String valdPrognos) {
        if (valdPrognos != null) {
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
}

class RekommendationerModule extends Module {
    static base = { $("#rekommendationerForm") }
    static content = {
        ressatt { $("#rekommendationRessatt") }
        kontaktAf { $("#rekommendationKontaktAf") }
        kontaktForetagshalsovard { $("#rekommendationKontaktForetagshalsovard") }
        ovrigt { $("#rekommendationOvrigt") }
        ovrigtBeskrivning { $("#rekommendationOvrigtBeskrivning") }
        arbetslivsinriktadRehabilitering { $("input", name: "recommendationsToFkReabInQuestion")}
    }

    def valjArbetslivsinriktadRehabilitering(String arAktuell) {
        if (arAktuell != null) {
            def validTypes = ["ja", "nej", "?"];
            assert validTypes.contains(arAktuell),
                    "Fältet 'arbetslivsinriktadRehabilitering' kan endast innehålla något av följande värden: ${validTypes}"

            if ("ja" == arAktuell) {
                arbetslivsinriktadRehabilitering = "JA"
            } else if ("nej" == arAktuell) {
                arbetslivsinriktadRehabilitering = "NEJ"
            } else if ("?" == valdPrognos) {
                arbetslivsinriktadRehabilitering = "GAREJ"
            }
        }
    }
}

class VardenhetModule extends Module {
    static base = { $("#vardenhetForm") }
    static content = {
        postadress { $("#clinicInfoPostalAddress") }
        postnummer { $("#clinicInfoPostalCode") }
        postort { $("#clinicInfoPostalCity") }
        telefonnummer { $("#clinicInfoPhone") }
        epost { $("#clinicInfoEmail") }
    }
}
