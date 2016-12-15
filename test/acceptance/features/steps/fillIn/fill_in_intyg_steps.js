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

/*global intyg,wcTestTools, protractor, browser,testdata*/

'use strict';

/*jshint maxcomplexity:false */
var fillIn = require('./').fillIn;
var generateIntygByType = require('../helpers.js').generateIntygByType;
var helpers = require('../helpers');
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var luseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var lisjpUtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
var td = wcTestTools.testdata;

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function() {
        if (!global.intyg.typ) {
            throw 'intyg.typ odefinierad.';
        } else {
            global.intyg = generateIntygByType(intyg.typ, intyg.id);
            console.log(intyg);
            return fillIn(global.intyg);
        }
    });

    this.Given(/^jag ändrar diagnoskod$/, function() {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var kod = td.values.fk.getRandomDiagnoskod();
        if (isSMIIntyg) {
            return luseUtkastPage.diagnoseCode.sendKeys(kod);
        } else {
            return fkUtkastPage.angeDiagnosKod(kod);
        }

    });


    this.Given(/^jag ändrar i fältet (arbetsförmåga|sjukskrivningsperiod|diagnoskod)$/, function(field) {
        console.log('Fältet som ändras är: ' + field);

        if (field === 'sjukskrivningsperiod') {
            browser.ignoreSynchronization = true;
            return fkUtkastPage.nedsatt.med25.tom.clear().then(function() {
                return fkUtkastPage.nedsatt.med25.tom.sendKeys('2017-01-02').then(function() {
                    browser.ignoreSynchronization = false;
                });
            });
        } else if (field === 'arbetsförmåga') {
            return fkUtkastPage.nedsatt.med25.checkbox.sendKeys(protractor.Key.SPACE);
        } else if (field === 'diagnoskod') {
            var diagnosKod = td.values.fk.getRandomDiagnoskod();

            var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
            if (isSMIIntyg) {
                return lisjpUtkastPage.angeDiagnosKoder([diagnosKod]);
            } else {
                return fkUtkastPage.angeDiagnosKod(diagnosKod);
            }


        } else {
            throw ('Fält saknas i steg-funktion');
        }


    });

    this.Given(/^jag ändrar i slumpat fält$/, function(callback) {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var field;
        var intygShortcode = helpers.getAbbrev(intyg.typ);

        if (intygShortcode === 'LUSE') {
            field = helpers.randomPageField(isSMIIntyg, intygShortcode);
            console.log('Fältet som ändras är: ' + field);

            if (field === 'aktivitetsbegransning') {
                intyg.aktivitetsbegransning = helpers.randomTextString();
                luseUtkastPage.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning).then(callback);
            } else if (field === 'sjukdomsforlopp') {
                intyg.sjukdomsforlopp = helpers.randomTextString();
                luseUtkastPage.sjukdomsforlopp.sendKeys(intyg.sjukdomsforlopp).then(callback);
            } else if (field === 'funktionsnedsattning') {
                intyg.funktionsnedsattning = {};
                intyg.funktionsnedsattning.intellektuell = helpers.randomTextString();

                luseUtkastPage.funktionsnedsattning.intellektuell.checkbox.sendKeys(protractor.Key.SPACE).then(function() {
                    browser.sleep(1000).then(function() {
                        luseUtkastPage.funktionsnedsattning.intellektuell.text.sendKeys(intyg.funktionsnedsattning.intellektuell)
                            .then(function() {
                                console.log('OK - Angav: ' + intyg.funktionsnedsattning.intellektuell);
                                callback();
                            }, function(reason) {
                                throw ('FEL - Angav: ' + intyg.funktionsnedsattning.intellektuell + ' ' + reason);
                            });
                    });
                });
            } else {
                callback(null, 'pending');
            }

        } else if (intygShortcode === 'LISJP') {
            field = helpers.randomPageField(isSMIIntyg, intygShortcode);
            console.log('Fältet som ändras är: ' + field);

            if (field === 'aktivitetsbegransning') {
                intyg.aktivitetsbegransning = helpers.randomTextString();
                lisjpUtkastPage.konsekvenser.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning).then(callback);
            } else if (field === 'funktionsnedsattning') {
                intyg.funktionsnedsattning = helpers.randomTextString();
                lisjpUtkastPage.konsekvenser.funktionsnedsattning.sendKeys(intyg.sjukdomsforlopp).then(callback);
            } else if (field === 'sysselsattning') {
                lisjpUtkastPage.angeSysselsattning({
                    typ: 'Arbetssökande'
                }).then(callback());
            } else {
                callback(null, 'pending');
            }

        } else if (intygShortcode === 'LUAE_NA') {
            field = helpers.randomPageField(isSMIIntyg, intygShortcode);
            console.log('Fältet som ändras är: ' + field);

            if (field === 'aktivitetsbegransning') {
                intyg.aktivitetsbegransning = helpers.randomTextString();
                lisjpUtkastPage.konsekvenser.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning).then(callback);
            } else if (field === 'funktionsnedsattning') {
                intyg.funktionsnedsattning = helpers.randomTextString();
                lisjpUtkastPage.konsekvenser.funktionsnedsattning.sendKeys(intyg.sjukdomsforlopp).then(callback);
            } else if (field === 'sysselsattning') {
                lisjpUtkastPage.angeSysselsattning({
                    typ: 'Arbetssökande'
                }).then(callback());
            } else {
                callback(null, 'pending');
            }

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


    this.Given(/^jag fyller i ett intyg som( inte)? är smitta$/, function(isSmitta) {
        isSmitta = (typeof isSmitta === 'undefined');
        console.log(isSmitta);
        global.intyg = testdata.fk['7263'].getRandom(false, isSmitta);
        console.log(intyg);
        return fillIn(global.intyg);
    });


};
