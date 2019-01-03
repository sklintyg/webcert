/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
    fk: {
        '7263': require('./fk.7263.js'),
        LUSE: require('./fk.LUSE.js'),
        LISJP: require('./fk.LISJP.js'),
        LUAE_FS: require('./fk.LUAE_FS.js'),
        LUAE_NA: require('./fk.LUAE_NA.js')
    },
    ts: {
        bas: require('./ts.bas.js'),
        diabetes: {
            v2: require('./ts.diabetes.v2.js'),
            v3: require('./ts.diabetes.v3.js')
        }
    },
    skv: {
        db: require('./skv.db.js')
    },
    soc: {
        doi: require('./soc.doi.js')
    },
    af: {
        af00213: require('./af.00213.js'),
        af00251: require('./af.00251.js')
    },
    ag: {
        ag114: require('./ag.114.js'),
        ag7804: require('./ag.7804.js')
    },
    values: require('./testvalues.js'),
    fmb: require('./diagnoskoderFMB.js'),
    diagnosKategorier: require('./diagnosKategorier_A-F.js')
};
