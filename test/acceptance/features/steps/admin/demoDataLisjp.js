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
var demoDataLisjp = [{
    // 0 Föräldraledig
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "FORALDRALEDIG"
    },
    "diagnos": {
        "kod": "J36"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(),
            "tom": idag(14)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "F412"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(15)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "S53"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(14)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "F321"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(4)
        }
    },
    "atgarder": [{
        namn: 'Inte aktuellt',
        key: 'EJ_AKTUELLT'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "M15"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-167),
            "tom": idag(-148)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "F018"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-147),
            "tom": idag(-21)
        }
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "F334"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-20),
            "tom": idag(47)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "M47"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed25": {
            "from": idag(-207),
            "tom": idag(-89)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "M47"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-88),
            "tom": idag(-35)
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
    "ovrigt": "Detta är ett Demo-Intyg"
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
        "kod": "M47"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-34),
            "tom": idag(-33)
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
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 10 Elektriker
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-32)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Elektriker"
    },
    "diagnos": {
        "kod": "M47"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-32),
            "tom": idag(-15)
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
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 11 Elektriker
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-14)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Elektriker"
    },
    "diagnos": {
        "kod": "M47"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-14),
            "tom": idag(28)
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
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 12 Student
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-5)
    },
    "sysselsattning": {
        "typ": "STUDIER"
    },
    "diagnos": {
        "kod": "N04"
    },
    //TODO
    /*Bidignoser: B180, F412 */
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-5),
            "tom": idag(10)
        }
    },
    "atgarder": [{
        namn: 'Hjälpmedel',
        beskrivning: 'Hjälpmedel-beskrivning',
        key: 'HJALPMEDEL'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 13 Sjuksköterska
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-6)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sjuksköterska"
    },
    "diagnos": {
        "kod": "R522C"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-6),
            "tom": idag(9)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Konflikthantering',
        beskrivning: 'Konflikthantering-beskrivning',
        key: 'KONFLIKTHANTERING'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 14 VD
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-9)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Verksamhetsdirektör"
    },
    "diagnos": {
        "kod": "D559"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-9),
            "tom": idag(9)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        namn: 'Besök på arbetsplatsen',
        beskrivning: 'Besök på arbetsplatsen-beskrivning',
        key: 'BESOK_ARBETSPLATS'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 15 Lärare
    "smittskydd": false,
    "nuvarandeArbeteBeskrivning": "Lärare",
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-14)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lärare"
    },
    "diagnos": {
        "kod": "M545"
    },
    /* Bidignoser: E10 */
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte umgås med andra människor",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-14),
            "tom": idag(-7)
        }
    },
    "atgarder": [{
        namn: 'Inte aktuellt',
        key: 'EJ_AKTUELLT'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 16 Lärare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-6)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lärare"
    },
    "diagnos": {
        "kod": "M545"
    },
    /* Bidignoser: E10 */
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte umgås med andra människor",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-6),
            "tom": idag(8)
        }
    },
    "atgarder": [{
        namn: 'Besök på arbetsplatsen',
        beskrivning: 'Besök på arbetsplatsen-beskrivning',
        key: 'BESOK_ARBETSPLATS'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 17 Föräldraledig Lärare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "FORALDRALEDIG"
    },
    "diagnos": {
        "kod": "M545"
    },
    /* Bidignoser: E10 */
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte umgås med andra människor",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(9),
            "tom": idag(24)
        }
    },
    "atgarder": [{
        namn: 'Inte aktuellt',
        key: 'EJ_AKTUELLT'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 18 Sekreterare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-15)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sekreterare"
    },
    "diagnos": {
        "kod": "J13"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-15),
            "tom": idag(-1)
        }
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 19 Sekreterare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sekreterare"
    },
    "diagnos": {
        "kod": "J13"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(7)
        }
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 20 Byggnadsarbetare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-20)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Byggnadsarbetare"
    },
    "diagnos": {
        "kod": "F329"
        //TODO bidiagnos M549
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-20),
            "tom": idag(29)
        }
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 21 Kock
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-19)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Kock"
    },
    "diagnos": {
        "kod": "K309"
        //TODO bidiagnos M549
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-19),
            "tom": idag(-11)
        }
    },
    "atgarder": [{
        "namn": 'Hjälpmedel',
        "beskrivning": 'Hjälpmedel-beskrivning',
        "key": 'HJALPMEDEL'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 22 Kock
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-10)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Kock"
    },
    "diagnos": {
        "kod": "K309"
        //TODO bidiagnos M549
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta höger ben",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-10),
            "tom": idag(8)
        }
    },
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        "namn": 'Hjälpmedel',
        "beskrivning": 'Hjälpmedel-beskrivning',
        "key": 'HJALPMEDEL'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 23 Läkare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-30)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Läkare"
    },
    "diagnos": {
        "kod": "R52"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte umgås med andra människor",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-30),
            "tom": idag(-15)
        }
    },
    "atgarder": [{
        namn: 'Inte aktuellt',
        key: 'EJ_AKTUELLT'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 24 Läkare
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-14)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Läkare"
    },
    "diagnos": {
        "kod": "R52"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte umgås med andra människor",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-14),
            "tom": idag(8)
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
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 25 Polis
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-111)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Polis"
    },
    "diagnos": {
        "kod": "M751"
        //TODO Bidignoser: J304
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-111),
            "tom": idag(-96)
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
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 26 Polis
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-95)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Polis"
    },
    "diagnos": {
        "kod": "M751"
        //TODO Bidignoser: J304
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-95),
            "tom": idag(-75)
        }
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        name: 'PROGNOS_OKLAR'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}, {
    // 27 Polis
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-74)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Polis"
    },
    "diagnos": {
        "kod": "M751"
        //TODO Bidignoser: J304
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Svårt att röra fingrarna",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-74),
            "tom": idag(10)
        }
    },
    "atgarder": [{
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }],
    "prognosForArbetsformaga": {
        name: 'STOR_SANNOLIKHET'
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}];


module.exports = {
    get: function(index, id) {
        var obj = demoDataLisjp[index];
        obj.id = id; /*("id": testdataHelper.generateTestGuid(),)*/
        obj.typ = "Läkarintyg för sjukpenning";
        logger.silly(JSON.stringify(obj));
        return obj;
    }


};
