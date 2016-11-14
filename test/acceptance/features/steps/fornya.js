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

/*globals intyg, logger, Promise, protractor, pages, wcTestTools, JSON*/

'use strict';

var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkValues = wcTestTools.testdata.values.fk;
var helpers = require('./helpers.js');

module.exports = function() {

    this.Given(/^jag förnyar intyget$/, function(callback) {
        helpers.updateEnhetAdressForNewIntyg();
        fkIntygPage.fornyaBtn.sendKeys(protractor.Key.SPACE).then(function() {
            return fkIntygPage.fornyaDialog.btn.sendKeys(protractor.Key.SPACE);
        }).then(callback);
    });

    this.Given(/^ska fält för Baserat på vara tomma$/, function(callback) {
        var baserasPa = fkUtkastPage.baserasPa;
        var minUndersokning = baserasPa.minUndersokning.datum;
        var minTelefonkontakt = baserasPa.minTelefonkontakt.datum;
        var journaluppgifter = baserasPa.journaluppgifter.datum;
        var annat = baserasPa.annat.datum;


        if (intyg.smittskydd) {
            logger.info('Eftersom intyget är ett smittskyddsintyg så kontrolleras att baserat-på fälten inte visas');
            Promise.all([
                expect(minUndersokning.isDisplayed()).to.become(false),
                expect(minTelefonkontakt.isDisplayed()).to.become(false),
                expect(journaluppgifter.isDisplayed()).to.become(false),
                expect(annat.isDisplayed()).to.become(false)
            ]).then(function(value) {
                logger.info('OK - ' + value);
                callback();
            }, function(reason) {
                callback('FEL, ' + reason);
            });
        } else {
            Promise.all([
                expect(minUndersokning.getAttribute('value')).to.eventually.contain(''),
                expect(minTelefonkontakt.getAttribute('value')).to.eventually.contain(''),
                expect(journaluppgifter.getAttribute('value')).to.eventually.contain(''),
                expect(annat.getAttribute('value')).to.eventually.contain('')
            ]).then(function(value) {
                logger.info('OK - ' + value);
                callback();
            }, function(reason) {
                callback('FEL, ' + reason);
            });
        }

    });
    this.Given(/^ska fält för Bedömning av arbetsförmåga vara tomma$/, function(callback) {
        var nedsatt = fkUtkastPage.nedsatt;

        Promise.all([
            expect(nedsatt.med25.from.getAttribute('value')).to.eventually.contain(''),
            expect(nedsatt.med25.tom.getAttribute('value')).to.eventually.contain(''),

            expect(nedsatt.med50.from.getAttribute('value')).to.eventually.contain(''),
            expect(nedsatt.med50.tom.getAttribute('value')).to.eventually.contain(''),

            expect(nedsatt.med75.from.getAttribute('value')).to.eventually.contain(''),
            expect(nedsatt.med75.tom.getAttribute('value')).to.eventually.contain(''),

            expect(nedsatt.med100.from.getAttribute('value')).to.eventually.contain(''),
            expect(nedsatt.med100.tom.getAttribute('value')).to.eventually.contain('')

        ]).then(function(value) {
            logger.info('OK - ' + value);
            callback();
        }, function(reason) {
            callback('FEL, ' + reason);
        });
    });

    this.Given(/^jag anger datum för Baserat på$/, function(callback) {
        intyg.baserasPa = fkValues.getRandomBaserasPa(intyg.smittskydd);
        //Ange baseras på
        fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa).then(function() {
            logger.info('OK - angeIntygetBaserasPa :' + JSON.stringify(intyg.baserasPa));
            callback();
        }, function(reason) {
            callback('FEL, angeIntygetBaserasPa,' + reason);
        });
    });

    this.Given(/^jag anger datum för arbetsförmåga$/, function(callback) {
        intyg.arbetsformaga = fkValues.getRandomArbetsformaga();

        fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function() {
            logger.info('OK - angeArbetsformaga :' + JSON.stringify(intyg.arbetsformaga));
            callback();
        }, function(reason) {
            callback('FEL, angeArbetsformaga,' + reason);
        });
    });

    this.Given(/^ska fält för Kontakt med FK vara tom$/, function(callback) {
        // Write code here that turns the phrase above into concrete actions
        expect(fkUtkastPage.kontaktFk.getAttribute('checked')).to.eventually.be.a('null')
            .then(function(value) {
                logger.info('OK - ' + value);
                callback();
            }, function(reason) {
                callback('FEL, ' + reason);
            });
    });

    this.Given(/^jag anger kontakt med FK$/, function(callback) {
        intyg.kontaktOnskasMedFK = fkValues.getRandomKontaktOnskasMedFK();
        fkUtkastPage.angeKontaktOnskasMedFK(intyg.kontaktOnskasMedFK).then(function() {
            logger.info('OK - angeKontaktOnskasMedFK :' + JSON.stringify(intyg.kontaktOnskasMedFK));
            callback();
        }, function(reason) {
            callback('FEL, angeKontaktOnskasMedFK,' + reason);
        });
    });
};
