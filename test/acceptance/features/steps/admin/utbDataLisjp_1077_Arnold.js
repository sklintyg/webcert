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
        "minUndersokningAvPatienten": idag(-11)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Tjänsteman, kontorsarbete."
	},
    "diagnos": {
        "kod": "F329"
    },
    "funktionsnedsattning": "Trötthet, energibrist, oförmåga att fatta beslut, oförmåga att planera, försämrat minne, bristande initiativförmåga, motivation och uthållighet samt ökad känslighet för stress. Social rädsla. Kan inte hantera var-dagliga problem eller ta vård om sig själv.",
    "aktivitetsbegransning": "Min undersökning och patientens utsaga: koncentrationssvårigheter, minnesstörning, kan inte hantera vardagliga problem eller ta vård om sig själv.",
	"pagaendeBehandling": "Antidepressiv medicinering kuratorsamtal.",
	"planeradBehandling": "Mindfullnes.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-11),
            "tom": idag(10)
        }
    },
    "atgarder": [{
        "namn": "Inte aktuellt",
        "key": "EJ_AKTUELLT"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "3 månad" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 1 
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-6),
		"journaluppgifter":idag(-6)
    },
    "sysselsattning": {
		"typ": "FORALDRALEDIG"
	},
    "diagnos": {
        "kod": "J36"
    },
    "funktionsnedsattning": "Hög feber och smärta i svalget. Grötigt tal, käkläsa.",
    "aktivitetsbegransning": "Så pass allmänpåverkad och trött att han ej ensam kan ta hand om barnen, behov av avlastning.",
	"pagaendeBehandling": "Punktion utav halsböld utföres på vårdcentral. Sedvanlig antibiotikabehandling. Vila.",
    "arbetsformaga": {
        "nedsattMed50": {
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
     // 2
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-2),
		"journaluppgifter":idag(-2)
    },
    "sysselsattning": {
		"typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Landskapsarkitekt."
    },
    "diagnos": {
        "kod": "M544"
    },
    "funktionsnedsattning": "Känselbortfall i höger ben och upplevd muskelsvaghet vid gång. Palpationsöm över ryggslut. Framkallar onormala känselförnimmelser vid provokationstest. Smärta som strålar ner över höger gluteus mot benet. Bedöms som lumbago med ischias. Mycket högt blodtryck uppmäts vid undersökningstillfället.",
    "aktivitetsbegransning": "Begränsad möjlighet att lyfta eller böja kroppen till följd av smärta och stelhet. Har sömnproblem pga smärta och upplever nedsatt koncentrationsförmåga på jobbet.",
	"pagaendeBehandling": "Lätt analgetika i smärtlindrande syfte. Vila ett par dagar, sedan upptrappad mobilisering utan hinder.",
	"planeradBehandling": "Uppföljning av blodtryck på hypertonimottagning i smärtfritt skede.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-2),
            "tom": idag(4)
        },
        "nedsattMed50": {
            "from": idag(5),
            "tom": idag(11)
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
        "minUndersokningAvPatienten": idag(-17)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Asfaltläggare på privat firma"
    },
    "diagnos": {
        "kod": "J44"
    },
    "funktionsnedsattning": "Nyligen avslutad KOL exacerbation som inte läkt ut, utan förvärrats till en bilateral lunginflammation . Högt CRP och febrig. Trött.",
    "aktivitetsbegransning": "Kan ej utföra lättare sysslor utan att få påtagliga svårigheter med lufthunger. Blir då ångestfull och stressad vilket förvärrar situationen. Bedöms ej kunna utföra nuvarande arbetssysslor under pågående antibiotikabehandling. Behov av inneliggande vård.",
	"pagaendeBehandling": "Kan ej utföra lättare sysslor utan att få påtagliga svårigheter med lufthunger. Blir då ångestfull och stressad vilket förvärrar situationen. Bedöms ej kunna utföra nuvarande arbetssysslor under pågående antibiotikabehandling. Behov av inneliggande vård.",
	"planeradBehandling": "Antibiotikabehandling intravenöst inneliggandes. Remiss skrives för inläggning på Lungavdelning US.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-4),
            "tom": idag(26)
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
    // 4
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-30)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "IT konsult"
	},
    "diagnos": {
        "kod": "R52"
    },
    "funktionsnedsattning": "Molande värk i ländryggsregionen sedan början på sommaren, uppkommit i samband med fysiskt påfrestande grävningsarbete på tomten. Går med korta steg i korridoren, smärta vid ryggläge på undersökningsbrits. Palpations öm över ryggslutet. Uppger att han undviker rörelser eller moment som kan förvärra smärtan.",
    "aktivitetsbegransning": "Har svårt att sitta ner mer än en halvtimme åt gången. Försökt anpassa arbetsplatsen med ergonomisk stol utan resultat. Har svårt att utföra arbetsuppgifter då han har nedsatt koncentrationsförmåga till följd av sömnbrist.",
	"pagaendeBehandling": "Analgetika i smärtstillande syfte.",
	"planeradBehandling": "Skickar remiss till KS för datortomografi utav ryggen för att utesluta process. Därefter kontakt med fysioterapeut för anpassad träning.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-30),
            "tom": idag(-15)
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 5
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "IT konsult"
	},
    "diagnos": {
        "kod": "R52"
    },
    "funktionsnedsattning": "Molande värk i ländryggsregionen sedan början på sommaren, uppkommit i samband med fysiskt påfrestande grävningsarbete på tomten. Går med korta steg i korridoren, smärta vid ryggläge på undersökningsbrits. Palpations öm över ryggslutet. Uppger att han undviker rörelser eller moment som kan förvärra smärtan.",
    "aktivitetsbegransning": "Har svårt att sitta ner mer än en halvtimme åt gången. Försökt anpassa arbetsplatsen med ergonomisk stol utan resultat. Har svårt att utföra arbetsuppgifter då han har nedsatt koncentrationsförmåga till följd av sömnbrist.",
	"pagaendeBehandling": "Analgetika i smärtstillande syfte.",
	"planeradBehandling": "Skickar remiss till KS för datortomografi utav ryggen för att utesluta process. Därefter kontakt med fysioterapeut för anpassad träning.",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-14),
            "tom": idag(7)
        }
	},
   "atgarder": [{
        "namn": "Ergonomisk bedömning",
        "beskrivning": "Ergonomisk bedömning-beskrivning",
        "key": "ERGONOMISK"
    },
	{
        "namn": "Besök på arbetsplatsen",
        "beskrivning": "Besök på arbetsplatsen-beskrivning",
        "key": "BESOK_ARBETSPLATS"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "1 månad" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 6
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-1200)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "IT konsult"
	},
    "diagnos": {
        "kod": "J16",
    },
    "funktionsnedsattning": "Pneumoni orsakad av andra infektiösa organismer som ej klassificeras annorstädes.",
    "aktivitetsbegransning": "Kraftig hosta med förhöjd feber. Ökade infektionsparametrar.",
	"pagaendeBehandling": "Kraftigt påverkat allmäntillstånd. Behov av vila.",
 	"planeradBehandling": "Antibiotika samt febernedsättande att ta vid behov.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-1200),
            "tom": idag(-1185)
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
        "minUndersokningAvPatienten": idag(-200),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lärare."
    },
    "diagnos": {
        "kod": "J10"
    },
    "funktionsnedsattning": "Feber och påverkat allmäntillstånd sedan två veckor tillbaka, bedöms som säsongsinfluensa. Nu ytterligare besvär av akut sinuit. Palpations öm över pannan, munandas.",
    "aktivitetsbegransning": "Bedöms för allmänpåverkad för att kunna utföra ordinarie arbetssysslor.",
	"pagaendeBehandling": "Antibiotikabehandling och vila.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-200),
            "tom": idag(-190)
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
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 8
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Lärare för lågstadieklass."
	},
    "diagnos": {
        "kod": "F321"
    },
    "funktionsnedsattning": "Luxation av höger knä efter cykelolycka. Svullen i knät, kan ej ta ut full rörlighet.",
    "aktivitetsbegransning": "Kan ej stödja på benet, svårigheter att gå. Behov av kryckor. Bedöms därför ej kunna utföra sitt arbete.",
	"pagaendeBehandling": "Vila.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(),
            "tom": idag(60)
        }
	},
   "atgarder": [{
        "namn": "Kontakt med företagshälsovård",
        "beskrivning": "Kontakt med företagshälsovård-beskrivning",
        "key": "KONTAKT_FHV"
    },
	{
        "namn": "Besök på arbetsplatsen",
        "beskrivning": "Besök på arbetsplatsen-beskrivning",
        "key": "BESOK_ARBETSPLATS"
	}],
    "prognosForArbetsformaga": {
        "name": "ATER_X_ANTAL_DGR",
		"within": "3 månad" 
    },
    "ovrigt": "Detta är ett Intyg skapat i utbildningssyfte",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
}, {
    // 9
    "smittskydd": false,
    "baseratPa": {
       "minUndersokningAvPatienten": idag(-4),
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Jobbar inom renhållningsbranschen."
	},
    "diagnos": {
        "kod": "J15"
    },
    "funktionsnedsattning": "Påverkat allmäntillstånd och kraftig hosta sedan en vecka tillbaka. Stegrande infektionsparametra. Kreptationer och ökad slemstagnation bilateralt över lungorna. Även pågående ÖVI med förkylning.",
    "aktivitetsbegransning": "På grund av trötthet och det påverkade allmäntillståndet bedöms ej patienten kunna utföra arbetet med sophantering. Kan ej omplaceras.",
	"pagaendeBehandling": "Antibiotika och vila.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-4),
            "tom": idag(10)
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
    // 10
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
        "nedsattMed50": {
            "from": idag(-2),
            "tom": idag(4)
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
    // 11
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
    // 12
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-25),
		"journaluppgifter":idag(-25)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Asfaltläggare på privat firma"
    },
    "diagnos": {
        "kod": "J440"
    },
    "funktionsnedsattning": "Ångestpåverkad och stressad över lufthunger till följd av försämring i sin kol pga nedre luftvägsinfektion. Stegrande infektionsparametrar. Har dragit ner på rökning men ej helt slutat.",
    "aktivitetsbegransning": "Kan ej utföra lättare sysslor utan att få påtagliga svårigheter med lufthunger. Blir då ångestfull och stressad vilket förvärrar situationen. Bedöms ej kunna utföra nuvarande arbetssysslor under pågående antibiotikabehandling. Behov av inneliggande vård.",
	"pagaendeBehandling": "Kan ej utföra lättare sysslor utan att få påtagliga svårigheter med lufthunger. Blir då ångestfull och stressad vilket förvärrar situationen. Bedöms ej kunna utföra nuvarande arbetssysslor under pågående antibiotikabehandling. Behov av inneliggande vård.",
	"planeradBehandling": "Antibiotikabehandling intravenöst inneliggandes. Remiss skrives för inläggning på Lungavdelning US.",
    "arbetsformaga": {
        "nedsattMed100": {
            "from": idag(-4),
            "tom": idag(26)
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
        "minUndersokningAvPatienten": idag(),
		"journaluppgifter":idag()
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Frisör"
    },
    "diagnos": {
        "kod": "S53"
    },
    "funktionsnedsattning": "Efter fallolycka från cykel ådragit sig en distorsion av höger armbåges dorsala ligament. Kan ej vinkla ut höger arm i fullt läge, smärtar vid palpation. Svullen och öm.",
    "aktivitetsbegransning": "Kan ej utföra sitt arbete som frisör då ej fullgod rörlighet föreligger i armbågsled, patienten är högerhänt.",
	"pagaendeBehandling": "Armbågsortos och vila.",
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
    // 14
    "smittskydd": false,
    "baseratPa": {
        "minUndersokningAvPatienten": idag(-4),
		"journaluppgifter":idag(-4)
    },
    "sysselsattning": {
        "typ": "NUVARANDE_ARBETE",
        "yrkesAktiviteter": "Florist"
    },
    "diagnos": {
        "kod": "D51"
    },
    "funktionsnedsattning": "Nedsatt allmäntillstånd och trött sedan tre veckor tillbaka. Labbprover visar på järnbristanemi till följd av vitaminbrist. Kända alkoholproblem sedan tidigare. Leverprover påverkade som tidigare.",
    "aktivitetsbegransning": "Till följd av nedsatt ork och nedsatt allmäntillstånd förmår ej patienten att fullt ut utföra sina arbetssysslor.",
	"pagaendeBehandling": "Upptrappning av vitaminer och järn enligt schema. Uppföljning med nya prover om två veckor.",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(-4),
            "tom": idag(9)
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
