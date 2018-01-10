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

 /* globals intyg*/

 'use strict';

var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;

var today = new Date();

var deathDate = new Date();

if (typeof(global.intyg) !== 'undefined' && typeof(global.intyg.dbIntyg) !== 'undefined') {
	deathDate = intyg.dbIntyg.deathDate;
} else {
	deathDate.setDate(today.getDate() - Math.floor(Math.random() * 365));
}

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
				year : shuffle(['2017', '2018', '0000 (ej känt)'])[0],
				month : shuffle(monthArr.slice(0,today.getMonth() - 1))[0],
				antraffadDod : testdataHelper.dateFormat(today)
			}
		}
	}	
}
function getDodsOrsak() {
	var n = Math.floor(Math.random() * 4);
	var obj = {
		a : getDodsOrsakObj(1)
	};
	if (n >= 1) {
		obj.b = getDodsOrsakObj(2);
	} else if (n >= 2) {
		obj.c = getDodsOrsakObj(3);
	} else if (n >= 3) {
		obj.d = getDodsOrsakObj(4);
	}
	return obj;
}
function getDodsOrsakObj(n) {
	var datum = new Date(dayBeforeDeath);
	datum.setDate(deathDate.getDate() -n);
	
	var obj = {
		beskrivning : testdataHelper.randomTextString(),
		datum : testdataHelper.dateFormat(datum),
		tillstandSpec : shuffle(['Akut', 'Kronisk', 'Uppgift saknas'])[0]
		};
	return obj;
}
function getOperation() {
	var ja = { 
		ja: {
			datum : testdataHelper.dateFormat(dayBeforeDeath),
			beskrivning : testdataHelper.randomTextString()
			}
		};
	return shuffle([ja, 'Nej', 'Uppgift om operation saknas'])[0];
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
			typ : "Dödsorsaksintyg",
			deathDate : deathDate, //datumvariabel som används för att ta fram test-data till andra variablar.
            identitetStyrktGenom : shuffle(["körkort", "pass", "fingeravtryck", "tandavgjutning"])[0],
            dodsdatum : getDodsdatum(datumSakert),
            dodsPlats : {kommun : testdataHelper.randomTextString(), boende : shuffle(["sjukhus","ordinartBoende","sarskiltBoende","annan"])[0]},
			dodsorsak : getDodsOrsak(),
			operation : getOperation(),
			skadaForgiftning : testdataHelper.randomTrueFalse(),
			dodsorsaksuppgifter : {
				foreDoden : testdataHelper.randomTrueFalse(),
				efterDoden : testdataHelper.randomTrueFalse(),
				kliniskObduktion : testdataHelper.randomTrueFalse(),
				rattsmedicinskObduktion : testdataHelper.randomTrueFalse(),
				rattsmedicinskBesiktning : testdataHelper.randomTrueFalse()
			}
		};
		if (datumSakert === false) {
			obj.barn = testdataHelper.randomTrueFalse();
		}
		
		return obj;
		
	}
};