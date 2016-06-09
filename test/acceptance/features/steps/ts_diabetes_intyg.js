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

/*global browser, intyg, logger, wcTestTools, user, person, protractor, pages, Promise, JSON, wcTestTools */
'use strict';

// function stringStartWith (string, prefix) {
//     return string.slice(0, prefix.length) === prefix;
// }
var testdataHelper = wcTestTools.helpers.testdata;
var loginHelpers = require('./inloggning/login.helpers.js');
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;

function getIntygElement(intygstyp, status, cb) {
    var qaTable = element(by.css('table.table-qa'));
    qaTable.all(by.cssContainingText('tr', status)).filter(function(elem, index) {
        return elem.all(by.css('td')).get(2).getText().then(function(text) {
            return (text === intygstyp);
        });
    }).then(function(filteredElements) {
        cb(filteredElements[0]);
    });
}

function gotoIntyg(intygstyp, status, intygRadElement, cb) {

    //Om det inte finns några intyg att använda
    if (!intygRadElement) {
        logger.info('Hittade inget intyg, skapar ett nytt..');
        createIntygWithStatus(intygstyp, status, function(err) {
            if (err) {
                cb(err);
            }

            //Uppdatera sidan och gå in på patienten igen
            browser.refresh();
            browser.get('/web/dashboard#/create/choose-patient/index');

            sokSkrivIntygPage.selectPersonnummer(person.id);

            getIntygElement(intygstyp, status, function(el) {
                el.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE);
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
module.exports = function() {

    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, {
        timeout: 500 * 1000
    }, function(intygstyp, status, callback) {
        getIntygElement(intygstyp, status, function(el) {
            gotoIntyg(intygstyp, status, el, function(err) {
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
            });
        });
    });
};

var rUtil = wcTestTools.restUtil;
var intygGenerator = require('../../../webcertTestTools/util/intygGenerator.util.js');

function createIntygWithStatus(typ, status, cb) {
    //TODO, Hantera ts-intyg

    intyg.id = testdataHelper.generateTestGuid();
    logger.debug('intyg.id = ' + intyg.id);

    if (typ.indexOf('Transportstyrelsen') > -1) {
        createTsIntyg(typ, status, cb);

    } else if (typ === 'Läkarintyg FK 7263') {

        createIntygWithRest({
            personnr: person.id,
            patientNamn: 'Test Testsson',
            //issuerId : '',
            issuer: user.fornamn + ' ' + user.efternamn,
            issued: '2015-04-01',
            validFrom: '2015-04-01',
            validTo: '2015-04-11',
            enhetId: user.enhetId,
            //enhet : '',
            vardgivarId: 'TSTNMT2321000156-1002',
            intygType: 'fk7263',
            intygId: intyg.id,
            sent: (status === 'Mottaget' || status === 'Makulerat'),
            revoked: (status === 'Makulerat')
        }, cb);
    } else if (typ === 'Läkarutlåtande för sjukersättning') {

        createIntygWithRest({
            personnr: person.id,
            patientNamn: 'Test Testsson',
            issuer: user.hsaId,
            issued: '2016-04-01',
            validFrom: '2016-04-01',
            validTo: '2016-04-11',
            enhetId: user.enhetId,
            vardgivarId: 'TSTNMT2321000156-1002',
            intygType: 'luse',
            intygId: intyg.id,
            sent: (status === 'Mottaget' || status === 'Makulerat'),
            revoked: (status === 'Makulerat')
        }, cb);

    } else {
        cb('TODO: Hantera fall då det inte redan finns något intyg att använda');
    }
}

function createIntygWithRest(intygOptions, cb) {
    var userObj = {
        fornamn: user.fornamn,
        efternamn: user.efternamn,
        hsaId: user.hsaId,
        enhetId: user.enhetId,
        lakare: user.lakare,
        forskrivarKod: user.forskrivarKod
    };

    rUtil.login(userObj).then(function(data) {
        logger.debug('Login OK');
        return Promise.resolve('SUCCESS');
    }, function(error) {
        cb(error);
    }).then(function() {
        rUtil.createIntyg(intygGenerator.buildIntyg(intygOptions)).then(function(response) {
            logger.info('Skapat intyg via REST-api');
            cb();
        }, function(error) {
            cb(error);
        });
    });
}


function createTsIntyg(typ, status, cb) {
    var standardUser = global.user;

    var userObj = {
        fornamn: 'Erik',
        efternamn: 'Nilsson',
        hsaId: 'TSTNMT2321000156-105H',
        enhetId: 'TSTNMT2321000156-105F',
        lakare: true,
        forskrivarKod: '2481632',
        befattningsKod: '204090'
    };
    loginHelpers.logInAsUserRole(userObj, 'Läkare')
        .and.notify(function() {
            sokSkrivIntygPage.selectPersonnummer(person.id);
            sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(typ);
            sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);
            global.intyg = require('./helpers').generateIntygByType(typ);
            require('./fillIn').fillIn(intyg, function() {
                fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
                if (status === 'Mottaget') {
                    fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE);
                    fkIntygPage.skicka.samtyckeCheckbox.sendKeys(protractor.Key.SPACE);
                    fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE);
                }

                var userObj = {
                    fornamn: standardUser.fornamn,
                    efternamn: standardUser.efternamn,
                    hsaId: standardUser.hsaId,
                    enhetId: standardUser.enhetId,
                    lakare: standardUser.lakare,
                    forskrivarKod: standardUser.forskrivarKod
                };

                loginHelpers.logInAsUserRole(userObj, standardUser.roleName, standardUser.origin, standardUser.role).and.notify(cb);
            });
        });
}
