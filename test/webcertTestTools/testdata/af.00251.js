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
    get: function (intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Läkarintyg för deltagare i arbetsmarknadspolitiska program',
            minUndersokning: {
                checked: true,
                datum: "2014-01-03"
            },
            annanUndersokning: {
                checked: true,
                datum: "2014-11-19",
                text: "Annan undersökning"
            },
            arbetsmarknadspolitisktProgram: {
                text: "Aktiviteters som ingår i programmet",
                radio: "DELTID",
                deltidText: 20
            },
            funktionsNedsattning: "Ont i armen.",
            aktivitetsBegransning: "Patienten kan knapp lyfta en kopp kaffe.",
            harForhinder: true,
            sjukfranvaro: [{
                checked: true,
                from: "2014-08-12",
                tom: "2014-08-19",
                niva: 100
            }, {
                checked: false,
                from: "2014-08-20",
                tom: "2014-08-27",
                niva: 13
            }, {
                checked: false,
                from: "2014-08-28",
                tom: "2014-09-04",
                niva: 94
            }, {
                checked: true,
                from: "2014-09-05",
                tom: "2014-09-12",
                niva: 50
            }],
            begransningSjukfranvaro: {
                value: true,
                text: "Om patienten får en assistent kan man förkorta perioden med några dagar."
            },
            prognosAtergang: {
                radio: "ATERGA_MED_ANPASSNING",
                text: "Patienten behöver nya hörlurar för att kunna lyssna på musik effektivare."
            }
        };

    },
    getRandom: function (intygsID) {
        function randomNumber(start, end) {
            if (start == null && end == null) {
                start = 0;
                end = 100;
            }
            return Math.round(start + Math.random() * (end - start))
        }

        function randomDate(start, end) {
            if (start == null && end == null) {
                start = new Date();
                start.setFullYear(start.getFullYear() - 5);
                end = new Date();
            }
            return testdataHelper.dateFormat(
                new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime())));
        }

        function randomizeValue(value) {
            if (testdataHelper.randomTrueFalse()) {
                return value;
            } else {
                return undefined;
            }
        }

        function addDays(date, days) {
            var result = new Date(date);
            result.setDate(result.getDate() + days);
            return result;
        }

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        var obj = {
            id: intygsID,
            typ: 'Läkarintyg för deltagare i arbetsmarknadspolitiska program',
            minUndersokning: {
                checked: true,
                datum: randomizeValue(randomDate())
            },
            annanUndersokning: {
                checked: testdataHelper.randomTrueFalse(),
                datum: randomizeValue(randomDate()),
                text: testdataHelper.randomTextString(5, 50)
            },
            arbetsmarknadspolitisktProgram: {
                text: testdataHelper.randomTextString(5, 50),
                radio: testdataHelper.shuffle(['HELTID', 'DELTID', 'OKAND'])[0],
                deltidText: randomNumber(1, 39)
            },
            funktionsNedsattning: testdataHelper.randomTextString(5, 50),
            aktivitetsBegransning: testdataHelper.randomTextString(5, 50),
            harForhinder: testdataHelper.randomTrueFalse(),
            sjukfranvaro: [],
            begransningSjukfranvaro: {
                value: testdataHelper.randomTrueFalse(),
                text: testdataHelper.randomTextString(5, 50)
            },
            prognosAtergang: {
                radio: testdataHelper.shuffle(['ATERGA_UTAN_ANPASSNING', 'KAN_EJ_ATERGA',
                    'EJ_MOJLIGT_AVGORA', 'ATERGA_MED_ANPASSNING'])[0],
                text: testdataHelper.randomTextString(5, 50)
            }
        };

        if (obj.harForhinder) {
            var rows = randomNumber(1, 4);
            var lastTom = randomDate();
            for (var i = 0; i < rows; i++) {
                var from = addDays(lastTom, 1);
                var tom = addDays(from, 7);

                var fromString = testdataHelper.dateFormat(from);
                var tomString = testdataHelper.dateFormat(tom);
                var row = {
                    checked: true,
                    from: fromString,
                    tom: tomString,
                    niva: 100
                };
                if (i > 0) {
                    row.checked = testdataHelper.randomTrueFalse();
                    row.niva = randomNumber(1, 99);
                }
                obj.sjukfranvaro.push(row);

                lastTom = tom;
            }
        }

        return obj;
    }
    ,

}
;
