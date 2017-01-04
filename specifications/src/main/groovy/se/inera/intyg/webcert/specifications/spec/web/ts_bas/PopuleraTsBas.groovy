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

package se.inera.intyg.webcert.specifications.spec.web.ts_bas

import se.inera.intyg.webcert.specifications.page.AbstractPage
import se.inera.intyg.webcert.specifications.pages.ts_bas.EditeraTsBasPage
import se.inera.intyg.webcert.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class PopuleraTsBas extends ExceptionHandlingFixture {

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
                at EditeraTsBasPage
            }

            page.setAutoSave(false);
            page.setSaving(true);

            if (postadress != null) page.patient.postadress = postadress
            if (postnummer != null) page.patient.postnummer = postnummer
            if (postort != null) page.patient.postort = postort

            page.intygetAvser.valjBehorigheter(intygetAvser)

            page.identitet.valjTyp(identifieringstyp)

            if (synA != null) { AbstractPage.scrollIntoView(page.syn.fragaA.attr('id')); page.syn.fragaA = synA }
            if (synB != null) page.syn.fragaB = synB
            if (synC != null) page.syn.fragaC = synC
            if (synD != null) page.syn.fragaD = synD
            if (synE != null) page.syn.fragaE = synE
            if (synHogerOgaUtanKorrektion != null) { AbstractPage.scrollIntoView(page.syn.hogerOgaMedKorrektion.attr('id')); page.syn.hogerOgaUtanKorrektion = synHogerOgaUtanKorrektion }
            if (synHogerOgaMedKorrektion != null) page.syn.hogerOgaMedKorrektion = synHogerOgaMedKorrektion
            if (synHogerOgaKontaktlins != null) page.syn.hogerOgaKontaktlins = synHogerOgaKontaktlins
            if (synVansterOgaUtanKorrektion != null) page.syn.vansterOgaUtanKorrektion = synVansterOgaUtanKorrektion
            if (synVansterOgaMedKorrektion != null) page.syn.vansterOgaMedKorrektion = synVansterOgaMedKorrektion
            if (synVansterOgaKontaktlins != null) page.syn.vansterOgaKontaktlins = synVansterOgaKontaktlins
            if (synBinokulartUtanKorrektion != null) page.syn.binokulartUtanKorrektion = synBinokulartUtanKorrektion
            if (synBinokulartMedKorrektion != null) page.syn.binokulartMedKorrektion = synBinokulartMedKorrektion
            if (glasOverskrider8Dioptrier != null) page.syn.dioptrier = glasOverskrider8Dioptrier

            if (horselA != null || horselB != null) {
                AbstractPage.scrollIntoView(page.horselBalans.fragaA.attr('id'));
            }
            if (horselA != null) { AbstractPage.scrollIntoView(page.horselBalans.fragaA.attr('id')); page.horselBalans.fragaA = horselA }
            if (horselB != null) page.horselBalans.fragaB = horselB

            if (funktionsnedsattningA != null || funktionsnedsattningBeskrivning != null || funktionsnedsattningB != null) {
                AbstractPage.scrollIntoView(page.funktionsnedsattning.fragaA.attr('id'));
            }
            if (funktionsnedsattningA != null) page.funktionsnedsattning.fragaA = funktionsnedsattningA
            if (funktionsnedsattningBeskrivning != null) page.funktionsnedsattning.beskrivning = funktionsnedsattningBeskrivning
            if (funktionsnedsattningB != null) page.funktionsnedsattning.fragaB = funktionsnedsattningB

            if (hjartkarlA != null || hjartkarlB != null || hjartkarlC != null || hjartkarlBeskrivning != null) {
                AbstractPage.scrollIntoView(page.hjartkarl.fragaA.attr('id'));
            }
            if (hjartkarlA != null) page.hjartkarl.fragaA = hjartkarlA
            if (hjartkarlB != null) page.hjartkarl.fragaB = hjartkarlB
            if (hjartkarlC != null) page.hjartkarl.fragaC = hjartkarlC
            if (hjartkarlBeskrivning != null) page.hjartkarl.beskrivning = hjartkarlBeskrivning

            if (diabetesA != null || diabetesBehandlingKost != null || diabetesBehandlingTabletter != null || diabetesBehandlingInsulin != null) {
                AbstractPage.scrollIntoView(page.diabetes.fragaA.attr('id'));
            }
            if (diabetesA != null) page.diabetes.fragaA = diabetesA
            page.diabetes.valjTyp(diabetestyp)
            if (diabetesBehandlingKost != null) page.diabetes.behandlingKost = diabetesBehandlingKost
            if (diabetesBehandlingTabletter != null) page.diabetes.behandlingTabletter = diabetesBehandlingTabletter
            if (diabetesBehandlingInsulin != null) page.diabetes.behandlingInsulin = diabetesBehandlingInsulin

            if (neurologiA != null) { AbstractPage.scrollIntoView(page.neurologi.fragaA.attr('id')); page.neurologi.fragaA = neurologiA }

            if (medvetandestorningA != null) { AbstractPage.scrollIntoView(page.medvetandestorning.fragaA.attr('id')); page.medvetandestorning.fragaA = medvetandestorningA }
            if (medvetandestorningBeskrivning != null) page.medvetandestorning.beskrivning = medvetandestorningBeskrivning

            if (njurarA != null) { AbstractPage.scrollIntoView(page.njurar.fragaA.attr('id')); page.njurar.fragaA = njurarA }

            if (kognitivtA != null) { AbstractPage.scrollIntoView(page.kognitivt.fragaA.attr('id')); page.kognitivt.fragaA = kognitivtA }

            if (somnvakenhetA != null) { AbstractPage.scrollIntoView(page.somnvakenhet.fragaA.attr('id')); page.somnvakenhet.fragaA = somnvakenhetA }

            if (narkotikalakemedelA != null || narkotikalakemedelB != null || narkotikalakemedelB2 != null || narkotikalakemedelC != null) {
                AbstractPage.scrollIntoView(page.narkotikaLakemedel.fragaA.attr('id'));
            }
            if (narkotikalakemedelA != null) page.narkotikaLakemedel.fragaA = narkotikalakemedelA
            if (narkotikalakemedelB != null) page.narkotikaLakemedel.fragaB = narkotikalakemedelB
            if (narkotikalakemedelB2 != null) page.narkotikaLakemedel.fragaB2 = narkotikalakemedelB2
            if (narkotikalakemedelC != null) page.narkotikaLakemedel.fragaC = narkotikalakemedelC
            if (narkotikalakemedelBeskrivning != null) page.narkotikaLakemedel.beskrivning = narkotikalakemedelBeskrivning

            if (psykisktA != null) { AbstractPage.scrollIntoView(page.psykiskt.fragaA.attr('id')); page.psykiskt.fragaA = psykisktA }

            if (utvecklingsstorningA != null || utvecklingsstorningB) {
                AbstractPage.scrollIntoView(page.utvecklingsstorning.fragaA.attr('id'));
            }
            if (utvecklingsstorningA != null) page.utvecklingsstorning.fragaA = utvecklingsstorningA
            if (utvecklingsstorningB != null) page.utvecklingsstorning.fragaB = utvecklingsstorningB

            if (sjukhusvardA != null) page.sjukhusvard.fragaA = sjukhusvardA
            if (sjukhusvardTidpunkt != null) page.sjukhusvard.tidpunkt = sjukhusvardTidpunkt
            if (sjukhusvardVardinrattning != null) page.sjukhusvard.vardinrattning = sjukhusvardVardinrattning
            if (sjukhusvardAnledning != null) page.sjukhusvard.anledning = sjukhusvardAnledning

            if (medicineringA != null) { AbstractPage.scrollIntoView(page.medicinering.fragaA.attr('id')); page.medicinering.fragaA = medicineringA }
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

            page.setAutoSave(true);
            // after any updates we need to wait for saving before we can start checking the state of the page in a fixture
            page.doneSaving();

        }
    }
}
