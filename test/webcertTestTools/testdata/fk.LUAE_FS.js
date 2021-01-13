/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
    return {
      "id": intygsID,
      "typ": "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång",
      "baseratPa": {
        "minUndersokningAvPatienten": "2017-09-27",
        "personligKannedom": "2017-09-27"
      },
      "diagnos": {
        "diagnoser": [{
          "kod": "Z720B"
        }]
      },
      "funktionsnedsattning": {
        "debut": "TTIÅhTZFFÄHjLbOk",
        "paverkan": "Ö502ZH0bVTLSåijx"
      },
      "ovrigt": "Ej angivet",
      "kontaktMedFk": false
    };
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
        annatBeskrivning: testdataHelper.randomTextString(2, 5) /*3500*/,
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
        debut: testdataHelper.randomTextString(2, 5) /*3500*/,
        paverkan: testdataHelper.randomTextString(2, 5) /*3500*/
      },
      ovrigt: testdataHelper.randomTextString(2, 5) /*3500*/,
      kontaktMedFk: shuffle([false, {
        motivering: testdataHelper.randomTextString(2, 5) /*3500*/
      }])
    };
  }
};
