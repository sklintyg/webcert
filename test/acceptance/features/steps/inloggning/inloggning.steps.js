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

/* globals pages, protractor, person, browser, intyg, logger,wcTestTools, Promise*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var baseIntyg = pages.intyg.base.intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
// var webcertBase = pages.webcertBase;
var checkValues = require('../checkValues');
var testdataHelpers = wcTestTools.helpers.testdata;
var testdata = wcTestTools.testdata;
var testpatienter = testdata.values.patienter;
//var testpatientAvliden = testdata.values.patientAvliden;
// var logInAsUserRole = require('./login.helpers.js').logInAsUserRole;
var parallell = require('./parallellt_util.js');
var helpers = require('../helpers.js');



/*
 *	Stödfunktioner
 *
 */
var insertDashInPnr = helpers.insertDashInPnr;

function gotoPatient(patient) { //förutsätter  att personen finns i PU-tjänsten
    global.person = patient;

    if (global.user.origin !== 'DJUPINTEGRATION') {
        element(by.id('menu-skrivintyg')).click().then(function() {
            return helpers.pageReloadDelay();
        });
    }
    return sokSkrivIntygPage.selectPersonnummer(person.id).then(function() {
            return helpers.pageReloadDelay();
        })
        .then(function() {
            logger.info('Går in på patient ' + person.id);
            //Patientuppgifter visas
            var patientUppgifter = sokSkrivIntygPage.patientNamn;
            return expect(patientUppgifter.getText()).to.eventually.contain(insertDashInPnr(person.id)).then(function() {
                return helpers.smallDelay();
            });
        });

}

var forkedBrowser;

function setForkedBrowser(forkedBrowser2) {
    logger.silly('Store forked browser for next step');
    forkedBrowser = forkedBrowser2;
}

function gotoIntygUtkast(intygtyp) {
    intyg.typ = intygtyp;
    return sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intygtyp).then(function() {
        return sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);
    }).then(function() {
        // Spara intygsid för kommande steg
        return browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            return logger.info('intyg.id: ' + intyg.id);
        });
    });
}

/*
 *	Test steg
 *
 */

Given(/^jag går in på en patient med samordningsnummer$/, function() {
    return gotoPatient(testdataHelpers.shuffle(testdata.values.patienterMedSamordningsnummer)[0]);
});

// When(/^jag väljer patienten "([^"]*)"$/, function(personnummer) { //förutsätter att personen finns i PU-tjänsten
//     return gotoPatient(personnummer);
// });

Given(/^jag går in på patienten$/, function() {
    return gotoPatient(global.person);
});


Given(/^jag anger ett (samordningsnummer|personnummer) som inte finns i PUtjänsten$/, function(typAvNum) {
    if (typAvNum === 'samordningsnummer') {
        global.person = testdata.values.patienterMedSamordningsnummerEjPU[0];
    } else {
        global.person = testdata.values.patienterEjPU[0];
    }
    return sokSkrivIntygPage.selectPersonnummer(person.id);

});

Then(/^jag går in på (?:"([^"]*)" )?testpatienten för "([^"]*)"$/, function(index, testSyfte) {
    var testvalues = wcTestTools.testdata.values;
    var patient = testvalues.dedikeradeTestPatienter.medSyfte[testSyfte][helpers.getIntFromTxt(index || 'första')];
    return gotoPatient(patient);
});

Then(/^jag går in på testpatienten "([^"]*)"$/, function(personnummer) {
    var patient = {
        id: personnummer.replace("-", "")
    };
    return gotoPatient(patient);
});


Given(/^jag går in på en patient med sekretessmarkering$/, function() {
    var patient = testdataHelpers.shuffle(testdata.values.patienterMedSekretessmarkering)[0];
    return gotoPatient(patient);
});

Given(/^jag går in på en patient som saknar namn i PU\-tjänsten$/, function() {
    var patient = {
        id: '193804139149'
    };
    return element(by.id('pnr')).sendKeys(patient.id).then(function() {
        return element(by.id('skapapersonnummerfortsatt')).sendKeys(protractor.Key.SPACE);
    });
});

Given(/^jag går in på en patient som är avliden$/, function() {
    //return gotoPatient(testdataHelpers.shuffle(testdata.values.patienterMedSamordningsnummer)[0]);
    var patient = testdataHelpers.shuffle(testdata.values.patienterAvlidna)[0];
    logger.silly(patient);
    return gotoPatient(patient);

});

Given(/^jag går in på en( annan)? patient$/, function(annan) {
    if (annan) {
        var andraPatienter = testpatienter;
        andraPatienter.splice(testpatienter.indexOf(global.person), 1);
        logger.silly('testpatienter: ');
        logger.silly(testpatienter);

        logger.silly('andraPatienter: ');
        logger.silly(andraPatienter);

        return gotoPatient(testdataHelpers.shuffle(andraPatienter)[0]);
    } else {
        return gotoPatient(testdataHelpers.shuffle(testpatienter)[0]);
    }
});

