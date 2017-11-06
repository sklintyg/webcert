/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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



function getDodsdatumSakertDatum(datumSakert){
	var today = new Date();
	var date = new Date();
	date.setDate(today.getDate() - Math.floor(Math.random() * 365));
	
	if (datumSakert) {
		return testdataHelper.dateFormat(date);
	} else {
		var monthArr = ['januari', 'februari', 'mars', 'april', 'maj', 'juni', 'juli', 'augusti', 'september', 'oktober', 'november', 'december'];
		return {
			year : shuffle(['2016', '2017', '0000 (ej känt)'][0]),
			month : shuffle(monthArr.slice(0,today.getMonth() - 1)[0]),
			antraffadDod : testdataHelper.dateFormat(today)
		}
	}
	
}

function getExplosivImplantat() {
	obj1 = {false}
	obj2 = {true, avlagsnat : testdataHelper.randomTrueFalse()}
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
		
		return {
            id : intygsID,
			typ : "Dödsbevis",
            identitetStyrktGenom : shuffle(["körkort", "pass", "fingeravtryck", "tandavgjutning"])[0],
            dodsdatum : {datumSakert : datumSakert,	datum : getDodsdatumSakertDatum(datumSakert)}
            dodsPlats : {kommun : testdataHelper.randomTextString(), boende : shuffle(["sjukhus","ordinartBoende","sarskiltBoende","annan"])},
            barn : testdataHelper.randomTrueFalse(),
            explosivImplantat : getExplosivImplantat(),
            yttreUndersokning : shuffle(["Ja", "nejUndersokningSkaGoras", "nejUndersokningGjortKortFore"])[0],
            polisanmalan : testdataHelper.randomTrueFalse();
		};
		
	}
};