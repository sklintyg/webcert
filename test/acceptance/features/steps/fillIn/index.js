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

/*global browser*/
'use strict';

module.exports = {
    fillIn: function(intyg) {

        // Vänta på animering
        return browser.sleep(2000).then(function() {
            switch (intyg.typ) {
                case 'Transportstyrelsens läkarintyg':
                    return require('./ts.bas.js').fillIn(intyg);
                case 'Transportstyrelsens läkarintyg, diabetes':
                    return require('./ts.diabetes.js').fillIn(intyg);
                case 'Läkarintyg FK 7263':
                    return require('./fk.7263.js').fillIn(intyg);
                case 'Läkarutlåtande för sjukersättning':
                    return require('./fk.LUSE.js').fillIn(intyg);
                case 'Läkarintyg för sjukpenning utökat':
                    return require('./fk.lisjp.js').fillIn(intyg);
                default:
                    throw 'Intyg.typ odefinierad.';
            }
        });

    }
};