Given(/^jag går in på en patient med personnummer "(\d{12})"$/, pnr => gotoPatient(testpatienter.filter(pat => pat.id === pnr)[0]));

Given(/^ska en varningsruta innehålla texten "([^"]*)"$/, function(text) {
    var alertWarnings = element.all(by.css('.alert-warning'));
    var warnings = [];
    return alertWarnings.each(function(element) {
        return element.getText().then(function(warning) {
            logger.info('Varning: ' + warning);
            warnings.push(warning);
        });
    }).then(function() {
        return expect(warnings.join('\n')).to.contain(text);
    });
});

Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function(intygsTyp) {
    intyg.typ = intygsTyp;
    return gotoIntygUtkast(intyg.typ);

});


Given(/^ska jag inte kunna skapa ett "([^"]*)" intyg$/, function(intygsTyp) {
    return expect(sokSkrivIntygUtkastTypePage.intygTypeSelector.all(by.css('option[label="' + intygsTyp + '"]')).first().isPresent()).to.become(false).then(function() {
        logger.info('OK - intygstypen finns i listan med valbara intygstyper');
    }, function(reason) {
        throw ('FEL : ' + reason);
    });
});

Given(/^jag går in på att skapa ett slumpat intyg$/, function() {
    intyg.typ = testdataHelpers.shuffle([
        'Läkarintyg för sjukpenning',
        'Läkarutlåtande för sjukersättning',
        'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
        'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
        //'Läkarintyg FK 7263', //Disabled i fristående läge och ersätts av Lisjp.
        'Transportstyrelsens läkarintyg',
        'Transportstyrelsens läkarintyg, diabetes'
    ])[0];
    logger.silly('intyg.typ: ' + intyg.typ);
    return gotoIntygUtkast(intyg.typ);

});

Given(/^jag går in på att skapa ett slumpat SMI\-intyg$/, function() {
    intyg.typ = testdataHelpers.shuffle([
        'Läkarintyg för sjukpenning',
        'Läkarutlåtande för sjukersättning',
        'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
        'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
    ])[0];
    logger.silly('intyg.typ: ' + intyg.typ);
    return Promise.all([
        sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intyg.typ),
        sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE)
    ]).then(function() {
        // Spara intygsid för kommande steg
        return browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            return logger.info('intyg.id: ' + intyg.id);
        });
    });
});

Given(/^jag går in på att skapa ett slumpat TS\-intyg$/, function() {
    intyg.typ = testdataHelpers.shuffle([
        'Transportstyrelsens läkarintyg',
        'Transportstyrelsens läkarintyg, diabetes'
    ])[0];
    logger.silly('intyg.typ: ' + intyg.typ);
    return Promise.all([
        sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intyg.typ),
        sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE)
    ]).then(function() {
        // Spara intygsid för kommande steg
        return browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            return logger.info('intyg.id: ' + intyg.id);
        });
    });
});


Given(/^sedan öppnar intyget i två webbläsarinstanser$/, function(callback) {
    // var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var intygtyp, userObj, forkedBrowser, intygEditUrl;
    // if (isSMIIntyg) {
    //     intygtyp = helpers.getAbbrev(intyg.typ);

    //     // User
    //     userObj = helpers.getUserObj(helpers.userObj.UserKey.EN);
    //     inteAccepteratKakor = true;

    //     // Browser & URL
    //     forkedBrowser = browser.forkNewDriverInstance(true);
    //     intygEditUrl = process.env.WEBCERT_URL + '#/' + intygtyp.toLowerCase() + '/edit/' + intyg.id + '/';

    //     parallell.login({
    //         userObj: userObj,
    //         role: helpers.userObj.Role.DOCTOR,
    //         cookies: inteAccepteratKakor
    //     }, intygEditUrl, forkedBrowser).then(function() {
    //         setForkedBrowser(forkedBrowser);
    //         callback();
    //     });
    // } else {
    // throw new Error(intyg.typ + ' is not implemented.');
    intygtyp = helpers.getAbbrev(intyg.typ);

    // User
    userObj = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-107Q'
    };
    //inteAccepteratKakor = true;

    // Browser & URL
    forkedBrowser = browser.forkNewDriverInstance(true);
    intygEditUrl = process.env.WEBCERT_URL + '#/' + intygtyp.toLowerCase() + '/edit/' + intyg.id + '/';

    parallell.login({
        userObj: userObj,
        role: 'Läkare'
    }, intygEditUrl, forkedBrowser).then(function() {
        setForkedBrowser(forkedBrowser);
        callback();
    });
    // }

});

Given(/^ska ett felmeddelande visas som innehåller texten "([^"]*)"$/, function(errorMsg) {
    var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
        return elm.getText();
    });
    return alertTexts.then(function(result) {
        return expect(result.join('\n')).to.have.string(errorMsg);
    });
});

