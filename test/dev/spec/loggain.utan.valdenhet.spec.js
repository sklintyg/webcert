/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
var WelcomePage = wcTestTools.pages.welcome;
var specHelper = wcTestTools.helpers.spec;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Verify logging in without vardEnhet selected', function() {

    it('should login with a user without preselecting a vardEnhet', function() {
        browser.ignoreSynchronization = false;
        WelcomePage.get();
        //User  IFV1239877878-104B förväntas ha fakeinloggnings pararamtern enhetId tom, men flera enheter att välja på.
        //Detta skall göra att ingen enhet väljs vid inloggningen i backend.
        WelcomePage.login('staffan_', false);

        //Inloggning via welcomesidan innebär sidbyte, så vi väntar på att angular testability är redo så att testet kan fortsätta..
        specHelper.waitForAngularTestability();
    });

    it('should display unit selection dialog', function() {
        expect(element(by.tagName('wc-integration-enhet-selector')).isPresent()).toBeTruthy();
    });

    it('should be able to select a unit in unit selection dialog', function() {

        //Ett känt tillgängligt enhetsId-val skall nu vara tillgängligt.
        var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-linkoping-link'));
        expect(enhetSelectorLink.isDisplayed()).toBeTruthy();

        // Välj enheten...
        enhetSelectorLink.click();

    });

    it('should be redirected to default start-view', function() {
        expect(SokSkrivIntygPage.isAt()).toBeTruthy();
    });

});

describe('Verify logging in WITH vardEnhet selected', function() {

    it('should login with a user with a vardEnhet selected', function() {
        browser.ignoreSynchronization = false;
        WelcomePage.get();
        //Logga in med TSTNMT2321000156-1079_TSTNMT2321000156-1077 som förutsätts bara ha 1 enhet
        WelcomePage.login('TSTNMT2321000156-1079_TSTNMT2321000156-1077', false);

        //Inloggning via welcomesidan innebär sidbyte, så vi väntar på att angular testability är redo så att testet kan fortsätta..
        specHelper.waitForAngularTestability();
    });

    it('should show default start-view directly', function() {
        expect(SokSkrivIntygPage.isAt()).toBeTruthy();
    });

});
