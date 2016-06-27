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
var lisuUtkastPage = pages.intyg.lisu.utkast;

module.exports = {
    fillIn: function(intyg, cb) {
        logger.info('intyg.typ:' + intyg.typ);
        browser.ignoreSynchronization = true;

        //Baserat på
        lisuUtkastPage.baseratPa.minUndersokningAvPatienten.checkbox.sendKeys(protractor.Key.SPACE);
        lisuUtkastPage.baseratPa.journaluppgifter.checkbox.sendKeys(protractor.Key.SPACE);

        // Sysselsättning
        lisuUtkastPage.sysselsattning.typ.nuvarandeArbete.sendKeys(protractor.Key.SPACE);
        lisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.sendKeys(intyg.nuvarandeArbeteBeskrivning);

        // Diagnos
        lisuUtkastPage.diagnoseCode.sendKeys(intyg.diagnos.kod);
        lisuUtkastPage.diagnoseCode.sendKeys(protractor.Key.TAB);

        // Konsekvenser för patient
        lisuUtkastPage.konsekvenser.funktionsnedsattning.sendKeys(intyg.funktionsnedsattning);
        lisuUtkastPage.konsekvenser.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning);

        // Bedömin
        lisuUtkastPage.sjukskrivning[100].fran.sendKeys(intyg.sjukskrivning.fran);
        lisuUtkastPage.sjukskrivning[100].till.sendKeys(intyg.sjukskrivning.till);
        lisuUtkastPage.sjukskrivning.forsakringsmedicinsktBeslutsstodBeskrivning.sendKeys(intyg.sjukskrivning.forsakringsmedicinsktBeslutsstodBeskrivning);
        lisuUtkastPage.sjukskrivning.arbetsresor.ja.sendKeys(protractor.Key.SPACE);
        lisuUtkastPage.sjukskrivning.formagaTrotsBegransningBeskrivning.sendKeys(intyg.sjukskrivning.formagaTrotsBegransningBeskrivning);
        lisuUtkastPage.sjukskrivning.prognos.typ[1].sendKeys(protractor.Key.SPACE);

        // Åtgärd
        lisuUtkastPage.atgarder.typ[1].sendKeys(protractor.Key.SPACE);
        browser.ignoreSynchronization = false; // Cannot ignore synchronization in this step
        lisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderEjAktuelltBeskrivning.sendKeys(intyg.arbetslivsinriktadeAtgarderEjAktuelltBeskrivning);
        browser.ignoreSynchronization = true;



        browser.ignoreSynchronization = false;

        // browser.driver.wait(protractor.until.elementIsVisible(luseUtkastPage.signeraButton));
        cb();
    }
};
