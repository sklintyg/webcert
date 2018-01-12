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

/*globals pages,intyg,protractor,wcTestTools,Promise,browser,logger*/


'use strict';
var luseUtkastPage = pages.intyg.luse.utkast;
var lisjpUtkastPage = pages.intyg.lisjp.utkast;
var tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
var tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;

var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var fillInIntyg = require('./fillIn/fill_in_intyg_steps');
var testdata = wcTestTools.testdata;
var testdataHelpers = wcTestTools.helpers.testdata;

var synVarTSD = tsdUtkastPage.syn;
var synVarBAS = tsBasUtkastPage.syn;

var anhorigIgnoreKeys = ['forsakringsmedicinsktBeslutsstodBeskrivning', 'arbetstidsforlaggning', 'arbetsresor', 'formagaTrotsBegransningBeskrivning', 'prognos'];
var synVarArrayTSD = [synVarTSD.hoger.utan, synVarTSD.hoger.med, synVarTSD.vanster.utan, synVarTSD.vanster.med, synVarTSD.binokulart.utan, synVarTSD.binokulart.med];
var synVarArrayBAS = [synVarBAS.hoger.utan, synVarBAS.hoger.med, synVarBAS.vanster.utan, synVarBAS.vanster.med, synVarBAS.binokulart.utan, synVarBAS.binokulart.med];

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
    var promiseArray = [];

    array.forEach(function(el) {
        promiseArray.push(helpers.moveAndSendKeys(el, keyToSend));
    });

    return Promise.all(promiseArray);
    // synVar.binokulart.med.sendKeys(protractor.Key.TAB);
}

