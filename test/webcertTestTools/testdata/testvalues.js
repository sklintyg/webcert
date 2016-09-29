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
var shuffle = require('./../helpers/testdataHelper.js').shuffle;
module.exports = {
    ts: require('./testvalues.ts.js'),
    fk: require('./testvalues.fk.js'),
    patienter: [
        '19000119-9801',
        '19000717-9815',
        '19000128-9818'
    ],
    patienterMedSamordningsnummer: [{
        nummer: '19440178-6530',
        namn: '',
        efternamn: ''
    }],
    patienterMedSamordningsnummerEjPU: [{
        nummer: '20081063-2398 ',
        namn: '',
        efternamn: ''
    }],

    patienterMedSekretessmarkering: [{
        nummer: '19420119-9124',
        namn: '',
        efternamn: ''
    }],

    enhetsAdress: function() {
        return {
            gata: shuffle(['Bryggaregatan 1', 'Svampstigen 2'])[0],
            postnummer: shuffle(['655 91', '655 90'])[0],
            postadress: shuffle(['Karlstad', 'Säffle'])[0],
            telefon: shuffle(['0705121314', '054121314'][0])


        };
    }


};
