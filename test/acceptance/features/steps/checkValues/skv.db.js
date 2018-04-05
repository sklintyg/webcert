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

/* globals logger, pages, Promise, wcTestTools */

'use strict';

var dbPage = pages.intyg.skv.db.intyg;
var testdataHelper = wcTestTools.helpers.testdata;
//var regExp = require('./common.js').regExp;

function checkBarn(barn) {
    if (barn) {
        return expect(dbPage.barn.value.getText()).to.eventually.contain(testdataHelper.boolTillJaNej(barn));
    } else {
        return Promise.resolve();
    }
}

module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Dödsbevis --');

        //Barn 
        return checkBarn(intyg.barn)
            .then(function(value) {
                logger.info('OK - Barn');
            }, function(reason) {
                throw ('FEL, Barn: ' + reason);
            });
    }
};
