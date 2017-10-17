/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
    patient: {
        id: '195206172339',
        fornamn: 'Björn Anders Daniel',
        efternamn: 'Pärsson',
        adress: {
            postadress: 'KUNGSGATAN 5',
            postort: 'GÖTEBORG',
            postnummer: '41234'
        },
        kon: 'man'
    },

    diagnoskoder: {
        'finns i SRS': 'M79',
        'inte finns i SRS': 'A23',
        'har åtgärder': 'M54',
        'har förhöjd risk': '',
        'inte har förhöjd risk': '',
        'saknar prediktion': 'M79',
        'saknar statistik': 'S82',
        'saknar åtgärder': 'S82',
    },

    inloggningar: {
        'med SRS': {
            forNamn: 'Arnold',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-1079',
            enhetId: 'TSTNMT2321000156-1077',
            origin: 'DJUPINTEGRATION'
        },
        'utan SRS': {
            hsaId: 'TST5565594230-106J',
            forNamn: 'Markus',
            efterNamn: 'Gran',
            enhetId: 'IFV1239877878-103D',
            origin: 'DJUPINTEGRATION'
        }
    },

    atgarder: {
        'åtgärdslista 1': [
            'partiell sjukskrivning',
            'FaR med regelbunden styrketräning för att förebygger nya besvär'
        ],
        'åtgärdslista 2': [
            'borsta tänderna',
            'inte vara uppe för länge',
            'inte prata med främlingar.'
        ]
    },

    position: {
        SAMTYCKE: 'samtycke',
        PREDIKTIONSMEDDELANDET: 'prediktionsmeddelandet',
        ATGARDER: 'åtgärder',
        STATISTIK: 'statistik',
    }
};
