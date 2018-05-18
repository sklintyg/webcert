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

'use strict';

module.exports = {
    fmbInfo: {
        diagnoser: [{
            kod: 'S83',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld mot kroppens extremiteter',
            generellInfo: 'arbetsförmågan är ofta inte nedsatt någon längre tid ',
            funktionsnedsattning: 'Skadorna minskar förmågan att gå och stå',
            aktivitetsbegransning: null,
            beslutsunderlag: 'luxation och distorsion i höftled och höftligament'

        }, {
            kod: 'S832',
            symptomPrognosBehandling: 'Meniskskador kan uppkomma',
            generellInfo: 'Akuta besvär efter en meniskskada',
            funktionsnedsattning: 'upphakningar i knäleden',
            aktivitetsbegransning: 'Påverkad gångförmåga',
            beslutsunderlag: 'meniskruptur, aktuell med degeneration av brosk och/eller menisk i knä'

        }, {
            kod: 'S835',
            symptomPrognosBehandling: 'Meniskskador kan uppkomma efter trauma',
            generellInfo: 'Akuta besvär efter en meniskskada',
            funktionsnedsattning: 'Meniskskador kan ge upphakningar',
            aktivitetsbegransning: 'Påverkad gångförmåga',
            beslutsunderlag: 'distorsion engagerande knäets korsband och utförd åtgärd operation av korsband i knä med artroskopi'

        }, {
            kod: 'S90',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'skador på nedre extremiteten (frakturer, luxationer, distorsioner, sårskador)'

        }, {
            kod: 'S92',
            symptomPrognosBehandling: 'Skador orsakade av',
            generellInfo: 'arbetsförmågan är ofta inte nedsatt',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'lindrig fraktur på ländkotpelaren och bäckene, lårben, underben'

        }, {
            kod: 'S93',
            symptomPrognosBehandling: 'Skador orsakade av',
            generellInfo: 'arbetsförmågan är ofta ',
            funktionsnedsattning: 'Skadorna minskar förmågan ',
            aktivitetsbegransning: null,
            beslutsunderlag: 'luxation och distorsion i höftled och höftligament, knäets leder och ligament'

        }, {
            kod: 'O267',
            symptomPrognosBehandling: 'rygg eller bäcken med debut under aktuell graviditet',
            generellInfo: 'Observandum',
            funktionsnedsattning: 'begränsas förmågan att lyfta',
            aktivitetsbegransning: 'Gå, stå, sitta',
            beslutsunderlag: 'medelsvår graviditetsrelaterad rygg- och bäckensmärta (inklusive symfyseolys)'
        }, {
            kod: 'N850',
            symptomPrognosBehandling: 'Rekommendationen omfattar operation',
            generellInfo: 'Komplikationer kan',
            funktionsnedsattning: 'Bukväggskirurgi',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid kombinerad laparoskopisk/vaginal hysterektomi och fysiskt tyngre arbeten'
        }, {
            kod: 'M753',
            symptomPrognosBehandling: 'Kortvariga besvär',
            generellInfo: 'De akuta besvären',
            funktionsnedsattning: 'axelbesvär ger ofta så akuta smärtor',
            aktivitetsbegransning: null,
            beslutsunderlag: 'tendinit med förkalkning i skulderled där arbetsbelastningen innefattar arbete som huvudsakligen görs med armarna'
        }, {
            kod: 'M751',
            symptomPrognosBehandling: 'drabbade axelleden',
            generellInfo: 'rotatorcuffsyndrom',
            funktionsnedsattning: 'axelbesvär ger ofta',
            aktivitetsbegransning: null,
            beslutsunderlag: 'cuff-syndrom i skulderled och utförd åtgärd rekonstruktion av rotatorkuff med sutur'
        }]
    },
    utanEgenFMBInfo: {
        diagnoser: [{
            kod: 'S830',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'knäets leder och ligament eller leder och ligament på fotleds- och fotnivå där arbetsbelastningen innefattar krav på kroppslig rörlighet och belastning rekommenderas sjukskrivning upp till',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden S83',
            overliggandeDiagnos: 'S83'

        }, {
            kod: 'S831',
            symptomPrognosBehandling: 'Skador orsakade av yttre våld',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan',
            aktivitetsbegransning: null,
            beslutsunderlag: 'knäets leder och ligament eller leder och ligament på fotleds- och fotnivå där arbetsbelastningen innefattar krav på kroppslig rörlighet och belastning rekommenderas sjukskrivning upp till',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden S83',
            overliggandeDiagnos: 'S83'

        }, {
            kod: 'N210',
            symptomPrognosBehandling: 'Symtomen vid njursten är svåra smärtor',
            generellInfo: 'Efter akuta pyelonefrit',
            funktionsnedsattning: 'Vid akuta njurstensanfall',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid njursten och utförd åtgärd avlägsnande av urinvägssten rekommenderas sjukskrivning upp till ',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden N21',
            overliggandeDiagnos: 'N21'
        }, {
            kod: 'S302',
            symptomPrognosBehandling: 'Skador orsakade av yttre',
            generellInfo: 'arbetsförmågan är ofta inte',
            funktionsnedsattning: 'Skadorna minskar förmågan att gå och stå',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid skador på nedre extremiteten (frakturer, luxationer, distorsioner, sårskador)',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden S30',
            overliggandeDiagnos: 'S30'
        }, {
            kod: 'F420',
            symptomPrognosBehandling: 'Tvångssyndrom kännetecknas',
            generellInfo: 'Det finns en spännvidd för hur en given sjukdom påverkar olika individers arbetsförmåga och förmåga att utföra olika aktiviteter',
            funktionsnedsattning: 'Tvångssyndrom karakteriseras av problem med tvångstankar',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid lindrigt till medelsvårt tvångssyndrom kan sjukskrivning i många fall undvikas',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden F42',
            overliggandeDiagnos: 'F42'
        }, {
            kod: 'G359',
            symptomPrognosBehandling: 'Multipel skleros',
            generellInfo: 'Vid sjukdomsdebut',
            funktionsnedsattning: 'Hur MS påverkar',
            aktivitetsbegransning: null,
            beslutsunderlag: 'Vid primär progressiv multipel skleros eller sekundär progressiv multipel skleros krävs ofta en längre tids sjukskrivning',
            overliggandeTxt: 'Det FMB-stöd som visas är för koden G35',
            overliggandeDiagnos: 'G35'
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
