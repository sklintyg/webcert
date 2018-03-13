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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var UtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var IntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var intygGenerator = wcTestTools.intygGenerator;

describe('Verifiera att legacy fk7263-utkast kan visas med uv-ramverket', function() {

    var utkastId = 'fk7263-utkast-1';
    var intygsId;

    afterAll(function() {
        restTestdataHelper.deleteIntyg(intygsId);
        restTestdataHelper.deleteUtkast(utkastId);
    });


    describe('Verifiera utkast', function() {
        beforeAll(function() {
            browser.ignoreSynchronization = false;
            specHelper.login();

            var utkastData = {
                'contents': intygGenerator.getIntygJson({
                    'intygType': 'fk7263',
                    'intygId': utkastId
                }),
                'utkastStatus': 'DRAFT_INCOMPLETE',
                'revoked': false
            };
            restTestdataHelper.deleteUtkast(utkastId);
            restTestdataHelper.createWebcertIntyg(utkastData);
        });

        it('skall visa utkast read-only', function() {
            UtkastPage.get(utkastId);
            expect(UtkastPage.isAt()).toBeTruthy();

        });

    });

    describe('Verifiera intyg med uv-ramverket', function() {
        beforeAll(function() {
            browser.ignoreSynchronization = false;
            specHelper.login();

            var intyg = intygFromJsonFactory.defaultFK7263();
            intygsId = intyg.id;

            restUtil.createIntyg(intyg).then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                logger.error('Error calling createIntyg');
            });

        });

        it('Skall visa signerat intyg med uv-ramverket', function() {
            IntygPage.get(intygsId);
            expect(IntygPage.isAt()).toBeTruthy();

        });
    });

});
