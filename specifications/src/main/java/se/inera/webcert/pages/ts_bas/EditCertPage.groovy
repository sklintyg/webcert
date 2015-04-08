package se.inera.webcert.pages.ts_bas

import geb.Module
import se.inera.certificate.page.AbstractPage

class EditCertPage extends AbstractPage {

    static at = { doneLoading() && $("#edit-ts-bas").isDisplayed() }

    static content = {

        // Knappar
        sparaKnapp { $("#spara-utkast") }
        visaVadSomSaknasKnapp { $("#showCompleteButton") }
        doljVadSomSaknasKnapp { $("#hideCompleteButton") }

        // Meddelanden
        intygetSparatMeddelande { $("#intyget-sparat-meddelande") }
        intygetEjKomplettMeddelande { $("#intyget-ej-komplett-meddelande") }

        // Formulärfält
        patient { module PatientModule }
        intygetAvser { module IntygetAvserModule }
        identitet { module IdentitetModule }
        syn { module SynModule }
        horselBalans { module HorselBalansModule }
        funktionsnedsattning { module FunktionsnedsattningModule }
        hjartkarl { module HjartkartModule }
        diabetes { module DiabetesModule }
        neurologi { module NeurologiModule }
        medvetandestorning { module MedvetandestorningModule }
        njurar { module NjurarModule }
        kognitivt { module KognitivtModule }
        somnvakenhet { module SomnvakenhetModule }
        narkotikaLakemedel { module NarkotikaLakemedelModule }
        psykiskt { module PsykisktModule }
        utvecklingsstorning { module UtvecklingsstorningModule }
        sjukhusvard { module SjukhusvardModule }
        medicinering { module MedicineringModule }
        kommentar { $("#kommentar") }
        bedomning { module BedomningModule }
        vardenhet { module VardenhetModule }

        // Intygsvalidering
        valideringPatient(required: false)              { $("#validationMessages_patient") }
        valideringIntygAvser(required: false)           { $("#validationMessages_intygavser") }
        valideringIdentitet(required: false)            { $("#validationMessages_identitet") }
        valideringSyn(required: false)                  { $("#validationMessages_syn") }
        valideringHorselBalans(required: false)         { $("#validationMessages_horselbalans") }
        valideringFunktionsNedsattning(required: false) { $("#validationMessages_funktionsnedsattning") }
        valideringHjartkarl(required: false)            { $("#validationMessages_hjartkarl") }
        valideringNeurologi(required: false)            { $("#validationMessages_neurologi") }
        valideringMedvetandestorning(required: false)   { $("#validationMessages_medvetandestorning") }
        valideringNjurar(required: false)               { $("#validationMessages_njurar") }
        valideringKognitivt(required: false)            { $("#validationMessages_kognitivt") }
        valideringSomnVakenhet(required: false)         { $("#validationMessages_somnvakenhet") }
        valideringNarkotikaLakemedel(required: false)   { $("#validationMessages_narkotikalakemedel") }
        valideringPsykiskt(required: false)             { $("#validationMessages_psykiskt") }
        valideringUtvecklingsStorning(required: false)  { $("#validationMessages_utvecklingsstorning") }
        valideringSjukhusVard(required: false)          { $("#validationMessages_sjukhusvard") }
        valideringMedicinering(required: false)         { $("#validationMessages_medicinering") }
        valideringBedomning(required: false)            { $("#validationMessages_bedomning") }
        valideringVardEnhet(required: false)            { $("#validationMessages_vardenhet") }
    }
}

class PatientModule extends Module {
    static base = { $("#patientForm") }
    static content = {
        postadress { $("#patientPostadress") }
        postnummer { $("#patientPostnummer") }
        postort { $("#patientPostort") }
    }
}

class IntygetAvserModule extends Module {
    static base = { $("#intygetAvserForm") }
    static content = {
        c1 { $("#typcheck0") }
        c1e { $("#typcheck1") }
        c { $("#typcheck2") }
        ce { $("#typcheck3") }
        d1 { $("#typcheck4") }
        d1e { $("#typcheck5") }
        d { $("#typcheck6") }
        de { $("#typcheck7") }
        taxi { $("#typcheck8") }
        annat { $("#typcheck9") }
    }

    def valjBehorigheter(String valdaBehorigheter) {
        if (valdaBehorigheter != null) {
            c1 = false
            c1e = false
            c = false
            ce = false
            d1 = false
            d1e = false
            d = false
            de = false
            taxi = false
            annat = false

            def behorigheter = valdaBehorigheter.split(",");

            if (behorigheter.contains("C1")) c1 = true
            if (behorigheter.contains("C1E")) c1e = true
            if (behorigheter.contains("C")) c = true
            if (behorigheter.contains("CE")) ce = true
            if (behorigheter.contains("D1")) d1 = true
            if (behorigheter.contains("D1E")) d1e = true
            if (behorigheter.contains("D")) d = true
            if (behorigheter.contains("DE")) de = true
            if (behorigheter.contains("Taxi")) taxi = true
            if (behorigheter.contains("Annat")) annat = true
        }
    }

