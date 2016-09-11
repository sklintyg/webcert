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
    ts: require('./testvalues.ts.js'),
    fk: require('./testvalues.fk.js'),
    patienter: [
        '19000119-9801',
        '19000717-9815',
        '19000128-9818'
        // sekretessmarkerad (skyddad identitet)
    ],
    patienterMedSamordningsnummer: [{
        nummer: '200810632398',
        namn: '',
        efternamn: ''
    }, {
        nummer: '196403602391',
        namn: '',
        efternamn: ''
    }],

    patienterMedSekretessmarkering: [{
        nummer: '19420119-9124',
        namn: '',
        efternamn: ''
    }]
};
