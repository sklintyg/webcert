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
//var shuffle = testdataHelper.shuffle;

var testValues = require('./testvalues.js').ts;

module.exports = {
  get: function(intygsID) {

    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();

    }
    return {
      'id': intygsID,
      'typ': 'Läkares anmälan till Transportstyrelsen',
      'identitetStyrktGenom': 'Svenskt körkort',
      'anmalanAvser': 'OLAMPLIGHET',
      'medicinskaForhallanden': 'Sjuk men hyfsat alert i benstommen.',
      'senasteUndersokningsdatum': '2019-01-01',
      'intygetAvserBehorigheter': [
        'Alla behörigheter inkl. körkortstillstånd, traktorkort och taxiförarlegitimation'
      ],
      informationOmTsBeslutOnskas: true
    };
  }
};
