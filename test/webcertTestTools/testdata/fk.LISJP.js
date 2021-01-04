/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

function getRandomSysselsattning() {
  return shuffle([{
    typ: 'NUVARANDE_ARBETE',
    yrkesAktiviteter: testdataHelper.randomTextString()
  }, {
    typ: 'ARBETSSOKANDE'
  }, {
    typ: 'FORALDRALEDIG'
  }, {
    typ: 'STUDIER'
  }])[0];
}

function getRandomArbetstidsforlaggning(arbetsformaga) {
  if (arbetsformaga.nedsattMed25 || arbetsformaga.nedsattMed50 || arbetsformaga.nedsattMed75) {
    return shuffle([{
      val: 'Ja',
      beskrivning: testdataHelper.randomTextString()
    }, {
      val: 'Nej'
    }])[0];
  }
  return null;
}

function getRandomAtgarder() {
  var atgarder = [{
    namn: 'Arbetsträning',
    beskrivning: 'Arbetsträning-beskrivning',
    key: 'ARBETSTRANING'
  }, {
    namn: 'Arbetsanspassning',
    beskrivning: 'Arbetsanspassning-beskrivning',
    key: 'ARBETSANPASSNING'
  }, {
    namn: 'Söka nytt arbete',
    beskrivning: 'Söka nytt arbete-beskrivning',
    key: 'SOKA_NYTT_ARBETE'
  }, {
    namn: 'Besök på arbetsplatsen',
    beskrivning: 'Besök på arbetsplatsen-beskrivning',
    key: 'BESOK_ARBETSPLATS'
  }, {
    namn: 'Ergonomisk bedömning',
    beskrivning: 'Ergonomisk bedömning-beskrivning',
    key: 'ERGONOMISK'
  }, {
    namn: 'Hjälpmedel',
    beskrivning: 'Hjälpmedel-beskrivning',
    key: 'HJALPMEDEL'
  }, {
    namn: 'Konflikthantering',
    beskrivning: 'Konflikthantering-beskrivning',
    key: 'KONFLIKTHANTERING'
  }, {
    namn: 'Kontakt med företagshälsovård',
    beskrivning: 'Kontakt med företagshälsovård-beskrivning',
    key: 'KONTAKT_FHV'
  }, {
    namn: 'Omfördelning av arbetsuppgifter',
    beskrivning: 'Omfördelning av arbetsuppgifter-beskrivning',
    key: 'OMFORDELNING'
  }, {
    namn: 'Övrigt',
    beskrivning: 'Övrigt-beskrivning',
    key: 'OVRIGA_ATGARDER'
  }];
  var randomLength = Math.floor(Math.random() * 3) + 1;

  // 33% chans för inte aktuellt
  return shuffle([
    [{
      namn: 'Inte aktuellt',
      key: 'EJ_AKTUELLT'
    }], shuffle(atgarder).slice(0, randomLength),
    shuffle(atgarder).slice(0, randomLength)
  ])[0];
}

function getRandomPrognosForArbetsformaga() {
  return shuffle([{
    name: 'STOR_SANNOLIKHET'
  }, {
    name: 'SANNOLIKT_INTE'
  }, {
    name: 'PROGNOS_OKLAR'
  },
    {
      name: 'ATER_X_ANTAL_DGR',
      within: shuffle(['1 månad', '2 månader', '3 månader'])[0]
    }
  ])[0];
}

module.exports = {
  get: function(intygsID, smittskydd) {
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }
    if (smittskydd) {
      return {
        "id": intygsID,
        "typ": "Läkarintyg för sjukpenning",
        "smittskydd": true,
        "baseratPa": {
          "minUndersokningAvPatienten": "2017-09-27"
        },
        "diagnos": {
          "kod": "D00"
        },
        "arbetsformaga": {
          "nedsattMed100": {
            "from": "2017-10-18",
            "tom": "2017-10-27"
          }
        },
        ovrigt: 'Ej angivet'
      };
    }
    return {
      "id": intygsID,
      "typ": "Läkarintyg för sjukpenning",
      "smittskydd": false,
      "baseratPa": {
        "minUndersokningAvPatienten": "2017-09-27"
      },
      "sysselsattning": {
        "typ": "ARBETSSOKANDE"
      },
      "diagnos": {
        "kod": "Z413"
      },
      "funktionsnedsattning": "3wexbyEqÄsoxaWWl",
      "aktivitetsbegransning": "Wpnv0SMoElXnTJXh",
      "medicinskbehandling": {},
      "arbetsformaga": {
        "nedsattMed50": {
          "from": "2017-10-03",
          "tom": "2017-10-07"
        }
      },
      "arbetstidsforlaggning": {
        "val": "Nej"
      },
      "resorTillArbete": true,
      "atgarder": [{
        "namn": "Besök på arbetsplatsen",
        "beskrivning": "",
        "key": "BESOK_ARBETSPLATS"
      }],
      "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
      },
      "kontaktMedFk": false,
      "ovrigt": "Ej angivet"
    };
  },
  getRandom: function(intygsID, smittskydd) {
    var arbetsformaga = fkValues.getRandomArbetsformaga();
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }

    if (smittskydd) {
      return {
        id: intygsID,
        typ: 'Läkarintyg för sjukpenning',

        smittskydd: true,
        baseratPa: {
          minUndersokningAvPatienten: today,
          journaluppgifter: today,
          telefonkontakt: today,
          annat: today,
          annatBeskrivning: testdataHelper.randomTextString()
        },

        diagnos: {
          kod: shuffle(fkValues.ICD10)[0],
          bakgrund: testdataHelper.randomTextString()
        },
        arbetsformaga: arbetsformaga,
        arbetstidsforlaggning: getRandomArbetstidsforlaggning(arbetsformaga),
        ovrigt: testdataHelper.randomTextString()
      };
    }

    return {
      id: intygsID,
      typ: 'Läkarintyg för sjukpenning',

      smittskydd: false,
      nuvarandeArbeteBeskrivning: testdataHelper.randomTextString(),
      baseratPa: {
        minUndersokningAvPatienten: today,
        journaluppgifter: today,
        telefonkontakt: today,
        annat: today,
        annatBeskrivning: testdataHelper.randomTextString()
      },
      sysselsattning: getRandomSysselsattning(),
      diagnos: {
        kod: shuffle(fkValues.ICD10)[0],
        bakgrund: testdataHelper.randomTextString()
      },
      medicinskbehandling: {
        planerad: testdataHelper.randomTextString(),
        pagaende: testdataHelper.randomTextString()
      },
      funktionsnedsattning: testdataHelper.randomTextString(),
      aktivitetsbegransning: testdataHelper.randomTextString(),
      arbetsformaga: arbetsformaga,
      arbetstidsforlaggning: getRandomArbetstidsforlaggning(arbetsformaga),
      arbetsformagaFMB: testdataHelper.randomTextString(),
      resorTillArbete: testdataHelper.randomTrueFalse(),
      atgarder: getRandomAtgarder(),
      prognosForArbetsformaga: getRandomPrognosForArbetsformaga(),
      kontaktMedFk: testdataHelper.randomTrueFalse(),
      ovrigt: testdataHelper.randomTextString(),
      tillaggsfragor: [{
        id: 9001,
        svar: testdataHelper.randomTextString()
      }, {
        id: 9002,
        svar: testdataHelper.randomTextString()
      }]
    };
  }
};
