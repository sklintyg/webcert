/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/* globals pages */
/* globals browser, intyg, protractor */

'use strict';
var fkIntygPage = pages.intygpages.fkIntyg;

module.exports = function() {

    this.Given(/^jag skickar intyget till Transportstyrelsen/, function(callback) {

        //Fånga intygets id
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            logg('Intygsid: ' + intyg.id);
        });

        fkIntygPage.skicka.knapp.click();
        fkIntygPage.skicka.samtyckeCheckbox.click();
        fkIntygPage.skicka.dialogKnapp.click();
        callback();
    });

    this.Given(/^jag skickar intyget till Försäkringskassan$/, function(callback) {

    	browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            intyg.id = intyg.id.replace('?signed', '');
        });

        fkIntygPage.skicka.knapp.click();
        fkIntygPage.skicka.samtyckeCheckbox.click();
        fkIntygPage.skicka.dialogKnapp.click();
        callback();
    });

};
