/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/*global intyg,wcTestTools, protractor, browser, testdata, logger, Promise*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


/*jshint maxcomplexity:false */
const fillIn = require('./').fillIn;
const generateIntygByType = require('../helpers.js').generateIntygByType;
const helpers = require('../helpers');
const fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
const luseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
const lisjpUtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
const luaeFSUtkastPage = wcTestTools.pages.intyg.luaeFS.utkast;
const luaeNAUtkastPage = wcTestTools.pages.intyg.luaeNA.utkast;
const tsBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
const tsDiabetesUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
const dbUtkastPage = wcTestTools.pages.intyg.skv.db.utkast;
const doiUtkastPage = wcTestTools.pages.intyg.soc.doi.utkast;
const moveAndSendKeys = helpers.moveAndSendKeys;

const td = wcTestTools.testdata;
const fkValues = wcTestTools.testdata.values.fk;

/*
 *	Stödfunktioner
 *
 */

function clearField(intygShortcode, field) {
    logger.info('Fältet som tas bort ' + field + ' i intyg ' + intygShortcode);
    if (intygShortcode === 'LUSE') {
        if (field === 'aktivitetsbegransning') {
            return luseUtkastPage.aktivitetsbegransning.clear();
        } else if (field === 'sjukdomsforlopp') {
            return luseUtkastPage.sjukdomsforlopp.clear();
        } else if (field === 'funktionsnedsattning') {
            var fnElm = luseUtkastPage.funktionsnedsattning;
            var promiseArr = [];

            if (intyg.funktionsnedsattning.intellektuell) {
                promiseArr.push(fnElm.intellektuell.checkbox.click());
            }
            if (intyg.funktionsnedsattning.kommunikation) {
                promiseArr.push(fnElm.kommunikation.checkbox.click());
            }
            if (intyg.funktionsnedsattning.koncentration) {
                promiseArr.push(fnElm.koncentration.checkbox.click());
            }
            if (intyg.funktionsnedsattning.annanPsykisk) {
                promiseArr.push(fnElm.annanPsykisk.checkbox.click());
            }
            if (intyg.funktionsnedsattning.synHorselTal) {
                promiseArr.push(fnElm.synHorselTal.checkbox.click());
            }
            if (intyg.funktionsnedsattning.balansKoordination) {
                promiseArr.push(fnElm.balansKoordination.checkbox.click());
            }
            if (intyg.funktionsnedsattning.annanKroppslig) {
                promiseArr.push(fnElm.annanKroppslig.checkbox.click());
            }

            return Promise.all(promiseArr);

        }

    } else if (intygShortcode === 'LISJP') {
        if (field === 'aktivitetsbegransning') {
            return lisjpUtkastPage.konsekvenser.aktivitetsbegransning.clear();
        } else if (field === 'funktionsnedsattning') {
            return lisjpUtkastPage.konsekvenser.funktionsnedsattning.clear();
        } else if (field === 'sysselsattning') {
            if (intyg.sysselsattning.typ === 'NUVARANDE_ARBETE') {
                return lisjpUtkastPage.sysselsattning.typ.nuvarandeArbete.click();
            } else if (intyg.sysselsattning.typ === 'ARBETSSOKANDE') {
                return lisjpUtkastPage.sysselsattning.typ.arbetssokande.click();
            } else if (intyg.sysselsattning.typ === 'FORALDRALEDIG') {
                return lisjpUtkastPage.sysselsattning.typ.foraldraledighet.click();
            } else if (intyg.sysselsattning.typ === 'STUDIER') {
                return lisjpUtkastPage.sysselsattning.typ.studier.click();
            }
        }
    } else if (intygShortcode === 'LUAE_NA') {
        if (field === 'aktivitetsbegransning') {
            return lisjpUtkastPage.konsekvenser.aktivitetsbegransning.clear();
        } else if (field === 'ovrigt') {
            return element(by.id('ovrigt')).clear();
        } else if (field === 'sjukdomsforlopp') {
            return;
            //TODO
        }
    } else if (intygShortcode === 'LUAE_FS') {

        if (field === 'funktionsnedsattningDebut') {
            return luaeFSUtkastPage.funktionsnedsattning.debut.clear();
        } else if (field === 'funktionsnedsattningPaverkan') {
            return luaeFSUtkastPage.funktionsnedsattning.paverkan.clear();
        }


        //TODO} else if (intygShortcode === 'TSTRK1007') {

        //TODO} else if (intygShortcode === 'TSTRK1031') {

    }
    throw ('intygShortcode' + intygShortcode + ' och eller field ' + field + ' matchar inte med något av alternativen i clearField funktionen');
}


