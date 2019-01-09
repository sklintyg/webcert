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

describe('Luse locked utkast makulera tests', function() {
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

    it('should load utkast and possible to copy', function() {
        LuseUtkastPage.get(intygId);
        expect(LuseUtkastPage.makulera.btn.isPresent()).toBeTruthy();
    });

    it('revoke draft', function() {
        LuseUtkastPage.makulera.btn.sendKeys(protractor.Key.SPACE);
        LuseUtkastPage.makulera.dialogRadioAnnatAllvarligtFel.sendKeys(protractor.Key.SPACE);
        browser.wait(LuseUtkastPage.makulera.dialogRadioAnnatAllvarligtFelClarification.isDisplayed())
            .then(LuseUtkastPage.makulera.dialogRadioAnnatAllvarligtFelClarification.sendKeys("Patienten har en helt annan diagnos än den angivna, blandade ihop mjältbrand med nageltrång. Lätt hänt..."));

        LuseUtkastPage.makulera.dialogMakulera.sendKeys(protractor.Key.SPACE);

        expect(LuseUtkastPage.makulera.statusRevoked.isDisplayed()).toBeTruthy();

        expect(LuseUtkastPage.makulera.btn.isPresent()).toBeFalsy();
        expect(LuseUtkastPage.skrivUtBtn.isPresent()).toBeFalsy();
        expect(LuseUtkastPage.kopiera.btn.isPresent()).toBeFalsy();
        expect(LuseUtkastPage.signeraButton.isPresent()).toBeFalsy();
        expect(LuseUtkastPage.radera.knapp.isPresent()).toBeFalsy();
    });
});