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

/*globals pages,intyg,protractor,wcTestTools,Promise,browser,logger*/


'use strict';
var luseUtkastPage = pages.intyg.luse.utkast;
var lisjpUtkastPage = pages.intyg.lisjp.utkast;
var tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;

var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var fillInIntyg = require('./fillIn/fill_in_intyg_steps');
var testdata = wcTestTools.testdata;
var testdataHelpers = wcTestTools.helpers.testdata;

var synVar = tsdUtkastPage.syn;
var anhorigIgnoreKeys = ['forsakringsmedicinsktBeslutsstodBeskrivning', 'arbetstidsforlaggning', 'arbetsresor', 'formagaTrotsBegransningBeskrivning', 'prognos'];
var synVarArray = [synVar.hoger.utan, synVar.hoger.med, synVar.vanster.utan, synVar.vanster.med, synVar.binokulart.utan, synVar.binokulart.med];

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

function antalAvLoop(array, str1) {
    var counter = 0;
    for (var i = 0; i < array.length; i++) {
        if (array[i] === str1) {
            counter++;
        }
    }
    return String(counter);
}

function synLoop(array, keyToSend) {
    var counter = 0;
    array.forEach(function(el) {
        el.sendKeys(keyToSend);
        counter++;
        if (counter === array.length) {
            return Promise.resolve();
        }

    });
    synVar.binokulart.med.sendKeys(protractor.Key.TAB);
}

function populateSynTSD(typAvSyn) {

    var slumpatSynFalt = testdataHelpers.shuffle([synVar.hoger, synVar.vanster, synVar.binokulart])[0];

    if (typAvSyn === 'slumpat synfält') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return slumpatSynFalt.utan.sendKeys('9').then(function() {
            return slumpatSynFalt.med.sendKeys('8').sendKeys(protractor.Key.TAB);
        });
        // });
    } else if (typAvSyn === 'alla synfält') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return synLoop(synVarArray, 9);
        // });
    }
}

function checkFMB(fmbDiagnos) {
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var page;
    if (isSMIIntyg) {
        page = lisjpUtkastPage;
    } else {
        page = fkUtkastPage;
    }
    var elm = page.fmbButtons.falt2;
    return elm.sendKeys(protractor.Key.SPACE).then(function() {
            return browser.sleep(2000);
        })
        .then(function() {

            var promiseArray = [];

            if (fmbDiagnos.overliggande) {
                logger.info('Kontrollerar överliggande');
                promiseArray.push(expect(page.fmbAlertText.getText()).to.eventually.contain(fmbDiagnos.overliggande));

            }
            if (fmbDiagnos.symptomPrognosBehandling) {
                logger.info('Kontrollerar Symtom Prognos Behandling');
                promiseArray.push(expect(page.fmbDialogs.symptomPrognosBehandling.getText()).to.eventually.contain(fmbDiagnos.symptomPrognosBehandling));

            }
            if (fmbDiagnos.generellInfo) {
                logger.info('Kontrollerar Generell info');
                promiseArray.push(expect(page.fmbDialogs.generellInfo.getText()).to.eventually.contain(fmbDiagnos.generellInfo));

            }
            if (fmbDiagnos.funktionsnedsattning) {
                logger.info('Kontrollerar Funktionsnedsättning');
                promiseArray.push(

                    page.fmbButtons.falt4.sendKeys(protractor.Key.SPACE)
                    .then(function() {
                        return browser.sleep(2000);
                    })
                    .then(function() {
                        return expect(page.fmbDialogs.funktionsnedsattning.getText()).to.eventually.contain(fmbDiagnos.funktionsnedsattning);
                    }));
            }
            if (fmbDiagnos.aktivitetsbegransning) {
                logger.info('Kontrollerar Aktivietsbegränsning');
                promiseArray.push(
                    page.fmbButtons.falt5.sendKeys(protractor.Key.SPACE)
                    .then(function() {
                        return browser.sleep(2000);
                    })
                    .then(function() {
                        return expect(page.fmbDialogs.aktivitetsbegransning.getText()).to.eventually.contain(fmbDiagnos.aktivitetsbegransning);
                    }));

            }
            if (fmbDiagnos.beslutsunderlag) {
                logger.info('Kontrollerar Beslutsunderlag');

                promiseArray.push(page.fmbButtons.falt8.sendKeys(protractor.Key.SPACE)
                    .then(function() {
                        return browser.sleep(2000);
                    })
                    .then(function() {
                        return expect(page.fmbDialogs.beslutsunderlag.getText()).to.eventually.contain(fmbDiagnos.beslutsunderlag);
                    }));
            }
            return Promise.all(promiseArray);

        });

}


function fillInDiagnoskod(diagnos) {
    logger.info('Anger diagnos:', diagnos);
    global.tmpDiagnos = diagnos;
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    if (isSMIIntyg) {
        return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
    } else {
        return fkUtkastPage.angeDiagnosKod(diagnos.kod);

    }
}

