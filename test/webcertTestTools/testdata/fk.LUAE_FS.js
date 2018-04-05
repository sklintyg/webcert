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
var fkValues = require('./testvalues.js').fk;

var today = testdataHelper.dateFormat(new Date());

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {"id":intygsID,"typ":"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång",
            "baseratPa":{"minUndersokningAvPatienten":"2017-09-27","journaluppgifter":"2017-09-27","anhorigsBeskrivning":"2017-09-27","annat":"2017-09-27","annatBeskrivning":"ÄMk9NcgukFxTMaAn","personligKannedom":"2017-09-27"},
            "andraMedicinskaUtredningar":[{"underlag":"Neuropsykiatriskt utlåtande","datum":"2016-04-09","infoOmUtredningen":"mÄwwO67piLrbeåID"}],
            "diagnos":{"diagnoser":[{"kod":"Z720B","bakgrund":"gwskchi5p1LmåzHÖ"}]},
            "funktionsnedsattning":{"debut":"TTIÅhTZFFÄHjLbOk","paverkan":"Ö502ZH0bVTLSåijx"},
            "ovrigt":"åbw0KhHaTjcQgzbÅ",
            "kontaktMedFk":false};
    },
    getRandom: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            id: intygsID,
            typ: 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',

            baseratPa: {
                minUndersokningAvPatienten: today,
                journaluppgifter: today,
                anhorigsBeskrivning: today,
                annat: today,
                annatBeskrivning: testdataHelper.randomTextString(),
                personligKannedom: today
            },
            andraMedicinskaUtredningar: fkValues.getRandomMedicinskaUtredningar(),

            diagnos: {
                diagnoser: [{
                    kod: shuffle(fkValues.ICD10)[0],
                    bakgrund: testdataHelper.randomTextString()
                }]
            },
            funktionsnedsattning: {
                //funktionsnedsattningar
                debut: testdataHelper.randomTextString(),
                paverkan: testdataHelper.randomTextString()
            },
            ovrigt: testdataHelper.randomTextString(),
            kontaktMedFk: testdataHelper.randomTrueFalse()
        };
    }
};