function changeField(intygShortcode, field) {
    logger.info('Fältet som ändras är: ' + field + ' i intyg ' + intygShortcode);

    var dodsdatumObj = {
        inteSakert: {
            year: '2018',
            month: '01',
            antraffadDod: '2017-09-27'
        }
    };


    if (intygShortcode === 'DB') {
        if (field === 'dodsdatum') {
            return dbUtkastPage.angeDodsdatum(dodsdatumObj);
        } else if (field === 'dodsplats') {
            return moveAndSendKeys(dbUtkastPage.dodsPlats.kommun.inputText, helpers.randomTextString());
        } else if (field === 'identitetstyrkt') {
            return moveAndSendKeys(dbUtkastPage.identitetStyrktGenom.inputText, helpers.randomTextString());
        }
    } else if (intygShortcode === 'DOI') {
        if (field === 'dodsdatum') {
            return doiUtkastPage.angeDodsdatum(dodsdatumObj);
        } else if (field === 'dodsplats') {
            return moveAndSendKeys(doiUtkastPage.dodsPlats.kommun.inputText, helpers.randomTextString());
        } else if (field === 'identitetstyrkt') {
            return moveAndSendKeys(doiUtkastPage.identitetStyrktGenom.inputText, helpers.randomTextString());
        }
    } else if (intygShortcode === 'LUSE') {
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
                return helpers.largeDelay();
            }).then(function() {
                //return intyg.funktionsnedsattning.intellektuell.text.isPresent();
                return luseUtkastPage.funktionsnedsattning.intellektuell.text.isPresent();
            }).then(function(present) {
                if (present) {
                    return moveAndSendKeys(luseUtkastPage.funktionsnedsattning.intellektuell.text, intyg.funktionsnedsattning.intellektuell);
                }
            }).then(function() {
                logger.info('OK - Angav: ' + intyg.funktionsnedsattning.intellektuell);
                return;
            }, function(reason) {
                console.trace(reason);
                throw ('FEL - Angav: ' + intyg.funktionsnedsattning.intellektuell + ' ' + reason);

            });
        }

    } else if (intygShortcode === 'LISJP') {
        if (field === 'aktivitetsbegransning') {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        } else if (field === 'funktionsnedsattning') {
            intyg.funktionsnedsattning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.funktionsnedsattning, intyg.funktionsnedsattning);
        } else if (field === 'sysselsattning') {
            return lisjpUtkastPage.angeSysselsattning({
                typ: 'ARBETSSOKANDE'
            });
        }

    } else if (intygShortcode === 'LUAE_NA') {
        if (field === 'aktivitetsbegransning') {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        } else if (field === 'ovrigt') {
            intyg.ovrigt = helpers.randomTextString();
            return moveAndSendKeys(luaeNAUtkastPage.ovrigt, intyg.ovrigt);
        } else if (field === 'sjukdomsforlopp') {
            intyg.sjukdomsforlopp = helpers.randomTextString();
            return moveAndSendKeys(luaeNAUtkastPage.sjukdomsforlopp, intyg.sjukdomsforlopp);
        }
    } else if (intygShortcode === 'LUAE_FS') {
        if (field === 'funktionsnedsattningDebut') {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.debut = helpers.randomTextString();

            return moveAndSendKeys(luaeFSUtkastPage.funktionsnedsattning.debut, intyg.funktionsnedsattning.debut);
        } else if (field === 'funktionsnedsattningPaverkan') {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.paverkan = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.funktionsnedsattning.paverkan, intyg.funktionsnedsattning.paverkan);
        } else if (field === 'ovrigt') {
            intyg.ovrigt = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.ovrigt, intyg.ovrigt);
        }

    } else if (intygShortcode === 'TSTRK1007') {
        if (field === 'funktionsnedsattning') {
            return moveAndSendKeys(tsBasUtkastPage.funktionsnedsattning.aYes, protractor.Key.SPACE).then(function() {
                return tsBasUtkastPage.funktionsnedsattning.aText.clear().then(function() {
                    return moveAndSendKeys(tsBasUtkastPage.funktionsnedsattning.aText, helpers.randomTextString());
                });
            });
        } else if (field === 'hjartKarlsjukdom') {
            return moveAndSendKeys(tsBasUtkastPage.hjartKarl.cYes, protractor.Key.SPACE).then(function() {
                return tsBasUtkastPage.hjartKarl.cText.clear().then(function() {
                    return moveAndSendKeys(tsBasUtkastPage.hjartKarl.cText, helpers.randomTextString());
                });
            });
        } else if (field === 'utanKorrektion') {
            return tsBasUtkastPage.syn.hoger.utan.clear().then(function() {
                return moveAndSendKeys(tsBasUtkastPage.syn.hoger.utan, '1.0');
            });
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
            return tsDiabetesUtkastPage.specialist.clear().then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.specialist, helpers.randomTextString());
            });


        }
    }
    throw ('intygShortcode ' + intygShortcode + ' och eller field ' + field + ' matchar inte med något av alternativen i changeField funktionen');
}



