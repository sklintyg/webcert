package se.inera.webcert.spec.ts_bas

import se.inera.webcert.pages.ts_bas.EditCertPage
import se.inera.certificate.spec.Browser

class PopuleraTsBas {

    String postadress
    String postnummer
    String postort
    String intygetAvser
    String identifieringstyp
    Boolean synA
    Boolean synB
    Boolean synC
    Boolean synD
    Boolean synE
    String synHogerOgaUtanKorrektion
    String synHogerOgaMedKorrektion
    Boolean synHogerOgaKontaktlins
    String synVansterOgaUtanKorrektion
    String synVansterOgaMedKorrektion
    Boolean synVansterOgaKontaktlins
    String synBinokulartUtanKorrektion
    String synBinokulartMedKorrektion
    Boolean glasOverskrider8Dioptrier
    Boolean horselA
    Boolean horselB
    Boolean funktionsnedsattningA
    String funktionsnedsattningBeskrivning
    Boolean funktionsnedsattningB
    Boolean hjartkarlA
    Boolean hjartkarlB
    Boolean hjartkarlC
    String hjartkarlBeskrivning
    Boolean diabetesA
    String diabetestyp
    Boolean diabetesBehandlingKost
    Boolean diabetesBehandlingTabletter
    Boolean diabetesBehandlingInsulin
    Boolean neurologiA
    Boolean medvetandestorningA
    String medvetandestorningBeskrivning
    Boolean njurarA
    Boolean kognitivtA
    Boolean somnvakenhetA
    Boolean narkotikalakemedelA
    Boolean narkotikalakemedelB
    Boolean narkotikalakemedelB2
    Boolean narkotikalakemedelC
    String narkotikalakemedelBeskrivning
    Boolean psykisktA
    Boolean utvecklingsstorningA
    Boolean utvecklingsstorningB
    Boolean sjukhusvardA
    String sjukhusvardTidpunkt
    String sjukhusvardVardinrattning
    String sjukhusvardAnledning
    Boolean medicineringA
    String medicineringBeskrivning
    String kommentar
    Boolean behorighet
    String bedomdBehorighet
    String specialist
    String vardenhetPostadress
    String vardenhetPostnummer
    String vardenhetPostort
    String vardenhetTelefonnummer

