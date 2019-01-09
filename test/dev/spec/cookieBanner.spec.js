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

/*globals protractor, describe,it,browser,element */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var WelcomePage = wcTestTools.pages.welcome;
var basepage = wcTestTools.pages.webcertBase;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Logga in och godkänn kakor', function() {

    it('Öppna och logga in utan att disabla cookiebanner', function() {
        browser.ignoreSynchronization = false;
        WelcomePage.get();
        WelcomePage.login('IFV1239877878-104B_IFV1239877878-1042', true);
        // Need explicit get here to load protractor mocks in main angular app since welcomepage is a separate angular app
        SokSkrivIntygPage.get();

        expect(basepage.cookie.consentBanner.isDisplayed()).toBeTruthy();
        expect(basepage.cookie.consentBtn.isDisplayed()).toBeTruthy();
        basepage.cookie.consentBtn.sendKeys(protractor.Key.SPACE);
        expect(basepage.cookie.consentBtn.isPresent()).toBeFalsy();

    });


});
