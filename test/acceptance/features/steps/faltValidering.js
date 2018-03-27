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

/*globals pages,intyg,protractor,wcTestTools,Promise,logger,assert,browser*/


'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli deprecated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

var testTools = require('common-testtools');
testTools.protractorHelpers.init();

var lisjpUtkastPage = pages.intyg.lisjp.utkast;
var tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
var tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
var utkastPage = pages.intyg.base.utkast;

var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var fillInIntyg = require('./fillIn/fill_in_intyg_steps');
var testdata = wcTestTools.testdata;
var testdataHelpers = wcTestTools.helpers.testdata;

var synVarTSD = tsdUtkastPage.syn;
var synVarBAS = tsBasUtkastPage.syn;

var synVarArrayTSD = [synVarTSD.hoger.utan, synVarTSD.hoger.med, synVarTSD.vanster.utan, synVarTSD.vanster.med, synVarTSD.binokulart.utan, synVarTSD.binokulart.med];
var synVarArrayBAS = [synVarBAS.hoger.utan, synVarBAS.hoger.med, synVarBAS.vanster.utan, synVarBAS.vanster.med, synVarBAS.binokulart.utan, synVarBAS.binokulart.med];

let valideringsVal = require('./faltvalidering_testdata.js');


/*
 *	Stödfunktioner
 *
 */
function synLoop(array, keyToSend) {
    var promiseArray = [];

    array.forEach(function(el) {
        promiseArray.push(helpers.moveAndSendKeys(el, keyToSend));
    });

    return Promise.all(promiseArray);
}

function populateSyn(typAvSyn) {

    var slumpatSynFaltTSD = testdataHelpers.shuffle([synVarTSD.hoger, synVarTSD.vanster, synVarTSD.binokulart])[0];
    var slumpatSynFaltBAS = testdataHelpers.shuffle([synVarBAS.hoger, synVarBAS.vanster, synVarBAS.binokulart])[0];

    if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        // return synVar.a.no.typeKeys(protractor.Key.SPACE).then(function() {
        return slumpatSynFaltTSD.utan.typeKeys('9').then(function() {
            return slumpatSynFaltTSD.med.typeKeys('8').typeKeys(protractor.Key.TAB);
        });
        // });
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        // return synVar.a.no.typeKeys(protractor.Key.SPACE).then(function() {
        return synLoop(synVarArrayTSD, 9);
        // });
    } else if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        // return synVar.a.no.typeKeys(protractor.Key.SPACE).then(function() {
        return slumpatSynFaltBAS.utan.typeKeys('9').then(function() {
            return slumpatSynFaltBAS.med.typeKeys('8').typeKeys(protractor.Key.TAB);
        });
        // });
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        // return synVar.a.no.typeKeys(protractor.Key.SPACE).then(function() {
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

let containsRequiredSymbol = () => el =>
    el.all(by.css('.required'))
    .filter(el => el.getText()
        .then(t => t === '*')).isPresent();

let findSectionsWithRequiredFields = () => element
    .all(by.css('.card')) // Sektion..
    .filter(containsRequiredSymbol()) // ..som har asterisk..
    .all(by.css('h3')) // ..ta fram rubrik..
    .map(el => el.getText()
        .then(t => t.replace('*', '').replace('\n', ''))); // ..och ta bort skräptecken

let findErrorMessages = () => element.all(by.repeater('category in categories')).map(k => k.getText());

let findValidationErrorsWithText = text => element.all(by.cssContainingText('div .validation-error', text));

let radioknappVal = (val, text) => {
    browser.ignoreSynchronization = true;
    logger.info(`Svarar ${val} i frågan ${text}`);
    return element.all(by.cssContainingText('.ue-fraga', text))
        .all(by.cssContainingText('wc-radio-wrapper', val))
        .all(by.tagName('input')).first().click()
        .then(() => browser.ignoreSynchronization = false);
};

let checkboxVal = text => {
    logger.info(`Bockar i ${text}`);
    browser.ignoreSynchronization = true;
    return element.all(by.css('.ue-fraga'))
        .all(by.cssContainingText('wc-checkbox-wrapper', text))
        .all(by.tagName('input')).first().click()
        .then(() => browser.ignoreSynchronization = false);
};

let fyllText = fieldtype => {
    switch (fieldtype) {
        case 'datum':
            return element.all(by.css('.wc-datepicker-wrapper input')).each(el => el.clear().then(() => el.sendKeys('2jfesk')));
        case 'postnummer':
            return utkastPage.enhetensAdress.postNummer.clear()
                .then(() => utkastPage.enhetensAdress.postNummer.typeKeys('1111'))
                .then(() => utkastPage.enhetensAdress.postNummer.typeKeys('111111'));
        case 'diabetes-årtal':
            return tsdUtkastPage.allmant.diabetesyear.sendKeys('1000').then(function() {
                return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys('1000', protractor.Key.TAB);
            });
        case 'alla synfält':
            return populateSyn(fieldtype);
        case 'slumpat synfält':
            return populateSyn(fieldtype);
        default:
            return logger.error(`Klarade inte att matcha fieldtype ${fieldtype}`);
    }
};

/*
 *	Test steg
 *
 */

Given(/^att textfält i intyget är rensade$/, () => element.all(by.css('input[type=text]')).each(i => i.clear()));

