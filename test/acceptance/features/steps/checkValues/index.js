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
/* globals Promise */

var commonCheckValues = require('./common.js').checkValues;

var fk7263CheckValues = require('./fk.7263.js').checkValues;
var fkLUSECheckValues = require('./fk.LUSE.js').checkValues;
var fkLISJPCheckValues = require('./fk.LISJP.js').checkValues;
var fkLUAENACheckValues = require('./fk.LUAE_NA.js').checkValues;
var fkLUAEFSCheckValues = require('./fk.LUAE_FS.js').checkValues;

var tsCommonCheckValues = require('./ts.common.js').checkValues;
var tsBasCheckValues = require('./ts.bas.js').checkValues;
var tsDiabetesCheckValues = require('./ts.diabetes.js').checkValues;

var skvDBCheckValues = require('./skv.db.js').checkValues;
var socDOICheckValues = require('./soc.doi.js').checkValues;

module.exports = {
    fk: {
        '7263': fk7263CheckValues,
        'LUSE': fkLUSECheckValues,
        'LISJP': fkLISJPCheckValues
    },
    ts: {
        common: tsCommonCheckValues,
        bas: tsBasCheckValues,
        diabetes: tsDiabetesCheckValues
    },
    forIntyg: function(intyg) {
        'use strict';
        var promiseArr = [commonCheckValues(intyg)];
        if (intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
            promiseArr.push(
                tsCommonCheckValues(intyg).then(function() {
                    return tsDiabetesCheckValues(intyg);
                })
            );
        } else if (intyg.typ === 'Transportstyrelsens läkarintyg') {
            promiseArr.push(
                tsCommonCheckValues(intyg).then(function() {
                    return tsBasCheckValues(intyg);
                })
            );
        } else if (intyg.typ === 'Läkarintyg FK 7263') {
            promiseArr.push(fk7263CheckValues(intyg));
        } else if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
            promiseArr.push(fkLUSECheckValues(intyg));
        } else if (intyg.typ === 'Läkarintyg för sjukpenning') {
            promiseArr.push(fkLISJPCheckValues(intyg));
        } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
            promiseArr.push(fkLUAENACheckValues(intyg));
        } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
            promiseArr.push(fkLUAEFSCheckValues(intyg));
        } else if (intyg.typ === 'Dödsbevis') {
            promiseArr.push(skvDBCheckValues(intyg));
        } else if (intyg.typ === 'Dödsorsaksintyg') {
            promiseArr.push(socDOICheckValues(intyg));
        } else {
            throw ('Saknar värdecheckar för intygstyp: ' + intyg.typ);
        }

        return Promise.all(promiseArr);
    }
};
