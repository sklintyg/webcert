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
        "minUndersokningAvPatienten": idag(),
		"journaluppgifter":idag()
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lokalvårdare, städar kontorsutrymmen."
	},
    "diagnos": {
        "kod": "C560"
    },
    "funktionsnedsattning": "Kvarstående besvär med domningar och stickningar höger hand. Ej blivit bättre efter 4 veckors sjukskrivning. Nedsatt kraft i tumme-pekfinger greppet som innan.",
    "aktivitetsbegransning": "Kan ej utföra sina arbetssysslor som kan vara mycket krävande. Nedsatt kraft tumme- pekfinger grepp. Domningar ut i fingrarna.",
	"pagaendeBehandling": "Analgetika att ta vid behov.",
	"planeradBehandling": "Remiss till handkirurgen för operation av karpaltunnelsyndrom. Tills dess utprovning av handledsortoser hos arbetsterapeuten.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(41)
        }
    },
    "atgarder": [{
        "namn": "Ergonomisk bedömning",
        "beskrivning": "Ergonomisk bedömning-beskrivning",
        "key": "ERGONOMISK"
    },
	{
        "namn": "Hjälpmedel",
        "beskrivning": "Hjälpmedel-beskrivning",
        "key": "HJÄLPMEDEL"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "6 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 1 
     "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-25)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "It arkitekt, kontorsarbete."
	},
    "diagnos": {
        "kod": "F431"
    },
    "funktionsnedsattning": "Mycket nedsatt psykologisk, kognitiv och social funktion, har betydande svårigheter att klara ett normalt liv. Sömnsvårigheter, episoder av återupplevande av traumat i form av påträngande minnen (flashbacks), drömmar eller mardrömmar, känslomässig stumhet, lättskrämdhet.",
    "aktivitetsbegransning": "Kan inte klara ett normalt liv. Grundas på patientens utsaga och min bedömning.",
	"pagaendeBehandling": "Medicinering med Seroxat. Medicinering mot magsår. Medicinering mot depression.",
	"planeradBehandling": "Remiss mottagen från VC och KBT terapi kommer startas hos oss på Rehabcentrum. Patienten väntar på att få tid hos en av våra terapeuter.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-30),
            "tom": idag(65)
        }
    },
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
     // 2
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Vaktmästare."
    },
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Svårt att att lyfta, böja sig och vrida kroppen är begränsad, stelhet och smärta, sömnstörning med påverkad kognitiv funktion.",
    "aktivitetsbegransning": "Enligt min undersökning och patientens utsaga: Icke ringa svårigheter att ändra och bibehålla kroppsställning, gå och röra sig normalt inomhus, statiskt belasta rygg och ben. Svårt att lyfta, stå framåtböjd, resa sig från stol / säng samt utföra manuellt rörligt arbete. Smärtpräglat rörelsemönster.",
	"pagaendeBehandling": "Allergimedicin mot rinit. Smärtlindring under utfasning.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(9),
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 3
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-4)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sjuksköterska"
    },
    "diagnos": {
        "kod": "S33"
    },
    "funktionsnedsattning": "Efter fall från häst svårtigheter att sitta, stå och gå till följd av nervskador på bäckennivå. Inkontinensbesvär med läckage och ofrivillig urinavgång.",
    "aktivitetsbegransning": "Kan ej stödja på benen. Bedöms ej kunna fullfölja sina arbetsuppgifter under rehabiliteringsfasen.",
	"pagaendeBehandling": "Rehabiliteringsträning genom teamsamverkan med fysioterapeut, läkare och arbetsterapeut på kliniken.",
	"planeradBehandling": "Remiss skriven till Neurologisk rehab för inneliggande utredning av kvarstående bäckensmärtor.",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-4),
            "tom": idag(26)
        }
    },
    "atgarder": [{
        "namn": "Ergonomisk bedömning",
        "beskrivning": "Ergonomisk bedömning-beskrivning",
        "key": "ERGONOMISK"
    }],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "2 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 4
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-9)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Brevbärare"
	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Svårt att att lyfta, böja sig och vrida kroppen är begränsad, stelhet och smärta, sömnstörning med påverkad kognitiv funktion.",
    "aktivitetsbegransning": "Enligt min undersökning och patientens utsaga: Icke ringa svårigheter att ändra och bibehålla kroppsställning, gå och röra sig normalt inomhus, statiskt belasta rygg och ben. Svårt att lyfta, stå framåtböjd, resa sig från stol / säng samt utföra manuellt rörligt arbete. Smärtpräglat rörelsemönster.",
	"pagaendeBehandling": "Smärtlindring.",
	"planeradBehandling": "Sjukgymnastik.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(20)
        }
	},
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 5
     "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-5)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, jobbar på SJ"
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Opererad med akromioplastik. Postoperativ smärta.",
    "aktivitetsbegransning": "Patienten kan inte använda den drabbade armen, min undersökning.",
	"pagaendeBehandling": "Rehabilitering kan påbörjas så fort den postoperativa smärta avtar.",
	"planeradBehandling": "Postoperativ rehabilitering under 6 veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(10),
            "tom": idag(15)
        },
		"nedsattMed75": {
            "from": idag(16),
            "tom": idag(26)
        },
		"nedsattMed50": {
            "from": idag(27),
            "tom": idag(37)
        },
		"nedsattMed25": {
            "from": idag(38),
            "tom": idag(52)
        }  
	},
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
	}, 
	{
        namn: 'Omfördelning av arbetsuppgifter',
        beskrivning: 'Omfördelning av arbetsuppgifter-beskrivning',
        key: 'OMFORDELNING'
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"

}, {
    // 6
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-1)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lastbilschaufför"
	},
    "diagnos": {
        "kod": "S984",
    },
    "funktionsnedsattning": "Traumatisk klämskada på höger stortå 10 augusti, operation med amputation 11 augusti. Postoperativ sårinfektion med manifestation på omkringliggande vävnad. Diabetiker med svårläkt sår.",
    "aktivitetsbegransning": "Fortsatta postoperativa besvär med smärta och stödproblem på foten till följd av mjukdelsinfektion. Kan ej utföra sina arbetsuppgifter med att lasta ur eller på gods.",
	"pagaendeBehandling": "Såromläggning på mottagningen två gånger per vecka, fortsatt översyn med återbesök till mottagningen för ställningstagande till eventuellt sårrevidering i narkos.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-1),
            "tom": idag(35)
        }
	},
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "3 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 7
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(),
		"journaluppgifter":idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lokalvårdare, städar kontorsutrymmen"
    },
    "diagnos": {
        "kod": "G560"
    },
    "funktionsnedsattning": "Besväras av domningar och stickningar höger hand, som även är den dominanta handen. Upplever fumlighet och nedsatt kraft i tumme-pekfinger greppet.",
    "aktivitetsbegransning": "Kan ej utföra sina arbetssysslor som kan vara mycket krävande. Nedsatt kraft tumme- pekfinger grepp. Domningar ut i fingrarna.",
	"pagaendeBehandling": "Analgetika att ta vid behov.",
	"planeradBehandling": "Besök till arbetsterapeut för information och eget träningsschema.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(27)
        }
    },
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "1 månad" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 8 ongoing
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Bussförare."
	},
    "diagnos": {
        "kod": "S83"
    },
    "funktionsnedsattning": "Luxation av höger knä efter cykelolycka. Svullen i knät, kan ej ta ut full rörlighet.",
    "aktivitetsbegransning": "Kan ej stödja på benet, svårigheter att gå. Behov av kryckor. Bedöms därför ej kunna utföra sitt arbete.",
	"pagaendeBehandling": "Vila.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(6),
            "tom": idag(20)
        }
	},
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
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
