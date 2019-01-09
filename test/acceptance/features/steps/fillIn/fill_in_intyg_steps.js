/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/*global wcTestTools, protractor, browser, testdata, logger */

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
let changeActions = {
    'DB': {
        'dodsdatum': function(intyg) {
            intyg.dodsdatum = {
                inteSakert: {
                    year: '2018',
                    month: '01',
                    antraffadDod: '2017-09-27'
                }
            };
            return dbUtkastPage.angeDodsdatum(intyg.dodsdatum);
        },
        'dodsplats': function(intyg) {
            intyg.dodsPlats = helpers.randomTextString();
            return moveAndSendKeys(dbUtkastPage.dodsPlats.kommun.inputText, intyg.dodsPlats);
        },
        'identitetstyrkt': function(intyg) {
            intyg.identitetstyrkt = helpers.randomTextString();
            return moveAndSendKeys(dbUtkastPage.identitetStyrktGenom.inputText, intyg.identitetstyrkt);
        }
    },
    'DOI': {
        'dodsdatum': function(intyg) {
            intyg.dodsdatum = {
                inteSakert: {
                    year: '2018',
                    month: '01',
                    antraffadDod: '2017-09-27'
                }
            };
            return doiUtkastPage.angeDodsdatum(intyg.dodsdatum);
        },
        'dodsplats': function(intyg) {
            intyg.dodsPlats = helpers.randomTextString();
            return moveAndSendKeys(doiUtkastPage.dodsPlats.kommun.inputText, intyg.dodsPlats);
        },
        'identitetstyrkt': function(intyg) {
            intyg.identitetstyrkt = helpers.randomTextString();
            return moveAndSendKeys(doiUtkastPage.identitetStyrktGenom.inputText, intyg.identitetstyrkt);
        }
    },
    'LUSE': {
        'aktivitetsbegransning': function(intyg) {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(luseUtkastPage.aktivitetsbegransning, intyg.aktivitetsbegransning);
        },
        'sjukdomsforlopp': function(intyg) {
            intyg.sjukdomsforlopp = helpers.randomTextString();
            return moveAndSendKeys(luseUtkastPage.sjukdomsforlopp, intyg.sjukdomsforlopp);
        },
        'funktionsnedsattning': function(intyg) {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.intellektuell = helpers.randomTextString();

            return moveAndSendKeys(luseUtkastPage.funktionsnedsattning.intellektuell.checkbox, protractor.Key.SPACE).then(function() {
                return helpers.largeDelay();
            }).then(function() {
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
    },
    'LISJP': {
        'aktivitetsbegransning': function(intyg) {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        },
        'funktionsnedsattning': function(intyg) {
            intyg.funktionsnedsattning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.funktionsnedsattning, intyg.funktionsnedsattning);
        },
        'sysselsattning': function(intyg) {
            if (intyg.sysselsattning && intyg.sysselsattning.typ === 'ARBETSSOKANDE') {
                intyg.sysselsattning = {
                    typ: 'FORALDRALEDIG'
                };
            } else {
                intyg.sysselsattning = {
                    typ: 'ARBETSSOKANDE'
                };
            }
            return lisjpUtkastPage.angeSysselsattning(intyg.sysselsattning);
        }
    },
    'LUAE_NA': {
        'aktivitetsbegransning': function(intyg) {
            intyg.aktivitetsbegransning = helpers.randomTextString();
            return moveAndSendKeys(lisjpUtkastPage.konsekvenser.aktivitetsbegransning, intyg.aktivitetsbegransning);
        },
        'ovrigt': function(intyg) {
            intyg.ovrigt = helpers.randomTextString();
            return moveAndSendKeys(luaeNAUtkastPage.ovrigt, intyg.ovrigt);
        },
        'sjukdomsforlopp': function(intyg) {
            intyg.sjukdomsforlopp = helpers.randomTextString();
            return moveAndSendKeys(luaeNAUtkastPage.sjukdomsforlopp, intyg.sjukdomsforlopp);
        }
    },
    'LUAE_FS': {
        'funktionsnedsattningDebut': function(intyg) {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.debut = helpers.randomTextString();

            return moveAndSendKeys(luaeFSUtkastPage.funktionsnedsattning.debut, intyg.funktionsnedsattning.debut);
        },
        'funktionsnedsattningPaverkan': function(intyg) {
            intyg.funktionsnedsattning = {};
            intyg.funktionsnedsattning.paverkan = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.funktionsnedsattning.paverkan, intyg.funktionsnedsattning.paverkan);
        },
        'ovrigt': function(intyg) {
            intyg.ovrigt = helpers.randomTextString();
            return moveAndSendKeys(luaeFSUtkastPage.ovrigt, intyg.ovrigt);
        }
    },
    'TSTRK1007': {
        'funktionsnedsattning': function(intyg) {
            return moveAndSendKeys(tsBasUtkastPage.funktionsnedsattning.aYes, protractor.Key.SPACE).then(function() {
                return tsBasUtkastPage.funktionsnedsattning.aText.clear().then(function() {
                    return moveAndSendKeys(tsBasUtkastPage.funktionsnedsattning.aText, helpers.randomTextString());
                });
            });
        },
        'hjartKarlsjukdom': function(intyg) {
            return moveAndSendKeys(tsBasUtkastPage.hjartKarl.cYes, protractor.Key.SPACE).then(function() {
                return helpers.largeDelay();
            }).then(function() {
                return tsBasUtkastPage.hjartKarl.cText.isPresent();
            }).then(function(present) {
                if (present) {
                    return tsBasUtkastPage.hjartKarl.cText.clear().then(function() {
                        return moveAndSendKeys(tsBasUtkastPage.hjartKarl.cText, helpers.randomTextString());
                    });
                }
            });
        },
        'utanKorrektion': function(intyg) {
            return tsBasUtkastPage.syn.hoger.utan.clear().then(function() {
                return moveAndSendKeys(tsBasUtkastPage.syn.hoger.utan, '1.0');
            });
        }
    },
    'TSTRK1031': {
        'hypoglykemier': function(intyg) {
            return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.b.yes, protractor.Key.SPACE).then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.d.yes, protractor.Key.SPACE).then(function() {
                    return moveAndSendKeys(tsDiabetesUtkastPage.hypoglykemier.d.antalEpisoder, helpers.randomTextString());
                });
            });
        },
        'diabetesBehandling': function(intyg) {
            return tsDiabetesUtkastPage.allmant.annanbehandling.clear().then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.allmant.annanbehandling, helpers.randomTextString());
            });
        },
        'specialist': function(intyg) {
            return tsDiabetesUtkastPage.specialist.clear().then(function() {
                return moveAndSendKeys(tsDiabetesUtkastPage.specialist, helpers.randomTextString());
            });
        }
    }
};

