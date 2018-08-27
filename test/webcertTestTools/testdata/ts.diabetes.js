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
var shuffle = testdataHelper.shuffle;

var testValues = require('./testvalues.js').ts;

module.exports = {
    get: function(intygsID) {
        //a valid hypoglykemidatum must be < today and > 1 year ago.
        var yesterday = testdataHelper.dateFormat(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            "id": intygsID,
            "typ": "Transportstyrelsens läkarintyg, diabetes",
            "korkortstyper": ["A1", "B", "D", "DE", "D1", "Taxi", "BE", "A2", "Traktor", "D1E", "C1", "A"],
            "identitetStyrktGenom": "Försäkran enligt 18 kap. 4§",
            "allmant": {
                "year": 1981,
                "typ": "Typ 1",
                "behandling": {
                    "typer": ["Insulin"],
                    "insulinYear": 1986
                },
                "annanbehandling": "Hypnos behandling"
            },
            "hypoglykemier": {
                "a": "Nej",
                "b": "Ja",
                "f": "Ja",
                "g": "Ja",
                "gDatum": yesterday,
                "c": "Ja",
                "d": "Nej",
                "e": "Nej"
            },
            "synintyg": {
                "a": "Ja"
            },
            "kommentar": "wrlTWNKA08ÖePpBB",
            "specialist": "4YråJOäqkö2YxRRd",
            "bedomning": {
                "stallningstagande": "Någon av följande behörighet",
                "lamplighet": "Nej",
                "behorigheter": ["A1", "B", "D", "DE", "D1", "Taxi", "BE", "A2", "Traktor", "D1E", "C1", "A"]
            }
        };
    },
    getRandom: function(intygsID, patient) {
        var randomKorkortstyper = testValues.getRandomKorkortstyper();

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        //Använd patientens födelseår för att ta fram allmant.year (vilket år ställdes diagnosen diabetes får inte vare tidigare än födelseår)
        if (!patient || patient === true) {
            patient = {
                id: '1980'
            };
        }

        return {
            id: intygsID,
            typ: 'Transportstyrelsens läkarintyg, diabetes',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: testValues.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: parseInt(patient.id.substring(0, 4)) + 1,
                typ: shuffle(testValues.diabetestyp)[0],
                behandling: testValues.getRandomBehandling(),
                annanbehandling: 'Hypnos behandling'
            },


            hypoglykemier: testValues.getRandomHypoglykemier(randomKorkortstyper),
            // TODO: Gör denna slumpade likt ovanstående
            synintyg: {
                a: 'Ja'
            },
            kommentar: testdataHelper.randomTextString(),
            specialist: testdataHelper.randomTextString(),
            bedomning: testValues.getRandomBedomning(randomKorkortstyper)
        };
    }
};
