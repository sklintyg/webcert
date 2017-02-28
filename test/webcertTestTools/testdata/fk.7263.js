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

'use strict';
var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;
var fkValues = require('./testvalues.js').fk;

var testdata = {
    smittskydd: [true, false]
};

function randomKontaktMedAF(smittskydd) {
    if (smittskydd) {
        return false;
    } else {
        return shuffle([true, false])[0];
    }
}

function randomRekommendationOvrigt(smittskydd) {
    if (smittskydd) {
        return false;
    } else {
        return shuffle(['Övrig rekommendation beskrivning', 'Övrig rekommendation beskrivning med lite extra'])[0];

    }
}

function randomRehabAktuell(smittskydd) {
    if (smittskydd) {
        return false;
    } else {
        return shuffle(['Ja', 'Nej', 'Går inte att bedöma'])[0];
    }
}

function randomPrognosFortydligande(val) {
    if (val === 'Går inte att bedöma') {
        return 'Prognos förtydligande text';
    }
}

var random = {
    diagnos: function(smittskydd) {
        if (smittskydd) {
            return false;
        }
        return {
            diagnoser: [{
                ICD10: 'A00',
                diagnosText: 'Kolera'
            }],
            fortydligande: 'Förtydligande text',
            samsjuklighetForeligger: true
        };
    },
    aktuelltSjukdomsforlopp: function(smittskydd) {
        if (smittskydd) {
            return '';
        }
        return 'Aktuellt sjukdomsförlopp text';
    },
    funktionsnedsattning: function(smittskydd) {
        if (smittskydd) {
            return '';
        }
        return 'Funktionsnedsattning text';
    },
    aktivitestbegransning: function(smittskydd) {
        if (smittskydd) {
            return '';
        }
        return 'Aktivitetsbegränsning text';
    },
    arbete: function(smittskydd) {
        if (smittskydd) {
            return false;
        }
        return {
            nuvarandeArbete: {
                aktuellaArbetsuppgifter: 'Aktuella arbetsuppgifter text'
            },
            arbetsloshet: true,
            foraldraledighet: true
        };
    },
    prognos: function() {
        var val = shuffle(['Ja', 'Ja, delvis', 'Nej', 'Går inte att bedöma'])[0];
        return {
            val: val,
            fortydligande: randomPrognosFortydligande(val)
        };
    },
    atgarder: function(smittskydd) {
        if (smittskydd) {
            return false;
        }
        return {
            planerad: 'Planerad eller pågående behandling text',
            annan: 'Annan åtgärd text'
        };
    },
    rekommendationer: function(smittskydd) {
        return {
            resor: shuffle([true, false])[0],
            kontaktMedArbetsformedlingen: false,
            kontaktMedForetagshalsovard: false,
            //TODO: Dessa fält fylls inte i om true. Fixa!
            // kontaktMedArbetsformedlingen: randomKontaktMedAF(smittskydd),
            // kontaktMedForetagshalsovard: randomKontaktMedAF(smittskydd),
            ovrigt: shuffle([false, randomRekommendationOvrigt(smittskydd)])[0],
            arbetslivsinriktadRehab: randomRehabAktuell(smittskydd)
        };
    },
    ovrigaUpplysningar: function() {
        return 'Övriga upplysningar och förtydliganden text';
    },
    arbetsformagaFMB: function() {
        return 'Arbetsförmåga bedöms nedsatt längre tid än FMB anger text';
    }


};



module.exports = {
    getRandom: function(intygsID, isSmitta) {
        var isSmittskydd = isSmitta;

        if (typeof isSmitta === 'undefined') {
            isSmittskydd = shuffle(testdata.smittskydd)[0];
        }

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            id: intygsID,
            typ: 'Läkarintyg FK 7263',
            smittskydd: isSmittskydd,
            baserasPa: fkValues.getRandomBaserasPa(isSmittskydd),
            diagnos: random.diagnos(isSmittskydd),
            aktuelltSjukdomsforlopp: random.aktuelltSjukdomsforlopp(isSmittskydd),
            funktionsnedsattning: random.funktionsnedsattning(isSmittskydd),
            aktivitetsBegransning: random.aktivitestbegransning(isSmittskydd),
            arbete: random.arbete(isSmittskydd),
            arbetsformaga: fkValues.getRandomArbetsformaga(),
            arbetsformagaFMB: random.arbetsformagaFMB(),
            prognos: random.prognos(),
            atgarder: random.atgarder(isSmittskydd),
            rekommendationer: random.rekommendationer(isSmittskydd),
            kontaktOnskasMedFK: fkValues.getRandomKontaktOnskasMedFK(),
            ovrigaUpplysningar: random.ovrigaUpplysningar()
        };
    }
};
