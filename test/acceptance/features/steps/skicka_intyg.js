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

/* globals pages */
/* globals browser, intyg, logger, protractor, Promise */

'use strict';
var fkIntygPage = pages.intyg.fk['7263'].intyg;

module.exports = function() {

    this.Given(/^jag skickar intyget till Transportstyrelsen/, function() {

        //Fånga intygets id
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            logger.info('Intygsid: ' + intyg.id);
            intyg.id = intyg.id.split('?')[0];
        });

        Promise.all([
            fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE),
            fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE)
        ]);
    });

    this.Given(/^jag skickar intyget till Försäkringskassan$/, function() {



        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            intyg.id = intyg.id.split('?')[0];
            logger.info('Följande intygs id skickas till Försäkringskassan: ' + intyg.id);
        });

        return fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE).then(function() {
            return fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE);
        });

        // callback();
    });

};
