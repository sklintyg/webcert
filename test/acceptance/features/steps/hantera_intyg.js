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

/* globals pages, intyg, protractor, browser, logger, browser, Promise*/

'use strict';

var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var utkastPage = pages.intyg.base.utkast;
var helpers = require('./helpers.js');

function signeraUtkast() {

    var uppdateraAdressOmErsattandeIntyg = function() {
        if (global.ersattintyg) {
            console.log('Intyget ersätter ett annat intyg');
            return require('./fillIn/common.js').setPatientAdressIfNotGiven();
        }

        return Promise.resolve();
    };

    return uppdateraAdressOmErsattandeIntyg().then(function() {
        return browser.sleep(2000).then(function() { // fix för nåt med animering?

            return expect(fkUtkastPage.sparatOchKomplettMeddelande.isDisplayed()).to.eventually.equal(true)
                .then(function() {
                    return fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
                })
                .then(function() {
                    // Verifiera att det inte finns valideringsfel
                    var ejKomplettEL = element(by.cssContainingText('h3', 'Utkastet saknar uppgifter i följande avsnitt'));
                    return expect(ejKomplettEL.isPresent()).to.become(false)
                        .then(function(val) {
                            //Elementet finns inte i DOM
                            return Promise.resolve();
                        }, function(val) {
                            //Om elementet finns tillgänligt på sidan så ska det iallafall inte vara synligt!
                            return expect(ejKomplettEL.isDisplayed()).to.become(false)
                                .then(function() {
                                    return Promise.resolve('Elementet är inte tillgänligt och inte synligt');
                                }, function() {
                                    throw ('Utkastet är inte komplett och kunde inte signeras. Se screenshot' + '\n' + val);

                                });

                        });
                });
        });
    });
}

module.exports = function() {
    this.Given(/^jag signerar intyget$/, function() {
        return signeraUtkast();
    });

    this.Given(/^jag signerar och skickar kompletteringen$/, function() {
        return signeraUtkast();
    });

    this.Given(/^ska det inte finnas någon knapp för "([^"]*)"$/, function(texten) {
        return expect(element(by.cssContainingText('.btn', texten)).isPresent()).to.become(false);
    });

    this.Given(/^jag klickar på signera\-knappen$/, function() {
        return fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
    });



    this.Given(/^ska signera\-knappen inte vara klickbar$/, function(callback) {
        utkastPage.signeraButton.isEnabled().then(function(isVisible) {
            if (isVisible) {
                callback('FEL - Signera-knappen är klickbar!');
            } else {
                logger.debug('OK Signera-knappen är ej klickbar!');
            }
        }).then(callback);
    });



    this.Given(/^jag makulerar intyget$/, function() {

        return browser.getCurrentUrl()
            .then(function(text) {
                intyg.id = text.split('/').slice(-1)[0];
                intyg.id = intyg.id.split('?')[0];
            })
            .then(function() {
                return fkIntygPage.makulera.btn.sendKeys(protractor.Key.SPACE);
            })
            .then(function() {
                return browser.sleep(2000); // fix för animering
            })
            .then(function() {
                return fkIntygPage.pickMakuleraOrsak();
            })
            .then(function() {
                return fkIntygPage.makulera.dialogMakulera.sendKeys(protractor.Key.SPACE);
            });
    });

    /*this.Given(/^jag kopierar intyget$/, function() {
        //global.intyg.isKopia = true;
        helpers.updateEnhetAdressForNewIntyg();
        return fkIntygPage.copy.button.sendKeys(protractor.Key.SPACE).then(function() {
            return fkIntygPage.copy.dialogConfirmButton.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    return browser.getCurrentUrl()
                        .then(function(text) {
                            intyg.id = text.split('/').slice(-1)[0];
                            intyg.id = intyg.id.split('?')[0];
                            logger.info('intyg.id: ' + intyg.id);
                            return require('./fillIn/common.js').setPatientAdressIfNotGiven();

                        });
                });
        });
    });*/

    this.Given(/^jag raderar utkastet$/, function(callback) {
        fkUtkastPage.radera.knapp.sendKeys(protractor.Key.SPACE);
        fkUtkastPage.radera.bekrafta.sendKeys(protractor.Key.SPACE)
            .then(callback);
    });


    this.Given(/^jag skriver ut intyget$/, function() {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);

        if (isSMIIntyg) {
            return element(by.id('downloadprint')).sendKeys(protractor.Key.SPACE);
        } else {
            return fkIntygPage.skrivUtFullstandigtIntyg();
        }
    });

    this.Given(/^ska det finnas en referens till gamla intyget$/, function() {
        return browser.sleep(3000).then(function() {
            return element(by.id('wc-intyg-relations-button')).click().then(function() { // May not be needed. Only to graphically illustrate normal user behavior.
                return browser.findElement(by.css('.btn-info')).sendKeys(protractor.Key.SPACE).then(function() {
                    return browser.getCurrentUrl().then(function(text) {
                        logger.info('(%s contain %s) => %s', text, intyg.id, (text.indexOf(intyg.id) !== -1 ? true : false));
                        return expect(text).to.contain(intyg.id);
                    });
                });

            });
        });
    });
    this.Given(/^ska intyget inte innehålla gamla personuppgifter$/, function() {
        var namn = global.intyg.person.forNamn + ' ' + global.intyg.person.efterNamn;
        return expect(element(by.id('patientNamnPersonnummer')).getText()).to.eventually.not.contain(namn);

    });

    this.When(/^jag markerar intyget som klart för signering$/, function() {
        return element(by.id('markeraKlartForSigneringButton')).sendKeys(protractor.Key.SPACE);
    });

    this.When(/^ska jag se texten "([^"]*)"$/, function(msg) {
        return expect(element(by.id('draft-marked-ready-text')).getText()).to.eventually.contain(msg);
    });



};
