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

/*globals pages,intyg,protractor,wcTestTools,Promise,logger,assert*/


'use strict';

/*
 *	Stödlib och ramverk
 *
 */
const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

let testTools = require('common-testtools');
testTools.protractorHelpers.init();


let tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
let tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
let utkastPage = pages.intyg.base.utkast;
let checkboxVal = utkastPage.checkboxVal;
let radioknappVal = utkastPage.radioknappVal;

let helpers = require('./helpers');
let unique = helpers.uniqueItemsInArray;
let testdataHelpers = wcTestTools.helpers.testdata;

let synVarTSD = tsdUtkastPage.syn;
let synVarBAS = tsBasUtkastPage.syn;

let synVarArrayTSD = [synVarTSD.hoger.utan, synVarTSD.hoger.med, synVarTSD.vanster.utan, synVarTSD.vanster.med, synVarTSD.binokulart.utan, synVarTSD.binokulart.med];
let synVarArrayBAS = [synVarBAS.hoger.utan, synVarBAS.hoger.med, synVarBAS.vanster.utan, synVarBAS.vanster.med, synVarBAS.binokulart.utan, synVarBAS.binokulart.med];

let valideringsData = require('./faltvalidering_testdata.js');
let valideringsVal = valideringsData.val;
let meddelanden = valideringsData.meddelanden;



/*
 *	Stödfunktioner
 *
 */
let synLoop = (array, keyToSend) => Promise.all(array.map(el => helpers.moveAndSendKeys(el, keyToSend)));

let populateSyn = typAvSyn => {

    let slumpatSynFaltTSD = testdataHelpers.shuffle([synVarTSD.hoger, synVarTSD.vanster, synVarTSD.binokulart])[0];
    let slumpatSynFaltBAS = testdataHelpers.shuffle([synVarBAS.hoger, synVarBAS.vanster, synVarBAS.binokulart])[0];

    if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        return slumpatSynFaltTSD.utan.typeKeys('9').then(() => slumpatSynFaltTSD.med.typeKeys('8').typeKeys(protractor.Key.TAB));
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
        return synLoop(synVarArrayTSD, 9);
    } else if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        return slumpatSynFaltBAS.utan.typeKeys('9').then(() => slumpatSynFaltBAS.med.typeKeys('8').typeKeys(protractor.Key.TAB));
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg') {
        return synLoop(synVarArrayBAS, 9);
    }
};

let containsRequiredSymbol = el => el.all(by.css('span.icon-wc-ikon-38')).isPresent();

let sectionsContainingRequiredSymbol = () => element.all(by.css('.card'))
    .filter(containsRequiredSymbol).count();

let findSectionsWithRequiredFields = () => element
    .all(by.css('.card')) // Sektion..
    .filter(containsRequiredSymbol) // ..som har asterisk..
    .all(by.css('h3')) // ..ta fram rubrik..
    .map(el => el.getText()
        .then(t => t.replace('*', '').replace('\n', ''))); // ..och ta bort skräptecken

let findErrorMessages = () => element.all(by.repeater('category in categories')).map(el => el.getText());

let findValidationErrorsWithText = text => element.all(by.cssContainingText('div .validation-error', text));

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
        default:
            return logger.error(`Klarade inte att matcha fieldtype ${fieldtype}`);
    }
};

let chainCheckboxActions = intygsTyp => valideringsVal[intygsTyp].checkboxar
    .reduce((prev, text) => prev.then(() => checkboxVal(text)), Promise.resolve());

let chainRadiobuttonActions = intygsTyp => () => Object.keys(valideringsVal[intygsTyp].radioknappar)
    .reduce((prev, text) => prev.then(() => radioknappVal(valideringsVal[intygsTyp].radioknappar[text], text)), Promise.resolve());

let chainTextFieldActions = intygsTyp => valideringsVal[intygsTyp].text
    .reduce((prev, text) => prev.then(() => fyllText(text)), Promise.resolve());



/*
 *	Teststeg
 *
 */

Then(/^ska inga asterisker finnas$/, () =>
    expect(sectionsContainingRequiredSymbol()).to.eventually.equal(0));

Then(/^ska alla valideringsmeddelanden finnas med i listan över godkända meddelanden$/,
    () => element.all(by.repeater('validation in validations'))
    .map(el => el.getText())
    .then(texts => {
        let ogiltiga = texts.filter(unique).filter(text => !meddelanden.includes(text));
        return expect(ogiltiga, `Följande valideringsmeddelanden är inte giltiga: ${ogiltiga.join('\n')}`).to.be.empty;
    })
);

Given(/^att textfält i intyget är rensade$/, () => element.all(by.css('input[type=text]')).each(i => i.clear()));

Then(/^ska alla sektioner innehållandes valideringsfel listas$/, () =>
    Promise.all([findSectionsWithRequiredFields(), // expected
        findErrorMessages() // actual
    ]).then(result => {
        let expected = result[0];
        let actual = result[1];
        logger.info('Expected: ' + expected);
        logger.info('Actual: ' + actual);
        expect(actual).to.eql(expected);
    }).catch(msg => assert.fail(msg))
);

Then(/^ska valideringsfel i sektion "([^"]*)" visas$/, sektion => expect(findErrorMessages()).to.eventually.include(sektion));

Then(/^ska inga valideringsfel listas$/, () =>
    findErrorMessages().then(errors => {
        errors.forEach(err => logger.warn(`Oväntat valideringsfel i sektion: ${err}`));
        return expect(errors).to.be.empty;
    }));


Then(/^ska statusmeddelande att obligatoriska uppgifter saknas visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Obligatoriska uppgifter saknas'));

Then(/^ska statusmeddelande att intyget är klart att signera visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Klart att signera'));

Then(/^ska "(\d+)" valideringsfel visas med texten "([^"]+)"$/, (antal, text) =>
    expect(findValidationErrorsWithText(text).count()).to.eventually.equal(Number.parseInt(antal, 10))
);

When(/^jag gör val för att få fram maximalt antal fält i "([^"]+)"$/, intyg =>
    chainCheckboxActions(intyg)
    .then(chainRadiobuttonActions(intyg))
);

When(/^jag fyller i textfält med felaktiga värden i "([^"]+)"$/, intyg => chainTextFieldActions(intyg));
