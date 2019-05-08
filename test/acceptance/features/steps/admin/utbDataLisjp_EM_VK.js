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
 *	Utb Data Lisjp
 *
 */
var utbDataLisjp = [{
    // 0 
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-4)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arbetar 50% på förskola som barnskötare."
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Smärtor och nedsatt rörlighet i vä axelleden, trött, medtagen. Började smygande och efterhand komr mer värk och rörelseinskränkning.Ögonsmärta.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: Helt nedsatt arbetsförmåga.",
	"pagaendeBehandling": "Vila smärtstillande antibiotika behandling mot ögoninflamamtion.",
	"planeradBehandling": "Sjukgymnastik.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-4),
            "tom": idag(6)
        }
    },
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 1
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-10)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Hotell receptionist, arbetar 50%."
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Trötthet, energibrist, oförmåga att fatta beslut, oförmåga att planera, försämrat minne, bristande initiativförmåga, moti-vation och uthållighet samt ökad känslighet för stress.",
    "aktivitetsbegransning": "Koncentrationssvårigheter, minnesstörning, personen inte hantera vardagliga problem eller ta vård om sig själv.",
	"pagaendeBehandling": "Kuratorsamtal. Medicinering.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-12),
            "tom": idag(-5)
        },
       "nedsattMed75": {
            "from": idag(-4),
            "tom": idag(7)
        },
       "nedsattMed50": {
            "from": idag(8),
            "tom": idag(15)
        },
       "nedsattMed25": {
            "from": idag(16),
            "tom": idag(23)
        }
	},
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 2
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-3)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Receptionist på ett fastighetsbolag."
	},
    "diagnos": {
        "kod": "R522C"
    },
    "funktionsnedsattning": "Sämre period med skärande, molande smärta sedan flera veckor. Trött, medtagen, något nedstämd, har haft kraftig huvudvärk under senaste vecka.",
    "aktivitetsbegransning": "Utredd tidigare utan konstaterande om smärtorsaker.",
	"pagaendeBehandling": "Höjer dos på smärtstillande.",
    "arbetsformaga": {
       "nedsattMed75": {
            "from": idag(-6),
            "tom": idag(8)
        }
	},
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 3
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-180)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "It arkitekt, kontorsarbete."
	},
    "diagnos": {
        "kod": "K250"
    },
    "funktionsnedsattning": "Patienten utsattes för överfall och grovt misshandel när han var på väg hem från arbete. Sedan tidigare konstaterad magsår, nu akut blödning. Helt nedsatt, på grund av stark rädsla och andra intensiva och okontrollerbara affekter, dåligt nattsömn. Magsmärtor, illamående och kräkningar.",
    "aktivitetsbegransning": "Kan inte sova, vistas i allmänna platser, ha social umgänge. Omfattande minnesstörning och koncentrationssvårigheter.",
	"pagaendeBehandling": "Farmakologisk behandling med SSRI- preparat, vila, täta kurator samtal på VC, så att det ej utvecklar sig till ett Posttraumatiskt stresstillstånd.",
    "arbetsformaga": {
       "nedsattMed75": {
            "from": idag(-60),
            "tom": idag(-46)
        }
	},
    "atgarder": [{
        "namn": "Kontakt med företagshälsovård",
        "key": "KONTAKT_FHV"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 4
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-90)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tjänsteman, kontorsarbete."
	},
    "diagnos": {
        "kod": "J11"
    },
    "funktionsnedsattning": "Helt nedsatt på grund av trötthet, feber och besvärlig hosta, energibrist, social rädsla.",
    "aktivitetsbegransning": "Helt nedsatt på grund av trötthet, feber och besvärlig hosta. Försämrad i sin depression.",
	"pagaendeBehandling": "Vila, febernedsättande.",
    "arbetsformaga": {
       "nedsattMed75": {
            "from": idag(-90),
            "tom": idag(-50)
        }
	},
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}];


module.exports = {
    get: function(index, id) {
        var obj = utbDataLisjp[index];
        obj.id = id; /*("id": testdataHelper.generateTestGuid(),)*/
        obj.typ = "Läkarintyg för sjukpenning";
        logger.silly(JSON.stringify(obj));
        return obj;
    }

};
