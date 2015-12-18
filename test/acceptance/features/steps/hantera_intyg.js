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
 
/* globals pages, protractor, intyg, logg, browser,should*/

'use strict';

var fkIntygPage = pages.intyg.fkIntyg;

module.exports = function () {
	this.Given(/^signerar intyget$/, function (callback) {
        // Klicka på 'visa vad som saknas' innan signering för att underlätta felsökning
        element(by.id('showCompleteButton')).click();
        
        element(by.id('signera-utkast-button')).click().then(callback);
    });

    this.Given(/^jag makulerar intyget$/, function (callback) {
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            intyg.id = intyg.id.replace('?signed', '');
        });

        fkIntygPage.makulera.btn.click();
        fkIntygPage.makulera.dialogAterta.click();
        fkIntygPage.makulera.kvittensOKBtn.click()
        .then(callback);
    });
};