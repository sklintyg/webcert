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

/* globals pages, intyg, protractor, browser*/

'use strict';

var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var utkastPage = pages.intyg.base.utkast;

module.exports = function() {
    this.Given(/^jag signerar intyget$/, function(callback) {
        // fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE).then(callback);
        expect(utkastPage.signeraButton.isEnabled()).to.eventually.be.ok.then(function() {
            console.log('Signeringsknapp är klickbar');
        }, function(reason) {
            callback('FEL, Signeringsknapp är inte klickbar, ' + reason);
        }).then(callback);
    });

    this.Given(/^jag makulerar intyget$/, function(callback) {

        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            intyg.id = intyg.id.split('?')[0];
        });

        fkIntygPage.makulera.btn.sendKeys(protractor.Key.SPACE);
        fkIntygPage.makulera.dialogAterta.sendKeys(protractor.Key.SPACE);
        fkIntygPage.makulera.kvittensOKBtn.sendKeys(protractor.Key.SPACE).then(callback);
    });

    this.Given(/^jag kopierar intyget$/, function(callback) {
        fkIntygPage.copy.button.sendKeys(protractor.Key.SPACE).then(function() {
            fkIntygPage.copy.dialogConfirmButton.sendKeys(protractor.Key.SPACE).then(callback);
        });
    });

};