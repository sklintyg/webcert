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

/*global browser, intyg, logger,protractor, JSON,wcTestTools, Promise, testdata */
'use strict';
var createIntygWithStatus = require('./helpers_create_intyg.js').createIntygWithStatus;
var helpers = require('./helpers.js');
var getIntygElementRow = helpers.getIntygElementRow;
var shuffle = wcTestTools.helpers.testdata.shuffle;
var fillIn = require('./fillin').fillIn;

function signNewIntyg() {
    return browser.refresh().then(function() {
        logger.info('Fyller i det nya ersatta intyget.');
        global.intyg = testdata.fk.LUAE_FS.getRandom(intyg.id, false);
        console.log(intyg);
        return fillIn(global.intyg);
    });
}

function ifIntygReplaced() {
    var replacedMsgDiv = element(by.id('wc-intyg-replaced-message'));
    return expect(replacedMsgDiv.getText()).to.eventually.contain('Intyget har ersatts av').then(function() {
        return replacedMsgDiv.element(by.css('a')).click().then(function() {
            logger.info('Intyget är ersatt. Går in på nya intyget.');
            return Promise.resolve(true);
        });
    }, function() {
        logger.info('Intyget är inte ersatt. Fortsätter...');
        return Promise.resolve(false);
    });
}

function gotoIntyg(intygstyp, status, intygRadElement, cb) {

    //Om det inte finns några intyg att använda
    if (!intygRadElement) {
        logger.info('Hittade inget intyg, skapar ett nytt..');
        createIntygWithStatus(intygstyp, status).then(function() {

            //Gå till det nyskapade intyget
            console.log(helpers.intygURL(intygstyp, global.intyg.id));
            browser.get(helpers.intygURL(intygstyp, global.intyg.id)).then(function() {
                cb();
            });
        });
    }
    //Gå in på intyg
    else {
        intygRadElement.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE);
        cb();
    }
}

function getIER(intygstyp, status, callback) {
    getIntygElementRow(intygstyp, status, function(el) {
        gotoIntyg(intygstyp, status, el, function(err) {
            ifIntygReplaced().then(function(ifIntygReplaced) {
                if (ifIntygReplaced) {
                    signNewIntyg().then(function() {
                        logger.info('Signerar intyget.');

                        browser.sleep(3000).then(function() {
                            element(by.id('grundData.patient.postadress')).clear().sendKeys('ygvuhbjnk').then(function() {
                                element(by.id('grundData.patient.postnummer')).clear().sendKeys('12345').then(function() {
                                    element(by.id('grundData.patient.postort')).clear().sendKeys('ygvuhbjnk').then(function() {
                                        browser.sleep(3000).then(function() {
                                            element(by.id('signera-utkast-button')).sendKeys(protractor.Key.SPACE).then(callback);
                                        });
                                    });
                                });
                            });
                        });

                    });
                } else {
                    browser.getCurrentUrl().then(function(text) {
                        intyg.id = text.split('/').slice(-1)[0];
                        intyg.id = intyg.id.split('?')[0];
                        logger.info('intyg.id:' + intyg.id);
                        if (err) {
                            callback(JSON.stringify(err));
                        } else {
                            callback();
                        }
                    });
                }
            });
        });
    });
}

module.exports = function() {

    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, {
        timeout: 700 * 1000
    }, function(intygstyp, status, callback) {
        intyg.typ = intygstyp;
        getIER(intygstyp, status, callback);
    });



    this.Given(/^jag går in på ett slumpat SMI\-intyg med status "([^"]*)"$/, {
        timeout: 700 * 1000
    }, function(status, callback) {
        var randomIntyg = shuffle([
            'Läkarintyg för sjukpenning',
            'Läkarutlåtande för sjukersättning',
            'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
        ])[0];
        logger.info('Intyg type: ' + randomIntyg);
        intyg.typ = randomIntyg;
        getIER(randomIntyg, status, callback);
    });


};
