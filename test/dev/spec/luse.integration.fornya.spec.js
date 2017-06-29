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

/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var intygGenerator = wcTestTools.intygGenerator;

describe('Djupintegration on luse intyg', function() {

    var intygId = 'luse-integration-renew-1';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false
        };

        // If were nog ignoring sync while setting user, protractor complains that it cannot sync with angular on the testability page loaded during setUserOrigin
        browser.ignoreSynchronization = true;
        specHelper.setUserOrigin('DJUPINTEGRATION').then(function() {
            browser.ignoreSynchronization = false;
            restTestdataHelper.deleteUtkast(intygId);
            restTestdataHelper.createWebcertIntyg(testData);
        });
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
    });

    it('should load intyg', function() {
        LuseIntygPage.getIntegration(intygId, {
            fornamn:'Nytt förnamn',
            mellannamn:'Nytt mellannamn',
            efternamn:'Nytt efternamn',
            postadress:'Ny postadress',
            postnummer:'Nytt postnummer',
            postort:'Ny postort',
            enhet:'TSTNMT2321000156-1039'
        });
        expect(LuseIntygPage.isAt()).toBeTruthy();
        expect(LuseIntygPage.statusNameAndAddressChanged.isDisplayed()).toBeTruthy();
    });

    it('should fornya intyg and view resulting utkast', function() {
        LuseIntygPage.fornya.button.click();
        LuseIntygPage.fornya.dialogConfirmButton.click();
        expect(LuseUtkastPage.isAt()).toBeTruthy();
        expect(LuseUtkastPage.patientNamnPersonnummer.getText()).toBe('Nytt förnamn Nytt mellannamn Nytt efternamn - 19121212-1212');
    });

});