Given(/^ska ett felmeddelande visas$/, function(callback) {

    var intygShortCode = helpers.getAbbrev(intyg.typ);
    var elemntId = 'aktivitetsbegransning';
    if ('LUAE_FS' === intygShortCode) {
        elemntId = 'funktionsnedsattningDebut';
    } else if ('FK7263' === intygShortCode) {
        elemntId = 'diseaseCause';
    }

    parallell.changeFields(forkedBrowser, elemntId).then(function() {
        logger.info('saveErrorMessage found');
        return parallell.refreshBrowser(forkedBrowser);
    }).then(function() {
        // Known issue - https://github.com/angular/protractor/issues/2203
        parallell.closeBrowser(forkedBrowser).then(callback);
    });
});

Given(/^ska varningen "([^"]*)" visas om man försöker (skicka|förnya|makulera) intyget i andra webbläsarinstansen$/, function(msg, action, callback) {
    var elemntIds;
    if ('förnya' === action) {
        elemntIds = {
            firstBtn: 'fornyaBtn',
            btnDialog: 'button1fornya-dialog',
            alertDanger: '.alert-danger'
        };
    } else if ('skicka' === action) {
        elemntIds = {
            firstBtn: 'sendBtn',
            btnDialog: 'button1send-dialog',
            alertDanger: '.alert-danger'
        };
        /*} else if ('kopiera' === action) {
            elemntIds = {
                firstBtn: 'copyBtn',
                btnDialog: 'button1copy-dialog',
                alertDanger: '.alert-danger'
            };*/
    } else if ('makulera' === action) {
        elemntIds = {
            firstBtn: 'makuleraBtn',
            radioBtn: 'reason-FEL_PATIENT',
            btnDialog: 'button1makulera-dialog',
            alertDanger: '.alert-danger'
        };
    }

    parallell.findErrorMsg(forkedBrowser, elemntIds, msg).then(function() {
        logger.info('notSendErrorMessage found');
        parallell.closeBrowser(forkedBrowser).then(callback);
    });
});

Then(/^jag klickar på skicka knappen$/, function() {
    var elemntIds = {
        firstBtn: 'sendBtn',
        btnDialog: 'button1send-dialog'
    };

    return parallell.clickModalBtn(browser, elemntIds).then(function() {
        return parallell.refreshBrowser(forkedBrowser);
    });
});

Then(/^jag skickar en fråga till Försäkringskassan$/, function() {
    return parallell.askNewQuestion(forkedBrowser);
});
Then(/^ska varningen "([^"]*)" visas$/, function(msg) {
    var errorModal = forkedBrowser.findElement(by.id('arendeNewModel-load-error'));
    return expect(errorModal.getText()).to.eventually.contain(msg).then(function() {
        return parallell.closeBrowser(forkedBrowser);
    });
});

Then(/^ska jag varnas om(?: att) "([^"]*)"( i nytt fönster)?$/, function(msg, nyttFonster) {
    var elementArray = [
        element(by.id('wc-avliden-text-' + person.id.replace(/(\d{8})(\d{4})/, '$1-$2'))), //.patient-alert? db/doi?
        element(by.id('intyg-load-error')), //?
        element(by.id('errorPage')), //Raderat utkast
        element(by.id('error-panel')) //Behörighet saknas => sekretessmarkering
    ];

    var msgString = '';
    return helpers.largeDelay().then(function() {
            return element.all(by.css('.modal-body')).map(function(elm) { //nyttFonster => sekretessmarkering
                return elementArray.push(elm);
            });
        })
        .then(function() {
            return element.all(by.css('.patient-alert')).map(function(elm) {
                return elementArray.push(elm);
            });
        }).then(function() {
            return element.all(by.css('.alert')).map(function(elm) {
                return elementArray.push(elm);
            });
        }).then(function() {
            elementArray.forEach(function(elm, index) {
                elm.isPresent().then(function(present) {
                    if (present) {
                        elm.getText().then(function(theMsg) {
                            if (theMsg !== '') {
                                msgString += theMsg;
                                return;
                            } else {
                                return;
                            }
                        });

                    } else {
                        return;
                    }
                });
            });
        }).then(function() {
            return expect(msgString).to.contain(msg);
        });
});

Then(/^ska intygets första status vara "([^"]*)"$/, function(statustext) {
    return expect(baseIntyg.intygStatus[0].getText()).to.eventually.contain(statustext);
});
Then(/^ska intygets andra status vara "([^"]*)"$/, function(statustext) {
    return expect(baseIntyg.intygStatus[1].getText()).to.eventually.contain(statustext);
});

Then(/^(?:ska jag|jag ska) se den data jag angett för intyget$/, function() {
    return checkValues.forIntyg(intyg);
});

Given(/^ska signera\-knappen inte vara synlig$/, function(callback) {
    expect(fk7263Utkast.signeraButton.isPresent()).to.eventually.become(false).and.notify(callback);
});

Given(/^ska jag bli inloggad som "([^"]*)"$/, function(arg1) {
    var wcHeader = element(by.id('wcHeader'));
    return expect(wcHeader.getText()).to.eventually.contain(arg1);
});
