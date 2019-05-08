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
        "minUndersokningAvPatienten": idag(-120)
    },
    "sysselsattning": {
		"typ": "ARBETSSOKANDE"
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Trött och orkeslös, anhörig är med till vårdcentralen för stöd. Pratar knapphändigt och svara endast kort på frågor. Ej sovit adekvat på nätterna sedan flera månader enligt anhörig, sover istället på dagarna.",
    "aktivitetsbegransning": "Kan ej hantera vardagssysslor eller personlig omvårdnad. Har ej orkat eller förmått ta sig till arbetsförmedlingen på planerade möten.",
	"pagaendeBehandling": "Insättning av antidepressiv medicin, trappas upp enligt sedvanligt schema. Samtalsterapi.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-120),
            "tom": idag(-55)
        }
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 1
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-35)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "IT arkitekt, kontorsarbete."
	},
    "diagnos": {
        "kod": "F430"
    },
    "funktionsnedsattning": "Kan inte ta sig till VC på egen hand, har närstående med sig pga rädsla. Klarar inte av sociala sammanhang.",
    "aktivitetsbegransning": "Helt nedsatt pga rädsla, ångest och sömnbrist. Patienten uppvisar koncentrationssvårigheter och pratar osammanhängande.",
	"pagaendeBehandling": "Medicinering.",
	"planeradBehandling": "Samtal med VC kurator, 3 gg/v.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-46),
            "tom": idag(-35)
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
        "minUndersokningAvPatienten": idag(-55)
    },
    "sysselsattning": {
		"typ": "ARBETSSOKANDE"
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Trött och orkeslös, anhörig är med till vårdcentralen för stöd. Pratar knapphändigt och svara endast kort på frågor. Ej sovit adekvat på nätterna sedan flera månader enligt anhörig, sover istället på dagarna.",
    "aktivitetsbegransning": "Kan ej hantera vardagssysslor eller personlig omvårdnad. Har ej orkat eller förmått ta sig till arbetsförmedlingen på planerade möten.",
	"pagaendeBehandling": "Insättning av antidepressiv medicin, trappas upp enligt sedvanligt schema. Samtalsterapi.",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-55),
            "tom": idag(-35)
        },
        "nedsattMed25": {
            "from": idag(-34),
            "tom": idag(2)
        }
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "2 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
	// 3 
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-1)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Rörmokare."	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Huggande plötsligt påkommen längryggssmärta sedan igår. Palpationsöm över ryggslutet. Går med släpande stegi korridoren, svårigheter att lägga sig på britsen. Smärtpåverkad.",
    "aktivitetsbegransning": "Kan ej utföra sina arbetsuppgifter som rörmokare då svårigheter föreligger med att böja ryggen eller lyfta tunga saker.",
	"pagaendeBehandling": "Analgetika.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-1),
            "tom": idag(13)
        }
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
