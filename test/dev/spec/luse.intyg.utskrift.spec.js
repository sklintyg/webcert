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

/**
 * Created by Magnus Ekstrand on 2016-10-12.
 */
/*globals browser,JSON,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var IntygPage = wcTestTools.pages.intyg.luse.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('verify luse\'s print buttons', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;

        var intyg = intygFromJsonFactory.defaultLuse();
        intygsId = intyg.id;

        restUtil.createIntyg(intyg).then(function(response) {
            var intyg = JSON.parse(response.request.body);
            expect(intyg.id).not.toBeNull();
        }, function(error) {
            console.log('Error calling createIntyg');
        });

    });

    afterAll(function() {
        restUtil.deleteIntyg(intygsId);
        specHelper.logout();
    });

    it('login through the welcome page with default user', function() {
        specHelper.login();
    });

    it('view luaena intyg', function() {
        SokSkrivIntygPage.selectPersonnummer('19121212-1212');
        SokSkrivValjIntyg.selectIntygById(intygsId);
        expect(IntygPage.isAt()).toBeTruthy();
    });

    it('verify the employer print button is not displayed', function() {
        expect(element(by.id('intyg-header-dropdown-select-pdf-type')).isPresent()).toBeFalsy();
    });

    it('verify the normal print button is not displayed', function() {
        expect(element(by.id('downloadprint')).isDisplayed()).toBeTruthy();
    });

});
