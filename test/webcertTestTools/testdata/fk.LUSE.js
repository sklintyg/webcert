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
var fkValues = require('./testvalues.js').fk;
var shuffle = testdataHelper.shuffle;

var today = testdataHelper.dateFormat(new Date());

module.exports = {
  get: function(intygsID) {
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }
    return {
      "id": intygsID,
      "typ": "Läkarutlåtande för sjukersättning",
      "baseratPa": {
        "minUndersokningAvPatienten": "2017-09-27",
        "personligKannedom": "2017-09-27"
      },
      "andraMedicinskaUtredningar": false,
      "diagnos": {
        "diagnoser": [{
          "kod": "D00"
        }],
        "narOchVarStalldesDiagnoserna": "hqÖGiCLIpAÄtk4LÅ",
        "nyBedomning": false
      },
      "sjukdomsForlopp": "wT0e1BYOsxb9q2ZI",
      "funktionsnedsattning": {
        "intellektuell": "Intellektuell funktionsnedsättningstext",
        "kommunikation": 'Ej angivet',
        "koncentration": 'Ej angivet',
        "psykisk": 'Ej angivet',
        "synHorselTal": 'Ej angivet',
        "balansKoordination": 'Ej angivet',
        "annan": 'Ej angivet'
      },
      "aktivitetsbegransning": "åGppJÄrFQRXb3g1C",
      "medicinskbehandling": {},
      "medicinskaForutsattningar": {
        "utecklasOverTid": "gxKkxwT2reHis4EÅ"
      },
      "ovrigt": "Ej angivet",
      "kontaktMedFk": false,
    };
  },
  getRandom: function(intygsID) {
    var nyBedomningDiagnos = testdataHelper.randomTrueFalse();
    var diagnosForNyBedomning;
    if (nyBedomningDiagnos) {
      diagnosForNyBedomning = testdataHelper.randomTextString();
    }
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }

    return {
      id: intygsID,
      typ: 'Läkarutlåtande för sjukersättning',
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
        }],
        narOchVarStalldesDiagnoserna: testdataHelper.randomTextString(),
        nyBedomning: nyBedomningDiagnos,
        diagnosForNyBedomning: diagnosForNyBedomning
      },
      sjukdomsForlopp: testdataHelper.randomTextString(),
      // sjukdomsForlopp: 'Sjukdomsförlopp kommentar',

      funktionsnedsattning: {
        //funktionsnedsattningar
        intellektuell: 'Intelektuell funktionsnedsättningstext',
        kommunikation: 'kommunikation funktionsnedsättningstext',
        koncentration: 'koncentration funktionsnedsättningstext',
        psykisk: 'psykisk funktionsnedsättningstext',
        synHorselTal: 'synHorselTal funktionsnedsättningstext',
        balansKoordination: 'balansKoordination funktionsnedsättningstext',
        annan: 'annan funktionsnedsättningstext'
      },
      aktivitetsbegransning: testdataHelper.randomTextString(),
      medicinskbehandling: {
        avslutad: 'avslutad behandlings-text',
        pagaende: 'pagaende behandlings-text',
        planerad: 'planerad behandlings-text',
        substansintag: 'substansintag behandlings-text'
      },
      medicinskaForutsattningar: {
        utecklasOverTid: testdataHelper.randomTextString(),
        trotsBegransningar: testdataHelper.randomTextString()
      },
      ovrigt: testdataHelper.randomTextString(),
      kontaktMedFk: testdataHelper.randomTrueFalse(),
      // Tillägsfrågor saknas tillsvidare
      tillaggsfragor: [
        // {
        //     svar: testdataHelper.randomTextString()
        // }, {
        //     svar: testdataHelper.randomTextString()
        // }
      ]
    };
  }
};
