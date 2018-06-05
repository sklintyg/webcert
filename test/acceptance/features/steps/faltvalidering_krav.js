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
    antalValideringsMeddelanden: {
        'Dödsbevis': {
            'tomma fält': {
                tomt: 8,
                ettAlternativ: 4,
                minstEttAlternativ: 0,
                diagnos: 0,
                minstEnRad: 0,
                datum: 2,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 2,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 2,
                datumHypoglykemi: 0,
                underlag: 0,
                utredningInfo: 0,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Dödsorsaksintyg': {
            'tomma fält': {
                tomt: 9,
                ettAlternativ: 3,
                minstEttAlternativ: 1,
                diagnos: 0,
                minstEnRad: 0,
                datum: 3,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 2,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 7,
                datumHypoglykemi: 0,
                underlag: 0,
                utredningInfo: 0,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga': {
            'tomma fält': {
                tomt: 10,
                ettAlternativ: 0,
                minstEttAlternativ: 1,
                diagnos: 1,
                minstEnRad: 1,
                datum: 1,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 1,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 8,
                datumHypoglykemi: 0,
                underlag: 3,
                utredningInfo: 3,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång': {
            'tomma fält': {
                tomt: 5,
                ettAlternativ: 0,
                minstEttAlternativ: 0,
                diagnos: 1,
                minstEnRad: 1,
                datum: 1,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 1,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 8,
                datumHypoglykemi: 0,
                underlag: 3,
                utredningInfo: 3,
                funkDebut: 1,
                funkPaverkan: 1
            }
        },
        'Läkarutlåtande för sjukersättning': {
            'tomma fält': {
                tomt: 10,
                ettAlternativ: 0,
                minstEttAlternativ: 1,
                diagnos: 1,
                minstEnRad: 1,
                datum: 1,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 1,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 8,
                datumHypoglykemi: 0,
                underlag: 3,
                utredningInfo: 3,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Transportstyrelsens läkarintyg högre körkortsbehörighet': {
            'tomma fält': {
                tomt: 16,
                ettAlternativ: 22,
                minstEttAlternativ: 1,
                diagnos: 0,
                minstEnRad: 0,
                datum: 0,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 2,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 0,
                datumHypoglykemi: 0,
                underlag: 0,
                utredningInfo: 0,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Transportstyrelsens läkarintyg diabetes': {
            'tomma fält': {
                tomt: 11,
                ettAlternativ: 9,
                minstEttAlternativ: 0,
                diagnos: 0,
                minstEnRad: 0,
                datum: 1,
                arbetstid: 0
            },
            'otillåten input': {
                postnummer: 2,
                synintervall: 6,
                arFodelse: 1,
                arNittonhundra: 1,
                datumFormat: 0,
                datumHypoglykemi: 1,
                underlag: 0,
                utredningInfo: 0,
                funkDebut: 0,
                funkPaverkan: 0
            }
        },
        'Läkarintyg för sjukpenning': {
            'tomma fält': {
                tomt: 12,
                ettAlternativ: 1,
                minstEttAlternativ: 0,
                diagnos: 1,
                minstEnRad: 0,
                datum: 0,
                arbetstid: 1
            },
            'otillåten input': {
                postnummer: 1,
                synintervall: 0,
                arFodelse: 0,
                arNittonhundra: 0,
                datumFormat: 12,
                datumHypoglykemi: 0,
                underlag: 0,
                utredningInfo: 0,
                funkDebut: 0,
                funkPaverkan: 0
            }
        }
    },
    kravTxt: {
        'tomma fält': {
            tomt: "Fältet får inte vara tomt.",
            ettAlternativ: "Du måste välja ett alternativ.",
            minstEttAlternativ: "Du måste välja minst ett alternativ.",
            diagnos: "Minst en diagnos måste anges.",
            minstEnRad: "Minst en rad måste fyllas i.",
            datum: "Du måste välja datum.",
            arbetstid: "Arbetstidsförläggning måste fyllas i om period 75%, 50% eller 25% har valts."
        },
        'otillåten input': {
            postnummer: "Postnummer måste anges med fem siffror.",
            synintervall: "Måste ligga i intervallet 0,0 till 2,0.",
            arFodelse: "År måste anges enligt formatet åååå. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår.",
            arNittonhundra: "År måste anges enligt formatet åååå. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900.",
            datumFormat: "Datum behöver skrivas på formatet åååå-mm-dd.",
            datumHypoglykemi: "Tidpunkt för allvarlig hypoglykemi under vaken tid måste anges som åååå-mm-dd, och får inte vara tidigare än ett år tillbaka eller senare än dagens datum.",
            underlag: "Du måste ange ett underlag.",
            utredningInfo: "Du måste ange var Försäkringskassan kan få information om utredningen.",
            funkDebut: "Funktionsnedsättningens debut och utveckling måste fyllas i.",
            funkPaverkan: "Funktionsnedsättningens påverkan måste fyllas i."
        }
    },

    meddelanden: [ // Listan kommer ifrån https://inera-certificate.atlassian.net/wiki/spaces/IT/pages/320176155/Valideringsmeddelanden
        'Postnummer måste anges med fem siffror.',
        'Datum får inte ligga för långt fram eller tillbaka i tiden.',
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
        'År måste anges enligt formatet åååå. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900.',
        'År måste anges enligt formatet åååå. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår.',
        'Datum behöver skrivas på formatet åååå-mm-dd.',
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
