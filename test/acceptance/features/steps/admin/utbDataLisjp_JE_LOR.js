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
var utbDataLisjp2 = [{
    // 0 
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-6)
    },
    "sysselsattning": {
		"typ": "ARBETSSOKANDE"
	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Svårt att att lyfta, böja sig och vrida kroppen är begränsad, stelhet och smärta, sömnstörning med påverkad kognitiv funktion.",
    "aktivitetsbegransning": "Enligt min undersökning och patientens utsaga: Icke ringa svårigheter att ändra och bibehålla kroppsställning, gå och röra sig normalt inomhus, statiskt belasta rygg och ben. Svårt att lyfta, stå framåtböjd, resa sig från stol / säng samt utföra manuellt rörligt arbete. Smärtpräglat rörelsemönster.",
	"pagaendeBehandling": "Smärtlindring.",
	"planeradBehandling": "Remiss till ortopeden.",
    "arbetsformaga": {
        "nedsattMed100": {
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 1
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-17)
    },
   "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sjuksköterska"
    },
    "diagnos": {
        "kod": "S33",
        "kod": "R10",
        "kod": "S34"
  },
    "funktionsnedsattning": "Efter fall från häst svårtigheter att sitta, stå och gå till följd av nervskador på bäckennivå. Inkontinensbesvär med läckage och ofrivillig urinavgång.",
    "aktivitetsbegransning": "Kan ej stödja på benen. Bedöms ej kunna fullfölja sina arbetsuppgifter under rehabiliteringsfasen.",
	"pagaendeBehandling": "Rehabiliteringsträning genom teamsamverkan med fysioterapeut, läkare och arbetsterapeut på kliniken.",
	"planeradBehandling": "Remiss skriven till Neurologisk rehab för inneliggande utredning av kvarstående bäckensmärtor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-17),
            "tom": idag(66)
        }
    },
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "6 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte"
}, {
    // 2
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
            "from": idag(-9),
            "tom": idag(-2)
        },
		"nedsattMed50": {
            "from": idag(-1),
            "tom": idag(6)
        },
		"nedsattMed25": {
            "from": idag(7),
            "tom": idag(14)
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
    // 3
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-22)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lastbilschaufför"
	},
    "diagnos": {
        "kod": "S984",
		"kod": "E11"
    },
    "funktionsnedsattning": "Traumatisk klämskada på höger stortå 10 augusti, operation med amputation 11 augusti. Postoperativ sårinfektion med manifestation på omkringliggande vävnad. Diabetiker med svårläkt sår.",
    "aktivitetsbegransning": "Fortsatta postoperativa besvär med smärta och stödproblem på foten till följd av mjukdelsinfektion. Kan ej utföra sina arbetsuppgifter med att lasta ur eller på gods.",
	"pagaendeBehandling": "Såromläggning på mottagningen två gånger per vecka, fortsatt översyn med återbesök till mottagningen för ställningstagande till eventuellt sårrevidering i narkos.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-22),
            "tom": idag(-2)
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
    // 4
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-60)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Jobbar på ICA."
	},
    "diagnos": {
        "kod": "S82"
    },
    "funktionsnedsattning": "Fotledsfraktur vänster fot efter fall från stege. Operation 27/7.",
    "aktivitetsbegransning": "Postoperativa mobiliseringsinskränkningar, får ej belasta foten under läkningstid.",
	"pagaendeBehandling": "Borttagning av gips den 26/8. Därefter mobiliseringsträning med fysioterapeut.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-60),
            "tom": idag(-30)
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
    // 5
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Jobbar på ICA."
	},
    "diagnos": {
        "kod": "S82"
    },
    "funktionsnedsattning": "Fotledsfraktur vänster fot efter fall från stege. Operation 27/7.",
    "aktivitetsbegransning": "Postoperativa mobiliseringsinskränkningar, får ej belasta foten under läkningstid.",
	"pagaendeBehandling": "Borttagning av gips den 26/8. Därefter mobiliseringsträning med fysioterapeut.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-29),
            "tom": idag()
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
    // 6
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-28)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Jobbar på ICA."
	},
    "diagnos": {
        "kod": "S82"
    },
    "funktionsnedsattning": "Fotledsfraktur vänster fot efter fall från stege. Operation 27/7.",
    "aktivitetsbegransning": "Postoperativa mobiliseringsinskränkningar, får ej belasta foten under läkningstid.",
	"pagaendeBehandling": "Borttagning av gips den 26/8. Därefter mobiliseringsträning med fysioterapeut.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-28),
            "tom": idag(2)
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
    // 7
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-90),
       "journaluppgifter": idag(-90)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Jobbar som dramaturg på folkuniversitet."
	},
    "diagnos": {
        "kod": "I61"
    },
    "funktionsnedsattning": "Insjuknat i snabbt förlopp med påverkan på synfält, kräkning och tappade balansen. Inneliggande vård tom 20170729. Har efter hjärnblödningen fortsatt problem med hjärntrötthetssyndrom, och känslomässiga förändringar.",
    "aktivitetsbegransning": "Tappar lätt koncentrationen och blir trött efter mindre än 20 minuter. Är i habiliteringsfas med fokus på att återfå talförmåga samt finna strategier för att hantera tjärntröttheten. Bedöms ej kunna utföra arbetssysslor.",
	"pagaendeBehandling": "Teambaserad rehabilitering på kliniken. Talpedagog samt fysioterapeutisk träning regelbundet.",
 	"planeradBehandling": "Återgång till arbete.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-90),
            "tom": idag(-20)
        },
		"nedsattMed50": {
            "from": idag(-21),
            "tom": idag(10)
        },
		"nedsattMed25": {
            "from": idag(11),
            "tom": idag(60)
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
    // 8
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-60),
       "journaluppgifter": idag(-60)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tekniker på ventilationsföretag."
	},
    "diagnos": {
        "kod": "S90"
    },
    "funktionsnedsattning": "Femurfraktur efter motorcykelolycka 25/7.",
    "aktivitetsbegransning": "Kan ej stödja på benet postoperativt på åtta veckor. Bedöms då ej kunna utföra sina arbetssysslor. Etter avgipsning endast stödgång på benet, får ej belastas.",
	"pagaendeBehandling": "Gips åtta veckor, därefter träning med fysioterapeut.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-60),
            "tom": idag(30)
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
    // 9
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-14),
       "journaluppgifter": idag(-14)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arborist."
	},
    "diagnos": {
        "kod": "S62"
    },
    "funktionsnedsattning": "Falltruama från stege på jobbet, tog emot sig med höger hand och ådrog sig en handledsfraktur.",
    "aktivitetsbegransning": "Kan ej utföra sina arbetssysslor då höger arm är gipsad. Kan inte omplaceras på jobbet till andra sysslor.",
	"pagaendeBehandling": "Gips av arm 6 veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-14),
            "tom": idag(28)
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
    // 10
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-30),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lagerarbetare på matgrossist."
	},
    "diagnos": {
        "kod": "S62",
        "kod": "I27"
   },
    "funktionsnedsattning": "Falltruama från stege på jobbet, tog emot sig med höger hand och ådrog sig en handledsfraktur.",
    "aktivitetsbegransning": "Kan ej utföra sina arbetssysslor då höger arm är gipsad. Kan inte omplaceras på jobbet till andra sysslor.",
	"pagaendeBehandling": "Gips av arm 6 veckor.",
 	"planeradBehandling": "Sjukgymnastik.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-30),
            "tom": idag(7)
        },
		"nedsattMed50": {
            "from": idag(8),
            "tom": idag(22)
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
    // 11
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-9),
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
            "from": idag(-9),
            "tom": idag(5)
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
