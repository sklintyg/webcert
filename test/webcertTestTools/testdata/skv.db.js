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

var today = new Date();
var deathDate = new Date();
deathDate.setDate(today.getDate() - Math.floor(Math.random() * 365));


function getRelativeDeathDate(modifier) {
    // Modifier : days
    let datum = new Date(deathDate);
    datum.setDate(deathDate.getDate() + modifier);
    return datum;
}


function getDodsdatum(datumSakert) {
    if (datumSakert === true) {
        return {
            sakert: {
                datum: testdataHelper.dateFormat(deathDate)
            }
        };
    } else {
        var monthArr = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];
        let year = deathDate.getYear() + 1900;
        year = shuffle([String(year), '0000'])[0];
        return {
            inteSakert: {
                year: year,
                month: (year === '0000') ? '00' : shuffle(monthArr.slice(0, today.getMonth() - 1))[0],
                antraffadDod: testdataHelper.dateFormat(today)
            }
        };
    }
}

function getExplosivImplantat() {
    var obj1 = false;
    var obj2 = {
        avlagsnat: testdataHelper.randomTrueFalse()
    };
    return shuffle([obj1, obj2])[0];
}

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
    },
    getRandom: function(intygsID, patient) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        let datumSakert = testdataHelper.randomTrueFalse();

        var obj = {
            id: intygsID,
            typ: "Dödsbevis",
            deathDate: deathDate, //datumvariabel som används för att ta fram test-data till andra variablar.
            identitetStyrktGenom: shuffle(["körkort", "pass", "fingeravtryck", "tandavgjutning", testdataHelper.randomTextString(5, 100)])[0],
            dodsdatum: getDodsdatum(datumSakert),
            dodsPlats: {
                kommun: shuffle(["Karlstad", "Forshaga", "Hagfors", "Munkfors", "Torsby", testdataHelper.randomTextString(5, 100)])[0],
                boende: shuffle(["Sjukhus", "Ordinärt boende", "Särskilt boende", "Annan/okänd"])[0]
            },
            explosivImplantat: getExplosivImplantat(),
            yttreUndersokning: {
                value: shuffle(["Ja", "Nej, rättsmedicinsk undersökning ska göras", "Nej, den avlidne undersökt kort före döden"])[0]
            }
        };
        if (datumSakert === false) {
            obj.barn = testdataHelper.randomTrueFalse();
        }
        if (obj.yttreUndersokning.value === 'Nej, den avlidne undersökt kort före döden') {
            obj.yttreUndersokning.datum = testdataHelper.dateFormat(getRelativeDeathDate(-1));
        }
        if (obj.yttreUndersokning.value !== 'Nej, rättsmedicinsk undersökning ska göras') {
            obj.polisanmalan = testdataHelper.randomTrueFalse();
        }



        return obj;

    }
};