module.exports = function() {

    this.Given(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
        return fillInDiagnoskod({
            kod: dKod
        });


    });
    this.Given(/^jag fyller i diagnoskod$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
        return fillInDiagnoskod(diagnos);

    });
    this.Given(/^jag fyller i diagnoskod utan egen FMB info$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.utanEgenFMBInfo.diagnoser)[0];
        return fillInDiagnoskod(diagnos);
    });


    this.Given(/^ska rätt info gällande FMB visas$/, function() {

        logger.info(global.tmpDiagnos);
        return checkFMB(global.tmpDiagnos);

    });

    this.Given(/^ska FMB info för överliggande diagnoskod visas$/, function() {
        logger.info(global.tmpDiagnos);
        return checkFMB(global.tmpDiagnos); //kontrollerar även allert texten
    });
    this.Given(/^jag fyller i diagnoskod utan FMB info$/, function() {
        var diagnos = testdataHelpers.shuffle(testdata.fmb.utanFMBInfo.diagnoser)[0];
        fillInDiagnoskod(diagnos);
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

        promiseArray.push(expect(page.fmbButtons.falt2.isDisplayed()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt4.isDisplayed()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt5.isDisplayed()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt8.isDisplayed()).to.become(false));


        return Promise.all(promiseArray);

    });

    this.Given(/^ska valideringsfelet "([^"]*)" visas$/, function(arg1) {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            // console.log(result);
            return expect(result.join('\n')).to.have.string(arg1);
        });
    });


    this.Given(/^ska valideringsfelet "([^"]*)" visas "([^"]*)" gånger$/, function(arg1, arg2) {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            // console.log(result);
            return expect(antalAvLoop(result, arg1)).to.equal(arg2);
        });

    });

    this.Given(/^ska alla (standard|utökade) valideringsfel för "([^"]*)" visas*$/, function(arg1, intygsTyp) {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        if (arg1 === 'standard' && intygsTyp === 'Transportstyrelsens läkarintyg, diabetes') {
            return alertTexts.then(function(result) {
                // console.log(result);
                expect(antalAvLoop(result, 'Fältet får inte vara tomt.')).to.be.oneOf(['1', '4']);
                expect(antalAvLoop(result, 'Du måste välja minst ett alternativ.')).to.equal('3');
                expect(antalAvLoop(result, 'Du måste välja ett alternativ.')).to.equal('4');
                expect(antalAvLoop(result, 'Minst en behandling måste väljas.')).to.equal('1');
            });
        }
        if (arg1 === 'utökade' && intygsTyp === 'Transportstyrelsens läkarintyg, diabetes') {
            return alertTexts.then(function(result) {
                // console.log(result);
                expect(antalAvLoop(result, 'Fältet får inte vara tomt.')).to.be.oneOf(['6', '9']);
                expect(antalAvLoop(result, 'Du måste välja minst ett alternativ.')).to.equal('3');
                expect(antalAvLoop(result, 'Du måste välja ett alternativ.')).to.equal('5');
                expect(antalAvLoop(result, 'År då behandling med insulin påbörjades måste anges.')).to.equal('1');
            });
        }
    });





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
        }
        // else if (fieldtype === 'diabetes-artal') {
        //     return tsdUtkastPage.fillInAllmant({
        //         year: 'text',
        //         typ: 'Typ 1',
        //         behandling: {
        //             typer: ['Insulin'],
        //             insulinYear: 'text'
        //         }
        //     }).then(function() {
        //         return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys(protractor.Key.TAB);
        //     });

        // } 
        else if (fieldtype === 'diabetes-årtal') {

            return tsdUtkastPage.allmant.diabetesyear.sendKeys('text').then(function() {
                return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys('text').then(function() {
                    return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys(protractor.Key.TAB);
                });
            });



        } else if (fieldtype === 'UndersökningsDatum') {

            return fkUtkastPage.baserasPa.minUndersokning.datum.sendKeys('10/12-2017').then(function() {
                logger.info('Fyller i felaktigt formaterat datum: 10/12-2017');
                var enter = browser.actions().sendKeys(protractor.Key.ENTER);
                return enter.perform();
            });


        } else if (fieldtype === 'slumpat synfält' || fieldtype === 'alla synfält') {
            return populateSynTSD(fieldtype);
        } else {
            return fkUtkastPage.diagnosKod.sendKeys(date);
        }

    });

    this.Given(/^jag raderar ett  slumpat obligatoriskt fält$/, function(callback) {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var intygShortcode = helpers.getAbbrev(intyg.typ);

        fillInIntyg.changingFields(isSMIIntyg, intygShortcode, callback, true);
        //fillInIntyg.chooseRandomFieldBasedOnIntyg(isSMIIntyg, false, callback, true);


    });

    this.Given(/^jag kryssar i Prognos Går ej att bedöma utan beskrivning$/, function(callback) {

        fkUtkastPage.prognos.GAR_EJ_ATT_BEDOMA.sendKeys(protractor.Key.SPACE).then(function() {
            fkUtkastPage.prognos.fortydligande.clear().then(callback);
        });



    });
    this.Given(/^jag ändrar till giltig text i "([^"]*)"$/, function(fieldtype) {
        if (fieldtype === 'UndersökningsDatum') {
            return fkUtkastPage.baserasPa.minUndersokning.datum.clear().then(function() {
                return fkUtkastPage.baserasPa.minUndersokning.datum.sendKeys('2017-01-12').then(function() {
                    var enter = browser.actions().sendKeys(protractor.Key.ENTER);
                    console.log('Ändrar datum');
                    return enter.perform();

                });
            });
        }

    });




};
