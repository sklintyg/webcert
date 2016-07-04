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

/*global intyg,wcTestTools, protractor, browser */

'use strict';
var fillIn = require('./').fillIn;
var generateIntygByType = require('../helpers.js').generateIntygByType;
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var td = wcTestTools.testdata;

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function(callback) {
        if (!global.intyg.typ) {
            callback('Intyg.typ odefinierad.');
        } else {
            global.intyg = generateIntygByType(intyg.typ, intyg.id);
            console.log(intyg);
            fillIn(global.intyg, callback);
        }
    });

    this.Given(/^jag ändrar diagnoskod$/, function(callback) {
        fkUtkastPage.angeDiagnosKod(td.values.fk.getRandomDiagnoskod())
            .then(callback());
    });

    this.Given(/^jag ändrar i fältet (sjukskrivningsperiod|arbetsförmåga|diagnoskod)*$/, function(field, callback) {
        if (field === 'sjukskrivningsperiod') {
            browser.ignoreSynchronization = true;
            fkUtkastPage.nedsatt.med25.tom.clear().then(function() {
                fkUtkastPage.nedsatt.med25.tom.sendKeys('2017-01-02').then(function() {
                    browser.ignoreSynchronization = false;
                    callback();
                });
            });
        } else if (field === 'arbetsförmåga') {
            fkUtkastPage.nedsatt.med25.checkbox.sendKeys(protractor.Key.SPACE).then(callback);
        } else if (field === 'diagnoskod') {
            var diagnosKod = td.values.fk.getRandomDiagnoskod();
            fkUtkastPage.diagnosKod.sendKeys(diagnosKod).then(callback);
        } else {
            callback(null, 'pending');
        }
    });

    this.Given(/^jag fyller i resten av de nödvändiga fälten\.$/, function(callback) {
        fkUtkastPage.baserasPa.minUndersokning.checkbox.sendKeys(protractor.Key.SPACE).then(function() {
            fkUtkastPage.funktionsNedsattning.sendKeys('Halt och lytt').then(function() {
                fkUtkastPage.aktivitetsBegransning.sendKeys('Orkar inget').then(function() {
                    fkUtkastPage.nuvarandeArbete.sendKeys('Stuveriarbetare').then(callback);
                });
            });
        });
    });


};
