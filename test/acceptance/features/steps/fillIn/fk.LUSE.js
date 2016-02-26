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

/* globals pages, browser, protractor, logger */

'use strict';
var luseUtkastPage = pages.intyg.fk.luse.utkast;
module.exports = {
    fillIn: function(intyg, cb) {
        logger.info('intyg.typ:' + intyg.typ);

        browser.ignoreSynchronization = true;

        //Baserat på
        luseUtkastPage.baseratPa.minUndersokningAvPatienten.checkbox3.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.baseratPa.minUndersokningAvPatienten.checkbox4.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.baseratPa.minUndersokningAvPatienten.checkbox5.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.baseratPa.kannedomOmPatient.checkbox.sendKeys(protractor.Key.SPACE);

        luseUtkastPage.underlagFinnsNo.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.diagnoseCode.sendKeys(intyg.diagnos.kod);
        luseUtkastPage.diagnoseCode.sendKeys(protractor.Key.TAB);
        luseUtkastPage.diagnosgrund.sendKeys(intyg.diagnos.bakgrund);
        luseUtkastPage.nyBedomningDiagnosgrundNo.sendKeys(protractor.Key.SPACE);

        //Funktionsnedsättning
        luseUtkastPage.funktionsnedsattning.intellektuell.sendKeys(intyg.funktionsnedsattning.intellektuell);
        luseUtkastPage.funktionsnedsattning.kommunikation.sendKeys(intyg.funktionsnedsattning.kommunikation);
        // luseUtkastPage.funktionsnedsattning.koncentration.sendKeys(intyg.funktionsnedsattning.;
        luseUtkastPage.funktionsnedsattning.psykisk.sendKeys(intyg.funktionsnedsattning.psykisk);
        luseUtkastPage.funktionsnedsattning.synHorselTal.sendKeys(intyg.funktionsnedsattning.synHorselTal);
        luseUtkastPage.funktionsnedsattning.balansKoordination.sendKeys(intyg.funktionsnedsattning.balansKoordination);
        luseUtkastPage.funktionsnedsattning.annan.sendKeys(intyg.funktionsnedsattning.annan);
        luseUtkastPage.sjukdomsforlopp.sendKeys(intyg.sjukdomsForlopp);

        luseUtkastPage.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning);
        luseUtkastPage.avslutadBehandling.sendKeys(intyg.avslutadBehandling);
        luseUtkastPage.pagaendeBehandling.sendKeys(intyg.pagaendeBehandling);
        luseUtkastPage.planeradBehandling.sendKeys(intyg.planeradBehandling);
        luseUtkastPage.substansintag.sendKeys(intyg.substansintag);

        luseUtkastPage.medicinskaForutsattningarForArbete.sendKeys(intyg.medicinskaForutsattningarForArbete);
        luseUtkastPage.aktivitetsFormaga.sendKeys(intyg.aktivitetsFormaga);
        luseUtkastPage.ovrigt.sendKeys(intyg.ovrigt);
        luseUtkastPage.kontaktMedFkNo.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.tillaggsfragor0svar.sendKeys(intyg.tillaggsfragor0svar);
        luseUtkastPage.tillaggsfragor1svar.sendKeys(intyg.tillaggsfragor1svar);

        browser.ignoreSynchronization = false;

        // browser.driver.wait(protractor.until.elementIsVisible(luseUtkastPage.signeraButton));
        cb();
    }
};