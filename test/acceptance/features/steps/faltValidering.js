/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/*globals pages, protractor, wcTestTools, Promise, logger, assert, browser*/


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


let tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.v2.utkast;
let tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
let doiUtkastPage = wcTestTools.pages.intyg.soc.doi.utkast;
let utkastPage = pages.intyg.base.utkast;
let checkboxVal = utkastPage.checkboxVal;
let radioknappVal = utkastPage.radioknappVal;
let dropdownVal = utkastPage.dropdownVal;
let fyllTextfalt = utkastPage.fyllTextfalt;

let helpers = require('./helpers');
let unique = helpers.uniqueItemsInArray;
let testdataHelpers = wcTestTools.helpers.testdata;

let synVarTSD = tsdUtkastPage.syn;
let synVarBAS = tsBasUtkastPage.syn;

let synVarArrayTSD = [synVarTSD.hoger.utan, synVarTSD.hoger.med, synVarTSD.vanster.utan, synVarTSD.vanster.med, synVarTSD.binokulart.utan, synVarTSD.binokulart.med];
let synVarArrayBAS = [synVarBAS.hoger.utan, synVarBAS.hoger.med, synVarBAS.vanster.utan, synVarBAS.vanster.med, synVarBAS.binokulart.utan, synVarBAS.binokulart.med];

let valideringsData = require('./faltvalidering_krav.js');
let valideringsVal = require('./faltvalidering_testdata.js').val;
let meddelanden = valideringsData.meddelanden;



/*
 *	Stödfunktioner
 *
 */

let synLoop = (array, keyToSend) => Promise.all(array.map(el => helpers.moveAndSendKeys(el, keyToSend)));

let populateSyn = (typAvSyn, intyg) => {

    let slumpatSynFaltTSD = testdataHelpers.shuffle([synVarTSD.hoger, synVarTSD.vanster, synVarTSD.binokulart])[0];
    let slumpatSynFaltBAS = testdataHelpers.shuffle([synVarBAS.hoger, synVarBAS.vanster, synVarBAS.binokulart])[0];

    if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg diabetes') {
        return slumpatSynFaltTSD.utan.typeKeys('9').then(() => slumpatSynFaltTSD.med.typeKeys('8').typeKeys(protractor.Key.TAB));
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg diabetes') {
        return synLoop(synVarArrayTSD, 9);
    } else if (typAvSyn === 'slumpat synfält' && intyg.typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
        return slumpatSynFaltBAS.utan.typeKeys('9').then(() => slumpatSynFaltBAS.med.typeKeys('8').typeKeys(protractor.Key.TAB));
    } else if (typAvSyn === 'alla synfält' && intyg.typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
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

let findValidationWarningsWithText = text => element.all(by.cssContainingText('div .validation-warning', text));

let fillInDates = date => element.all(by.css('.wc-datepicker-wrapper input')).each(el => el.clear().then(() => el.sendKeys(date)));

let fyllText = (fieldtype, intyg) => {
    switch (fieldtype) {
        case 'datum':
            return fillInDates('2fjesk');
        case 'postnummer':
            return utkastPage.enhetensAdress.postNummer.clear()
                .then(() => utkastPage.enhetensAdress.postNummer.typeKeys('1111'))
                .then(() => utkastPage.enhetensAdress.postNummer.typeKeys('111111'));
        case 'diabetes-årtal':
            return tsdUtkastPage.allmant.diabetesyear.sendKeys('1000').then(function() {
                return tsdUtkastPage.allmant.insulinbehandlingsperiod.sendKeys('1000', protractor.Key.TAB);
            });
        case 'alla synfält':
            return populateSyn(fieldtype, intyg);
        default:
            return logger.error(`Klarade inte att matcha fieldtype ${fieldtype}`);
    }
};

let chainCheckboxActions = intygsTyp => valideringsVal[intygsTyp].checkboxar
    .reduce((prev, text) => prev.then(() => checkboxVal(text)), Promise.resolve());

let chainRadiobuttonActions = intygsTyp => () => Object.keys(valideringsVal[intygsTyp].radioknappar)
    .reduce((prev, text) => prev.then(() => radioknappVal(valideringsVal[intygsTyp].radioknappar[text], text)), Promise.resolve());

let chainDropdownActions = intygsTyp => () => Object.keys(valideringsVal[intygsTyp].dropdowns)
    .reduce((prev, text) => prev.then(() => dropdownVal(valideringsVal[intygsTyp].dropdowns[text], text)), Promise.resolve());

let chainTextFieldActions = intyg => valideringsVal[intyg.typ].text
    .reduce((prev, text) => prev.then(() => fyllText(text, intyg)), Promise.resolve());

let focusOmWebcertLink = () => browser.executeScript('document.getElementById("aboutLink").focus();').then(() => logger.silly('OmWebcert länk - focus'));
let blurOmWebcertLink = () => browser.executeScript('document.getElementById("aboutLink").blur();').then(() => logger.silly('OmWebcert länk - blur'));
let tab = () => element.all(by.css('input[type=text]')).first().sendKeys(protractor.Key.TAB).then(() => logger.info('OmWebcert länk - tab'));

let changeFocus = () => focusOmWebcertLink().then(helpers.largeDelay).then(blurOmWebcertLink).then(tab).then(helpers.hugeDelay).then(() => logger.silly('Fokus ändrat'));

let currentYear = () => `${new Date().getFullYear()}`;

let currentMonth = () => `0${new Date().getMonth()}`.slice(-2);

let validateValidation = (intygsTyp, valideringsTyp) => {
    let antal = valideringsData.antalValideringsMeddelanden;

    return Object.keys(antal[intygsTyp][valideringsTyp]).reduce((acc, valideringsMeddelande) => acc.then(function(results) {
        return findValidationErrorsWithText(valideringsData.kravTxt[valideringsTyp][valideringsMeddelande]).count().then(actual => {
            let result = {};
            let expected = antal[intygsTyp][valideringsTyp][valideringsMeddelande];
            result.equal = (expected === actual);
            result.expected = expected;
            result.actual = actual;
            result.shorthand = valideringsMeddelande;
            result.txt = valideringsData.kravTxt[valideringsTyp][valideringsMeddelande];

            if (!result.equal) {
                logger.warn(JSON.stringify(result));
            } else {
                logger.info(JSON.stringify(result));
            }


            results.push(result);
            return Promise.resolve(results);
        });
    }), Promise.resolve([]));
};

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

Given(/^att textfält i intyget är rensade$/, () => {
    //Vänta på att alla element finns på sidan.
    return helpers.pageReloadDelay().then(element.all(by.css('input[type=text]')).filter(el => {
        return el.getAttribute('id').then(id => logger.silly('tar bort value ifrån element: ' + id)).then(() => el.isEnabled());
    }).each(i => i.typeKeys("a").then(i.clear()))).then(changeFocus);
});

Then(/^ska alla sektioner innehållandes valideringsfel listas$/, () =>
    Promise.all([findSectionsWithRequiredFields(), // expected
        findErrorMessages() // actual
    ]).then(result => {
        let expected = result[0];
        let actual = result[1];
        logger.info('Expected: ' + expected);
        logger.info('Actual: ' + actual);
        expect(actual).to.have.members(expected);
    }).catch(msg => {
        logger.error(msg);
        assert.fail(msg);
    })
);

Then(/^ska valideringsfel i sektion "([^"]*)" visas$/, sektion => expect(findErrorMessages()).to.eventually.include(sektion));

