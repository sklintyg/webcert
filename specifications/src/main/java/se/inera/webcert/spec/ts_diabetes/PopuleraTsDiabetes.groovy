package se.inera.webcert.spec.ts_diabetes

import se.inera.webcert.pages.ts_diabetes.EditCertPage
import se.inera.certificate.spec.Browser

class PopuleraTsDiabetes {

    String postadress
    String postnummer
    String postort
    String intygetAvser
    String identifieringstyp
    String diabetesAr
    String diabetestyp
    Boolean diabetesBehandlingKost
    Boolean diabetesBehandlingTabletter
    Boolean diabetesBehandlingInsulin
    String diabetesBehandlingInsulinPeriod
    String diabetesBehandlingAnnan
    Boolean hypoglykemierA
    Boolean hypoglykemierB
    Boolean hypoglykemierC
    Boolean hypoglykemierD
    String hypoglykemierAllvarligForekomstEpisoder
    Boolean hypoglykemierE
    String hypoglykemierAllvarligForekomstTrafikEpisoder
    Boolean hypoglykemierF
    Boolean hypoglykemierG
    String hypoglykemierAllvarligForekomstVakenTid
    Boolean synA
    Boolean synB
    String synHogerOgaUtanKorrektion
    String synHogerOgaMedKorrektion
    String synVansterOgaUtanKorrektion
    String synVansterOgaMedKorrektion
    String synBinokulartUtanKorrektion
    String synBinokulartMedKorrektion
    Boolean synD
    Boolean behorighet
    String bedomdBehorighet
    Boolean bedomning
    String kommentar
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

            if (diabetesAr != null) page.allmant.ar = diabetesAr
            page.allmant.valjTyp(diabetestyp)
            if (diabetesBehandlingKost != null) page.allmant.behandlingKost = diabetesBehandlingKost
            if (diabetesBehandlingTabletter != null) page.allmant.behandlingTabletter = diabetesBehandlingTabletter
            if (diabetesBehandlingInsulin != null) page.allmant.behandlingInsulin = diabetesBehandlingInsulin
            if (diabetesBehandlingInsulinPeriod != null) page.allmant.behandlingInsulinPeriod = diabetesBehandlingInsulinPeriod
            if (diabetesBehandlingAnnan != null) page.allmant.behandlingAnnan = diabetesBehandlingAnnan

            if (hypoglykemierA != null) page.hypoglykemier.fragaA = hypoglykemierA
            if (hypoglykemierB != null) page.hypoglykemier.fragaB = hypoglykemierB
            if (hypoglykemierC != null) page.hypoglykemier.fragaC = hypoglykemierC
            if (hypoglykemierD != null) page.hypoglykemier.fragaD = hypoglykemierD
            if (hypoglykemierAllvarligForekomstEpisoder != null) page.hypoglykemier.allvarligForekomstEpisoder = hypoglykemierAllvarligForekomstEpisoder
            if (hypoglykemierE != null) page.hypoglykemier.fragaE = hypoglykemierE
            if (hypoglykemierAllvarligForekomstTrafikEpisoder != null) page.hypoglykemier.allvarligForekomstTrafikEpisoder = hypoglykemierAllvarligForekomstTrafikEpisoder
            if (hypoglykemierF != null) page.hypoglykemier.fragaF = hypoglykemierF
            if (hypoglykemierG != null) page.hypoglykemier.fragaG = hypoglykemierG
            if (hypoglykemierAllvarligForekomstVakenTid != null) page.hypoglykemier.allvarligForekomstVakenTid = hypoglykemierAllvarligForekomstVakenTid

            if (synA != null) page.syn.fragaA = synA
            if (synB != null) page.syn.fragaB = synB
            if (synHogerOgaUtanKorrektion != null) page.syn.hogerOgaUtanKorrektion = synHogerOgaUtanKorrektion
            if (synHogerOgaMedKorrektion != null) page.syn.hogerOgaMedKorrektion = synHogerOgaMedKorrektion
            if (synVansterOgaUtanKorrektion != null) page.syn.vansterOgaUtanKorrektion = synVansterOgaUtanKorrektion
            if (synVansterOgaMedKorrektion != null) page.syn.vansterOgaMedKorrektion = synVansterOgaMedKorrektion
            if (synBinokulartUtanKorrektion != null) page.syn.binokulartUtanKorrektion = synBinokulartUtanKorrektion
            if (synBinokulartMedKorrektion != null) page.syn.binokulartMedKorrektion = synBinokulartMedKorrektion
            if (synD != null) page.syn.fragaD = synD

            if (behorighet != null) {
                page.bedomning.valjBehorighet(behorighet)
                if (!behorighet) { // behorighet == kanInteTaStallning så false är bedömning
                    page.bedomning.valjBehorigheter(bedomdBehorighet)
                }
            }

            if (bedomning != null) page.bedomning.bedomning = bedomning

            if (kommentar != null) page.kommentar = kommentar

            if (specialist != null) page.specialist = specialist

            if (vardenhetPostadress != null) page.vardenhet.postadress = vardenhetPostadress
            if (vardenhetPostnummer != null) page.vardenhet.postnummer = vardenhetPostnummer
            if (vardenhetPostort != null) page.vardenhet.postort = vardenhetPostort
            if (vardenhetTelefonnummer != null) page.vardenhet.telefonnummer = vardenhetTelefonnummer
        }
    }
}