function populateSyn(typAvSyn) {

    var slumpatSynFaltTSD = testdataHelpers.shuffle([synVarTSD.hoger, synVarTSD.vanster, synVarTSD.binokulart])[0];
    var slumpatSynFaltBAS = testdataHelpers.shuffle([synVarBAS.hoger, synVarBAS.vanster, synVarBAS.binokulart])[0];

    if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return slumpatSynFaltTSD.utan.sendKeys('9').then(function() {
            return slumpatSynFaltTSD.med.sendKeys('8').sendKeys(protractor.Key.TAB);
        });
        // });
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return synLoop(synVarArrayTSD, 9);
        // });
    } else if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return slumpatSynFaltBAS.utan.sendKeys('9').then(function() {
            return slumpatSynFaltBAS.med.sendKeys('8').sendKeys(protractor.Key.TAB);
        });
        // });
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        // return synVar.a.no.sendKeys(protractor.Key.SPACE).then(function() {
        return synLoop(synVarArrayBAS, 9);
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
    logger.info('Anger diagnos:', diagnos.kod);
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

        promiseArray.push(expect(page.fmbButtons.falt2.isPresent()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt4.isPresent()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt5.isPresent()).to.become(false));
        promiseArray.push(expect(page.fmbButtons.falt8.isPresent()).to.become(false));


        return Promise.all(promiseArray);

    });

    this.Given(/^ska valideringsfelet "([^"]*)" visas$/, function(fel) {
        return element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        }).then(function(result) {
            logger.silly(result);
            return expect(result.join('\n')).to.have.string(fel);
        });
    });
    this.Given(/^ska valideringsfelet "([^"]*)"  inte visas$/, function(fel) {
        return element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        }).then(function(result) {
            logger.silly(result);
            return expect(result.join('\n')).to.not.have.string(fel);
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
                expect(antalAvLoop(result, 'Fältet får inte vara tomt.')).to.be.oneOf(['7', '10']);
                expect(antalAvLoop(result, 'Du måste välja minst ett alternativ.')).to.equal('2');
                expect(antalAvLoop(result, 'Du måste välja ett alternativ.')).to.equal('7');
                expect(antalAvLoop(result, 'År då behandling med insulin påbörjades måste anges.')).to.equal('1');
            });
        }
        if (arg1 === 'standard' && intygsTyp === 'Transportstyrelsens läkarintyg') {
            return alertTexts.then(function(result) {
                // console.log(result);
                expect(antalAvLoop(result, 'Fältet får inte vara tomt.')).to.be.oneOf(['3', '6']);
                expect(antalAvLoop(result, 'Du måste välja minst ett alternativ.')).to.equal('3');
                expect(antalAvLoop(result, 'Du måste välja ett alternativ.')).to.equal('24');
            });
        }
        if (arg1 === 'utökade' && intygsTyp === 'Transportstyrelsens läkarintyg') {
            return alertTexts.then(function(result) {
                // console.log(result);
                expect(antalAvLoop(result, 'Fältet får inte vara tomt.')).to.be.oneOf(['10', '13']);
                expect(antalAvLoop(result, 'Du måste välja minst ett alternativ.')).to.equal('3');
                expect(antalAvLoop(result, 'Du måste välja ett alternativ.')).to.equal('20');
            });
        }
    });

    this.Given(/^ska inga valideringsfel visas$/, function() {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            // console.log(result);
            result.forEach(function(n) {
                // console.log(n += 'H');
                expect(n.length).to.be.at.most(1);
            });
        });
    });


    this.Given(/^jag fyller i text i insulin\-datum fältet$/, function() {
        return tsdUtkastPage.fillInAllmant({
            year: 'text',
            typ: 'Typ 1',
            behandling: {
                typer: ['Insulin'],
                insulinYear: 'text'
            }
        }).then(function() {
            return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys(protractor.Key.TAB);
        });
    });

    this.Given(/^jag fyller i text i "([^"]*)" fältet$/, function(fieldtype) {
        /*jshint maxcomplexity:12 */

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);

        if (intyg.typ === 'Läkarintyg för sjukpenning' && fieldtype === 'underlag-datum') {
            logger.warn('andraMedicinskaUtredningar finns inte för LISJP //TODO: byt ut underlag-datum till något annat, här och i helpers.');
        }

        var date = helpers.randomTextString().substring(0, 4);

        if (isSMIIntyg) {
            switch (fieldtype) {
                case 'kännedom-datum':
                    return luseUtkastPage.baseratPa.kannedomOmPatient.datum.sendKeys(date);
                case 'underlag-datum':
                    return luseUtkastPage.andraMedicinskaUtredningar.finns.JA.sendKeys(protractor.Key.SPACE).then(function() {
                        return testdataHelpers.shuffle(populateFieldArray(luseUtkastPage.underlag))[0].datum.sendKeys(date);
                    });
                case 'slumpat-datum':
                    return testdataHelpers.shuffle(populateFieldArray(luseUtkastPage.baseratPa, ['anhorigBeskrivning', 'kannedomOmPatient']))[0].datum.sendKeys(date);
                case 'postnummer':
                    return luseUtkastPage.enhetensAdress.postNummer.clear().then(function() {
                        return luseUtkastPage.enhetensAdress.postNummer.sendKeys('111111');
                    });
                case 'arbetsförmåga-datum':
                    var arbetsfarmagaProcent = testdataHelpers.shuffle(populateFieldArray(lisjpUtkastPage.sjukskrivning, anhorigIgnoreKeys))[0];
                    return testdataHelpers.shuffle([arbetsfarmagaProcent.fran, arbetsfarmagaProcent.till])[0].sendKeys(date);
                default:
                    return logger.warn('Klarade inte att matcha fieldtype');
            }
        } else {
            switch (fieldtype) {
                case 'diabetes-årtal':
                    return tsdUtkastPage.allmant.diabetesyear.sendKeys('text').then(function() {
                        return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys('text').then(function() {
                            return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys(protractor.Key.TAB);
                        });
                    });
                case 'UndersökningsDatum':
                    return fkUtkastPage.baserasPa.minUndersokning.datum.sendKeys('10/12-2017').then(function() {
                        logger.info('Fyller i felaktigt formaterat datum: 10/12-2017');
                        return helpers.enter.perform();
                    });
                case 'alla synfält':
                    return populateSyn(fieldtype);
                case 'slumpat synfält':
                    return populateSyn(fieldtype);
                default:
                    //return fkUtkastPage.diagnosKod.sendKeys(date); //TODO default borde vara felhantering
					return logger.warn('Klarade inte att matcha fieldtype');
            }
        }
    });

    this.Given(/^jag tar bort information i "([^"]*)" fältet$/, function(fieldtype) {
        if (fieldtype === 'diabetes-allmant') {
            return tsdUtkastPage.allmant.diabetesyear.clear().then(function() {
                return tsdUtkastPage.allmant.insulinbehandlingsperiod.clear().then(function() {
                    return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys(protractor.Key.TAB).then(function() {
                        return element(by.cssContainingText('label.checkbox', 'Insulin')).sendKeys(protractor.Key.SPACE);
                    });

                });
            });
        }
        if (fieldtype === 'synfälten' && intyg.typ === 'Transportstyrelsens läkarintyg') {
            return tsBasUtkastPage.syn.hoger.utan.clear().then(function() {
                return tsBasUtkastPage.syn.hoger.med.clear().then(function() {
                    return tsBasUtkastPage.syn.vanster.utan.clear().then(function() {
                        return tsBasUtkastPage.syn.vanster.med.clear().then(function() {
                            return tsBasUtkastPage.syn.binokulart.utan.clear().then(function() {
                                return tsBasUtkastPage.syn.binokulart.med.clear().then(function() {

                                });
                            });
                        });
                    });

                });
            });
        }

    });


    this.Given(/^jag lägger till fältet "([^"]*)"$/, function(fieldtype) {

        switch (fieldtype) {
            case 'Intyget baseras på':
                return helpers.moveAndSendKeys(fkUtkastPage.baserasPa.minUndersokning.datum, '2016-12-10').then(function() {
                    logger.info('Fyller i rätt datum: 2016-12-10 Intyget baseras på');
                    return helpers.enter.perform();
                });

            case 'Arbete':
                logger.info('Arbete switch');
                return helpers.moveAndSendKeys(fkUtkastPage.nuvarandeArbete, 'Testare');

            case 'Aktivitetsbegransning':
                logger.info('Ändrar Aktivitetsbegransning');
                return helpers.moveAndSendKeys(fkUtkastPage.aktivitetsBegransning, 'Aktivitetsbegransning');

            case 'Funktionsnedsattning':
                logger.info('Ändrar Funktionsnedsattning');
                return helpers.moveAndSendKeys(fkUtkastPage.funktionsNedsattning, 'Funktionsnedsättning');

            case 'Går ej att bedöma':
                logger.info('Ändrar Går ej att bedöma');
                return fkUtkastPage.prognos.GAR_EJ_ATT_BEDOMA.click();

            case 'Diagnoskod':
                logger.info('Ändrar Diagnoskod');
                return fkUtkastPage.angeDiagnosKod('A00');
            case 'Arbetsförmåga':
                logger.info('Ändrar arbetsförmåga');
                return helpers.moveAndSendKeys(fkUtkastPage.nedsatt.med100.checkbox, protractor.Key.SPACE);

            case 'Intyget baseras på Annat':
                logger.info('Fyller i rätt datum: 2016-12-10 Annat ');
                return helpers.moveAndSendKeys(fkUtkastPage.baserasPa.annat.datum, '2016-12-10').then(function() {
                    return helpers.enter.perform();
                });
            case 'UndersökningsDatum':
                return fkUtkastPage.baserasPa.minUndersokning.datum.clear().then(function() {
                    return helpers.moveAndSendKeys(fkUtkastPage.baserasPa.minUndersokning.datum, '2017-01-12').then(function() {
                        logger.info('Ändrar undersökningsdatum: 2017-01-12 ');
                        //console.log('Ändrar datum');
                        return helpers.enter.perform();
                    });
                });
            default:
                logger.error('Felaktigt Fält valt');
                break;
        }

    });
    this.Given(/^jag fyller i blanksteg i "([^"]*)" fältet$/, function(field) {
        if (field === 'Funktionsnedsattning') {
            fkUtkastPage.funktionsNedsattning.sendKeys(protractor.Key.SPACE);

            return helpers.enter.perform();
        } else if (field === 'Aktivitetsbegransning') {
            fkUtkastPage.aktivitetsbegransning.sendKeys(protractor.Key.SPACE);

            return helpers.enter.perform();
        } else if (field === 'Arbete') {
            fkUtkastPage.nuvarandeArbete.sendKeys(protractor.Key.SPACE);

            return helpers.enter.perform();
        }

    });

    this.Given(/^jag raderar ett  slumpat obligatoriskt fält$/, function(callback) {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var intygShortcode = helpers.getAbbrev(intyg.typ);

        fillInIntyg.changingFields(isSMIIntyg, intygShortcode, callback, true);

    });
    this.Given(/^jag raderar fältet "([^"]*)" fältet$/, function(field, callback) {
        if (field === 'Annat Intyget Baseras på') {
            fkUtkastPage.baserasPa.annat.text.clear().then(callback);
        } else if (field === 'Förtydligande') {
            fkUtkastPage.prognos.fortydligande.clear().then(callback);
        }

    });


    this.Given(/^jag kryssar i Prognos Går ej att bedöma utan beskrivning$/, function(callback) {

        fkUtkastPage.prognos.GAR_EJ_ATT_BEDOMA.sendKeys(protractor.Key.SPACE).then(function() {
            fkUtkastPage.prognos.fortydligande.clear().then(callback);
        });

    });

};
