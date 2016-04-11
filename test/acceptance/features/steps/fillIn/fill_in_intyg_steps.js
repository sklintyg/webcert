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

/*global intyg,wcTestTools */

'use strict';
var fillIn = require('./').fillIn;
var generateIntygByType = require('../helpers.js').generateIntygByType;
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var td = wcTestTools.testdata;
module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function(callback) {
        if (!global.intyg.typ) {
            callback('Intyg.typ odefinierad.');
        } else {
            global.intyg = generateIntygByType(intyg.typ, intyg.id);
            fillIn(global.intyg, callback);
        }
    });

    this.Given(/^jag ändrar diagnoskod$/, function(callback) {
        fkUtkastPage.angeDiagnosKod(td.values.fk.getRandomDiagnoskod())
            .then(callback());
    });


};
