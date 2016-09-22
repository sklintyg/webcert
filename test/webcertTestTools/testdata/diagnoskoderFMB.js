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
            falt: ['Skador orsakade av yttre våld mot kroppens extremiteter',
                'arbetsförmågan är ofta inte nedsatt någon längre tid ',
                'Skadorna minskar förmågan att gå och stå', null,
                'I arbeten som kan utföras mestadels stillasittande'
            ]
        }, {
            kod: 'S832',
            falt: ['Meniskskador kan uppkomma', 'Akuta besvär efter en meniskskada',
                'upphakningar i knäleden', 'Påverkad gångförmåga', 'I akutskede efter knädistorsionsskada'
            ]
        }, {
            kod: 'S835',
            falt: ['Meniskskador kan uppkomma efter trauma', 'Akuta besvär efter en meniskskada',
                'Meniskskador kan ge upphakningar', 'Påverkad gångförmåga', 'I akutskede'
            ]
        }, {
            kod: 'S90',
            falt: ['Skador orsakade av yttre våld', 'arbetsförmågan är ofta',
                'Skadorna minskar förmågan', null, null
            ]
        }, {
            kod: 'S92',
            falt: ['Skador orsakade av', 'arbetsförmågan är ofta inte nedsatt',
                'Skadorna minskar förmågan', null, 'I arbeten som kan utföras'
            ]
        }, {
            kod: 'S93',
            falt: ['Skador orsakade av', 'arbetsförmågan är ofta ',
                'Skadorna minskar förmågan ', null, null
            ]
        }, {
            kod: 'R09',
            falt: [null, 'Sjukskrivning enbart', null, null, null]
        }, {
            kod: 'O267',
            falt: ['Smärtor i bäckenet och', 'Observandum', 'begränsas förmågan att lyfta', 'Gå, stå sitta', 'Flertalet gravida']
        }, {
            kod: 'N850',
            falt: ['Rekommendationen omfattar operation', 'Komplikationer kan', 'Bukväggskirurgi', null, 'Vid kombinerad laparoskopisk']
        }, {
            kod: 'M753',
            falt: ['Kortvariga besvär', 'De akuta besvären', 'axelbesvär ger ofta så akuta smärtor', null, 'Kalkaxel och impingementsyndrom']
        }, {
            kod: 'M751',
            falt: ['drabbade axelleden', 'rotatorcuffsyndrom', 'axelbesvär ger ofta', null, 'Kalkaxel och impingementsyndrom']
        }, {
            kod: 'R40',
            falt: [null, 'Sjukskrivning enbart utifrån symtom ska undvikas', null, null, null]
        }]
    },
    utanEgenFMBInfo: {
        diagnoser: [{
            kod: 'S830',
            falt: ['För S830 finns inget FMB-stöd', 'Skador orsakade av yttre våld',
                'arbetsförmågan är ofta inte', 'Skadorna minskar förmågan', null,
                'I arbeten som kan utföras mestadels stillasittande'
            ]
        }, {
            kod: 'S831',
            falt: ['För S831 finns inget FMB-stöd', 'Skador orsakade av yttre våld',
                'arbetsförmågan är ofta inte', 'Skadorna minskar förmågan', null,
                'I arbeten som kan utföras mestadels stillasittande'
            ]
        }, {
            kod: 'R010',
            falt: ['För R010 finns inget FMB-stöd', null, 'Sjukskrivning enbart utifrån ', null, null, null]
        }, {
            kod: 'R090',
            falt: ['För R090 finns inget FMB-stöd', null, 'Sjukskrivning enbart utifrån symtom', null, null, null]
        }, {
            kod: 'N210',
            falt: ['För N210 finns inget FMB-stöd', 'Symtomen vid njursten är svåra smärtor', 'Efter akuta pyelonefrit',
                'Vid akuta njurstensanfall', null, 'Vid akut njurstensanfall'
            ]
        }, {
            kod: 'S302',
            falt: ['För S302 finns inget FMB-stöd', 'Skador orsakade av yttre', 'arbetsförmågan är ofta inte',
                'Skadorna minskar förmågan att gå och stå', null, null
            ]
        }, {
            kod: 'F420',
            falt: ['För F420 finns inget FMB-stöd', 'Tvångssyndrom kännetecknas', 'Sjukskrivning kan i',
                'Tvångssyndrom kan medföra', null, 'Sjukskrivning kan i många fall undvikas'
            ]
        }, {
            kod: 'G359',
            falt: ['För G359 finns inget FMB-stöd', 'Multipel skleros', 'Vid sjukdomsdebut', 'Hur MS påverkar', null, 'Vid sjukdomsdebut']
        }]

    },
    utanFMBInfo: {
        diagnoser: [{
            kod: 'H92',
            falt: [{}]
        }, {
            kod: 'I00',
            falt: [{}]
        }, {
            kod: 'N08',
            falt: [{}]
        }, {
            kod: 'M76',
            falt: [{}]
        }, {
            kod: 'O00',
            falt: [{}]
        }, {
            kod: 'A00',
            falt: [{}]
        }]

    }

};
