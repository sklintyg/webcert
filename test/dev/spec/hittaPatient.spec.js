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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjUtkastType = wcTestTools.pages.sokSkrivIntyg.valjUtkastType;

describe('Create and Sign FK utkast', function() {

    describe('Login through the welcome page', function() {
        it('with user', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            SokSkrivIntygPage.selectPersonnummer('191212121212');
            expect(SokSkrivValjUtkastType.isAt()).toBe(true);
        });
    });

});
