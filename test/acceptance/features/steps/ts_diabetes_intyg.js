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

/*global browser, intyg, logg, wcTestTools, user, person, protractor, pages */
'use strict';

// function stringStartWith (string, prefix) {
//     return string.slice(0, prefix.length) === prefix;
// }
var testdataHelper = wcTestTools.helpers.testdata;
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;

function getIntygElement(intygstyp, status, cb) {
    var qaTable = element(by.css('table.table-qa'));
    qaTable.all(by.cssContainingText('tr', intygstyp)).filter(function(elem, index) {
        return elem.getText().then(function(text) {
            return (text.indexOf(status) > -1);
        });
    }).then(function(filteredElements) {
        cb(filteredElements[0]);
    });
}

function gotoIntyg(intygstyp, status, intygRadElement, cb) {

    //Om det inte finns några intyg att använda
    if (!intygRadElement) {
        logg('Hittade inget intyg, skapar ett nytt via rest..');
        createIntygWithStatus(intygstyp, status, function(err) {

            if (err) {
                cb(err);
            }

            //Uppdatera sidan och gå in på patienten igen
            browser.refresh();
            element(by.id('menu-skrivintyg')).sendKeys(protractor.Key.SPACE);
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



    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, function(intygstyp, status, callback) {
        getIntygElement(intygstyp, status, function(el) {
            gotoIntyg(intygstyp, status, el, function() {
                browser.getCurrentUrl().then(function(text) {
                    intyg.id = text.split('/').slice(-1)[0];
                    intyg.id = intyg.id.split('?')[0];
                    callback();
                });
            });
        });
    });

    // this.Given(/^jag skickar intyget till "([^"]*)"$/, function(dest, callback) {

    //     //Fånga intygets id
    //     if (!global.intyg) {
    //         global.intyg = {};
    //     }
    //     browser.getCurrentUrl().then(function(text) {
    //         intyg.id = text.split('/').slice(-1)[0];
    //         logg('Intygsid: ' + intyg.id);
    //     });

    //     element(by.id('sendBtn')).click();
    //     element(by.id('patientSamtycke')).click();
    //     element(by.id('button1send-dialog')).click();

    //     callback();
    // });
};



var restUtil = require('../../../webcertTestTools/util/rest.util.js');
var intygGenerator = require('../../../webcertTestTools/util/intygGenerator.util.js');

function createIntygWithStatus(typ, status, cb) {
    //TODO, FUNKTION EJ KLAR
    // cb('TODO: Hantera fall då det inte redan finns något intyg att använda');


    intyg.id = testdataHelper.generateTestGuid();
    console.log('intyg.id = ' + intyg.id);

    // if (typ === 'Transportstyrelsens läkarintyg' && status === 'Signerat') {
    //     restTestdataHelper.createIntygFromTemplate('ts-bas', intyg.id).then(function(response) {
    //         console.log(response.request.body);

    //     }, function(error) {
    //         cb(error);
    //     }).then(cb);
    // } else 
    if (typ === 'Läkarintyg FK 7263' && status === 'Signerat') {

        createIntygWithRest({
            personnr: person.id,
            patientNamn: 'Test Testsson',
            //issuerId : '',
            issuer: user.fornamn + ' ' + user.efternamn,
            issued: '2013-04-01',
            validFrom: '2013-04-01',
            validTo: '2013-04-11',
            enhetId: user.enhetId,
            //enhet : '',
            vardgivarId: 'IFV1239877878-1041',
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
            issued: '2013-04-01',
            validFrom: '2013-04-01',
            validTo: '2013-04-11',
            enhetId: user.enhetId,
            //enhet : '',
            vardgivarId: 'IFV1239877878-1041',
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
    // user.lakare = true;
    // user.forskrivarKod = '2481632';

    restUtil.login(user).then(function(data) {
        console.log('Login OK');
    });

    restUtil.createIntyg(intygGenerator.buildIntyg(intygOptions)).then(function(response) {
        logg('Skapat intyg via REST-api');
        cb();
    }, function(error) {
        cb(error);
    });
}