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

/* globals pages, protractor, browser, logger,wcTestTools*/

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


const fk7263Utkast = pages.intyg.fk['7263'].utkast;
const baseIntyg = pages.intyg.base.intyg;
const sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
const sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
const testdataHelpers = wcTestTools.helpers.testdata;
const testdata = wcTestTools.testdata;
const testpatienter = testdata.values.patienter;
const parallell = require('./parallellt_util.js');
const helpers = require('../helpers.js');



/*
 *	Stödfunktioner
 *
 */
var insertDashInPnr = helpers.insertDashInPnr;

function gotoPatient(patient, user) { //förutsätter  att personen finns i PU-tjänsten

    if (user.origin !== 'DJUPINTEGRATION') {
        element(by.id('menu-skrivintyg')).click().then(function() {
            return helpers.pageReloadDelay();
        });
    }
    return sokSkrivIntygPage.selectPersonnummer(patient.id).then(function() {
            return helpers.pageReloadDelay();
        })
        .then(function() {
            logger.info('Går in på patient ' + patient.id);
            //Patientuppgifter visas
            var patientUppgifter = sokSkrivIntygPage.patientNamn;
            return expect(patientUppgifter.getText()).to.eventually.contain(insertDashInPnr(patient.id)).then(function() {
                return helpers.smallDelay();
            });
        });

}

var forkedBrowser;

function setForkedBrowser(forkedBrowser2) {
    logger.silly('Store forked browser for next step');
    forkedBrowser = forkedBrowser2;
}


function gotoIntygUtkast(intyg) {
    browser.ignoreSynchronization = true;
    let skapadoiknapp = element(by.id('button1doi-info-dialog'));

    return sokSkrivIntygUtkastTypePage.createUtkast(helpers.getInternShortcode(intyg.typ)).then(function() {
        return skapadoiknapp.isPresent();
    }).then(function(present) {
        if (present) {
            logger.info('Specialhantering för Dödsbevis saknas modalen');
            return skapadoiknapp.click();
        }
    }).then(function() {
        return helpers.pageReloadDelay();
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
    return gotoPatient(testdataHelpers.shuffle(testdata.values.patienterMedSamordningsnummer)[0], this.user);
});

// When(/^jag väljer patienten "([^"]*)"$/, function(personnummer) { //förutsätter att personen finns i PU-tjänsten
//     return gotoPatient(personnummer);
// });

Given(/^jag går in på patienten$/, function() {
    return gotoPatient(this.patient, this.user);
});


Given(/^jag anger ett (samordningsnummer|personnummer) som inte finns i PUtjänsten$/, function(typAvNum) {
    if (typAvNum === 'samordningsnummer') {
        this.patient = testdata.values.patienterMedSamordningsnummerEjPU[0];
    } else {
        this.patient = testdata.values.patienterEjPU[0];
    }
    return sokSkrivIntygPage.selectPersonnummer(this.patient.id);

});

Then(/^jag går in på (?:"([^"]*)" )?testpatienten för "([^"]*)"$/, function(index, testSyfte) {
    let patienter = wcTestTools.testdata.values.dedikeradeTestPatienter.medSyfte[testSyfte];
    this.patient = patienter[helpers.getIntFromTxt(index || 'första')];
    return gotoPatient(this.patient, this.user);
});

Then(/^jag går in på testpatienten "([^"]*)"$/, function(personnummer) {
    this.patient = {
        id: personnummer.replace("-", "")
    };
    return gotoPatient(this.patient, this.user);
});


Given(/^jag går in på en patient med sekretessmarkering$/, function() {
    this.patient = testdataHelpers.shuffle(testdata.values.patienterMedSekretessmarkering)[0];
    return gotoPatient(this.patient, this.user);
});

Given(/^jag går in på en patient som saknar namn i PU\-tjänsten$/, function() {
    this.patient = {
        id: '201203122393'
    };
    return element(by.id('pnr')).sendKeys(this.patient.id).then(function() {
        return element(by.id('skapapersonnummerfortsatt')).sendKeys(protractor.Key.SPACE);
    });
});

Given(/^jag går in på en patient som är avliden$/, function() {
    this.patient = testdataHelpers.shuffle(testdata.values.patienterAvlidna)[0];
    logger.silly(this.patient);
    return gotoPatient(this.patient, this.user);

});