function isValid(intygShortcode) {
    return (intygShortcode in helpers.intygShortcode);
}

module.exports.changingFields = function(isSMIIntyg, intygShortcode, clearFlag) {
    var field = helpers.randomPageField(isSMIIntyg, intygShortcode);
    if (!clearFlag) {
        return changeField(intygShortcode, field);
    } else {
        return clearField(intygShortcode, field);
    }
};


/*
 *	Test steg
 *
 */

Given(/^jag fyller i alla nödvändiga fält för intyget(?:\s"([^"]*)")?$/, function(intygsTyp) {
    if (!intyg.typ && !intygsTyp) {
        throw 'intyg.typ odefinierad.';
    } else {
        global.intyg = generateIntygByType(intygsTyp || intyg.typ, intyg.id);
        logger.silly(intyg);
        return fillIn(global.intyg);
    }
});

Given(/^jag ändrar diagnoskod$/, function() {
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var diagnos = {
        kod: td.values.fk.getRandomDiagnoskod()
    };
    if (isSMIIntyg) {
        return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
    } else {
        return fkUtkastPage.angeDiagnosKod(diagnos.kod);
    }

});


Given(/^jag ändrar i fältet (arbetsförmåga|sjukskrivningsperiod|diagnoskod)$/, function(field) {
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

Given(/^jag ändrar i slumpat fält$/, function() {
    let intygShortcode = helpers.getAbbrev(intyg.typ);

    if (isValid(intygShortcode)) {
        let field = helpers.randomPageField(helpers.isSMIIntyg(intyg.typ), intygShortcode);
        return changeField(intygShortcode, field);
    } else {
        throw Error('Intyg code not valid \'' + intygShortcode + '\'');
    }

});

Given(/^jag fyller i resten av de nödvändiga fälten\.$/, function() {
    return moveAndSendKeys(fkUtkastPage.baserasPa.minUndersokning.checkbox, protractor.Key.SPACE).then(function() {
        return moveAndSendKeys(fkUtkastPage.funktionsNedsattning, 'Halt och lytt').then(function() {
            return moveAndSendKeys(fkUtkastPage.aktivitetsBegransning, 'Orkar inget').then(function() {
                return moveAndSendKeys(fkUtkastPage.nuvarandeArbete, 'Stuveriarbetare');
            });
        });
    });
});

Given(/^jag fyller i ett intyg som( inte)? är smitta$/, function(isSmitta) {
    isSmitta = (typeof isSmitta === 'undefined');
    logger.silly('isSmitta : ' + isSmitta);
    global.intyg = testdata.fk['7263'].getRandom(false, isSmitta);
    logger.silly(intyg);
    return fillIn(global.intyg);
});
