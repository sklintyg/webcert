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

/* globals pages, protractor, person, browser, intyg, logger,wcTestTools, Promise*/

'use strict';

var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var fk7263Intyg = pages.intyg.fk['7263'].intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
// var webcertBase = pages.webcertBase;
var checkValues = require('../checkValues');
var testdataHelpers = wcTestTools.helpers.testdata;
var testdata = wcTestTools.testdata;
var testpatienter = testdata.values.patienter;
// var logInAsUserRole = require('./login.helpers.js').logInAsUserRole;
var parallell = require('./parallellt_util.js');
var helpers = require('../helpers.js');

// webcertBase.flikarsokSkrivIntyg

function gotoPatient(pnr) {//förutsätter  att personen finns i PU-tjänsten
    person.id = pnr;

    if (global.user.origin !== 'DJUPINTEGRATION') {
        element(by.id('menu-skrivintyg')).click();
        browser.sleep(1000);
    }
    sokSkrivIntygPage.selectPersonnummer(pnr);
    logger.info('Går in på patient ' + pnr);
    //Patientuppgifter visas
    var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
    return expect(patientUppgifter.getText()).to.eventually.contain(pnr);
}
function gotoPerson(pnr,callback) {//förutsätter inte att personen finns i PU-tjänsten
    person.id = pnr;

    sokSkrivIntygPage.selectPersonnummer(pnr);
    logger.info('Går in på patient ' + pnr);
    callback();
}

var forkedBrowser;

function setForkedBrowser(forkedBrowser2) {
    console.log('Store forked browser for next step');
    forkedBrowser = forkedBrowser2;
}

module.exports = function() {

    this.When(/^jag väljer patienten "([^"]*)"$/, function(personnummer) {//förutsätter att personen finns i PU-tjänsten
        return gotoPatient(personnummer);
    });

     this.Given(/^jag matar in personnummer som inte finns i PUtjänsten$/, function (callback) {
         return gotoPerson(testdata.values.patienterMedSamordningsnummerEjPU[0].nummer,callback);//personnummret finns inte med i PU-tjänsten
       });


    this.Given(/^jag går in på en patient med sekretessmarkering$/, function() {
        var patient = testdataHelpers.shuffle(testdata.values.patienterMedSekretessmarkering)[0];
        return gotoPatient(patient.nummer);
    });

    this.Given(/^jag går in på en patient$/, function() {
        return gotoPatient(testdataHelpers.shuffle(testpatienter)[0]);
    });

    this.Given(/^ska en varningsruta innehålla texten "([^"]*)"$/, function(text) {
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

    this.Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function(intygsTyp, callback) {
        intyg.typ = intygsTyp;
        Promise.all([
            sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intygsTyp),
            sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE)
        ]).then(function() {
            // Spara intygsid för kommande steg
            browser.getCurrentUrl().then(function(text) {
                intyg.id = text.split('/').slice(-1)[0];
                logger.info('intyg.id: ' + intyg.id, function() {
                    callback();
                });

            });
        });

    });

    this.Given(/^sedan öppnar intyget i två webbläsarinstanser$/, function(callback) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            var intygtyp = helpers.getAbbrev(intyg.typ);

            // User
            var userObj = helpers.getUserObj(helpers.userObj.UserKey.EN);
            var inteAccepteratKakor = true;

            // Browser & URL
            var forkedBrowser = browser.forkNewDriverInstance(true);
            var intygEditUrl = process.env.WEBCERT_URL + 'web/dashboard#/' + intygtyp.toLowerCase() + '/edit/' + intyg.id;

            parallell.login({
                userObj: userObj,
                role: helpers.userObj.Role.DOCTOR,
                cookies: inteAccepteratKakor
            }, intygEditUrl, forkedBrowser).then(function() {
                setForkedBrowser(forkedBrowser);
                callback();
            });
        } else {
            throw new Error(intyg.typ + ' is not implemented.');
        }

    });

    this.Given(/^ska ett felmeddelande visas$/, function(callback) {
        parallell.changeFields(forkedBrowser).then(function() {
            console.log('saveErrorMessage found');
            return parallell.refreshBroswer(forkedBrowser);
        }).then(function() {
            // Known issue - https://github.com/angular/protractor/issues/2203
            parallell.closeBrowser(forkedBrowser).then(callback);
        });
    });

    this.Then(/^ska intygets status vara "([^"]*)"$/, function(statustext, callback) {
        expect(fk7263Intyg.intygStatus.getText()).to.eventually.contain(statustext).and.notify(callback);
        // expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Then(/^(?:ska jag|jag ska) se den data jag angett för intyget$/, function() {
        return checkValues.forIntyg(intyg);
    });

    this.Given(/^ska signera\-knappen inte vara synlig$/, function(callback) {
        expect(fk7263Utkast.signeraButton.isPresent()).to.eventually.become(false).and.notify(callback);
    });

};
