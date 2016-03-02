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

/*global browser, intyg, logger, wcTestTools, user, person, protractor, pages, Promise */
'use strict';

// function stringStartWith (string, prefix) {
//     return string.slice(0, prefix.length) === prefix;
// }
var testdataHelper = wcTestTools.helpers.testdata;
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
                el.element(by.cssContainingText('button', 'Visa')).click();
                cb();
            });
        });

    }
    //Gå in på intyg
    else {
        intygRadElement.element(by.cssContainingText('button', 'Visa')).click();
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
                    callback(err);
                });
            });
        });
    });
};

var restUtil = require('../../../webcertTestTools/util/rest.util.js');
var intygGenerator = require('../../../webcertTestTools/util/intygGenerator.util.js');

function createIntygWithStatus(typ, status, cb) {
    //TODO, Hantera ts-intyg

    intyg.id = testdataHelper.generateTestGuid();
    logger.debug('intyg.id = ' + intyg.id);

    if (typ.indexOf('Transportstyrelsen') > -1) {
        createTsIntyg(typ, status, cb);

    } else if (typ === 'Läkarintyg FK 7263' && status === 'Signerat') {

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
            sent: false,
            revoked: false
        }, cb);
    } else if (typ === 'Läkarintyg FK 7263' && status === 'Mottaget') {

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
            sent: true,
            revoked: false
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

    restUtil.login(userObj).then(function(data) {
        logger.debug('Login OK');
        return Promise.resolve('SUCCESS');
    }, function(error) {
        cb(error);
    }).then(function() {
        restUtil.createIntyg(intygGenerator.buildIntyg(intygOptions)).then(function(response) {
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
        fornamn: 'Åsa',
        efternamn: 'Svensson',
        hsaId: 'TSTNMT2321000156-100L',
        enhetId: 'TSTNMT2321000156-1003',
        lakare: true,
        forskrivarKod: '2481632'
    };
    require('./login.helpers.js').logInAsUserRole(userObj, 'Läkare')
        .and.notify(function() {
            sokSkrivIntygPage.selectPersonnummer(person.id);
            sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(typ);
            sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);
            global.intyg = require('./helpers').generateIntygByType(typ);
            require('./fillIn').fillIn(intyg, function() {
                fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
                if (status === 'Mottaget') {
                    fkIntygPage.skicka.knapp.click();
                    fkIntygPage.skicka.samtyckeCheckbox.click();
                    fkIntygPage.skicka.dialogKnapp.click();
                }

                var userObj = {
                    fornamn: standardUser.fornamn,
                    efternamn: standardUser.efternamn,
                    hsaId: standardUser.hsaId,
                    enhetId: standardUser.enhetId,
                    lakare: standardUser.lakare,
                    forskrivarKod: standardUser.forskrivarKod
                };

                require('./login.helpers.js').logInAsUserRole(userObj, standardUser.roleName, standardUser.origin, standardUser.role).and.notify(cb);
            });
        });
}
