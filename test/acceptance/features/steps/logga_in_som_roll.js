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

/* globals pages*/
/* globals logg, browser, JSON */

'use strict';

// var webcertBasePage = pages.webcertBase;
// var webcertBase = pages.webcertBase;

module.exports = function() {

    this.Given(/^att jag är inloggad som tandläkare$/, function(callback) {
        var userObj = {
            fornamn: 'Louise',
            efternamn: 'Ericsson',
            hsaId: 'TSTNMT2321000156-103B',
            enhetId: 'TSTNMT2321000156-1039'
        };
        logInAsUserRole(userObj, 'Tandläkare', callback);
    });

    this.Given(/^att jag är inloggad som vårdadministratör$/, function(callback) {
        var userObj = {
            fornamn: 'Lena',
            efternamn: 'Karlsson',
            hsaId: 'IFV1239877878-104N',
            enhetId: 'IFV1239877878-1045'
        };
        logInAsUserRole(userObj, 'Vårdadministratör', callback);
    });

    this.Given(/^att jag är inloggad som uthoppad vårdadministratör$/, function(callback) {
        var userObj = {
            fornamn: 'Åsa',
            efternamn: 'Andersson',
            hsaId: 'IFV1239877878-104B',
            enhetId: 'IFV1239877878-1042'
        };
        logInAsUserRole(userObj, 'Läkare', callback, 'UTHOPP', 'VARDADMINISTRATOR');
    });
    this.Given(/^att jag är inloggad som läkare$/, function(callback) {
        var userObj = {
            fornamn: 'Jan',
            efternamn: 'Nilsson',
            hsaId: 'IFV1239877878-1049',
            enhetId: 'IFV1239877878-1042',
            lakare: true
        };
        logInAsUserRole(userObj, 'Läkare', callback);
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare$/, function(callback) {
        var userObj = {
            fornamn: 'Ivar',
            efternamn: 'Integration',
            hsaId: 'SE4815162344-1B01',
            enhetId: 'SE4815162344-1A02',
            lakare: true,
            forskrivarKod: '2481632'
            //     var userObj = {
            // fornamn:    'Åsa',
            // efternamn:  'Svensson',
            // hsaId:      'TSTNMT2321000156-100L',
            // enhetId:    'TSTNMT2321000156-1003',
            // lakare: true,
            // forskrivarKod: '2481632'
        };
        logInAsUserRole(userObj, 'Läkare', callback, 'DJUPINTEGRATION', 'LAKARE');
    });

    this.Given(/^att jag är inloggad som uthoppsläkare$/, function(callback) {
        var userObj = {
            fornamn: 'Jan',
            efternamn: 'Nilsson',
            hsaId: 'IFV1239877878-1049',
            enhetId: 'IFV1239877878-1042',
            lakare: true
        };
        logInAsUserRole(userObj, 'Läkare', callback, 'UTHOPP', 'LAKARE');
    });

    this.Given(/^ska jag ha rollen "([^"]*)"$/, function(roll, callback) {
        checkUserRole().then(function(value) {
            var re = /\[\"(.*)\"\]/;
            value = value.replace(re,'$1');
            expect(value).to.equal(roll);
            callback();
        });
    });

    this.Given(/^jag ska ha origin "([^"]*)"/, function(origin, callback) {
        expect(checkUserOrigin()).to.eventually.be.equal(origin).and.notify(callback);
    });
};

function logInAsUserRole(userObj,roleName,callback, newOrigin, newUserRole){
        logg('Loggar in som ' + userObj.fornamn+' '+userObj.efternamn + '..');
        global.user = JSON.parse(JSON.stringify(userObj));
        global.user.role = newUserRole || roleName;
        global.user.origin = newOrigin || 'NORMAL';
        browser.ignoreSynchronization = true;
        pages.welcome.get();
        pages.welcome.loginByJSON(JSON.stringify(userObj));

        if (newUserRole) {
            logg('Testability-api, sätter ny roll ' + newUserRole + ' för ' + userObj.fornamn+' '+userObj.efternamn + '..');
            browser.get('testability/user/role/' + newUserRole);
            browser.navigate().back();
        }
        if (newOrigin) {
            logg('Testability-api, sätter ny origin ' + newOrigin + ' för ' + userObj.fornamn+' '+userObj.efternamn + '..');
            browser.get('testability/user/origin/' + newOrigin);
            browser.navigate().back();
        }

        browser.ignoreSynchronization = false;
        browser.sleep(2000);
        // webcertBasePage.header.getText()
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain(roleName + ' - ' + userObj.fornamn+ ' ' + userObj.efternamn)
        // expect(webcertBase.header.getText()).to.eventually.contain(roleName + ' - ' + userObj.fornamn+ ' ' + userObj.efternamn)
	 .and.notify(callback);
}

function checkUserRole() {
    return performUserCheck('role');
}

function logInAsUserRole(userObj, roleName, callback, newOrigin, newUserRole) {
    logg('Loggar in som ' + userObj.fornamn + ' ' + userObj.efternamn + '..');
    global.user = userObj;

    browser.ignoreSynchronization = true;
    pages.welcome.get();
    pages.welcome.loginByJSON(JSON.stringify(userObj));

    if (newUserRole) {
        logg('Testability-api, sätter ny roll ' + newUserRole + ' för ' + userObj.fornamn + ' ' + userObj.efternamn + '..');
        browser.get('testability/user/role/' + newUserRole);
        browser.navigate().back();
    }
    if (newOrigin) {
        logg('Testability-api, sätter ny origin ' + newOrigin + ' för ' + userObj.fornamn + ' ' + userObj.efternamn + '..');
        browser.get('testability/user/origin/' + newOrigin);
        browser.navigate().back();
    }

    browser.ignoreSynchronization = false;
    browser.sleep(2000);
    // webcertBasePage.header.getText()
    expect(element(by.id('wcHeader')).getText()).to.eventually.contain(roleName + ' - ' + userObj.fornamn + ' ' + userObj.efternamn)
    // expect(webcertBase.header.getText()).to.eventually.contain(roleName + ' - ' + userObj.fornamn+ ' ' + userObj.efternamn)
    .and.notify(callback);
}


function checkUserOrigin() {
    return performUserCheck('origin');
}

function performUserCheck(userconfig) {
    browser.ignoreSynchronization = true;
    if (userconfig === 'role') {
        browser.get('testability/user/role/');
    }
    else if (userconfig === 'origin') {
         browser.get('testability/user/origin/');
    }
    var attribute = element(by.css('pre')).getText();
    browser.navigate().back();
    browser.sleep(1000);
    browser.ignoreSynchronization = false;
    return attribute;
}