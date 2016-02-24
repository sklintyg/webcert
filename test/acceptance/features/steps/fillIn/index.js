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

module.exports = {
	fillIn:function(intyg,cb) {
	    switch(intyg.typ) {
		    case 'Transportstyrelsens läkarintyg':
		        require('./ts.bas.js').fillIn(intyg, cb);
		        break;
		    case 'Transportstyrelsens läkarintyg, diabetes':
		        require('./ts.diabetes.js').fillIn(intyg, cb);
		        break;
		    case 'Läkarintyg FK 7263':
		        require('./fk.7263.js').fillIn(intyg, cb);
		        break;
		    case 'Läkarintyg FK 7263':
		        require('./fk.LUSE.js').fillIn(intyg, cb);
		        break;
		    default:
		        cb('Intyg.typ odefinierad.');
		}
	}
};