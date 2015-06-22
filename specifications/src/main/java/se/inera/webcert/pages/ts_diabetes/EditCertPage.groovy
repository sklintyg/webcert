package se.inera.webcert.pages.ts_diabetes

import geb.Module
import se.inera.certificate.page.AbstractPage
import se.inera.webcert.pages.AbstractEditCertPage
import se.inera.webcert.pages.VardenhetModule

class EditCertPage extends AbstractEditCertPage {

    static at = { doneLoading() && $("#edit-ts-diabetes").isDisplayed() }

    static content = {

        // Knappar
        sparaKnapp { $("#spara-utkast") }
        visaVadSomSaknasKnapp { $("#showCompleteButton") }
        doljVadSomSaknasKnapp { $("#hideCompleteButton") }

        // Meddelanden
        intygetSparatMeddelande { $("#intyget-sparat-meddelande") }
        intygetEjKomplettMeddelande { $("#intyget-ej-komplett-meddelande") }

        // Formulärfält
        form { $("form") }
        patient { module PatientModule }
        intygetAvser { module IntygetAvserModule }
        identitet { module IdentitetModule }
        allmant { module AllmantModule }
        hypoglykemier { module HypoglykemierModule }
        syn { module SynModule }
        bedomning { name -> module BedomningModule, form: form }
        kommentar { $("#kommentar") }
        specialist { $("#specialist") }
        vardenhet { module VardenhetModule }

        // Intygsvalidering
        valideringPatient(required: false)              { $("#validationMessages_patient") }
        valideringIntygAvser(required: false)           { $("#validationMessages_intygavser") }
        valideringIdentitet(required: false)            { $("#validationMessages_identitet") }
        valideringDiabetes(required: false)             { $("#validationMessages_diabetes") }
        valideringHypoglykemier(required: false)        { $("#validationMessages_hypoglykemier") }
        valideringSyn(required: false)                  { $("#validationMessages_syn") }
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
        am { $("#typcheck0") }
        a1 { $("#typcheck1") }
        a2 { $("#typcheck2") }
        a { $("#typcheck3") }
        b { $("#typcheck4") }
        be { $("#typcheck5") }
        traktor { $("#typcheck6") }
        c1 { $("#typcheck7") }
        c1e { $("#typcheck8") }
        c { $("#typcheck9") }
        ce { $("#typcheck10") }
        d1 { $("#typcheck11") }
        d1e { $("#typcheck12") }
        d { $("#typcheck13") }
        de { $("#typcheck14") }
        taxi { $("#typcheck15") }
    }

    def valjBehorigheter(String valdaBehorigheter) {
        if (valdaBehorigheter != null) {

            // scroll into view
            AbstractPage.scrollIntoView("typcheck0");

            am = false
            a1 = false
            a2 = false
            a = false
            b = false
            be = false
            traktor = false
            c1 = false
            c1e = false
            c = false
            ce = false
            d1 = false
            d1e = false
            d = false
            de = false
            taxi = false

            def behorigheter = valdaBehorigheter.split(",");

            if (behorigheter.contains("AM")) am = true
            if (behorigheter.contains("A1")) a1 = true
            if (behorigheter.contains("A2")) a2 = true
            if (behorigheter.contains("A")) a = true
            if (behorigheter.contains("B")) b = true
            if (behorigheter.contains("BE")) be = true
            if (behorigheter.contains("Traktor")) traktor = true
            if (behorigheter.contains("C1")) c1 = true
            if (behorigheter.contains("C1E")) c1e = true
            if (behorigheter.contains("C")) c = true
            if (behorigheter.contains("CE")) ce = true
            if (behorigheter.contains("D1")) d1 = true
            if (behorigheter.contains("D1E")) d1e = true
            if (behorigheter.contains("D")) d = true
            if (behorigheter.contains("DE")) de = true
            if (behorigheter.contains("Taxi")) taxi = true
        }
    }

