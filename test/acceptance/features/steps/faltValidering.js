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
    // console.log(diagnos);

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

function checkFMB(promArray, isSMIIntyg, egenDiagnos, fmbDiagnos, antalDiagnoser) {
    var page;
    if (isSMIIntyg) {
        page = lisjpUtkastPage;
    } else {
        page = fkUtkastPage;
    }
    console.log(antalDiagnoser);
    var elm;
    elm = element(by.id(page.fmbButtons[0]));
    elm.sendKeys(protractor.Key.SPACE);
    if (egenDiagnos === false) { //kontrollerar allerttexten att det är en överliggande fmb text
        promArray.push(expect(page.fmbAlertText.getText()).to.eventually.contain(fmbDiagnos.falt[5]));
    }
    for (var k = 0; k < 2; k++) {
        if (fmbDiagnos.falt[k]) { //kontrollerar texterna i diagnosrutan
            promArray.push(expect(element(by.id(page.fmbDialogs[k])).getText()).to.eventually.contain(fmbDiagnos.falt[k]));
        }

    }
    for (var i = 2; i < antalDiagnoser; i++) {
        if (fmbDiagnos.falt[i]) { //kontroll av övriga fmbtexter
            elm = element(by.id(page.fmbButtons[i - 1]));
            elm.sendKeys(protractor.Key.SPACE);
            promArray.push(expect(element(by.id(page.fmbDialogs[i])).getText()).to.eventually.contain(fmbDiagnos.falt[i]));
        }
    }
    return Promise.all(promArray);
}


module.exports = function() {

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
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var promiseArray = [];
        //var diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
        //setDiagnos(diagnos);
        console.log(tmpDiagnos);

        if (isSMIIntyg) {
            return checkFMB(promiseArray, true, true, tmpDiagnos, tmpDiagnos.falt.length);
        } else {
            return checkFMB(promiseArray, false, true, tmpDiagnos, tmpDiagnos.falt.length);
        }
        return Promise.all(promiseArray);
    });

    this.Given(/^ska FMB info för överliggande diagnoskod visas$/, function() {
        var promiseArray = [];
        console.log(tmpDiagnos);

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            return checkFMB(promiseArray, true, false, tmpDiagnos, tmpDiagnos.falt.length - 1); //kontrollerar även allert texten

        } else {
            return checkFMB(promiseArray, false, false, tmpDiagnos, tmpDiagnos.falt.length - 1);
        }
        return Promise.all(promiseArray);
    });
    this.Given(/^jag fyller i diagnoskod utan FMB info$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.utanFMBInfo.diagnoser)[0];
        setDiagnos(diagnos);
        console.log(diagnos);
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
        } else {
            return fkUtkastPage.angeDiagnosKod(diagnos.kod);
        }
    });

    this.Given(/^ska ingen info gällande FMB visas$/, function() {
        var promiseArray = [];
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var page;
        if (isSMIIntyg) {
            page = lisjpUtkastPage;
        } else {
            page = fkUtkastPage;

        }

        promiseArray.push(expect(element(by.id(page.fmbButtons[0])).isDisplayed()).to.become(false));
        promiseArray.push(expect(element(by.id(page.fmbButtons[1])).isDisplayed()).to.become(false));
        promiseArray.push(expect(element(by.id(page.fmbButtons[2])).isDisplayed()).to.become(false));
        promiseArray.push(expect(element(by.id(page.fmbButtons[3])).isDisplayed()).to.become(false));


        return Promise.all(promiseArray);

    });

    this.Given(/^ska valideringsfelet "([^"]*)" visas$/, function(arg1) {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            return expect(result.join('\n')).to.have.string(arg1);
        });
    });

    // this.Given(/^ska "([^"]*)" valideringsfelet, "([^"]*)" visas$/, function(arg1, arg2) {

    //     if (arg1 === 'postnummer') {
    //         element(by.css('span[stat-key="common.validation.postnummer.incorrect-format"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'underlag') {
    //         element(by.css('span[stat-key="luse.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'underlag-LUAEFS') {
    //         element(by.css('span[stat-key="luae_fs.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'underlag-LUAENA') {
    //         element(by.css('span[stat-key="luae_na.validation.underlag.date.incorrect_format"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'datum') {
    //         element(by.css('span[key="common.validation.date.INVALID_FORMAT"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'kännedom') {
    //         element(by.css('span[key="common.validation.singleDate.INVALID_FORMAT"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });
    //     } else if (arg1 === 'arbetsförmåga') {
    //         element(by.css('span[key="common.validation.date-period.INVALID_FORMAT"]')).getText().then(function(text) {
    //             return expect(text).to.contain(arg2);
    //         });

    //     } else {
    //         return;
    //     }

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


    // });
    this.Given(/^jag raderar ett  slumpat obligatoriskt fält$/, function(callback) {

    });

};
