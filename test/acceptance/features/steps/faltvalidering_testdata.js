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

module.exports = {
    'Transportstyrelsens läkarintyg, diabetes': {
        radioknappar: {
            'a) Ögonläkarintyg kommer att skickas in separat': 'Nej',
            'b) Förekommer hypoglykemier med tecken på nedsatt hjärnfunktion (neuroglukopena symtom) som bedöms kunna innebära en trafiksäkerhetsrisk?': 'Ja',
            'd) Har patienten haft allvarlig hypoglykemi (som krävt hjälp av annan för att hävas) under det senaste året?': 'Nej',
            'e) Har patienten haft allvarlig hypoglykemi i trafiken under det senaste året?': 'Ja',
            'g) Har patienten haft allvarlig hypoglykemi (som krävt hjälp av annan för att hävas) under vaken tid det senaste året?': 'Ja'
        },
        checkboxar: [
            'Insulin',
            'Taxi'
        ],
        text: [
            'diabetes-årtal',
            'alla synfält',
            'datum',
            'postnummer'
        ]
    },
    'Transportstyrelsens läkarintyg': {
        radioknappar: {
            'Har patienten diabetes?': 'Ja',
            'Vilken typ?': 'Typ 2',
            'a) Har patienten någon sjukdom eller funktionsnedsättning som påverkar rörligheten och som medför att fordon inte kan köras på ett trafiksäkert sätt?': 'Ja',
            'c) Föreligger viktiga riskfaktorer för stroke (tidigare stroke eller TIA, förhöjt blodtryck, förmaksflimmer eller kärlmissbildning)?': 'Ja',
            'a) Finns journaluppgifter, anamnestiska uppgifter, resultat av laboratorieprover eller andra tecken på missbruk eller beroende av alkohol, narkotika eller läkemedel?': 'Ja',
            'c) Pågår regelbundet läkarordinerat bruk av läkemedel som kan innebära en trafiksäkerhetsrisk?': 'Ja',
            'Har patienten vårdats på sjukhus eller haft kontakt med läkare med anledning av fälten 1-13?': 'Ja',
            'Har patienten någon stadigvarande medicinering?': 'Ja'
        },
        checkboxar: [
            'Taxi'
        ],
        text: [
            'postnummer'
        ]
    },
    'Läkarintyg för sjukpenning': {
        radioknappar: {
            'Prognos för arbetsförmåga utifrån aktuellt undersökningstillfälle': 'Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning inom',
        },
        checkboxar: [
            'Annat',
            'Nuvarande arbete',
            '25 procent',
            '50 procent',
            '75 procent',
            '100 procent',
        ],
        text: [
            'postnummer',
            'datum'
        ]
    },
    'Läkarutlåtande för sjukersättning': {
        radioknappar: {
            'Är utlåtandet även baserat på andra medicinska utredningar eller underlag?': 'Ja',
            'Finns skäl till att revidera/uppdatera tidigare satt diagnos?': 'Ja'
        },
        checkboxar: [
            'Annat',
        ],
        text: [
            'postnummer',
            'datum'
        ]
    },
    'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång': {
        radioknappar: {
            'Är utlåtandet även baserat på andra medicinska utredningar eller underlag?': 'Ja'
        },
        checkboxar: [
            'Annat',
        ],
        text: [
            'postnummer',
            'datum'
        ]
    }
};