Then(/^ska alla sektioner innehållandes valideringsfel listas$/, () => {
    Promise.all([findSectionsWithRequiredFields(), // expected
        findErrorMessages() // actual
    ]).then(result => {
        let expected = result[0];
        let actual = result[1];
        logger.info('Expected: ' + expected);
        logger.info('Actual: ' + actual);
        expect(actual).to.eql(expected);
    }).catch(msg => assert.fail(msg));
});

Then(/^ska valideringsfel i sektion "([^"]*)" visas$/, sektion => expect(findErrorMessages()).to.eventually.include(sektion));

Then(/^ska inga valideringsfel listas$/, () =>
    findErrorMessages().then(errors => {
        errors.forEach(logger.warn);
        return expect(errors).to.be.empty;
    }));


Then(/^ska statusmeddelande att obligatoriska uppgifter saknas visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Obligatoriska uppgifter saknas'));

Then(/^ska statusmeddelande att intyget är klart att signera visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Klart att signera'));

Then(/^ska "(\d+)" valideringsfel visas med texten "([^"]+)"$/, (antal, text) =>
    expect(findValidationErrorsWithText(text).count()).to.eventually.equal(Number.parseInt(antal, 10)));

let chainCheckboxActions = intyg => () => valideringsVal[intyg].checkboxar
    .reduce((prev, text) => prev.then(() => checkboxVal(text)), Promise.resolve());

let chainRadiobuttonActions = intyg => () => Object.keys(valideringsVal[intyg].radioknappar)
    .reduce((prev, text) => prev.then(() => radioknappVal(valideringsVal[intyg].radioknappar[text], text)), Promise.resolve());

let chainTextFieldActions = intyg => valideringsVal[intyg].text
    .reduce((prev, text) => prev.then(() => fyllText(text)), Promise.resolve());

When(/^jag gör val för att få fram maximalt antal fält i "([^"]+)"$/, intyg =>
    utkastPage.angeEnhetAdress({
        postadress: '',
        postort: '',
        postnummer: '',
        telefon: ''
    })
    .then(chainCheckboxActions(intyg))
    .then(chainRadiobuttonActions(intyg)));

When(/^jag fyller i textfält med felaktiga värden i "([^"]+)"$/, intyg => chainTextFieldActions(intyg));


Given(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
    return fillInDiagnoskod({
        kod: dKod
    });
});

Given(/^jag fyller i diagnoskod$/, function() {
    var diagnos = testdataHelpers.shuffle(testdata.fmb.fmbInfo.diagnoser)[0];
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

Given(/^ska valideringsfelet "([^"]*)" visas$/, function(fel) {
    return element.all(by.css('.alert-danger')).map(function(elm) {
        return elm.getText();
    }).then(function(result) {
        logger.silly(result);
        return expect(result.join('\n')).to.have.string(fel);
    });
});

Given(/^ska valideringsfelet "([^"]*)"  inte visas$/, function(fel) {
    return element.all(by.css('.alert-danger')).map(function(elm) {
        return elm.getText();
    }).then(function(result) {
        logger.silly(result);
        return expect(result.join('\n')).to.not.have.string(fel);
    });
});

Given(/^jag fyller i text i insulin\-datum fältet$/, function() {
    return tsdUtkastPage.fillInAllmant({
        year: 'text',
        typ: 'Typ 1',
        behandling: {
            typer: ['Insulin'],
            insulinYear: 'text'
        }
    }).then(function() {
        return tsdUtkastPage.allmant.insulinbehandlingsperiod.typeKeys(protractor.Key.TAB);
    });
});

Given(/^jag lägger till fältet "([^"]*)"$/, function(fieldtype) {

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
                    //logger.silly('Ändrar datum');
                    return helpers.enter.perform();
                });
            });
        default:
            logger.error('Felaktigt Fält valt');
            break;
    }

});
Given(/^jag fyller i blanksteg i "([^"]*)" fältet$/, function(field) {
    if (field === 'Funktionsnedsattning') {
        fkUtkastPage.funktionsNedsattning.typeKeys(protractor.Key.SPACE);

        return helpers.enter.perform();
    } else if (field === 'Aktivitetsbegransning') {
        fkUtkastPage.aktivitetsbegransning.typeKeys(protractor.Key.SPACE);

        return helpers.enter.perform();
    } else if (field === 'Arbete') {
        fkUtkastPage.nuvarandeArbete.typeKeys(protractor.Key.SPACE);

        return helpers.enter.perform();
    }

});

Given(/^jag raderar ett slumpat obligatoriskt fält$/, function() {

    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var intygShortcode = helpers.getAbbrev(intyg.typ);

    return fillInIntyg.changingFields(isSMIIntyg, intygShortcode, true);

});
Given(/^jag raderar fältet "([^"]*)" fältet$/, function(field, callback) {
    if (field === 'Annat Intyget Baseras på') {
        fkUtkastPage.baserasPa.annat.text.clear().then(callback);
    } else if (field === 'Förtydligande') {
        fkUtkastPage.prognos.fortydligande.clear().then(callback);
    }

});


Given(/^jag kryssar i Prognos Går ej att bedöma utan beskrivning$/, function(callback) {

    fkUtkastPage.prognos.GAR_EJ_ATT_BEDOMA.typeKeys(protractor.Key.SPACE).then(function() {
        fkUtkastPage.prognos.fortydligande.clear().then(callback);
    });

});
