/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

'use strict';

module.exports = {
    fmbInfo: {
        diagnoser: [{
            kod: 'S83',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld mot kroppens extremiteter',
            generellInfo: 'arbetsförmågan är ofta inte nedsatt någon längre tid ',
            funktionsnedsattning: 'Skadorna minskar förmågan att gå och stå',
            aktivitetsbegransning: null,
            beslutsunderlag: 'I arbeten som kan utföras mestadels stillasittande'

        }, {
            kod: 'S832',
            symptomPrognosBehandling: 'Meniskskador kan uppkomma',
            generellInfo: 'Akuta besvär efter en meniskskada',
            funktionsnedsattning: 'upphakningar i knäleden',
            aktivitetsbegransning: 'Påverkad gångförmåga',
            beslutsunderlag: 'I akutskede efter knädistorsionsskada'

        }, {
            kod: 'S835',
            symptomPrognosBehandling: 'Meniskskador kan uppkomma efter trauma',
            generellInfo: 'Akuta besvär efter en meniskskada',
            funktionsnedsattning: 'Meniskskador kan ge upphakningar',
            aktivitetsbegransning: 'Påverkad gångförmåga',
            beslutsunderlag: 'I akutskede'

        }, {
            kod: 'S90',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: null

        }, {
            kod: 'S92',
            symptomPrognosBehandling: 'Skador orsakade av',
            generellInfo: 'arbetsförmågan är ofta inte nedsatt',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'I arbeten som kan utföras'

        }, {
            kod: 'S93',
            symptomPrognosBehandling: 'Skador orsakade av',
            generellInfo: 'arbetsförmågan är ofta ',
            funktionsnedsattning: 'Skadorna minskar förmågan ',
            aktivitetsbegransning: null,
            beslutsunderlag: null

        }, {
            kod: 'R09',
            symptomPrognosBehandling: null,
            generellInfo: 'Sjukskrivning enbart',
            funktionsnedsattning: null,
            aktivitetsbegransning: null,
            beslutsunderlag: null
        }, {
            kod: 'O267',
            symptomPrognosBehandling: 'Smärtor i bäckenet och',
            generellInfo: 'Observandum',
            funktionsnedsattning: 'begränsas förmågan att lyfta',
            aktivitetsbegransning: 'Gå, stå sitta',
            beslutsunderlag: 'Flertalet gravida'
        }, {
            kod: 'N850',
            symptomPrognosBehandling: 'Rekommendationen omfattar operation',
            generellInfo: 'Komplikationer kan',
            funktionsnedsattning: 'Bukväggskirurgi',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid kombinerad laparoskopisk'
        }, {
            kod: 'M753',
            symptomPrognosBehandling: 'Kortvariga besvär',
            generellInfo: 'De akuta besvären',
            funktionsnedsattning: 'axelbesvär ger ofta så akuta smärtor',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Kalkaxel och impingementsyndrom'
        }, {
            kod: 'M751',
            symptomPrognosBehandling: 'drabbade axelleden',
            generellInfo: 'rotatorcuffsyndrom',
            funktionsnedsattning: 'axelbesvär ger ofta',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Kalkaxel och impingementsyndrom'
        }, {
            kod: 'R40',
            symptomPrognosBehandling: null,
            generellInfo: 'Sjukskrivning enbart utifrån symtom ska undvikas',
            funktionsnedsattning: null,
            aktivitetsbegransning: null,
            beslutsunderlag: null
        }]
    },
    utanEgenFMBInfo: {
        diagnoser: [{
            kod: 'S830',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'I arbeten som kan utföras mestadels stillasittande',
            overliggande: 'För S830 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden S83'

        }, {
            kod: 'S831',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'I arbeten som kan utföras mestadels stillasittande',
            overliggande: 'För S831 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden S83'

        }, {
            kod: 'R010',
            symptomPrognosBehandling: null,
            generellInfo: 'Sjukskrivning enbart utifrån ',
            funktionsnedsattning: null,
            aktivitetsbegransning: null,
            beslutsunderlag: null,
            overliggande: 'För R010 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden R01'
        }, {
            kod: 'R090',
            symptomPrognosBehandling: null,
            generellInfo: 'Sjukskrivning enbart utifrån symtom',
            funktionsnedsattning: null,
            aktivitetsbegransning: null,
            beslutsunderlag: null,
            overliggande: 'För R090 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden R09'
        }, {
            kod: 'N210',
            symptomPrognosBehandling: 'Symtomen vid njursten är svåra smärtor',
            generellInfo: 'Efter akuta pyelonefrit',
            funktionsnedsattning: 'Vid akuta njurstensanfall',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid akut njurstensanfall',
            overliggande: 'För N210 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden N21'

        }, {
            kod: 'S302',
            symptomPrognosBehandling: 'Skador orsakade av yttre',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan att gå och stå',
            aktivitetsbegransning: null,
            beslutsunderlag: null,
            overliggande: 'För S302 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden S30'

        }, {
            kod: 'F420',
            symptomPrognosBehandling: 'Tvångssyndrom kännetecknas',
            generellInfo: 'Sjukskrivning kan i',
            funktionsnedsattning: 'Tvångssyndrom kan medföra',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Sjukskrivning kan i många fall undvikas',
            overliggande: 'För F420 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden F42'

        }, {
            kod: 'G359',
            symptomPrognosBehandling: 'Multipel skleros',
            generellInfo: 'Vid sjukdomsdebut',
            funktionsnedsattning: 'Hur MS påverkar',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid sjukdomsdebut',
            overliggande: 'För G359 finns inget FMB-stöd. Det FMB-stöd som visas nedan gäller den mindre specifika koden G35'

        }]

    },
    utanFMBInfo: {
        diagnoser: [{
            kod: 'H92'
        }, {
            kod: 'I00'
        }, {
            kod: 'N08'
        }, {
            kod: 'M76'
        }, {
            kod: 'O00'
        }, {
            kod: 'A00'
        }]
    }
};
