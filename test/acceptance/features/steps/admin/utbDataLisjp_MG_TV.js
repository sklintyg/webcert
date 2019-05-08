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
        "minUndersokningAvPatienten": idag(-150)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Akuta smärtor och kraftigt nedsatt rörlighet i axelleden efter med smygande debut. Patienten har svårt att sova.",
    "aktivitetsbegransning": "Kan inte utföra sitt arbete r/t smärta i axel och dåligt sömn.",
	"pagaendeBehandling": "Smärtstillande, vila.",
	"planeradBehandling": "Återbesök efter två veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-150),
            "tom": idag(-135)
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
        "minUndersokningAvPatienten": idag(-119)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Akuta smärtor och kraftigt nedsatt rörlighet i axelleden efter med smygande debut. Patienten har svårt att sova.",
    "aktivitetsbegransning": "Kan inte utföra sitt arbete r/t smärta i axel och dåligt sömn.",
	"pagaendeBehandling": "Smärtstillande, vila.",
	"planeradBehandling": "Återbesök efter två veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-119),
            "tom": idag(-110)
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
        "minUndersokningAvPatienten": idag(-109)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
    },
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Förbättring efter injektion, mindre smärta och bättre rörlighet, sover bättre på nätterna, fortfarande vilovärk på lateral- och framsidan axeln. Smärta på framsidan axeln vid aktivitet med lyft arm.",
    "aktivitetsbegransning": "Min undersökning: kan ej utföra sina ordinariearbetsuppgifter, ska försöka återgå till arbete på 50% med andra arbetsuppgifter som anpassas på arbetsplatsen.",
	"pagaendeBehandling": "NSAID vb, sjukgymnastik.",
 	"planeradBehandling": "Återbesök efter två veckor.",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-109),
            "tom": idag(-95)
        }
    },
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
	}, 
	{
        namn: 'Kontakt med företagshälsovård',
        beskrivning: 'Kontakt med företagshälsovård-beskrivning',
        key: 'KONTAKT_FHV'
	},
	{
        namn: 'Omfördelning av arbetsuppgifter',
        beskrivning: 'Omfördelning av arbetsuppgifter-beskrivning',
        key: 'OMFORDELNING'
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
        "minUndersokningAvPatienten": idag(-105)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
    },
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Försämring efter patienten började arbeta på 50%. Stark nattlig smärta. Smärta på framsidan axeln vid minsta aktivitet med lyft arm. Trött, medtagen och uppgiven.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: kan ej utföra sina ordinariearbetsuppgifter som tågförare och ej heller anpassade uppgifter i kontoret.",
	"pagaendeBehandling": "Smärtstillande stående och vb, vila, fortsatt sjukgymnastik i den mån patienten orkar i väntan på operation.",
	"planeradBehandling": "I samråd med patienten remiss till ortoped för artroskopisk dekompression med shaving av akromions undersida.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-105),
            "tom": idag(-75)
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 4
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
    "funktionsnedsattning": "Sämre period med Skärande, Molande smärta sedan flera veckor. Trött, medtagen, något nedstämd, har haft kraftig huvudvärk under senaste vecka.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: kan inte arbeta.",
	"pagaendeBehandling": "Utredd tidigare utan konstaterande om smärtorsaker.",
	"planeradBehandling": "Höjer dos på smärtstillande.",
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
    // 5
     "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-13)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Asfaltläggare."
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Axelbesvär ger ofta så akuta smärtor och inskränker rörelsen så mycket att patienten inte kan använda den drabbade armen.",
    "aktivitetsbegransning": "Helt nedsatt arbetsförmåga.",
	"pagaendeBehandling": "Sjukgymnastik egen träning smärtlindring.",
	"planeradBehandling": "Remiss till ortped.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-13),
            "tom": idag(2)
        }
	},
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
    // 6
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Kommunikatör, kontorsarbete"
	},
    "diagnos": {
        "kod": "J459 ",
    },
    "funktionsnedsattning": "Kraftigt påverkad allmäntillstånd, feber, hosta, påtagligt trött, blek, stickande smärta vid inandning.",
    "aktivitetsbegransning": "Hög aktivitetsbegränsning, kan ej utföra sitt arbete.",
	"pagaendeBehandling": "Vila och läkemedelsbehandling.",
 	"planeradBehandling": "Vila och läkemedelsbehandling.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
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
    // 7
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-134)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
	},
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Fortsatta smärtor och nedsatt rörlighet i axelleden. Trött och medtagen, blek i ansiktet.",
    "aktivitetsbegransning": "Kan inte utföra sitt arbete r/t smärta i axel och dåligt sömn.",
	"pagaendeBehandling": "Smärtstillande, vila.",
	"planeradBehandling": "Återbesök efter två veckor.",
    "arbetsformaga": {
        "nedsattMed75": {
            "from": idag(-134),
            "tom": idag(-120)
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
    // 8
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-5),
    },
    "sysselsattning": {
        "typ": "ARBETSSOKANDE"
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Trötthet, energibrist, oförmåga att fatta beslut, oförmåga att planera, försämrat minne, bristande initiativförmåga, motorik, sensibilitet, balansen, synfältet och kognitionsförmåga påverkad.",
    "aktivitetsbegransning": "Enligt min undersökning och patientens utsaga: koncentrationssvårigheter, minnesstörning, personen inte hantera vardagliga problem, problem med balans och motorik.",
	"pagaendeBehandling": "Medicinering mindfullnes remiss till kurator.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-5),
            "tom": idag(11)
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 9
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-20),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arbetar inom flygtrafikledning på Arlanda."
	},
    "diagnos": {
        "kod": "D559"
    },
    "funktionsnedsattning": "Trötthet, nedsatt ork, Huvudvärk, yrsel, Andfåddhet, hjärtklappning, Koncentrationssvårigheter, sömnstörning, nedstämdhet. viktnedgång, nattsvettningar, skelettsmärta.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: kan ej arbeta.",
	"pagaendeBehandling": "Hb, MCV, retikulocyter, vita, trombocyter, CRP Medicinering med B12 och Järn i väntan på labsvar.",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-10),
            "tom": idag(8)
        }
	},
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
    // 10
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-74)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare, arbetar på SJ."
    },
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Smärtor och inskränker rörelsen så mycket att patienten inte kan använda den drabbade armen. Nattlig smärta. Trött, medtagen, väntar på operation.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: kan ej utföra sina ordinariearbetsuppgifter som tågförare och ej heller anpassade uppgifter i kontoret.",
	"pagaendeBehandling": "Smärtstillande stående och vb, vila, fortsatt sjukgymnastik i den mån patienten orkar i väntan på operation.",
	"planeradBehandling": "Remiss till ortoped för artroskopisk dekompression med shaving av akromions undersida, väntar på operation.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-74),
            "tom": idag(10)
        }
    },
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
    // 11
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-1)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arbetar på en städfirma, städar sjukhuslokaler."
    },
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Smärta, värk och stelhet i ländryggen, svängande blodsockernivåer, sömnproblem, trött, medtagen, nedstämd pga smärta.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: Helt nedsatt arbetsförmåga: svårigheter att ändra och bibehålla kroppsställning, gå och röra sig normalt inomhus, statiskt belasta rygg och ben. Svårt att lyfta, stå framåtböjd, resa sig från stol / säng samt utföra manuellt rörligt arbete. Smärtpräglat rörelsemönster. Yrsel.",
	"pagaendeBehandling": "Vila och sedan gradvis ökande fysisk aktivitet remiss sjukgymnast provtagning smärtstillande medicinering",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(7)
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
    // 12
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tågförare SJ."
    },
    "diagnos": {
        "kod": "M751"
    },
    "funktionsnedsattning": "Smärtor som inskränker rörligheten helt, vilo- och nattsmärta, nedsatt allmäntillstånd.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: Helt nedsatt arbetsförmåga.",
	"pagaendeBehandling": "Kortisoninjektion, smärtstillande medicinering, sjukgymnastisk behandling RTG.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(7)
        },
        "nedsattMed75": {
            "from": idag(8),
            "tom": idag(15)
        },
		"nedsattMed50": {
            "from": idag(16),
            "tom": idag(24)
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
    // 13
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-4),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Undersköterska, arbetar på vårdcentral med provtagning."
	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Fortsatt kraftig smärta och stelhet i ryggslutet, utstrålning till ljumskar och skinkor på hö sida. trött, medtagen, sover bara korta stunder på natten. Smärtan stråla även ut i hö ben till knävecken.",
    "aktivitetsbegransning": "Fortsatt väldigt nedsatt rörelseförmåga, grundas på undersökning av patienten, kan inte utföra sitt arbete pga smärta, trötthet.",
	"pagaendeBehandling": "Analgetika för att kunna fortsätta rörelseträningen, vila.",
  	"planeradBehandling": "Återbesök om två veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-6),
            "tom": idag(7)
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
    // 14
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-18),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Sjuksköterska på en akutmottagning, jobbar i tre skift."
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Koncentrationssvårigheter, minnesstörning och ökad uttröttbarhet, energibrist, oförmåga att fatta beslut, oförmåga att planera, försämrat minne, bristande initiativförmåga, ökad känslighet för stress. Förvärras av ryggsmärtor, svårt att sova på nätterna.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: koncentrationssvårigheter, minnesstörning, kan inte hantera vardagliga problem eller ta vård om sig själv.",
	"pagaendeBehandling": "Vila. Smärtbehandling. Antidepressiv behandling påbörjas med upptrappning. Remiss till sjukgymnast för ryggsmärtor.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-18),
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
            "tom": idag(26)
        }
	},
    "atgarder": [{
        "namn": "Kontakt med företagshälsovård",
        "beskrivning": "Kontakt med företagshälsovård-beskrivning",
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
    // 15
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-200),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "It arkitekt, kontorsarbete."
	},
    "diagnos": {
        "kod": "F430"
    },
    "funktionsnedsattning": "Patienten utsattes för överfall och grovt misshandel när han var på väg hem från arbete. Sedan tidigare konstaterad magsår, nu akut blödning. Helt nedsatt, på grund av stark rädsla och andra intensiva och okontrollerbara affekter, dåligt nattsömn. Magsmärtor, illamående och kräkningar.",
    "aktivitetsbegransning": "Kan inte sova, vistas i allmänna platser, ha social umgänge. Omfattande minnesstörning och koncentrationssvårigheter.",
	"pagaendeBehandling": "Farmakologisk behandling med SSRI- preparat, vila, täta kurator samtal på VC, så att det ej utvecklar sig till ett Posttraumatiskt stresstillstånd.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-60),
            "tom": idag(-46)
        }
	},
    "atgarder": [{
        "namn": "Kontakt med företagshälsovård",
        "beskrivning": "Kontakt med företagshälsovård-beskrivning",
        "key": "KONTAKT_FHV"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "12 månader" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 16
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-18),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Brevbärare, arbetar på Bring, använder cykel att förflytta sig när delar brev."
	},
    "diagnos": {
        "kod": "K309"
    },
    "funktionsnedsattning": "Trötthet, viktnedgång och besvärliga buksmärtor.",
    "aktivitetsbegransning": "Sjukdomen begränsar patientens förmåga att arbeta, är utmattad av illamående och kräkningar och brist på sömn. Patienten kan klarar inte av sitt arbete som kräver god fysik.",
	"pagaendeBehandling": "Hb, F-Hb, Ev leverprover.",
  	"planeradBehandling": "Remiss till gastroskopi.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-18),
            "tom": idag(-12)
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
    // 17
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Kommunikatör, kontorsarbete."
	},
    "diagnos": {
        "kod": "J13"
    },
    "funktionsnedsattning": "Kraftigt påverkad allmäntillstånd, feber, hosta, påtagligt trött, blek, stickande smärta vid inandning.",
    "aktivitetsbegransning": "Hög aktivitetsbegränsning, kan ej utföra sitt arbete.",
	"pagaendeBehandling": "Läkemedelsbehandling.",
  	"planeradBehandling": "Vila.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-16),
            "tom": idag(-1)
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
    // 18
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-12),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Fondförvaltare på PPM."
	},
    "diagnos": {
        "kod": "R522"
    },
    "funktionsnedsattning": "Nedsatt muskelstyrka/koordination, uthållighet, kognitiv förmåga och emotionell stabilitet. Rädsla för att ansträngning leder till försämring och otillräcklig acceptans för sänkt prestationsförmåga leder till undvikande av aktiviteter som befaras öka smärtan. Sömnstörning.",
    "aktivitetsbegransning": "Helt nedsatt arbetsförmåga.",
	"pagaendeBehandling": "Provtagning psykologsamtal smärtlindring.",
  	"planeradBehandling": "Remiss till smärtutredning till smärtkliniken.",
     "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-12),
            "tom": idag()
        },
        "nedsattMed75": {
            "from": idag(1),
            "tom": idag(7)
        },
		"nedsattMed50": {
            "from": idag(8),
            "tom": idag(15)
        }
	},
    "atgarder": [{
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
	}, 
	{
        namn: 'Kontakt med företagshälsovård',
        beskrivning: 'Kontakt med företagshälsovård-beskrivning',
        key: 'KONTAKT_FHV'
	}],
    "prognosForArbetsformaga": {
        "name": "PROGNOS_OKLAR"
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 19
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-5),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "IT tekniker, jobbar inom support på Telia."
	},
    "diagnos": {
        "kod": "N04"
    },
    "funktionsnedsattning": "Ödem, svullnad i ansiktet och runt anklarna. Blek, allmänpåverkad, anger att har ångestattacker dagligen. Patienten med Hepatit B söker för troliga biverkningar av antiviral behandling. Kraftig funktionsnedsättning.",
    "aktivitetsbegransning": "Hel funktionsnedsättning, kan inte arbeta, har svårt att vistas utanför hemmet, har svårt att sova på nätterna.",
	"pagaendeBehandling": "Antiviral behandling.",
  	"planeradBehandling": "U-sticka, tU-albumin (alternativt tU-protein eller U-albumin/kreatinin-kvot) S-albumin, S-kreatinin, S-urea S-kolesterol, S-triglycerider Serologiska antikroppar (ANA, anti-DNA, ANCA, anti-MPO) Antidepressiv behandling påbörjas med upptrappning.",
     "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-5),
            "tom": idag(9)
        }
	},
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
    // 20
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-180),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arbetar på en städfirma, städar sjukhuslokaler."
	},
    "diagnos": {
        "kod": "F430"
    },
    "funktionsnedsattning": "Stark rädsla, okontrollerbara affekter, trött, medtagen kan inte sova på natten efter maken har avlidit. Yrselbesvär och svängande blodsockernivåer.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: Helt nedsatt arbetsförmåga.",
	"pagaendeBehandling": "Medicinering.",
  	"planeradBehandling": "Kuratorsamtal Mindfullnes medicinering mot sömnlöshet provtagning.",
     "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-180),
            "tom": idag(-166)
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
    // 21
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Arbetar med planering av logistik på DHL."
	},
    "diagnos": {
        "kod": "F412"
    },
    "funktionsnedsattning": "Uttalade symtom på både ångest och depression: ingen aptit, koncentrationssvårigheter, nedstämd. Ingen klar utlösningsfaktor som patienten kan ange. Symtom kom plötsligt och med stor kraft. Inga självmordstankar enligt MADRS.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: Klarar inte av att ta sig till jobbet.",
	"pagaendeBehandling": "Medicinering. Kuratorsamtal.",
  	"planeradBehandling": "Återbesök inom två veckor.",
     "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(14)
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
    // 22
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-12),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Brevbärare, arbetar på Bring, använder cykel att förflytta sig när delar brev."
	},
    "diagnos": {
        "kod": "K309"
    },
    "funktionsnedsattning": "Trötthet, viktnedgång och besvärliga buksmärtor.",
    "aktivitetsbegransning": "Sjukdomen begränsar patientens förmåga att arbeta, är utmattad av illamående och kräkningar och brist på sömn. Patienten kan klarar inte av sitt arbete som kräver god fysik.",
	"pagaendeBehandling": "Hb, F-Hb, Ev leverprover.",
  	"planeradBehandling": "Remiss till gastroskopi.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-11),
            "tom": idag(-6)
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
    // 23
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(),
    },
    "sysselsattning": {
        "typ": "FORALDRALEDIG"
	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Fortsatt kraftig smärta och stelhet i ryggslutet, utstrålning till ljumskar och skinkor på hö sida. trött, medtagen, sover bara korta stunder på natten. Smärtan stråla även ut i hö ben till knävecken.",
    "aktivitetsbegransning": "Fortsatt väldigt nedsatt rörelseförmåga, grundas på undersökning av patienten, kan inte utföra sitt arbete pga smärta, trötthet.",
	"pagaendeBehandling": "Analgetika för att kunna fortsätta rörelseträningen, vila.",
  	"planeradBehandling": "Återbesök om två veckor.",
    "arbetsformaga": {
        "nedsattMed100": {
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 24
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-14),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Undersköterska, arbetar på vårdcentral med provtagning."
	},
    "diagnos": {
        "kod": "M545"
    },
    "funktionsnedsattning": "Kraftig smärta och stelhet i ryggslutet, utstrålning till ljumskar och skinkor på hö sida.",
    "aktivitetsbegransning": "Grundas på undersökning av patienten, kan inte utföra sitt arbete pga smärta.",
	"pagaendeBehandling": "Analgetika, vila.",
  	"planeradBehandling": "Återbesök efter en vecka.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-14),
            "tom": idag(-7)
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
