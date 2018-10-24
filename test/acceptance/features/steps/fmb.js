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

    //Kontroll av rubriker
    if (fmbDiagnos.overliggandeTxt) {
        promiseArray.push(expect(element(by.id('fmb_heading_BESLUTSUNDERLAG_TEXTUELLT')).getText()).to.eventually.contain('Vägledning för sjukskrivning'));
        //console.log("overliggandeTxt: " + element(by.id('fmb_heading_BESLUTSUNDERLAG_TEXTUELLT')).getText());
    }

    if (fmbDiagnos.funktionsnedsattning) {
        promiseArray.push(expect(element(by.id('fmb_heading_FUNKTIONSNEDSATTNING')).getText()).to.eventually.contain('Funktionsnedsättning'));
        //console.log("funktionsnedsattning: " + element(by.id('fmb_heading_FUNKTIONSNEDSATTNING')).getText());
    }

    if (fmbDiagnos.aktivitetsbegransning) {
        promiseArray.push(expect(element(by.id('fmb_heading_AKTIVITETSBEGRANSNING')).getText()).to.eventually.contain('Aktivitetsbegränsning'));
        //console.log("aktivitetsbegransning: " + element(by.id('fmb_heading_AKTIVITETSBEGRANSNING')).getText());
    }

    if (fmbDiagnos.generellInfo) {
        promiseArray.push(expect(element(by.id('fmb_heading_GENERELL_INFO')).getText()).to.eventually.contain('Försäkringsmedicinsk information'));
        //console.log("generellInfo: " + element(by.id('fmb_heading_GENERELL_INFO')).getText());
    }

    if (fmbDiagnos.symptomPrognosBehandling) {
        promiseArray.push(expect(element(by.id('fmb_heading_SYMPTOM_PROGNOS_BEHANDLING')).getText()).to.eventually.contain('Symtom, prognos, behandling'));
        //console.log("symptomPrognosBehandling: " + element(by.id('fmb_heading_SYMPTOM_PROGNOS_BEHANDLING')).getText());
    }


    return fmb.visaMer.klickaAlla().then(function() {
        /* 
		Kontroll av diagnosspecifik information
		Testdata i:
		..\webcert\test\webcertTestTools\testdata\diagnoskoderFMB.js
		..\acceptance\node_modules\webcert-testtools\testdata\diagnoskoderFMB.js
		*/

        /*
		if (fmbDiagnos.overliggandeDiagnos) {
  			console.log("GETTEXT:  fmb_diagnos_radio_" + fmbDiagnos.kod);
            var radioButton = "fmb_diagnos_radio_" + fmbDiagnos.kod;

            promiseArray.push(expect(element(by.id(radioButton)).getText().to.eventually.contain(fmbDiagnos.overliggandeDiagnos)));
            //promiseArray.push(expect(element(by.id('fmb_diagnos_radio_' + fmbDiagnos.kod)).getText().to.eventually.contain(fmbDiagnos.overliggandeDiagnos)));
            console.log("Vägledning för sjukskrivning: " + element(by.id('fmb_diagnos_radio_' + fmbDiagnos.kod).getText()));
        }
		*/
        if (fmbDiagnos.vagledningForSjukskrivning) {
            promiseArray.push(expect(element(by.id('fmb_bullet_BESLUTSUNDERLAG_TEXTUELLT_0')).getText()).to.eventually.contain(fmbDiagnos.vagledningForSjukskrivning));
            //console.log("vagledningForSjukskrivning: " + element(by.id('fmb_bullet_BESLUTSUNDERLAG_TEXTUELLT_0')).getText());
        }

        if (fmbDiagnos.funktionsnedsattning) {
            promiseArray.push(expect(element(by.id('fmb_text_FUNKTIONSNEDSATTNING')).getText()).to.eventually.contain(fmbDiagnos.funktionsnedsattning));
            //console.log("funktionsnedsattning: " + element(by.id('fmb_text_FUNKTIONSNEDSATTNING')).getText());
        }

        if (fmbDiagnos.generellInfo) {
            promiseArray.push(expect(element(by.id('fmb_text_GENERELL_INFO')).getText()).to.eventually.contain(fmbDiagnos.generellInfo));
            //console.log("generellInfo: " + element(by.id('fmb_text_GENERELL_INFO')).getText());
        }

        if (fmbDiagnos.aktivitetsbegransning) {
            promiseArray.push(expect(element(by.id('fmb_text_AKTIVITETSBEGRANSNING')).getText()).to.eventually.contain(fmbDiagnos.aktivitetsbegransning));
            //console.log("aktivitetsbegransning: " + element(by.id('fmb_text_AKTIVITETSBEGRANSNING')).getAttribute());
        }

        if (fmbDiagnos.symptomPrognosBehandling) {
            promiseArray.push(expect(element(by.id('fmb_text_SYMPTOM_PROGNOS_BEHANDLING')).getText()).to.eventually.contain(fmbDiagnos.symptomPrognosBehandling));
            //console.log("symptomPrognosBehandling: " + element(by.id('fmb_text_SYMPTOM_PROGNOS_BEHANDLING')).getText());
        }


        logger.info('Kontrollerar FMB texter');
        //console.log("fmb-diagnos: " + promiseArray);
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
