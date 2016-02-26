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
        // logger.info('Diagnoskod: ' + intyg.diagnos.kod);
        luseUtkastPage.diagnoseCode.sendKeys(intyg.diagnos.kod);
        luseUtkastPage.diagnoseCode.sendKeys(protractor.Key.TAB);
        // logger.info('Diagnosbakgrund: ' + intyg.diagnos.bakgrund);
        luseUtkastPage.diagnosgrund.sendKeys(intyg.diagnos.bakgrund);
        luseUtkastPage.nyBedomningDiagnosgrundNo.sendKeys(protractor.Key.SPACE);

        //Funktionsnedsättning
        // logger.info('funktionsnedsattning: ' + intyg.funktionsnedsattning.intellektuell);
        luseUtkastPage.funktionsnedsattning.intellektuell.sendKeys(intyg.funktionsnedsattning.intellektuell);
        // logger.info('kommunikation: ' + intyg.funktionsnedsattning.KOMMUNIKATION);
        luseUtkastPage.funktionsnedsattning.kommunikation.sendKeys(intyg.funktionsnedsattning.kommunikation);
        // luseUtkastPage.funktionsnedsattning.koncentration.sendKeys(intyg.funktionsnedsattning.;
        // logger.info('psykisk: ' + intyg.funktionsnedsattning.psykisk);
        luseUtkastPage.funktionsnedsattning.psykisk.sendKeys(intyg.funktionsnedsattning.psykisk);
        // logger.info('synHorselTal: ' + intyg.funktionsnedsattning.synHorselTal);
        luseUtkastPage.funktionsnedsattning.synHorselTal.sendKeys(intyg.funktionsnedsattning.synHorselTal);
        // logger.info('balansKoordination: ' + intyg.funktionsnedsattning.balansKoordination);
        luseUtkastPage.funktionsnedsattning.balansKoordination.sendKeys(intyg.funktionsnedsattning.balansKoordination);
        // logger.info('annan: ' + intyg.funktionsnedsattning.annan);
        luseUtkastPage.funktionsnedsattning.annan.sendKeys(intyg.funktionsnedsattning.annan);
        // logger.info('sjukdomsforlopp: ' + intyg.sjukdomsforlopp);
        luseUtkastPage.sjukdomsforlopp.sendKeys(intyg.sjukdomsForlopp);
        // logger.info('aktivitetsbegransning: ' + intyg.aktivitetsbegransning);
        luseUtkastPage.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning);
        // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        luseUtkastPage.avslutadBehandling.sendKeys(intyg.avslutadBehandling);
        // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        luseUtkastPage.pagaendeBehandling.sendKeys(intyg.pagaendeBehandling);
        // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        luseUtkastPage.planeradBehandling.sendKeys(intyg.planeradBehandling);
        // logger.info('substansintag: ' + intyg.substansintag);
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