    def hamtaBehorigheter() {
        def result = "";
        if (am.value() == "on")      { if (result != "") { result += "," }; result += "AM" }
        if (a1.value() == "on")      { if (result != "") { result += "," }; result += "A1" }
        if (a2.value() == "on")      { if (result != "") { result += "," }; result += "A2" }
        if (a.value() == "on")       { if (result != "") { result += "," }; result += "A" }
        if (b.value() == "on")       { if (result != "") { result += "," }; result += "B" }
        if (be.value() == "on")      { if (result != "") { result += "," }; result += "BE" }
        if (traktor.value() == "on") { if (result != "") { result += "," }; result += "Traktor" }
        if (c1.value() == "on")      { if (result != "") { result += "," }; result += "C1" }
        if (c1e.value() == "on")     { if (result != "") { result += "," }; result += "C1E" }
        if (c.value() == "on")       { if (result != "") { result += "," }; result += "C" }
        if (ce.value() == "on")      { if (result != "") { result += "," }; result += "CE" }
        if (d1.value() == "on")      { if (result != "") { result += "," }; result += "D1" }
        if (d1e.value() == "on")     { if (result != "") { result += "," }; result += "D1E" }
        if (d.value() == "on")       { if (result != "") { result += "," }; result += "D" }
        if (de.value() == "on")      { if (result != "") { result += "," }; result += "DE" }
        if (taxi.value() == "on")    { if (result != "") { result += "," }; result += "Taxi" }
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

            AbstractPage.scrollIntoView('identity0');
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

class AllmantModule extends Module {
    static base = { $("#allmantForm") }
    static content = {
        ar { $("#diabetesyear") }
        diabetestyp { $("input", name: "diabetestyp") }
        behandlingKost { $("#diabetestreat1") }
        behandlingTabletter { $("#diabetestreat2") }
        behandlingInsulin { $("#diabetestreat3") }
        behandlingInsulinPeriod { $("#insulinBehandlingsperiod") }
        behandlingAnnan { $("#annanBehandlingBeskrivning") }
    }

    def valjTyp(String valdDiabetestyp) {
        if (valdDiabetestyp != null) {
            AbstractPage.scrollIntoView('allmantForm');
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

class HypoglykemierModule extends Module {
    static base = { $("#hypoglykemierForm") }
    static content = {
        fragaA { $("input", name: "hypoa") }
        fragaB { $("input", name: "hypob") }
        fragaC { $("input", name: "hypoc") }
        fragaD { $("input", name: "hypod") }
        allvarligForekomstEpisoder { $("#allvarligForekomstBeskrivning") }
        fragaE { $("input", name: "hypoe") }
        allvarligForekomstTrafikEpisoder { $("#allvarligForekomstTrafikBeskrivning") }
        fragaF { $("input", name: "hypof") }
        fragaG { $("input", name: "hypog") }
        allvarligForekomstVakenTid { $("#allvarligForekomstVakenTidObservationstid") }
        allvarligForekomstVakenTidObservationstid_toggle { $("#allvarligForekomstVakenTidObservationstid-toggle") }
    }
}

class SynModule extends Module {
    static base = { $("#synForm") }
    static content = {
        fragaA { $("input", name: "syna") }
        fragaB { $("input", name: "synb") }
        hogerOgaUtanKorrektion { $("#synHogerOgaUtanKorrektion") }
        hogerOgaMedKorrektion { $("#synHogerOgaMedKorrektion") }
        vansterOgaUtanKorrektion { $("#synVansterOgaUtanKorrektion") }
        vansterOgaMedKorrektion { $("#synVansterOgaMedKorrektion") }
        binokulartUtanKorrektion { $("#synBinokulartUtanKorrektion") }
        binokulartMedKorrektion { $("#synBinokulartMedKorrektion") }
        fragaD { $("input", name: "synd") }
    }
}

class BedomningModule extends Module {
    def form
    static base = { $("#bedomningForm") }
    static content = {

        behorighet { $("input", name: "behorighet") }
        behorighetBedomning { $("#behorighet_bedomning") }
        behorighetKanInteTaStallning { $("#behorighet_kanintetastallning") }

        behorighetGroup { form.behorighet }

        am { $("#korkortstyp0") }
        a1 { $("#korkortstyp1") }
        a2 { $("#korkortstyp2") }
        a { $("#korkortstyp3") }
        b { $("#korkortstyp4") }
        be { $("#korkortstyp5") }
        traktor { $("#korkortstyp6") }
        c1 { $("#korkortstyp7") }
        c1e { $("#korkortstyp8") }
        c { $("#korkortstyp9") }
        ce { $("#korkortstyp10") }
        d1 { $("#korkortstyp11") }
        d1e { $("#korkortstyp12") }
        d { $("#korkortstyp13") }
        de { $("#korkortstyp14") }
        taxi { $("#korkortstyp15") }
        bedomning { $("input", name:  "bedomning") }
    }

    def valjBehorighet(Boolean value) {
        if (value != null) {
            AbstractPage.scrollIntoView("behorighet_bedomning");
            behorighet = value;
        }
    }

    def valjBehorigheter(String valdaBehorigheter) {
        if (valdaBehorigheter != null) {
            AbstractPage.scrollIntoView('korkortstyp0');
            am = false
            a1 = false
            a2 = false
            a = false
            b = false
            be = false
            traktor = false
            c1 = false
            c1e = false
            c = false
            ce = false
            d1 = false
            d1e = false
            d = false
            de = false
            taxi = false

            def behorigheter = valdaBehorigheter.split(",");

            if (behorigheter.contains("AM")) am = true
            if (behorigheter.contains("A1")) a1 = true
            if (behorigheter.contains("A2")) a2 = true
            if (behorigheter.contains("A")) a = true
            if (behorigheter.contains("B")) b = true
            if (behorigheter.contains("BE")) be = true
            if (behorigheter.contains("Traktor")) traktor = true
            if (behorigheter.contains("C1")) c1 = true
            if (behorigheter.contains("C1E")) c1e = true
            if (behorigheter.contains("C")) c = true
            if (behorigheter.contains("CE")) ce = true
            if (behorigheter.contains("D1")) d1 = true
            if (behorigheter.contains("D1E")) d1e = true
            if (behorigheter.contains("D")) d = true
            if (behorigheter.contains("DE")) de = true
            if (behorigheter.contains("Taxi")) taxi = true
        }
    }
}
