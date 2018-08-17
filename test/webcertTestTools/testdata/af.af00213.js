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
//var shuffle = testdataHelper.shuffle;

//var testValues = require('./testvalues.js').ts;

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            "id": intygsID,
            "typ": "Arbetsförmedlingens medicinska utlåtande",
            "funktionsnedsattning": "funktionsnedsattning text",
            "aktivitetsbegransning": "aktivitetsbegransning text",
            "utredningBehandling": "utredningBehandling text",
            "arbetetsPaverkan": "arbetetsPaverkan",
            "ovrigt": "Övrigt kommentar"
        };
    },
    getRandom: function(intygsID, patient) {

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        let obj = {
            id: intygsID,
            typ: 'Arbetsförmedlingens medicinska utlåtande',
            "funktionsnedsattning": testdataHelper.shuffle([testdataHelper.randomTextString(5, 1000), false])[0],
            "arbetetsPaverkan": testdataHelper.shuffle([testdataHelper.randomTextString(5, 1000), false])[0],
            ovrigt: testdataHelper.randomTextString(5, 1000)
        };

        if (obj.funktionsnedsattning !== false) {
            obj.aktivitetsbegransning = testdataHelper.shuffle([testdataHelper.randomTextString(5, 1000), false])[0];
        }

        if (Math.random() > 0.5) {
            obj.utredningBehandling = testdataHelper.shuffle([testdataHelper.randomTextString(5, 1000), false])[0];
        }

        return obj;
    }
};
