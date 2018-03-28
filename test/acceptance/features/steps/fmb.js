/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/*globals pages,intyg,wcTestTools,Promise,logger */


'use strict';

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

let testdataHelpers = wcTestTools.helpers.testdata;
let testdata = wcTestTools.testdata;
let helpers = require('./helpers');
let lisjpUtkastPage = pages.intyg.lisjp.utkast;
let fkUtkastPage = pages.intyg.fk['7263'].utkast;



/*
 *  Stödfunktioner
 *
 */
function checkFMB(fmbDiagnos) {
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var page;
    if (isSMIIntyg) {
        page = lisjpUtkastPage;
    } else {
        page = fkUtkastPage;
    }

    var promiseArray = [];

    return klickaAllaVisaMerFMBLankar().then(function() {

        if (fmbDiagnos.overliggande) {
            promiseArray.push(expect(page.fmbAlertText.getText()).to.eventually.contain(fmbDiagnos.overliggande));
        }
        if (fmbDiagnos.symptomPrognosBehandling) {
            promiseArray.push(expect(page.fmbDialogs.symptomPrognosBehandling.getText()).to.eventually.contain(fmbDiagnos.symptomPrognosBehandling));
        }
        if (fmbDiagnos.generellInfo) {
            promiseArray.push(expect(page.fmbDialogs.generellInfo.getText()).to.eventually.contain(fmbDiagnos.generellInfo));
        }
        if (fmbDiagnos.funktionsnedsattning) {
            promiseArray.push(expect(page.fmbDialogs.funktionsnedsattning.getText()).to.eventually.contain(fmbDiagnos.funktionsnedsattning));
        }
        if (fmbDiagnos.aktivitetsbegransning) {
            promiseArray.push(expect(page.fmbDialogs.aktivitetsbegransning.getText()).to.eventually.contain(fmbDiagnos.aktivitetsbegransning));
        }
        if (fmbDiagnos.beslutsunderlag) {
            promiseArray.push(expect(page.fmbDialogs.beslutsunderlag.getText()).to.eventually.contain(fmbDiagnos.beslutsunderlag));
        }

        logger.info('Kontrollerar FMB texter');
        return Promise.all(promiseArray);
    });
}


function klickaAllaVisaMerFMBLankar() {
    logger.silly('klickaAllaVisaMerFMBLankar');
    var fmbElm = {
        funktionsnedsattning: element(by.id('fmb-text-expandable-content-link-FUNKTIONSNEDSATTNING')),
        symptomPrognosBehandling: element(by.id('fmb-text-expandable-content-link-SYMPTOM_PROGNOS_BEHANDLING')),
        generellt: element(by.id('fmb-text-expandable-content-link-GENERELL_INFO'))
    };
    return Promise.all([
        fmbElm.funktionsnedsattning.isPresent().then(function(present) {
            if (present) {
                logger.silly('Klickar Funktionsnedsattning');
                return fmbElm.funktionsnedsattning.click();
            }
            return;
        }).then(function() {
            return fmbElm.symptomPrognosBehandling.isPresent();
        }).then(function(present) {
            if (present) {
                logger.silly('Klickar SymptomPrognosBehandling');
                return fmbElm.symptomPrognosBehandling.click();
            }
            return;
        }).then(function() {
            return fmbElm.generellt.isPresent();
        }).then(function(present) {
            if (present) {
                logger.silly('Klickar Generellt');
                return fmbElm.generellt.click();
            }
            return;
        })
    ]);
}


function fillInDiagnoskod(diagnos) {
    logger.info('Anger diagnos:', diagnos.kod);
    global.tmpDiagnos = diagnos;
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    if (isSMIIntyg) {
        return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
    } else {
        return fkUtkastPage.angeDiagnosKod(diagnos.kod);

    }
}


/*
 *  Teststeg
 *
 */

Given(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
    return fillInDiagnoskod({
        kod: dKod
    });
});

Given(/^jag fyller i diagnoskod$/, function() {
    let diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
    return fillInDiagnoskod(diagnos);

});

Given(/^jag fyller i diagnoskod utan egen FMB info$/, function() {
    var diagnos = testdataHelpers.shuffle(testdata.fmb.utanEgenFMBInfo.diagnoser)[0];
    return fillInDiagnoskod(diagnos);
});

Given(/^ska rätt info gällande FMB visas$/, function() {

    logger.info(global.tmpDiagnos);
    return checkFMB(global.tmpDiagnos);

});

Given(/^ska FMB info för överliggande diagnoskod visas$/, function() {
    logger.info(global.tmpDiagnos);
    return checkFMB(global.tmpDiagnos); //kontrollerar även allert texten
});
Given(/^jag fyller i diagnoskod utan FMB info$/, function() {
    var diagnos = testdataHelpers.shuffle(testdata.fmb.utanFMBInfo.diagnoser)[0];
    fillInDiagnoskod(diagnos);
});

Given(/^ska ingen info gällande FMB visas$/, function() {
    throw ('TODO - Kontrollera nya FMB fält');
    /*var promiseArray = [];
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var page;
    if (isSMIIntyg) {
        page = lisjpUtkastPage;
    } else {
        page = fkUtkastPage;

    }

    promiseArray.push(expect(page.fmbButtons.falt2.isPresent()).to.become(false));
    promiseArray.push(expect(page.fmbButtons.falt4.isPresent()).to.become(false));
    promiseArray.push(expect(page.fmbButtons.falt5.isPresent()).to.become(false));
    promiseArray.push(expect(page.fmbButtons.falt8.isPresent()).to.become(false));


    return Promise.all(promiseArray);*/

});
