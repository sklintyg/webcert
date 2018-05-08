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

/*global logger */

'use strict';
/*jshint newcap:false */


/*
 *	Stödlib och ramverk
 *
 */

var testdataHelper = require('common-testtools').testdataHelper;

/*
 *	Stödfunktioner
 *
 */
function idag(modifyer) {
    if (!modifyer) {
        modifyer = 0;
    }

    var datum = new Date();

    datum.setDate(datum.getDate() + modifyer);

    return testdataHelper.dateFormat(datum);
}
/*
 *	Demo Data Lisjp
 *
 */
var statistikDataLisjp = [{
    // 0 Föräldraledig
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "FORALDRALEDIG"
    },
    "diagnos": {
        "kod": "E90"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(),
            "tom": idag(89)
        }
    },
    "arbetsformagaFMB": "Prognosen är oklar",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        "namn": "Arbetsträning",
        "beskrivning": "Arbetsträning-beskrivning",
        "key": "ARBETSTRANING"
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 1 Byggnadsarbetare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Byggnadsarbetare"
    },
    "diagnos": {
        "kod": "L99"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(2),
            "tom": idag(85)
        }
    },
    "atgarder": [{
        "namn": 'Hjälpmedel',
        "beskrivning": 'Hjälpmedel-beskrivning',
        "key": 'HJALPMEDEL'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 2 Student
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "STUDIER"
    },
    "diagnos": {
        "kod": "M00"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-2),
            "tom": idag(93)
        }
    },
    "atgarder": [{
        namn: 'Konflikthantering',
        beskrivning: 'Konflikthantering-beskrivning',
        key: 'KONFLIKTHANTERING'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 3 Lärare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lärare"
    },
    "diagnos": {
        "kod": "M99"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(),
            "tom": idag(100)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Inte aktuellt',
        key: 'EJ_AKTUELLT'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 4 Ekonomichef
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-167)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Ekonomichef"
    },
    "diagnos": {
        "kod": "Y1113"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed25": {
            "from": idag(),
            "tom": idag(26)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 5 Ekonomichef
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-147)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Ekonomichef"
    },
    "diagnos": {
        "kod": "Q00"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-3),
            "tom": idag(73)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 6 Ekonomichef
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-20)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Ekonomichef"
    },
    "diagnos": {
        "kod": "Z99"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed25": {
            "from": idag(5),
            "tom": idag(105)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 7 Elektriker
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-207)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Elektriker"
    },
    "diagnos": {
        "kod": "F000"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed25": {
            "from": idag(-20),
            "tom": idag(200)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 8 Elektriker
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-88)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Elektriker"
    },
    "diagnos": {
        "kod": "O99"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-45),
            "tom": idag()
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}, {
    // 9 Elektriker
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-34)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Elektriker"
    },
    "diagnos": {
        "kod": "R99"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-89),
            "tom": idag()
        }
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Statistik-Intyg"
}];






module.exports = {
    get: function(index, id) {
        var obj = statistikDataLisjp[index];
        obj.id = id; /*("id": testdataHelper.generateTestGuid(),)*/
        obj.typ = "Läkarintyg för sjukpenning";
        logger.silly(JSON.stringify(obj));
        return obj;
    }
};
