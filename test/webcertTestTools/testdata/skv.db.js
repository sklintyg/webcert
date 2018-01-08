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

var dayBeforeDeath = new Date(deathDate);
dayBeforeDeath.setDate(deathDate.getDate() -1);

function getDodsdatum(datumSakert){
	if (datumSakert === true) {
		return {
			sakert : {
				datum : testdataHelper.dateFormat(deathDate)
			}
		}
	} else {
		var monthArr = ['00 (ej känt)', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];
		return {
			inteSakert : {
				year : shuffle(['2016', '2017', '0000 (ej känt)'])[0],
				month : shuffle(monthArr.slice(0,today.getMonth() - 1))[0],
				antraffadDod : testdataHelper.dateFormat(today)
			}
		}
	}	
}

function getExplosivImplantat() {
	var obj1 = false;
	var obj2 = {avlagsnat : testdataHelper.randomTrueFalse()};
	return shuffle([obj1,obj2])[0];
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
		
		var datumSakert = testdataHelper.randomTrueFalse();
		
		var obj = {
            id : intygsID,
			typ : "Dödsbevis",
			deathDate : deathDate, //datumvariabel som används för att ta fram test-data till andra variablar.
            identitetStyrktGenom : shuffle(["körkort", "pass", "fingeravtryck", "tandavgjutning"])[0],
            dodsdatum : getDodsdatum(datumSakert),
            dodsPlats : {kommun : testdataHelper.randomTextString(), boende : shuffle(["sjukhus","ordinartBoende","sarskiltBoende","annan"])[0]},
            explosivImplantat : getExplosivImplantat(),
            yttreUndersokning : {
				value: shuffle(["ja", "nejUndersokningSkaGoras", "nejUndersokningGjortKortFore"])[0]
				}
		};
		if (datumSakert === false) {
			obj.barn = testdataHelper.randomTrueFalse();
		}
		if (obj.yttreUndersokning.value === 'nejUndersokningGjortKortFore') {
			obj.yttreUndersokning.datum = testdataHelper.dateFormat(dayBeforeDeath);
		}
		if (obj.yttreUndersokning.value !== 'nejUndersokningSkaGoras') {
			obj.polisanmalan = testdataHelper.randomTrueFalse();
		}
		
		
		
		return obj;
		
	}
};