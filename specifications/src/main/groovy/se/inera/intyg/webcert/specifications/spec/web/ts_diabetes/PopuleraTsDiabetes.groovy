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

package se.inera.intyg.webcert.specifications.spec.web.ts_diabetes

import se.inera.intyg.webcert.specifications.page.AbstractPage
import se.inera.intyg.webcert.specifications.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.intyg.webcert.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class PopuleraTsDiabetes extends ExceptionHandlingFixture {

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
                at EditeraTsDiabetesPage
            }

            page.setAutoSave(false);
            page.setSaving(true);

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

            if (hypoglykemierA != null) { AbstractPage.scrollIntoView(page.hypoglykemier.fragaA.attr('id')); page.hypoglykemier.fragaA = hypoglykemierA }
            if (hypoglykemierB != null) page.hypoglykemier.fragaB = hypoglykemierB
            if (hypoglykemierC != null) page.hypoglykemier.fragaC = hypoglykemierC
            if (hypoglykemierD != null) page.hypoglykemier.fragaD = hypoglykemierD
            if (hypoglykemierAllvarligForekomstEpisoder != null) page.hypoglykemier.allvarligForekomstEpisoder = hypoglykemierAllvarligForekomstEpisoder
            if (hypoglykemierE != null) page.hypoglykemier.fragaE = hypoglykemierE
            if (hypoglykemierAllvarligForekomstTrafikEpisoder != null) page.hypoglykemier.allvarligForekomstTrafikEpisoder = hypoglykemierAllvarligForekomstTrafikEpisoder
            if (hypoglykemierF != null) { AbstractPage.scrollIntoView(page.hypoglykemier.fragaF.attr('id')); page.hypoglykemier.fragaF = hypoglykemierF }
            if (hypoglykemierG != null) { AbstractPage.scrollIntoView(page.hypoglykemier.fragaG.attr('id')); page.hypoglykemier.fragaG = hypoglykemierG }
            if (hypoglykemierAllvarligForekomstVakenTid != null) page.hypoglykemier.allvarligForekomstVakenTid = hypoglykemierAllvarligForekomstVakenTid

            if (synA != null) { AbstractPage.scrollIntoView(page.syn.fragaA.attr('id')); page.syn.fragaA = synA }
            if (synB != null) page.syn.fragaB = synB
            if (synHogerOgaUtanKorrektion != null) page.syn.hogerOgaUtanKorrektion = synHogerOgaUtanKorrektion
            if (synHogerOgaMedKorrektion != null) page.syn.hogerOgaMedKorrektion = synHogerOgaMedKorrektion
            if (synVansterOgaUtanKorrektion != null) page.syn.vansterOgaUtanKorrektion = synVansterOgaUtanKorrektion
            if (synVansterOgaMedKorrektion != null) page.syn.vansterOgaMedKorrektion = synVansterOgaMedKorrektion
            if (synBinokulartUtanKorrektion != null) page.syn.binokulartUtanKorrektion = synBinokulartUtanKorrektion
            if (synBinokulartMedKorrektion != null) page.syn.binokulartMedKorrektion = synBinokulartMedKorrektion
            if (synD != null) page.syn.valjFragaD(synD)

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

            page.setAutoSave(true);
            // after any updates we need to wait for saving before we can start checking the state of the page in a fixture
            page.doneSaving();
        }
    }
}
