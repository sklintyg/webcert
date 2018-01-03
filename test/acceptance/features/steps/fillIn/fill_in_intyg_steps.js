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

/*global intyg,wcTestTools, protractor, browser, testdata, pages ,logger*/

'use strict';

/*jshint maxcomplexity:false */
var fillIn = require('./').fillIn;
var generateIntygByType = require('../helpers.js').generateIntygByType;
var helpers = require('../helpers');
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var luseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var lisjpUtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
var luaeFSUtkastPage = wcTestTools.pages.intyg.luaeFS.utkast;
var tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
var tsDiabetesUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
var shuffle = wcTestTools.helpers.testdata.shuffle;
var moveAndSendKeys = helpers.moveAndSendKeys;

var td = wcTestTools.testdata;
var fkValues = wcTestTools.testdata.values.fk;

function chooseRandomFieldBasedOnIntyg(isSMIIntyg, intygShortcode, clearFlag) {
    var field = helpers.randomPageField(isSMIIntyg, intygShortcode);
    logger.info('Fältet som ändras är: ' + field + ' i intyg ' + intygShortcode);
    return changeField(intygShortcode, field, clearFlag);
}

function changeField(intygShortcode, field, clearFlag) {
    if (intygShortcode === 'LUSE') {
        if (field === 'aktivitetsbegransning') {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(luseUtkastPage.aktivitetsbegransning, intyg.aktivitetsbegransning);
        } else if (field === 'sjukdomsforlopp') {
            intyg.sjukdomsforlopp = helpers.randomTextString();
            return moveAndSendKeys(luseUtkastPage.sjukdomsforlopp, intyg.sjukdomsforlopp);
        } else if (field === 'funktionsnedsattning') {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.intellektuell = helpers.randomTextString();

            return moveAndSendKeys(luseUtkastPage.funktionsnedsattning.intellektuell.checkbox, protractor.Key.SPACE).then(function() {
                return browser.sleep(1000).then(function() {
                    return moveAndSendKeys(luseUtkastPage.funktionsnedsattning.intellektuell.text, intyg.funktionsnedsattning.intellektuell)
                        .then(function() {
                            logger.info('OK - Angav: ' + intyg.funktionsnedsattning.intellektuell);
                            return;
                        }, function(reason) {
                            console.trace(reason);
                            throw ('FEL - Angav: ' + intyg.funktionsnedsattning.intellektuell + ' ' + reason);
                        });
                });
            });
        }

    } else if (intygShortcode === 'LISJP') {
        if (field === 'aktivitetsbegransning') {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        } else if (field === 'funktionsnedsattning') {
            intyg.funktionsnedsattning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.funktionsnedsattning, intyg.sjukdomsforlopp);
        } else if (field === 'sysselsattning') {
            return lisjpUtkastPage.angeSysselsattning({
                typ: 'Arbetssökande'
            });
        }

    } else if (intygShortcode === 'LUAE_NA') {
        if (field === 'aktivitetsbegransning') {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        } else if (field === 'ovrigt') {
            return moveAndSendKeys(element(by.id('ovrigt')), helpers.randomTextString());
        } else if (field === 'sjukdomsforlopp') {
            return lisjpUtkastPage.angeSysselsattning({
                typ: 'Arbetssökande'
            });
        }
    } else if (intygShortcode === 'LUAE_FS') {
        if (field === 'funktionsnedsattningDebut') {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.debut = helpers.randomTextString();
            return browser.findElement(by.id('funktionsnedsattningDebut')).sendKeys(intyg.funktionsnedsattning.debut);
        } else if (field === 'funktionsnedsattningPaverkan') {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.paverkan = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.funktionsnedsattning.paverkan, intyg.funktionsnedsattning.paverkan);
        } else if (field === 'ovrigt') {
            intyg.ovrigt = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.ovrigt, intyg.ovrigt);
        }

    } else if (intygShortcode === 'FK7263') {
        if (clearFlag) {
            if (field === 'aktivitetsbegransning') {
                return fkUtkastPage.aktivitetsBegransning.clear();
            } else if (field === 'diagnoskod') {
                return fkUtkastPage.diagnosKod.clear();
            } else if (field === 'funktionsnedsattning') {
                return fkUtkastPage.funktionsNedsattning.clear();
            }
        }
    } else if (intygShortcode === 'TSTRK1007') {
        if (clearFlag) {
            if (field === 'funktionsnedsattning') {
                return moveAndSendKeys(tsBasUtkastPage.funktionsnedsattning.aYes, protractor.Key.SPACE).then(function() {
                    return tsBasUtkastPage.funktionsnedsattning.aText.clear();
                });
            } else if (field === 'hjartKarlsjukdom') {
                return moveAndSendKeys(tsBasUtkastPage.hjartKarl.cYes, protractor.Key.SPACE).then(function() {
                    return tsBasUtkastPage.hjartKarl.cText.clear();
                });
            } else if (field === 'utanKorrektion') {
                return tsBasUtkastPage.syn.hoger.utan.clear();
            }
        }
    } else if (intygShortcode === 'TSTRK1031') {
        if (field === 'hypoglykemier') {
            return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.b.yes, protractor.Key.SPACE).then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.d.yes, protractor.Key.SPACE).then(function() {
                    return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.d.antalEpisoder, helpers.randomTextString());
                });
            });
        } else if (field === 'diabetesBehandling') {
            return tsDiabetesUtkastPage.allmant.annanbehandling.clear().then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.allmant.annanbehandling, helpers.randomTextString());
            });
        } else if (field === 'specialist') {
            return element(by.id('specialist')).clear().then(function() {
                return moveAndSendKeys(element(by.id('specialist')), helpers.randomTextString());
            });


        }
    }
    throw ('intygShortcode och eller field matchar inte med något av alternativen i changeField funktionen');
}