Then(/^ska inga valideringsfel listas$/, () =>
    findErrorMessages().then(errors => {
        errors.forEach(err => logger.warn(`Oväntat valideringsfel i sektion: ${err}`));
        return expect(errors).to.be.empty;
    }));


Then(/^ska statusmeddelande att obligatoriska uppgifter saknas visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Obligatoriska uppgifter saknas'));

Then(/^ska statusmeddelande att intyget är klart att signera visas$/, () => expect(utkastPage.utkastStatus.getText()).to.eventually.contain('Klart att signera'));

Then(/^ska alla "([^"]*)" valideringsfel för "([^"]*)" visas$/, (valideringsTyp, intygsTyp) =>
    validateValidation(intygsTyp, valideringsTyp).then(results => {
        return Promise.all([
            expect(JSON.stringify(results)).to.not.contain('"equal":false'),
            expect(JSON.stringify(results)).to.contain('"equal":true') //avoid false positive
        ]);
    })
);


Then(/^ska "(\d+)" valideringsfel visas med texten "(.+)"$/, (antal, text) =>
    expect(findValidationErrorsWithText(text).count()).to.eventually.equal(antal)
);

Then(/^ska "(\d+)" varningsmeddelanden? visas med texten "([^"]+)"$/, (antal, text) =>
    expect(findValidationWarningsWithText(text).count()).to.eventually.equal(antal)
);

When(/^jag gör val för att få fram maximalt antal fält i "([^"]+)"$/, intyg =>
    chainCheckboxActions(intyg)
    .then(chainRadiobuttonActions(intyg))
    .then(chainDropdownActions(intyg))
);

When(/^jag fyller i textfält med felaktiga värden$/, function() {
    return chainTextFieldActions(this.intyg).then(changeFocus);
});


When(/^jag anger slutdatum som är tidigare än startdatum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '2017-03-27',
            tom: '2016-04-01'
        },
        nedsattMed50: {
            from: '2017-03-27',
            tom: '2016-04-01'
        },
        nedsattMed75: {
            from: '2017-03-27',
            tom: '2016-04-01'
        },
        nedsattMed100: {
            from: '2017-03-27',
            tom: '2016-04-01'
        },
    }).then(changeFocus);
});

When(/^jag anger start- och slutdatum för långt bort i tiden$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '1700-03-27',
            tom: '2116-04-01'
        },
        nedsattMed50: {
            from: '1700-03-27',
            tom: '2116-04-01'
        },
        nedsattMed75: {
            from: '1700-03-27',
            tom: '2116-04-01'
        },
        nedsattMed100: {
            from: '1700-03-27',
            tom: '2116-04-01'
        },
    }).then(changeFocus);
});

