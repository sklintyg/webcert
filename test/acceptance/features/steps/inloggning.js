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

/* globals pages, protractor, person, browser, intyg, logger,wcTestTools*/

'use strict';

var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var fk7263Intyg = pages.intyg.fk['7263'].intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
// var webcertBase = pages.webcertBase;
var checkValues = require('./checkValues');
var helpers = wcTestTools.helpers;

// webcertBase.flikarsokSkrivIntyg

function gotoPatient(pnr) {
    person.id = pnr;

    if (global.user.origin !== 'DJUPINTEGRATION') {
        element(by.id('menu-skrivintyg')).sendKeys(protractor.Key.SPACE);
        browser.sleep(1000);
    }
    sokSkrivIntygPage.selectPersonnummer(pnr);
    //Patientuppgifter visas
    var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
    return expect(patientUppgifter.getText()).to.eventually.contain(pnr);
}

module.exports = function() {

    // this.Then(/^vill jag vara inlogger.infoad$/, function(callback) {
    //     expect(webcertBase.header.getText()).to.eventually.contain('logger.infoa ut').and.notify(callback);
    //     // expect(element(by.id('wcHeader')).getText()).to.eventually.contain('logger.infoa ut').and.notify(callback);
    // });

    this.When(/^jag väljer patienten "([^"]*)"$/, function(personnummer, callback) {
        gotoPatient(personnummer).and.notify(callback);
    });

    this.Given(/^jag går in på en patient$/, function(callback) {
        var patienter = [
            '19000118-9810',
            '19000119-9801',
            '19000120-9816',
            '19000121-9807',
            '19000122-9814',
            '19000123-9805',
            '19000124-9812',
            '19000125-9803',
            '19000126-9810',
            '19000127-9801',
            '19000128-9818'
        ];

        gotoPatient(helpers.testdata.shuffle(patienter)[0]).and.notify(callback);
    });

    this.Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function(intygsTyp, callback) {
        logger.info('intygstyp: ' + intygsTyp);
        intyg.typ = intygsTyp;
        sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intygsTyp);
        sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);

        // Save INTYGS_ID:
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            logger.debug(intyg.id);
        });
        callback();
    });

    this.Then(/^ska intygets status vara "([^"]*)"$/, function(statustext, callback) {
        expect(fk7263Intyg.intygStatus.getText()).to.eventually.contain(statustext).and.notify(callback);
        // expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Then(/^(?:ska jag|jag ska) se den data jag angett för intyget$/, function(callback) {
        checkValues.forIntyg(intyg, callback);
    });

    this.Given(/^ska signera\-knappen inte vara synlig$/, function(callback) {
        expect(fk7263Utkast.signeraButton.isPresent()).to.eventually.become(false).and.notify(callback);
    });

};
