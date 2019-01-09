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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;
var restTestdataHelper = wcTestTools.helpers.restTestdata;

describe('Luse locked utkast tests', function() {
    var intygId = 'luse-locked-utkast-1';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents':intygGenerator.getIntygJson({'intygType':'luse','intygId':intygId}),
            'utkastStatus':'DRAFT_LOCKED',
            'revoked':false
        };
        restTestdataHelper.createWebcertIntyg(testData);
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygId);
    });

    it('should load utkast', function() {
        LuseUtkastPage.get(intygId);
    });

    it('input should be disabled', function() {
        // All input fields should be disabled
        expect(element.all(by.css('#certificate INPUT')).count()).toBeGreaterThan(0);
        expect(element.all(by.css('#certificate INPUT:not(:disabled)')).count()).toEqual(0);
        expect(element.all(by.css('#certificate TEXTAREA:not(:disabled)')).count()).toEqual(0);
        expect(element.all(by.css('#certificate BUTTON:not(:disabled)')).count()).toEqual(0);
    });

    it('Correct buttons visible in header', function() {
        // Should not exist
        expect(LuseUtkastPage.signeraButton.isPresent()).toBeFalsy();
        expect(LuseUtkastPage.radera.knapp.isPresent()).toBeFalsy();

        // Should exist
        expect(LuseUtkastPage.skrivUtBtn.isPresent()).toBeTruthy();
        expect(LuseUtkastPage.kopiera.btn.isPresent()).toBeTruthy();
        expect(LuseUtkastPage.makulera.btn.isPresent()).toBeTruthy();
    });
});