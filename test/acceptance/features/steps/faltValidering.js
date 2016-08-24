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
/*globals pages,intyg*/

'use strict';
var luseUtkastPage = pages.intyg.luse.utkast;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
module.exports = function() {

    this.Given(/^jag fyller i "([^"]*)" som diagnoskod$/, function(dKod) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);

        if (isSMIIntyg) {
            return luseUtkastPage.diagnoseCode.sendKeys(dKod);
        } else {
            return fkUtkastPage.diagnosKod.sendKeys(dKod);
        }


    });

    this.Given(/^ska valideringsfelet "([^"]*)" visas$/, function(arg1) {
        var alertTexts = element.all(by.css('.alert-danger')).map(function(elm) {
            return elm.getText();
        });
        return alertTexts.then(function(result) {
            return expect(result.join('\n')).to.have.string(arg1);

        });

    });
};