    def hamtaBehorigheter() {
        def result = "";
        if (c1.value() == "on")    { if (result != "") { result += "," }; result += "C1" }
        if (c1e.value() == "on")   { if (result != "") { result += "," }; result += "C1E" }
        if (c.value() == "on")     { if (result != "") { result += "," }; result += "C" }
        if (ce.value() == "on")    { if (result != "") { result += "," }; result += "CE" }
        if (d1.value() == "on")    { if (result != "") { result += "," }; result += "D1" }
        if (d1e.value() == "on")   { if (result != "") { result += "," }; result += "D1E" }
        if (d.value() == "on")     { if (result != "") { result += "," }; result += "D" }
        if (de.value() == "on")    { if (result != "") { result += "," }; result += "DE" }
        if (taxi.value() == "on")  { if (result != "") { result += "," }; result += "Taxi" }
        if (annat.value() == "on") { if (result != "") { result += "," }; result += "Annat" }
        result
    }
}

class IdentitetModule extends Module {
    static base = { $("#identitetForm") }
    static content = {
        idkort { $("#identity0") }
        foretagskortTjansterkort { $("#identity1") }
        korkort { $("#identity2") }
        personligKannedom { $("#identity3") }
        forsakran { $("#identity4") }
        pass { $("#identity5") }
    }

    def valjTyp(String identifieringstyp) {
        if (identifieringstyp != null) {
            def validTypes = ["idkort", "foretagskort", "korkort", "kannedom", "forsakran", "pass"]
            assert validTypes.contains(identifieringstyp),
                    "Fältet 'identifieringstyp' kan endast innehålla något av följande värden: ${validTypes}"

            if ("idkort" == identifieringstyp) {
                idkort.click()
            } else if ("foretagskort" == identifieringstyp) {
                foretagskortTjansterkort.click()
            } else if ("korkort" == identifieringstyp) {
                korkort.click()
            } else if ("kannedom" == identifieringstyp) {
                personligKannedom.click()
            } else if ("forsakran" == identifieringstyp) {
                forsakran.click()
            } else if ("pass" == identifieringstyp) {
                pass.click()
            }
        }
    }
}

class SynModule extends Module {
    static base = { $("#synForm") }
    static content = {
        fragaA { $("input", name: "syna") }
        fragaB { $("input", name: "synb") }
        fragaC { $("input", name: "sync") }
        fragaD { $("input", name: "synd") }
        fragaE { $("input", name: "syne") }
        hogerOgaUtanKorrektion { $("#synHogerOgaUtanKorrektion") }
        hogerOgaMedKorrektion { $("#synHogerOgaMedKorrektion") }
        hogerOgaKontaktlins { $("#synHogerOgaKontaktlins") }
        vansterOgaUtanKorrektion { $("#synVansterOgaUtanKorrektion") }
        vansterOgaMedKorrektion { $("#synVansterOgaMedKorrektion") }
        vansterOgaKontaktlins { $("#synVasterOgaKontaktlins") }
        binokulartUtanKorrektion { $("#synBinokulartUtanKorrektion") }
        binokulartMedKorrektion { $("#synBinokulartMedKorrektion") }
        dioptrier { $("#dioptrier") }
    }
}

class HorselBalansModule extends Module {
    static base = { $("#horselBalansForm") }
    static content = {
        fragaA { $("input", name: "horselbalansa") }
        fragaB { $("input", name: "horselbalansb") }
    }
}

class FunktionsnedsattningModule extends Module {
    static base = { $("#funktionsnedsattningForm") }
    static content = {
        fragaA { $("input", name: "funktionsnedsattninga") }
        beskrivning { $("#funktionsnedsattning") }
        fragaB { $("input", name: "funktionsnedsattningb") }
    }
}

class HjartkartModule extends Module {
    static base = { $("#hjartkartForm") }
    static content = {
        fragaA { $("input", name: "hjartkarla") }
        fragaB { $("input", name: "hjartkarlb") }
        fragaC { $("input", name: "hjartkarlc") }
        beskrivning { $("#beskrivningRiskfaktorer") }
    }
}

