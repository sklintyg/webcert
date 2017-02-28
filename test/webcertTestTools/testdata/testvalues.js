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
        id: '19000119-9801' // Har adress i PU
    }, {
        id: '19000717-9815' // Har adress i PU
    }, {
        id: '19000128-9818',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900424-2385',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900424-2393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900425-2384',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900425-2392',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900426-2383',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900426-2391',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900427-2382',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900427-2390',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900428-2381',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19900428-2399',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19990810-2388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19990811-2395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991225-2393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991214-2388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991215-2395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991216-2386',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991217-2393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991218-2384',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991219-2391',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991220-2380',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991221-2397',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991222-2388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }, {
        id: '19991223-2395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'

        }
    }],
    patienterMedSamordningsnummer: [{
        id: '19440178-6530',
        namn: '',
        efternamn: ''
    }],
    patienterMedSamordningsnummerEjPU: [{
        id: '20081063-2398 ',
        namn: '',
        efternamn: ''
    }],

    patienterMedSekretessmarkering: [{
        id: '19420119-9124',
        namn: '',
        efternamn: ''
    }],

    patienterEjPU: [{
        id: '20121212-1212',
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
