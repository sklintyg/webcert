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
  get: function(intygsId) {

    if (!intygsId) {
      intygsId = testdataHelper.generateTestGuid();
    }

    return {
      'id': intygsId,
      'typ': 'Transportstyrelsens läkarintyg diabetes',

      'intygetAvserKategorier': ['B', 'Taxi'],
      'identitetStyrktGenom': 'Svenskt körkort',

      'allmant': {
        'patientenFoljsAv': 'Primärvård',
        'diabetesDiagnosAr': '2020',
        'typAvDiabetes': 'Annan',
        'beskrivningAnnanTyp': 'Ovanlig typ 3 diabetes',
        'medicinering': 'Ja',
        'medicineringHypoglykemi': 'Ja',
        'behandling': ['Insulin', 'Tabletter', 'Annan'],
        'vilkenAnnanBehandling': 'Varma strumpor',
        'medicineringHypoglykemiTidpunkt': '2021-11-09'
      },

      'hypoglykemi': {
        'kontrollSjukdomstillstand': 'Nej',
        'kontrollSjukdomstillstandVarfor': 'Svårbehandlad typ 3 diabetes',
        'forstarRisker': 'Ja',
        'kannaVarningstecken': 'Ja',
        'adekvataAtgarder': 'Ja',
        'aterkommandeSenasteTolv': 'Ja',
        'aterkommandeSenasteTolvTidpunkt': '2021-11-09',
        'kontrollerasRegelbundet': 'Ja',
        'trafiksakerhetsrisk': 'Ja',
        'senasteTolvVaket': 'Ja',
        'senasteTre': 'Ja',
        'senasteTreTidpunkt': '2021-11-09',
        'allvarligSenasteTolv': 'Ja',
        'allvarligSenasteTolvTidpunkt': '2021-11-09',
        'blodsockerkontroller': 'Ja'
      },

      'ovrigt': {
        'komplikationer': 'Ja',
        'komplikationerVilka': 'Kalla fötter gör det svårt att panikbromsa',
        'undersokasSpecialistkompetens': 'Svårbehandlad typ 3 diabetes'
      },

      'bedomning': {
        'uppfyllerKrav': ['B', 'Taxi'],
        'ovrigaKommentarer': 'Det kan vara ganska kallt när det är vinter'
      }
    };
  }
};
