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

/*globals protractor, wcTestTools, browser, intyg */

'use strict';
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
module.exports = function() {

    this.Given(/^jag går tillbaka$/, function(callback) {
        fkUtkastPage.backBtn.sendKeys(protractor.Key.SPACE)
            .then(function() {
                callback();
            });
    });

    this.Given(/^jag går in på utkastet$/, function(callback) {
        browser.get('/web/dashboard#/fk7263/edit/' + intyg.id)
            .then(function() {
                callback();
            });
    });
};
