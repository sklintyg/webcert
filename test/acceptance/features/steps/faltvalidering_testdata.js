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
    val: {
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
        },
        'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga': {
            radioknappar: {
                'Är utlåtandet även baserat på andra medicinska utredningar eller underlag?': 'Ja',
                'Finns skäl till att revidera/uppdatera tidigare satt diagnos?': 'Ja'
            },
            checkboxar: [
                'Annat',
                'Intellektuell funktion',
                'Kommunikation och social interaktion',
                'Uppmärksamhet, koncentration och exekutiv funktion',
                'Annan psykisk funktion',
                'Sinnesfunktioner och smärta',
                'Balans, koordination och motorik',
                'Annan kroppslig funktion'
            ],
            text: [
                'postnummer',
                'datum'
            ]
        }
    },

    meddelanden: [ // Listan kommer ifrån https://inera-certificate.atlassian.net/wiki/spaces/IT/pages/320176155/Valideringsmeddelanden
        'Postnummer måste anges med fem siffror.',
        'ICD-10 kod saknas på diagnos. (Fält 2)',
        'Fritextfältet som hör till alternativet Övrigt måste fyllas i. (Fält 6a)',
        'Datum för nedsatt med 100% har angetts med felaktigt format (Fält 8b). Datum ska fyllas i åååå-mm-dd.',
        'Datum får inte ligga för långt fram eller tillbaka i tiden.',
        'Datum för nedsatt med 75% har angetts med felaktigt format (Fält 8b). Datum ska fyllas i åååå-mm-dd.',
        'Datum för nedsatt med 50% har angetts med felaktigt format (Fält 8b). Datum ska fyllas i åååå-mm-dd.',
        'Datum för nedsatt med 25% har angetts med felaktigt format (Fält 8b). Datum ska fyllas i åååå-mm-dd.',
        'Tvådatumintervallmed överlappande datum har angetts. (Fält 8b)',
        'Sjukskrivningsperiod med överlappande datum har angetts.',
        'Får inte vara senare än "Min undersökning av patienten"',
        'Får inte vara senare än "Anhörigs beskrivning av patienten".',
        'Minst en rad måste fyllas i.',
        'Du måste ange ett underlag.',
        'Du måste ange datum för underlaget.',
        'Du måste ange var Försäkringskassan kan få information om utredningen.',
        'Minst en diagnos måste anges.',
        'Diagnoskod måste anges.',
        'Funktionsnedsättningens debut och utveckling måste fyllas i.',
        'Funktionsnedsättningens påverkan måste fyllas i.',
        'Minst en sjukskrivningsperiod måste anges.',
        'Felaktigt datumformat.',
        'Arbetstidsförläggning måste fyllas i om period 75%, 50% eller 25% har valts.',
        'Åtgärder måste väljas eller Inte aktuellt.',
        'Minst en behandling måste väljas.',
        'År då behandling med insulin påbörjades måste anges.',
        'År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900.',
        'År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår.',
        'Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD.',
        'Ogiltigt datum.',
        'Fältet får inte vara tomt.',
        'Du måste välja minst ett alternativ.',
        'Du måste välja ett alternativ.',
        'Du måste välja datum.',
        'Du måste ange år och månad.',
        'Du måste ange månad.',
        'Datumet får inte vara senare än "Dödsdatum".',
        'Datumet får inte vara senare än datumet för "Anträffad död".',
        'Datumet får inte vara senare än dagens datum.',
        'Observera att de kontaktuppgifter du ändrat inte ändras i HSA. Om vårdenhetens kontaktuppgifter inte stämmer kontakta HSA för att ändra dem.',
        'Det datum du angett innebär en period på mer än 6 månader. Du bör kontrollera att tidsperioderna är korrekta.',
        'Det startdatum du angett är mer än en vecka före dagens datum. Du bör kontrollera att tidsperioderna är korrekta.',
        'Du måste ange ett startdatum innan du använder kortkommando i fältet "till och med".',
        'Startdatum får inte vara efter slutdatum.',
        'Ange underlag eller utredning i den översta raden först.',
        'Observera att du valt ett datum framåt i tiden.',
        'Du måste ange ett giltligt personnummer eller samordningsnummer.',
        '' // Valideringsmeddelanden kan alltid vara tomma.
    ]


};
