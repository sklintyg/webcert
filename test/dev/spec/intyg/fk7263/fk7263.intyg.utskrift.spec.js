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

/**
 * Created by Magnus Ekstrand on 2016-10-12.
 */
/*globals browser,JSON,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var FkIntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;

describe('verify a fk7263\'s print buttons', function() {

    var intygId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;

        var intyg = intygFromJsonFactory.defaultFK7263();
        intygId = intyg.id;

        restUtil.createIntyg(intyg).then(function(response) {
            var intyg = JSON.parse(response.request.body);
            expect(intyg.id).not.toBeNull();
        }, function(error) {
            logger.error('Error calling createIntyg');
        });
    });

    afterAll(function() {
        restTestdataHelper.deleteIntyg(intygId);
        specHelper.logout();
    });

    it('login through the welcome page with default user', function() {
        specHelper.login();
    });

    it('view fk intyg', function() {
        FkIntygPage.get(intygId);
        expect(FkIntygPage.isAt()).toBeTruthy();
    });

    it('verify the employer print button is displayed', function() {
        expect(element(by.id('intyg-header-dropdown-select-pdf-type')).isDisplayed()).toBeTruthy();
    });

    it('verify the normal print button is not displayed', function() {
        logger.debug('element(by.id(\'downloadprint\')' + element(by.id('downloadprint')));
        expect(element(by.id('downloadprint')).isPresent()).toBeFalsy();
    });

});
