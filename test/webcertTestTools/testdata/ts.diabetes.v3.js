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

var testValues = require('./testvalues.js').ts;

module.exports = {
    get: function(intygsID) {
        //a valid hypoglykemidatum must be < today and > 1 year ago.
        var twoDaysAgo = testdataHelper.dateFormat(new Date(new Date().getTime() - 24 * 2 * 60 * 60 * 1000));

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            'id': intygsID,
            'typ': 'Transportstyrelsens läkarintyg diabetes',
            'korkortstyper': ['A1', 'B', 'D', 'DE', 'D1', 'Taxi', 'BE', 'A2', 'Traktor', 'D1E', 'C1', 'A'],
            'identitetStyrktGenom': 'Svenskt körkort',
            'allmant': {
                'year': 1981,
                'typ': 'Typ 1',
                'behandling': {
                    'typer': ['Insulin', 'Annan behandling', 'Tabletter'],
                    'riskForHypoglykemi': 'Ja',
                    'insulinYear': 1986,
                    'annanBehandlingBeskrivning': 'Hypnos behandling'
                }
            },
            'hypoglykemier': {
                'a': 'Nej',
                'b': 'Ja',
                'c': 'Ja',
                'd': 'Nej',
                'e': 'Nej',
                'f': 'Ja',
                'g': 'Ja',
                'h': 'Ja',
                'i': 'Ja',
                'j': 'Ja',
                'hDatum': twoDaysAgo,
                'iDatum': twoDaysAgo,
                'jDatum': twoDaysAgo
            },
            'synfunktion': {
                'a': 'Ja',
                'b': 'Nej',
                'styrkor': testValues.getRandomStyrka()
            },
            'kommentar': 'wrlTWNKA08ÖePpBB',
            'specialist': '4YråJOäqkö2YxRRd',
            'bedomning': {
                'lamplig': 'Ja',
                'borUndersokasBeskrivning': 'Tjillevippen',
                'behorigheter': ['A1', 'B', 'D', 'DE', 'D1', 'Taxi', 'BE', 'A2', 'Traktor', 'D1E', 'C1', 'A']
            }
        };
    }
};