When(/^jag anger överlappande start- och slutdatum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '2016-03-27',
            tom: '2016-09-25'
        },
        nedsattMed50: {
            from: '2016-04-27',
            tom: '2016-10-25'
        }
    }).then(changeFocus);
});

When(/^jag anger start- och slutdatum med mer än 6 månaders mellanrum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '2015-03-27',
            tom: '2017-09-25'
        }
    }).then(changeFocus);
});

When(/^jag anger startdatum mer än en vecka före dagens datum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '2015-03-27',
            tom: '2017-09-25'
        }
    }).then(changeFocus);
});

When(/^jag anger ogiltiga datum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeArbetsformaga({
        nedsattMed25: {
            from: '2016-03-32',
            tom: '2016-09-32'
        },
        nedsattMed50: {
            from: '2016-03-32',
            tom: '2016-09-32'
        },
        nedsattMed75: {
            from: '2016-03-32',
            tom: '2016-09-32'
        },
        nedsattMed100: {
            from: '2016-03-32',
            tom: '2016-09-32'
        },
    }).then(changeFocus);
});

When(/^jag anger undersökningsdatum i framtiden$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeBaseratPa({
        minUndersokningAvPatienten: '2021-09-27',
        journaluppgifter: '2021-09-27',
        telefonkontakt: '2021-09-27',
        annat: '2021-09-27',
        annatBeskrivning: '',
    }).then(changeFocus);
});

When(/^jag anger patientkännedom senare än undersökningsdatum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeBaseratPa({
        minUndersokningAvPatienten: '2017-09-27',
        personligKannedom: '2017-09-28'
    }).then(changeFocus);
});

When(/^jag anger anhörigs beskrivning senare än patientkännedom$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeBaseratPa({
        anhorigsBeskrivning: '2017-09-27',
        personligKannedom: '2017-09-28'
    }).then(changeFocus);
});

When(/^jag väljer "([^"]+)" i dropdownen "([^"]*)"$/, (val, text) => dropdownVal(val, text).then(changeFocus));

When(/^jag väljer alternativet "([^"]+)" i frågan "([^"]*)"$/, (val, text) => radioknappVal(val, text).then(changeFocus));

When(/^jag kryssar i "([^"]+)"$/, text => checkboxVal(text).then(changeFocus));

When(/^jag anger ett tidigare datum för anträffad död$/, () => fillInDates(helpers.getCurrentDate().replace(/^\d{4}/, '2017')).then(changeFocus));

When(/^jag fyller i "(.*)" i fältet "(.*)"$/, (text, field) => fyllTextfalt(field, text).then(changeFocus));

When(/^jag anger dagens datum som ej säkert dödsdatum$/, () => dropdownVal(currentYear(), 'År').then(() => dropdownVal(currentMonth(), 'Månad')).then(changeFocus));

When(/^jag anger ett säkert dödsdatum i framtiden$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeDodsdatum({
        sakert: {
            datum: helpers.getCurrentDate().replace(/^\d{4}/, '2099')
        }
    }).then(changeFocus);
});

When(/^jag anger 31 december förrförra året som säkert dödsdatum$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ).angeDodsdatum({
        sakert: {
            datum: `${Number.parseInt(currentYear())-2}-12-31`
        }
    }).then(changeFocus);
});

When(/^jag anger 31 december förrförra året som operationsdatum$/, () => doiUtkastPage.angeOperation({
    ja: {
        datum: `${Number.parseInt(currentYear())-2}-12-31`,
        beskrivning: 'Hej'
    }
}).then(changeFocus));
When(/^jag anger 31 december förrförra året som anträffad död$/, function() {
    return pages.getUtkastPageByType(this.intyg.typ)
        .dodsdatum.inteSakert.antraffadDod.sendKeys(`${Number.parseInt(currentYear())-2}-12-31`).then(changeFocus);
});

When(/^jag anger 31 december förrförra året som skada\/förgiftnings-datum$/, () => fyllTextfalt('Datum för skada/förgiftning', `${Number.parseInt(currentYear())-2}-12-31`).then(changeFocus));


When(/^jag anger dödsorsaker med datum i stigande ordning$/, () =>
    doiUtkastPage.angeUtlatandeOmDodsorsak({
        a: {
            datum: '2018-03-04',
            beskrivning: 'abc',
            tillstandSpec: 'Kronisk'
        },
        b: {
            datum: '2018-03-05',
            beskrivning: 'abc',
            tillstandSpec: 'Kronisk'
        },
        c: {
            datum: '2018-03-06',
            beskrivning: 'abc',
            tillstandSpec: 'Kronisk'
        },
        d: {
            datum: '2018-03-07',
            beskrivning: 'abc',
            tillstandSpec: 'Kronisk'
        },
    }).then(changeFocus)
);
