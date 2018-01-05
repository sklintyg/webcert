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

/*global browser, Promise, logger*/
'use strict';

module.exports = {
    fillIn: function(intyg) {
        var promiseArr = [];
        promiseArr.push(browser.sleep(1).then(function() {
            switch (intyg.typ) {
                case 'Transportstyrelsens läkarintyg':
                    return require('./ts.bas.js').fillIn(intyg);
                case 'Transportstyrelsens läkarintyg, diabetes':
                    return require('./ts.diabetes.js').fillIn(intyg);
                case 'Läkarintyg FK 7263':
                    return require('./fk.7263.js').fillIn(intyg);
                case 'Läkarutlåtande för sjukersättning':
                    logger.info('LUSE - require fillIn/fk.LUSE.js');
                    return require('./fk.LUSE.js').fillIn(intyg);
                case 'Läkarintyg för sjukpenning':
                    logger.info('LISJP - require fillIn/fk.LISJP.js');
                    return require('./fk.LISJP.js').fillIn(intyg);
                case 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga':
                    logger.info('LUAE_NA - require fillIn/fk.LUAE_NA.js');
                    return require('./fk.LUAE_NA.js').fillIn(intyg);
                case 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång':
                    logger.info('LUAE_FS - require fillIn/fk.LUAE_FS.js');
                    return require('./fk.LUAE_FS.js').fillIn(intyg);
                case 'Dödsbevis':
                    logger.info('SKV DB - require fillIn/skv.db.js');
                    return require('./skv.db.js').fillIn(intyg);
                case 'Dödsorsaksintyg':
                    logger.info('SOC DOI - require fillIn/soc.doi.js');
                    return require('./soc.doi.js').fillIn(intyg);
                default:
                    throw 'Intyg.typ odefinierad.';
            }
        }));
        //promiseArr.push(require('./common.js').fillIn(intyg));
        return Promise.all(promiseArr).then(function() {
            return require('./common.js').fillIn(intyg).then(function() {
                browser.ignoreSynchronization = false;
            });
        });
    }
};