function changeField(intygShortcode, field, intyg) {
    logger.info('Fältet som ändras är: ' + field + ' i intyg ' + intygShortcode);

    return changeActions[intygShortcode][field](intyg);
}



function isValid(intygShortcode) {
    return (intygShortcode in helpers.intygShortcode);
}


/*
 *	Test steg
 *
 */

When(/^jag fyller i alla nödvändiga fält för intyget(?:\s"([^"]*)")?$/, function(intygsTyp) {
    if (!this.intyg.typ && !intygsTyp) {
        throw 'intyg.typ odefinierad.';
    } else {
        this.intyg = generateIntygByType(this.intyg, this.patient);
        logger.silly(this.intyg);
        return fillIn(this);
    }
});

When(/^jag ändrar diagnoskod$/, function() {
    var isSMIIntyg = helpers.isSMIIntyg(this.intyg.typ);
    var diagnos = {
        kod: td.values.fk.getRandomDiagnoskod()
    };
    if (isSMIIntyg) {
        return lisjpUtkastPage.angeDiagnosKoder([diagnos]);
    } else {
        return fkUtkastPage.angeDiagnosKod(diagnos.kod);
    }

});


When(/^jag ändrar i fältet (arbetsförmåga|sjukskrivningsperiod|diagnoskod)$/, function(field) {
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

        var isSMIIntyg = helpers.isSMIIntyg(this.intyg.typ);
        if (isSMIIntyg) {
            return lisjpUtkastPage.angeDiagnosKoder([diagnosKod]);
        } else {
            return fkUtkastPage.angeDiagnosKod(diagnosKod);
        }


    } else {
        throw ('Fält saknas i steg-funktion');
    }


});

When(/^jag ändrar i slumpat fält$/, function() {
    let intygShortcode = helpers.getAbbrev(this.intyg.typ);

    if (isValid(intygShortcode)) {
        let field = helpers.randomPageField(helpers.isSMIIntyg(this.intyg.typ), intygShortcode);
        return changeField(intygShortcode, field, this.intyg);
    } else {
        throw Error('Intyg code not valid \'' + intygShortcode + '\'');
    }

});

When(/^jag fyller i resten av de nödvändiga fälten\.$/, function() {
    return moveAndSendKeys(fkUtkastPage.baserasPa.minUndersokning.checkbox, protractor.Key.SPACE).then(function() {
        return moveAndSendKeys(fkUtkastPage.funktionsNedsattning, 'Halt och lytt').then(function() {
            return moveAndSendKeys(fkUtkastPage.aktivitetsBegransning, 'Orkar inget').then(function() {
                return moveAndSendKeys(fkUtkastPage.nuvarandeArbete, 'Stuveriarbetare');
            });
        });
    });
});

When(/^jag fyller i ett intyg som( inte)? är smitta$/, function(isSmitta) {
    isSmitta = (typeof isSmitta === 'undefined');
    logger.silly('isSmitta : ' + isSmitta);
    this.intyg = testdata.fk['7263'].getRandom(false, isSmitta);
    logger.silly(this.intyg);
    return fillIn(this);
});
