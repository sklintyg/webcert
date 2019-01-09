/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Arbetsgivarintyg dag 1-14',
            sysselsattning: {
                text: 'Beskrivning av nuvarande arbete.'
            },
            onskarFormedlaDiagnos: {
                no: 'Nej',
                yes: 'Ja',
                diagnoser: ['J22', 'J301', 'F110']
            },
            nedsattArbetsformaga: {
                text: 'Beskrivning av nedsatt arbetsförmåga.',
                yes: 'Ja',
                no: 'Nej',
                formaga: 'Kan utföra lättare uppgifter.'
            },
            bedomning: {
                sjukskrivningsgrad: '71%',
                from: '2018-10-01',
                tom: '2018-10-12'
            },
            ovrigt: 'Övriga upplysningar'
        };
    }
};
