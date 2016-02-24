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

/* globals pages, browser, protractor */

'use strict';
var luseUtkastPage = pages.intyg.fk.luse.utkast;
module.exports = {
    fillIn: function(intyg, cb) {

        browser.ignoreSynchronization = true;

        //Baserat på
        luseUtkastPage.baseratPa.minUndersokningAvPatienten.checkbox.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.baseratPa.kannedomOmPatient.checkbox.sendKeys(protractor.Key.SPACE);

        luseUtkastPage.underlagFinnsNo.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.diagnoseCode.sendKeys('A000');
        luseUtkastPage.diagnosgrund.sendKeys('Ingen grund alls');
        luseUtkastPage.nyBedomningDiagnosgrundNo.sendKeys(protractor.Key.SPACE);

        //Funktionsnedsättning
        luseUtkastPage.funktionsnedsattning.intellektuell.sendKeys('Problem...');
        luseUtkastPage.funktionsnedsattning.kommunikation.sendKeys('Inget tal');
        luseUtkastPage.funktionsnedsattning.koncentration.sendKeys('Ingen koncentration');
        luseUtkastPage.funktionsnedsattning.psykisk.sendKeys('Total');
        luseUtkastPage.funktionsnedsattning.synHorselTal.sendKeys('Blind');
        luseUtkastPage.funktionsnedsattning.balansKoordination.sendKeys('Svajig i benen');
        luseUtkastPage.funktionsnedsattning.annan.sendKeys('Ingen');

        luseUtkastPage.aktivitetsbegransning.sendKeys('Total');
        luseUtkastPage.avslutadBehandling.sendKeys('Ipren');
        luseUtkastPage.pagaendeBehandling.sendKeys('Alvedon');
        luseUtkastPage.planeradBehandling.sendKeys('Bamyl');
        luseUtkastPage.substansintag.sendKeys('Snus');
        luseUtkastPage.medicinskaForutsattningarForArbete.sendKeys('Inte speciellt');
        luseUtkastPage.aktivitetsFormaga.sendKeys('Liten');
        luseUtkastPage.ovrigt.sendKeys('Inget');
        luseUtkastPage.kontaktMedFkNo.sendKeys(protractor.Key.SPACE);
        luseUtkastPage.tillaggsfragor0svar.sendKeys('Question');
        luseUtkastPage.tillaggsfragor1svar.sendKeys('Answer');

        browser.ignoreSynchronization = false;

        // browser.driver.wait(protractor.until.elementIsVisible(luseUtkastPage.signeraButton));
        cb();
    }
};