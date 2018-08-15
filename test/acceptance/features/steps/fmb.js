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

/*globals pages,wcTestTools,Promise,logger */


'use strict';

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

const testdataHelpers = wcTestTools.helpers.testdata;
const testdata = wcTestTools.testdata;
const helpers = require('./helpers');
const lisjpUtkastPage = pages.intyg.lisjp.utkast;
const fkUtkastPage = pages.intyg.fk['7263'].utkast;
const fmb = pages.intyg.hogerfaltet.fmb;


/*
 *  Stödfunktioner
 *
 */
function checkFMB(fmbDiagnos, intyg) {
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var page;
    if (isSMIIntyg) {
        page = lisjpUtkastPage;
    } else {
        page = fkUtkastPage;
    }

    var promiseArray = [];

    return fmb.visaMer.klickaAlla().then(function() {

        if (fmbDiagnos.overliggandeDiagnos) {
            promiseArray.push(expect(fmb.container.getText()).to.eventually.contain(fmbDiagnos.overliggandeTxt));
            //TODO Saknar Id på alert-rutan, använder container istället.
            //promiseArray.push(expect(fmb.alert.overOrdnadDiagnos(fmbDiagnos.overliggandeDiagnos).getText()).to.eventually.contain(fmbDiagnos.overliggandeTxt));
        }
        if (fmbDiagnos.symptomPrognosBehandling) {
            promiseArray.push(expect(fmb.dialogs.symptomPrognosBehandling.getText()).to.eventually.contain(fmbDiagnos.symptomPrognosBehandling));
        }
        if (fmbDiagnos.generellInfo) {
            promiseArray.push(expect(fmb.dialogs.generellInfo.getText()).to.eventually.contain(fmbDiagnos.generellInfo));
        }
        if (fmbDiagnos.funktionsnedsattning) {
            promiseArray.push(expect(fmb.dialogs.funktionsnedsattning.getText()).to.eventually.contain(fmbDiagnos.funktionsnedsattning));
        }
        if (fmbDiagnos.aktivitetsbegransning) {
            promiseArray.push(expect(fmb.dialogs.aktivitetsbegransning.getText()).to.eventually.contain(fmbDiagnos.aktivitetsbegransning));
        }
        if (fmbDiagnos.beslutsunderlag) {
            promiseArray.push(expect(fmb.dialogs.beslutsunderlag.getText()).to.eventually.contain(fmbDiagnos.beslutsunderlag));
        }

        logger.info('Kontrollerar FMB texter');
        return Promise.all(promiseArray);
    });
}


function fillInDiagnoskod(diagnos, intyg) {
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

When(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
    return fillInDiagnoskod({
        kod: dKod

    }, this.intyg);
});

When(/^jag fyller i diagnoskod$/, function() {
    let diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
    return fillInDiagnoskod(diagnos, this.intyg);

});

When(/^jag fyller i diagnoskod utan egen FMB info$/, function() {
    var diagnos = testdataHelpers.shuffle(testdata.fmb.utanEgenFMBInfo.diagnoser)[0];
    return fillInDiagnoskod(diagnos, this.intyg);
});

Then(/^ska rätt info gällande FMB visas$/, function() {

    logger.info(global.tmpDiagnos);
    return checkFMB(global.tmpDiagnos, this.intyg);

});

Then(/^ska FMB info för överliggande diagnoskod visas$/, function() {
    logger.info(global.tmpDiagnos);
    return checkFMB(global.tmpDiagnos, this.intyg); //kontrollerar även allert texten
});
When(/^jag fyller i diagnoskod utan FMB info$/, function() {
    var diagnos = testdataHelpers.shuffle(testdata.fmb.utanFMBInfo.diagnoser)[0];
    fillInDiagnoskod(diagnos, this.intyg);
});

Then(/^ska ingen info gällande FMB visas$/, function() {

    var promiseArray = [];

    promiseArray.push(expect(fmb.dialogs.symptomPrognosBehandling.isPresent()).to.become(false));
    promiseArray.push(expect(fmb.dialogs.generellInfo.isPresent()).to.become(false));
    promiseArray.push(expect(fmb.dialogs.funktionsnedsattning.isPresent()).to.become(false));
    promiseArray.push(expect(fmb.dialogs.aktivitetsbegransning.isPresent()).to.become(false));
    promiseArray.push(expect(fmb.dialogs.beslutsunderlag.isPresent()).to.become(false));

    return Promise.all(promiseArray);

});
