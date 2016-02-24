
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

/*global testdata */
'use strict';

module.exports = {
	generateIntygByType:function(typ, id){
	    if (typ === 'Transportstyrelsens läkarintyg') {
	        return testdata.getRandomTsBasIntyg(id);
	    } else if (typ === 'Transportstyrelsens läkarintyg, diabetes') {
	        return testdata.getRandomTsDiabetesIntyg(id);
	    } else if (typ === 'Läkarintyg FK 7263') {
	        return testdata.fk.sjukintyg.getRandom(id);
	    } else if (typ === 'Läkarintyg, sjukersättning') {
	        return false;
	    }
	}
};