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
            ovrigt: 'Övriga upplysningar'
        };
    },
    getRandom: function(intygsID) {
        var harFunktionsnedsattning, funktionsnedsattning;
        var harAktivitetsbegransning, aktivitetsbegransning;
        var harUtredningBehandling, utredningBehandling;
        var harArbetetsPaverkan, arbetetsPaverkan;

        harFunktionsnedsattning = testdataHelper.randomTrueFalse();
        if (harFunktionsnedsattning) {
            funktionsnedsattning = testdataHelper.randomTextString();
            harAktivitetsbegransning = testdataHelper.randomTrueFalse();
            if (harAktivitetsbegransning) {
                aktivitetsbegransning = testdataHelper.randomTextString();
            }
        }

        harUtredningBehandling = testdataHelper.randomTrueFalse();
        if (harUtredningBehandling) {
            utredningBehandling = testdataHelper.randomTextString();
        }

        harArbetetsPaverkan = testdataHelper.randomTrueFalse();
        if (harArbetetsPaverkan) {
            arbetetsPaverkan = testdataHelper.randomTextString();
        }

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        if (harFunktionsnedsattning === false) {
            return {
                id: intygsID,
                typ: 'Arbetsförmedlingens medicinska utlåtande',
                funktionsnedsattning: {
                    val: testdataHelper.boolTillJaNej(harFunktionsnedsattning),
                    text: funktionsnedsattning
                },
                aktivitetsbegransning: {
                    val: undefined,
                    text: undefined
                },
                utredningBehandling: {
                    val: testdataHelper.boolTillJaNej(harUtredningBehandling),
                    text: utredningBehandling
                },
                arbetetsPaverkan: {
                    val: testdataHelper.boolTillJaNej(harArbetetsPaverkan),
                    text: arbetetsPaverkan
                },
                ovrigt: testdataHelper.randomTextString()
            };
        }

        return {
            id: intygsID,
            typ: 'Arbetsförmedlingens medicinska utlåtande',
            funktionsnedsattning: {
                val: testdataHelper.boolTillJaNej(harFunktionsnedsattning),
                text: funktionsnedsattning
            },
            aktivitetsbegransning: {
                val: testdataHelper.boolTillJaNej(harAktivitetsbegransning),
                text: aktivitetsbegransning
            },
            utredningBehandling: {
                val: testdataHelper.boolTillJaNej(harUtredningBehandling),
                text: utredningBehandling
            },
            arbetetsPaverkan: {
                val: testdataHelper.boolTillJaNej(harArbetetsPaverkan),
                text: arbetetsPaverkan
            },
            ovrigt: testdataHelper.randomTextString()
        };
    }
};