function isValid(intygShortcode) {
    return (intygShortcode in helpers.intygShortcode);
}

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function() {
        if (!intyg.typ) {
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
            return moveAndSendKeys(luseUtkastPage.diagnoseCode, kod);
        } else {
            return moveAndSendKeys(fkUtkastPage.angeDiagnosKod, kod);
        }

    });


    this.Given(/^jag ändrar i fältet (arbetsförmåga|sjukskrivningsperiod|diagnoskod)$/, function(field) {
        logger.info('Fältet som ändras är: ' + field);

        if (field === 'sjukskrivningsperiod') {
            browser.ignoreSynchronization = true;
            return fkUtkastPage.nedsatt.med25.tom.clear().then(function() {
                return moveAndSendKeys(fkUtkastPage.nedsatt.med25.tom, fkValues.getRandomArbetsformaga().nedsattMed25.tom).then(function() {
                    browser.ignoreSynchronization = false;
                });
            });
        } else if (field === 'arbetsförmåga') {
            return moveAndSendKeys(fkUtkastPage.nedsatt.med25.checkbox, protractor.Key.SPACE);
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

    this.Given(/^jag ändrar i slumpat fält$/, function() {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var intygShortcode = helpers.getAbbrev(intyg.typ);

        if (isValid(intygShortcode)) {
            return chooseRandomFieldBasedOnIntyg(isSMIIntyg, intygShortcode);
        } else {
            throw Error('Intyg code not valid \'' + intygShortcode + '\'');
        }

    });

    this.Given(/^jag fyller i resten av de nödvändiga fälten\.$/, function() {
        return moveAndSendKeys(fkUtkastPage.baserasPa.minUndersokning.checkbox, protractor.Key.SPACE).then(function() {
            return moveAndSendKeys(fkUtkastPage.funktionsNedsattning, 'Halt och lytt').then(function() {
                return moveAndSendKeys(fkUtkastPage.aktivitetsBegransning, 'Orkar inget').then(function() {
                    return moveAndSendKeys(fkUtkastPage.nuvarandeArbete, 'Stuveriarbetare');
                });
            });
        });
    });


    this.Given(/^jag fyller i ett intyg som( inte)? är smitta$/, function(isSmitta) {
        isSmitta = (typeof isSmitta === 'undefined');
        logger.silly('isSmitta : ' + isSmitta);
        global.intyg = testdata.fk['7263'].getRandom(false, isSmitta);
        console.log(intyg);
        return fillIn(global.intyg);
    });
    this.Given(/^jag fyller i alla obligatoriska  fält för intyget$/, function() {
        if (!global.intyg.typ) {
            throw 'intyg.typ odefinierad.';
        } else {
            global.intyg = testdata.fk['7263'].getRandom(intyg.id, false);
            console.log(intyg);
            return fillIn(global.intyg);
        }
    });

    this.When(/^anger ett slutdatum som är tidigare än startdatum$/, function() {
        var utkastPage = pages.getUtkastPageByType(intyg.typ);
        var nedsatthet = shuffle(['nedsattMed25', 'nedsattMed50', 'nedsattMed75', 'nedsattMed100'])[0];
        logger.info('nedsatthet : ' + nedsatthet);
        global.intyg.arbetsformaga = {};
        global.intyg.arbetsformaga[nedsatthet] = {
            from: '2017-03-27',
            tom: '2016-04-01'
        };
        return utkastPage.angeArbetsformaga(intyg.arbetsformaga);
    });


};

module.exports.changingFields = function(isSMIIntyg, intygShortcode, callback, clearFlag) {
    chooseRandomFieldBasedOnIntyg(isSMIIntyg, intygShortcode, callback, clearFlag);

};
