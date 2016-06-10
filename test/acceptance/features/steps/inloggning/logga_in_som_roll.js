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

/* globals browser */

'use strict';
var logInAsUserRole = require('./login.helpers.js').logInAsUserRole;
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
        logInAsUserRole(userObj, 'Tandläkare').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som vårdadministratör$/, function(callback) {
        var userObj = {
            fornamn: 'Susanne',
            efternamn: 'Johansson Karlsson',
            hsaId: 'TSTNMT2321000156-105J',
            enhetId: 'TSTNMT2321000156-105F'
        };
        logInAsUserRole(userObj, 'Vårdadministratör').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som uthoppad vårdadministratör$/, function(callback) {
        var userObj = {
            fornamn: 'Susanne',
            efternamn: 'Johansson Karlsson',
            hsaId: 'TSTNMT2321000156-105J',
            enhetId: 'TSTNMT2321000156-105F'
        };
        logInAsUserRole(userObj, 'Vårdadministratör', 'UTHOPP', 'VARDADMINISTRATOR').then(callback);
    });
    this.Given(/^att jag är inloggad som läkare( som inte accepterat kakor)?$/, function(inteAccepteratKakor, callback) {
        console.log('Kakor accepteras: ' + !inteAccepteratKakor);
        var userObj = {
            fornamn: 'Erik',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-105H',
            enhetId: 'TSTNMT2321000156-105F',
            forskrivarKod: '9300005',
            befattningsKod: '204090'
        };
        logInAsUserRole(userObj, 'Läkare', null, null, inteAccepteratKakor).and.notify(callback);
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare$/, function(callback) {

        var userObj = {
            fornamn: 'Åsa',
            efternamn: 'Svensson',
            hsaId: 'TSTNMT2321000156-100L',
            enhetId: 'TSTNMT2321000156-1003',
            forskrivarKod: '2481632'
        };
        logInAsUserRole(userObj, 'Läkare', 'DJUPINTEGRATION', 'LAKARE').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare på vårdenhet "([^"]*)"$/, function(enhetHsa, callback) {
        var userObj = {
            fornamn: 'Åsa',
            efternamn: 'Svensson',
            hsaId: 'TSTNMT2321000156-100L',
            enhetId: enhetHsa,
            forskrivarKod: '2481632'
        };
        logInAsUserRole(userObj, 'Läkare', 'UTHOPP', 'LAKARE').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som uthoppsläkare$/, function(callback) {
        var userObj = {
            fornamn: 'Åsa',
            efternamn: 'Svensson',
            hsaId: 'TSTNMT2321000156-100L',
            enhetId: 'TSTNMT2321000156-1003',
            forskrivarKod: '2481632'
        };
        logInAsUserRole(userObj, 'Läkare', 'UTHOPP', 'LAKARE').and.notify(callback);
    });

    this.Given(/^ska jag ha rollen "([^"]*)"$/, function(roll, callback) {
        checkUserRole().then(function(value) {
            var re = /\[\"(.*)\"\]/;
            value = value.replace(re, '$1');
            expect(value).to.equal(roll);
            callback();
        });
    });

    this.Given(/^jag ska ha origin "([^"]*)"/, function(origin, callback) {
        expect(checkUserOrigin()).to.eventually.be.equal(origin).and.notify(callback);
    });
};

function checkUserRole() {
    return performUserCheck('role');
}

function checkUserOrigin() {
    return performUserCheck('origin');
}

function performUserCheck(userconfig) {
    browser.ignoreSynchronization = true;
    browser.get('testability/user/' + userconfig + '/');
    var attribute = element(by.css('pre')).getText();
    browser.navigate().back();
    browser.sleep(1000);
    browser.ignoreSynchronization = false;
    return attribute;
}