Given(/^jag går in på en( annan)? patient$/, function(annan) {
    if (annan) {
        var andraPatienter = testpatienter;
        andraPatienter.splice(testpatienter.indexOf(this.patient), 1);
        logger.silly('testpatienter: ');
        logger.silly(testpatienter);

        logger.silly('andraPatienter: ');
        logger.silly(andraPatienter);

        return gotoPatient(testdataHelpers.shuffle(andraPatienter)[0], this.user);
    } else {
        return gotoPatient(testdataHelpers.shuffle(testpatienter)[0], this.user);
    }
});

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
    //intyg.typ = intygsTyp;
    this.intyg = {
        typ: intygsTyp
    };
    return gotoIntygUtkast(this.intyg);

});


Given(/^ska jag inte kunna skapa ett "([^"]*)" intyg$/, function(intygsTyp) {
    return sokSkrivIntygUtkastTypePage.intygTypeTable.getText().then(function(txt) {
        console.log(txt);
        return expect(txt).to.not.contain(intygsTyp);
    }).then(function() {
        logger.info('OK - intygstypen finns inte i listan med valbara intygstyper');
    }, function(reason) {
        throw ('FEL : ' + reason);
    });
});

Given(/^jag går in på att skapa ett slumpat intyg$/, function() {
    this.intyg = {
        typ: testdataHelpers.shuffle([
            'Läkarintyg för sjukpenning',
            'Läkarutlåtande för sjukersättning',
            'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
            //'Läkarintyg FK 7263', //Disabled i fristående läge och ersätts av Lisjp.
            'Transportstyrelsens läkarintyg högre körkortsbehörighet',
            'Transportstyrelsens läkarintyg diabetes'
        ])[0]
    };
    logger.silly('intyg.typ: ' + this.intyg.typ);


    return gotoIntygUtkast(this.intyg);
});

Given(/^jag går in på att skapa ett slumpat SMI\-intyg$/, function() {
    this.intyg.typ = testdataHelpers.shuffle([
        'Läkarintyg för sjukpenning',
        'Läkarutlåtande för sjukersättning',
        'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
        'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
    ])[0];
    logger.silly('intyg.typ: ' + this.intyg.typ);
    browser.ignoreSynchronization = true;
    return sokSkrivIntygUtkastTypePage.createUtkast(helpers.getInternShortcode(this.intyg.typ)).then(function() {
        return helpers.hugeDelay();
    }).then(function() {
        // Spara intygsid för kommande steg
        return browser.getCurrentUrl().then(function(text) {
            this.intyg.id = text.split('/').slice(-2)[0];
            return logger.info('intyg.id: ' + this.intyg.id);
        });
    });
});

Given(/^jag går in på att skapa ett slumpat TS\-intyg$/, function() {
    let intyg = this.intyg;

    intyg.typ = testdataHelpers.shuffle([
        'Transportstyrelsens läkarintyg högre körkortsbehörighet',
        'Transportstyrelsens läkarintyg diabetes'
    ])[0];
    logger.silly('intyg.typ: ' + intyg.typ);
    browser.ignoreSynchronization = true;
    return sokSkrivIntygUtkastTypePage.createUtkast(helpers.getInternShortcode(intyg.typ)).then(function() {
        return helpers.hugeDelay();
    }).then(function() {
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
    intygtyp = helpers.getAbbrev(this.intyg.typ);

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
    intygEditUrl = process.env.WEBCERT_URL + '#/' + intygtyp.toLowerCase() + '/edit/' + this.intyg.id + '/';

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

    var intygShortCode = helpers.getAbbrev(this.intyg.typ);
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
        element(by.id('wc-avliden-text-' + this.patient.id.replace(/(\d{8})(\d{4})/, '$1-$2'))), //.patient-alert? db/doi?
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

Given(/^ska signera\-knappen inte vara synlig$/, function(callback) {
    expect(fk7263Utkast.signeraButton.isPresent()).to.eventually.become(false).and.notify(callback);
});

Given(/^ska jag bli inloggad som "([^"]*)"$/, function(arg1) {
    var wcHeader = element(by.id('wcHeader'));
    return expect(wcHeader.getText()).to.eventually.contain(arg1);
});
