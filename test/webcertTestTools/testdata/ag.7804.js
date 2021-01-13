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

module.exports = {
  get: function(intygsID, smittskydd) {
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }
    if (smittskydd) {
      return {
        "id": intygsID,
        "typ": "ag7804",
        "smittskydd": true,
        "baseratPa": {
          "minUndersokningAvPatienten": "2017-09-27"
        },
        "diagnos": {
          "onskarFormedlaDiagnos": false
        },
        "arbetsformaga": {
          "nedsattMed25": {
            "from": "2017-09-27",
            "tom": "2017-10-02"
          }
        },
        "arbetstidsforlaggning": {
          "val": "Nej"
        },
        "ovrigt": "EhRgOSC9psäJcÅjt"
      };
    }
    return {
      "id": intygsID,
      "typ": "ag7804",
      "smittskydd": false,
      "nuvarandeArbeteBeskrivning": "iÅI3WYZMFe8ÖV5Ox",
      "baseratPa": {
        "minUndersokningAvPatienten": "2017-09-27",
      },
      "sysselsattning": {
        "typ": "ARBETSSOKANDE",
        "translated": "Arbetssökande - att utföra sådant arbete som är normalt förekommande på arbetsmarknaden"
      },

      "diagnos": {
        "onskarFormedlaDiagnos": true,
        "rows": [{
          "kod": "Z413"
        }]
      },
      "medicinskbehandling": {
        "planerad": "IawIÖIdU8sCfä7Uq",
        "pagaende": "cnEYDuNuqWBd59åY"
      },
      "funktionsnedsattning": "3wexbyEqÄsoxaWWl",
      "aktivitetsbegransning": "Wpnv0SMoElXnTJXh",
      "arbetsformaga": {
        "nedsattMed25": {
          "from": "2017-09-27",
          "tom": "2017-10-02"
        }
      },
      "arbetstidsforlaggning": {
        "val": "Ja",
        "beskrivning": "pB8TPLe3JZÄSV0YN"
      },
      "arbetsformagaFMB": "WewXÄUgv06gåOmÄH",
      "resorTillArbete": true,
      "atgarder": [{
        "namn": "Besök på arbetsplatsen",
        "beskrivning": "",
        "key": "BESOK_ARBETSPLATS"
      }],
      "prognosForArbetsformaga": {
        "name": "STOR_SANNOLIKHET"
      },
      "kontaktMedAg": false,
      "ovrigt": "iÄ0f0cÖ7NY0JEa1Ä"
    };
  }
};
