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
var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;

module.exports = {
    ts: require('./testvalues.ts.js'),
    fk: require('./testvalues.fk.js'),
    patienter: [{
        id: '190001199801', // Har adress i PU
		kon: 'kvinna'
    }, {
        id: '190007179815', // Har adress i PU
		kon: 'man'
    }, {
        id: '190001289818',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199004242385',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004242393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199004252384',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199004252392',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199004262383',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199004262391',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199004272382',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199004272390',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199004282381',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199004282399',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199908102388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199908112395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912252393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912142388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199912152395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912162386',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199912172393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912182384',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199912192391',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912202380',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199912212397',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }, {
        id: '199912222388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'kvinna'
    }, {
        id: '199912232395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
		kon: 'man'
    }],
    patienterMedSamordningsnummer: [{
        id: '194401786530',
        namn: '',
        efternamn: ''
    }],
    patienterMedSamordningsnummerEjPU: [{
        id: '200810632398 ',
        namn: '',
        efternamn: ''
    }],

    patienterMedSekretessmarkering: [{
        id: '194201199124',
        namn: '',
        efternamn: ''
    }],
    patienterAvlidna: [{
        id: '190001309814',
        namn: '',
        efternamn: ''
    }],
    patienterEjPU: [{
        id: '201212121212',
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
