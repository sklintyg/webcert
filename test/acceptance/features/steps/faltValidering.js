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
/*globals pages,intyg,protractor,wcTestTools,Promise*/

'use strict';
var luseUtkastPage = pages.intyg.luse.utkast;
var lisjpUtkastPage = pages.intyg.lisjp.utkast;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var testdata = wcTestTools.testdata;
var testdataHelpers = wcTestTools.helpers.testdata;
var tmpDiagnos;
var anhorigIgnoreKeys = ['forsakringsmedicinsktBeslutsstodBeskrivning', 'arbetstidsforlaggning', 'arbetsresor', 'formagaTrotsBegransningBeskrivning', 'prognos'];

function setDiagnos(diagnos) {
    tmpDiagnos = diagnos;
    console.log(diagnos);
}

function populateFieldArray(object, ignoreKeys) {
    var re = [];
    if (object) {
        for (var key in object) {
            if (object.hasOwnProperty(key)) {
                var index = (typeof ignoreKeys !== 'undefined') ? ignoreKeys.indexOf(key) : -1;
                if (index === -1) {

                    re.push(object[key]);
                }
            }
        }
    }
    return re;
}

module.exports = function() {

    this.Given(/^jag fyller i text i "([^"]*)" fältet$/, function(fieldtype) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);


        var date = helpers.randomTextString().substring(0, 4);

        if (isSMIIntyg && fieldtype === 'kännedom-datum') {
            return luseUtkastPage.baseratPa.kannedomOmPatient.datum.sendKeys(date);
        } else if (isSMIIntyg && fieldtype === 'slumpat-datum') {
            return testdataHelpers.shuffle(populateFieldArray(luseUtkastPage.baseratPa, ['anhorigBeskrivning', 'kannedomOmPatient']))[0].datum.sendKeys(date);

        } else if (isSMIIntyg && fieldtype === 'underlag-datum') {
            luseUtkastPage.andraMedicinskaUtredningar.finns.JA.sendKeys(protractor.Key.SPACE);
            return testdataHelpers.shuffle(populateFieldArray(luseUtkastPage.underlag))[0].datum.sendKeys(date);

        } else if (isSMIIntyg && fieldtype === 'postnummer') {
            return luseUtkastPage.enhetensAdress.postNummer.sendKeys(date);
        } else if (isSMIIntyg && fieldtype === 'arbetsförmåga-datum') {
            var arbetsfarmagaProcent = testdataHelpers.shuffle(populateFieldArray(lisjpUtkastPage.sjukskrivning, anhorigIgnoreKeys))[0];
            return testdataHelpers.shuffle([arbetsfarmagaProcent.fran, arbetsfarmagaProcent.till])[0].sendKeys(date);
        } else {
            return fkUtkastPage.diagnosKod.sendKeys(date);
        }

    });

    this.Given(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);

        if (isSMIIntyg) {
            return luseUtkastPage.diagnoseCode.sendKeys(dKod);
        } else {
            return fkUtkastPage.diagnosKod.sendKeys(dKod);
        }


    });
    this.Given(/^jag fyller i diagnoskod$/, function() {

        var diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
        setDiagnos(diagnos);
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
        } else {
            return fkUtkastPage.angeDiagnosKod(diagnos.kod);
        }

    });
    this.Given(/^jag fyller i diagnoskod utan egen FMB info$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.utanEgenFMBInfo.diagnoser)[0];
        setDiagnos(diagnos);
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
        } else {
            return fkUtkastPage.angeDiagnosKod(diagnos.kod);
        }
    });

    this.Given(/^ska rätt info gällande FMB visas$/, function() {

        var promiseArray = [];
        if (tmpDiagnos.falt[0]) { //Symptom prognos och behandling

            promiseArray.push(expect(element(by.id('fmb_text_SYMPTOM_PROGNOS_BEHANDLING')).getText()).to.eventually.contain(tmpDiagnos.falt[0]));
        }
        if (tmpDiagnos.falt[1]) { //Generell informationl
            promiseArray.push(expect(element(by.id('fmb_text_GENERELL_INFO')).getText()).to.eventually.contain(tmpDiagnos.falt[1]));

        }
        if (tmpDiagnos.falt[2]) { //Funktionsnedsättning
            promiseArray.push(expect(element(by.id('fmb_text_FUNKTIONSNEDSATTNING')).getText()).to.eventually.contain(tmpDiagnos.falt[2]));

        }
        if (tmpDiagnos.falt[3]) { //Aktivitetsbegränsning
            promiseArray.push(expect(element(by.id('fmb_text_AKTIVITETSBEGRANSNING')).getText()).to.eventually.contain(tmpDiagnos.falt[3]));

        }
        if (tmpDiagnos.falt[4]) { //Beslutsunderlag
            promiseArray.push(expect(element(by.id('fmb_text_BESLUTSUNDERLAG_TEXTUELLT')).getText()).to.eventually.contain(tmpDiagnos.falt[4]));

        }

        return Promise.all(promiseArray);

    });
    this.Given(/^ska FMB info för överliggande diagnoskod visas$/, function() {
        var promiseArray = [];
        if (tmpDiagnos.falt[0]) { //Symptom prognos och behandling
            promiseArray.push(expect(element(by.id('fmb_diagnos_not_in_fmb_alert')).getText()).to.eventually.contain(tmpDiagnos.falt[0]));
        }
        if (tmpDiagnos.falt[1]) { //Symptom prognos och behandling
            promiseArray.push(expect(element(by.id('fmb_text_SYMPTOM_PROGNOS_BEHANDLING')).getText()).to.eventually.contain(tmpDiagnos.falt[1]));
        }
        if (tmpDiagnos.falt[2]) { //Generell informationl
            promiseArray.push(expect(element(by.id('fmb_text_GENERELL_INFO')).getText()).to.eventually.contain(tmpDiagnos.falt[2]));

        }
        if (tmpDiagnos.falt[3]) { //Funktionsnedsättning
            promiseArray.push(expect(element(by.id('fmb_text_FUNKTIONSNEDSATTNING')).getText()).to.eventually.contain(tmpDiagnos.falt[3]));
        }
        if (tmpDiagnos.falt[4]) { //Aktivitetsbegränsning
            promiseArray.push(expect(element(by.id('fmb_text_AKTIVITETSBEGRANSNING')).getText()).to.eventually.contain(tmpDiagnos.falt[4]));

        }
        if (tmpDiagnos.falt[5]) { //Beslutsunderlag
            promiseArray.push(expect(element(by.id('fmb_text_BESLUTSUNDERLAG_TEXTUELLT')).getText()).to.eventually.contain(tmpDiagnos.falt[5]));
        }

        return Promise.all(promiseArray);
    });
    this.Given(/^jag fyller i diagnoskod utan FMB info$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.utanFMBInfo.diagnoser)[0];
        setDiagnos(diagnos);
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
        } else {
            return fkUtkastPage.angeDiagnosKod(diagnos.kod);
        }
    });

    this.Given(/^ska ingen info gällande FMB visas$/, function() {
        var promiseArray = [];
        promiseArray.push(expect(element(by.id('fmb_diagnos_not_in_fmb_alert')).isPresent()).to.become(false));
        promiseArray.push(expect(element(by.id('fmb_text_SYMPTOM_PROGNOS_BEHANDLING')).isPresent()).to.become(false));
        promiseArray.push(expect(element(by.id('fmb_text_GENERELL_INFO')).isPresent()).to.become(false));
        promiseArray.push(expect(element(by.id('fmb_text_FUNKTIONSNEDSATTNING')).isPresent()).to.become(false));
        promiseArray.push(expect(element(by.id('fmb_text_AKTIVITETSBEGRANSNING')).isPresent()).to.become(false));
        promiseArray.push(expect(element(by.id('fmb_text_BESLUTSUNDERLAG_TEXTUELLT')).isPresent()).to.become(false));
        return Promise.all(promiseArray);

    });



    this.Given(/^ska valideringsfelet "([^"]*)" visas$/, function(arg1) {
        var alertTexts = element.all(by.css('.alert-warning')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            return expect(result.join('\n')).to.have.string(arg1);

        });

    });

    this.Given(/^ska "([^"]*)" valideringsfelet, "([^"]*)" visas$/, function(arg1, arg2) {

        if (arg1 === 'postnummer') {
            element(by.css('span[stat-key="common.validation.postnummer.incorrect-format"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'underlag') {
            element(by.css('span[stat-key="luse.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'underlag-LUAEFS') {
            element(by.css('span[stat-key="luae_fs.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'underlag-LUAENA') {
            element(by.css('span[stat-key="luae_na.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'datum') {
            element(by.css('span[key="common.validation.date.INVALID_FORMAT"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'kännedom') {
            element(by.css('span[key="common.validation.singleDate.INVALID_FORMAT"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });
        } else if (arg1 === 'arbetsförmåga') {
            element(by.css('span[key="common.validation.date-period.INVALID_FORMAT"]')).getText().then(function(text) {
                return expect(text).to.contain(arg2);
            });

        } else {
            return;
        }


    });
};
