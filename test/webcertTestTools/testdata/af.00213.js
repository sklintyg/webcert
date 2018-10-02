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

var testdataHelper = require('common-testtools').testdataHelper;

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Arbetsförmedlingens medicinska utlåtande',
            funktionsnedsattning: {
                val: 'Ja',
                text: 'Typ av funktionsnedsättning'
            },
            aktivitetsbegransning: {
                val: 'Ja',
                text: 'Typ av aktivitetsbegransning'
            },
            utredningBehandling: {
                val: 'Ja',
                text: 'Typ av utredning/behandling'
            },
            arbetetsPaverkan: {
                val: 'Ja',
                text: 'Vilken påverkan har arbetet?'
            },
            skipparBalte: {
                val: 'Ja',
                text: 'Vilken påverkan har arbetet?'
            },
            ovrigt: 'Övriga upplysningar'
        };
    },
    getRandom: function(intygsID) {
        function slumpaValOchText() {
            return testdataHelper.shuffle([{
                val: 'Ja',
                text: testdataHelper.randomTextString(5, 1000)
            }, {
                val: 'Nej'
            }][0]);
        }

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        var obj = {
            id: intygsID,
            typ: 'Arbetsförmedlingens medicinska utlåtande',
            funktionsnedsattning: slumpaValOchText(),
            arbetetsPaverkan: slumpaValOchText(),
            utredningBehandling: slumpaValOchText(),
            skipparBalte: slumpaValOchText(),
            ovrigt: testdataHelper.randomTextString(5, 1000)
        };

        if (obj.funktionsnedsattning.val === 'Ja') {
            obj.aktivitetsbegransning = slumpaValOchText();
        }

        return obj;
    }
};