class DiabetesModule extends Module {
    static base = { $("#diabetesForm") }
    static content = {
        fragaA { $("input", name: "diabetesa") }
        diabetestyp { $("input", name: "diabetestyp") }
        behandlingKost { $("#diabetestreat1") }
        behandlingTabletter { $("#diabetestreat2") }
        behandlingInsulin { $("#diabetestreat3") }
    }

    def valjTyp(String valdDiabetestyp) {
        if (valdDiabetestyp != null) {
            def validTypes = ["typ1", "typ2"]
            assert validTypes.contains(valdDiabetestyp),
                    "Fältet 'diabetestyp' kan endast innehålla något av följande värden: ${validTypes}"

            if ("typ1" == valdDiabetestyp) {
                diabetestyp = "DIABETES_TYP_1"
            } else if ("typ2" == valdDiabetestyp) {
                diabetestyp = "DIABETES_TYP_2"
            }
        }
    }
}

class NeurologiModule extends Module {
    static base = { $("#neurologiForm") }
    static content = {
        fragaA { $("input", name: "neurologia") }
    }
}

class MedvetandestorningModule extends Module {
    static base = { $("#medvetandestorningForm") }
    static content = {
        fragaA { $("input", name: "medvetandestorninga") }
        beskrivning { $("#beskrivningMedvetandestorning") }
    }
}

class NjurarModule extends Module {
    static base = { $("#njurarForm") }
    static content = {
        fragaA { $("input", name: "njurara") }
    }
}

class KognitivtModule extends Module {
    static base = { $("#kognitivtForm") }
    static content = {
        fragaA { $("input", name: "kognitivta") }
    }
}

class SomnvakenhetModule extends Module {
    static base = { $("#somnvakenhetForm") }
    static content = {
        fragaA { $("input", name: "somnvakenheta") }
    }
}

class NarkotikaLakemedelModule extends Module {
    static base = { $("#narkotikaLakemedelForm") }
    static content = {
        fragaA { $("input", name: "narkotikalakemedela") }
        fragaB { $("input", name: "narkotikalakemedelb") }
        fragaB2 { $("input", name: "narkotikalakemedelb2") }
        fragaC { $("input", name: "narkotikalakemedelc") }
        beskrivning { $("#beskrivningNarkotikalakemedel") }
    }
}

class PsykisktModule extends Module {
    static base = { $("#psykisktForm") }
    static content = {
        fragaA { $("input", name: "psykiskta") }
    }
}

class UtvecklingsstorningModule extends Module {
    static base = { $("#utvecklingsstorningForm") }
    static content = {
        fragaA { $("input", name: "utvecklingsstorninga") }
        fragaB { $("input", name: "utvecklingsstorningb") }
    }
}

class SjukhusvardModule extends Module {
    static base = { $("#sjukhusvardForm") }
    static content = {
        fragaA { $("input", name: "sjukhusvarda") }
        tidpunkt { $("#tidpunkt") }
        vardinrattning { $("#vardinrattning") }
        anledning { $("#anledning") }
    }
}

class MedicineringModule extends Module {
    static base = { $("#medicineringForm") }
    static content = {
        fragaA { $("input", name: "medicineringa") }
        beskrivning { $("#beskrivningMedicinering") }
    }
}

class BedomningModule extends Module {
    static base = { $("#bedomningForm") }
    static content = {

        behorighetBedomning { $("#behorighet_bedomning") }
        behorighetKanInteTaStallning { $("#behorighet_kanintetastallning") }
        c1 { $("#korkortstyp0") }
        c1e { $("#korkortstyp1") }
        c { $("#korkortstyp2") }
        ce { $("#korkortstyp3") }
        d1 { $("#korkortstyp4") }
        d1e { $("#korkortstyp5") }
        d { $("#korkortstyp6") }
        de { $("#korkortstyp7") }
        taxi { $("#korkortstyp8") }
        annat { $("#korkortstyp9") }
        specialist { $("#specialist") }
    }

    def valjBehorigheter(String valdaBehorigheter) {
        if (valdaBehorigheter != null) {
            c1 = false
            c1e = false
            c = false
            ce = false
            d1 = false
            d1e = false
            d = false
            de = false
            taxi = false
            annat = false

            def behorigheter = valdaBehorigheter.split(",");

            if (behorigheter.contains("C1")) c1 = true
            if (behorigheter.contains("C1E")) c1e = true
            if (behorigheter.contains("C")) c = true
            if (behorigheter.contains("CE")) ce = true
            if (behorigheter.contains("D1")) d1 = true
            if (behorigheter.contains("D1E")) d1e = true
            if (behorigheter.contains("D")) d = true
            if (behorigheter.contains("DE")) de = true
            if (behorigheter.contains("Taxi")) taxi = true
            if (behorigheter.contains("Annat")) annat = true
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
    }
}
