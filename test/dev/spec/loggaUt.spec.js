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

/*globals browser,protractor */
/*globals pages */
/*globals describe,it,helpers */
'use strict';

var wcTestTools = require('webcert-testtools');

var specHelper = wcTestTools.helpers.spec;
var UtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
var landingPage = wcTestTools.pages.landing;
var restTestdataHelper = wcTestTools.helpers.restTestdata;

describe('Utloggning vid vidarenavigering', function() {

    var utkastId = 'new-utkast-123';
    var parameters = '?';
        parameters += 'fornamn=' + encodeURIComponent("firstname") + '&';
        parameters += 'efternamn=' + encodeURIComponent("lastname") + '&';
        parameters += 'postadress=' + encodeURIComponent("adress") + '&';
        parameters += 'postnummer=' + encodeURIComponent("123") + '&';
        parameters += 'postort=' + encodeURIComponent("test");

    beforeEach(function() {
        browser.ignoreSynchronization = false;
    });

    it('Logga in', function() {

        specHelper.login();
        restTestdataHelper.createEmptyUtkast(UtkastPage.intygType, UtkastPage.intygTypeVersion, utkastId);

        browser.ignoreSynchronization = true;
        specHelper.setUserOrigin('DJUPINTEGRATION').then(function() {
            browser.ignoreSynchronization = false;
        });
    });

    it('Öppna intyg ' + browser.baseUrl + 'visa/intyg/' + utkastId + parameters, function() {
        browser.driver.get(browser.baseUrl + 'visa/intyg/' + utkastId + parameters);

        expect(UtkastPage.isAt()).toBeTruthy();
    });

    it('Besök extern sida', function() {
        browser.ignoreSynchronization = true;
        browser.driver.get('http://www.google.com');
        expect(browser.getTitle()).toContain('Google');
    });

    it('Gå tillbaka till utkastet', function() {
        //browser.driver.get(browser.baseUrl);
        UtkastPage.get(utkastId);

        specHelper.waitForAngularTestability();
        expect(UtkastPage.isAt()).toBeTruthy();
    });

    it('Besök extern sida och vänta 9 sekunder', function() {
        browser.ignoreSynchronization = true;
        browser.driver.get('http://www.google.com');
        expect(browser.getTitle()).toContain('Google');
        browser.driver.sleep(9000);
    });

    it('Access denied visas om invånaren försöker navigera till startsidan', function() {
        UtkastPage.get(utkastId);

        specHelper.waitForAngularTestability();
        expect(landingPage.isAt()).toBeTruthy();
    });

    afterAll(function() {
        restTestdataHelper.deleteIntyg(utkastId);
        restTestdataHelper.deleteUtkast(utkastId);
    });

});