    def execute() {
        Browser.drive {

            waitFor {
                at EditCertPage
            }

            if (postadress != null) page.patient.postadress = postadress
            if (postnummer != null) page.patient.postnummer = postnummer
            if (postort != null) page.patient.postort = postort

            page.intygetAvser.valjBehorigheter(intygetAvser)

            page.identitet.valjTyp(identifieringstyp)

            if (synA != null) page.syn.fragaA = synA
            if (synB != null) page.syn.fragaB = synB
            if (synC != null) page.syn.fragaC = synC
            if (synD != null) page.syn.fragaD = synD
            if (synE != null) page.syn.fragaE = synE
            if (synHogerOgaUtanKorrektion != null) page.syn.hogerOgaUtanKorrektion = synHogerOgaUtanKorrektion
            if (synHogerOgaMedKorrektion != null) page.syn.hogerOgaMedKorrektion = synHogerOgaMedKorrektion
            if (synHogerOgaKontaktlins != null) page.syn.hogerOgaKontaktlins = synHogerOgaKontaktlins
            if (synVansterOgaUtanKorrektion != null) page.syn.vansterOgaUtanKorrektion = synVansterOgaUtanKorrektion
            if (synVansterOgaMedKorrektion != null) page.syn.vansterOgaMedKorrektion = synVansterOgaMedKorrektion
            if (synVansterOgaKontaktlins != null) page.syn.vansterOgaKontaktlins = synVansterOgaKontaktlins
            if (synBinokulartUtanKorrektion != null) page.syn.binokulartUtanKorrektion = synBinokulartUtanKorrektion
            if (synBinokulartMedKorrektion != null) page.syn.binokulartMedKorrektion = synBinokulartMedKorrektion
            if (glasOverskrider8Dioptrier != null) page.syn.dioptrier = glasOverskrider8Dioptrier

            if (horselA != null) page.horselBalans.fragaA = horselA
            if (horselB != null) page.horselBalans.fragaB = horselB

            if (funktionsnedsattningA != null) page.funktionsnedsattning.fragaA = funktionsnedsattningA
            if (funktionsnedsattningBeskrivning != null) page.funktionsnedsattning.beskrivning = funktionsnedsattningBeskrivning
            if (funktionsnedsattningB != null) page.funktionsnedsattning.fragaB = funktionsnedsattningB

            if (hjartkarlA != null) page.hjartkarl.fragaA = hjartkarlA
            if (hjartkarlB != null) page.hjartkarl.fragaB = hjartkarlB
            if (hjartkarlC != null) page.hjartkarl.fragaC = hjartkarlC
            if (hjartkarlBeskrivning != null) page.hjartkarl.beskrivning = hjartkarlBeskrivning

            if (diabetesA != null) page.diabetes.fragaA = diabetesA
            page.diabetes.valjTyp(diabetestyp)
            if (diabetesBehandlingKost != null) page.diabetes.behandlingKost = diabetesBehandlingKost
            if (diabetesBehandlingTabletter != null) page.diabetes.behandlingTabletter = diabetesBehandlingTabletter
            if (diabetesBehandlingInsulin != null) page.diabetes.behandlingInsulin = diabetesBehandlingInsulin

            if (neurologiA != null) page.neurologi.fragaA = neurologiA

            if (medvetandestorningA != null) page.medvetandestorning.fragaA = medvetandestorningA
            if (medvetandestorningBeskrivning != null) page.medvetandestorning.beskrivning = medvetandestorningBeskrivning

            if (njurarA != null) page.njurar.fragaA = njurarA

            if (kognitivtA != null) page.kognitivt.fragaA = kognitivtA

            if (somnvakenhetA != null) page.somnvakenhet.fragaA = somnvakenhetA

            if (narkotikalakemedelA != null) page.narkotikaLakemedel.fragaA = narkotikalakemedelA
            if (narkotikalakemedelB != null) page.narkotikaLakemedel.fragaB = narkotikalakemedelB
            if (narkotikalakemedelB2 != null) page.narkotikaLakemedel.fragaB2 = narkotikalakemedelB2
            if (narkotikalakemedelC != null) page.narkotikaLakemedel.fragaC = narkotikalakemedelC
            if (narkotikalakemedelBeskrivning != null) page.narkotikaLakemedel.beskrivning = narkotikalakemedelBeskrivning

            if (psykisktA != null) page.psykiskt.fragaA = psykisktA

            if (utvecklingsstorningA != null) page.utvecklingsstorning.fragaA = utvecklingsstorningA
            if (utvecklingsstorningB != null) page.utvecklingsstorning.fragaB = utvecklingsstorningB

            if (sjukhusvardA != null) page.sjukhusvard.fragaA = sjukhusvardA
            if (sjukhusvardTidpunkt != null) page.sjukhusvard.tidpunkt = sjukhusvardTidpunkt
            if (sjukhusvardVardinrattning != null) page.sjukhusvard.vardinrattning = sjukhusvardVardinrattning
            if (sjukhusvardAnledning != null) page.sjukhusvard.anledning = sjukhusvardAnledning

            if (medicineringA != null) page.medicinering.fragaA = medicineringA
            if (medicineringBeskrivning != null) page.medicinering.beskrivning = medicineringBeskrivning

            if (kommentar != null) page.kommentar = kommentar

            if (behorighet != null) {
                page.bedomning.valjBehorighet(behorighet)
                if (!behorighet) { // behorighet == kanInteTaStallning så false är bedömning
                    page.bedomning.valjBehorigheter(bedomdBehorighet)
                }
            }

            if (specialist != null) page.bedomning.specialist = specialist

            if (vardenhetPostadress != null) page.vardenhet.postadress = vardenhetPostadress
            if (vardenhetPostnummer != null) page.vardenhet.postnummer = vardenhetPostnummer
            if (vardenhetPostort != null) page.vardenhet.postort = vardenhetPostort
            if (vardenhetTelefonnummer != null) page.vardenhet.telefonnummer = vardenhetTelefonnummer
        }
    }
}
