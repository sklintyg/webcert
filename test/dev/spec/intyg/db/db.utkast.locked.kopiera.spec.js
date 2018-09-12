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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var UtkastPage = wcTestTools.pages.intyg.skv.db.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;
var restTestdataHelper = wcTestTools.helpers.restTestdata;

describe('DB locked utkast kopiera tests', function() {
    var intygsTyp = 'db';

    var intygId = 'db-locked-utkast-1';
    var intygId2 = 'db-locked-utkast-2';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents':intygGenerator.getIntygJson({'intygType': intygsTyp,'intygId':intygId}),
            'utkastStatus': 'DRAFT_LOCKED',
            'revoked': false
        };
        restTestdataHelper.createWebcertIntyg(testData);
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygId);
    });

    describe('samma enhet', function() {
        describe('finns redan draft', function() {
            beforeAll(function() {
                var testData = {
                    'contents': intygGenerator.getIntygJson({'intygType': intygsTyp, 'intygId': intygId2}),
                    'utkastStatus': 'DRAFT_COMPLETE',
                    'revoked': false
                };
                restTestdataHelper.createWebcertIntyg(testData);
            });

            it('should load utkast and not possible to copy', function() {
                UtkastPage.get(intygsTyp, intygId);
                expect(UtkastPage.kopiera.btn.isEnabled()).toBeFalsy();

                expect(UtkastPage.getAlert(intygsTyp, 'previousutkast').isPresent()).toBeTruthy();
            });

            afterAll(function() {
                testdataHelper.deleteUtkast(intygId2);
            });
        });

        describe('finns redan signerad', function() {
            beforeAll(function() {
                var testData = {
                    'contents':intygGenerator.getIntygJson({'intygType': intygsTyp,'intygId':intygId2}),
                    'utkastStatus': 'SIGNED',
                    'revoked': false
                };
                restTestdataHelper.createWebcertIntyg(testData);
            });

            it('should load utkast and not possible to copy', function() {
                UtkastPage.get(intygsTyp, intygId);
                expect(UtkastPage.kopiera.btn.isEnabled()).toBeFalsy();

                expect(UtkastPage.getAlert(intygsTyp, 'previousintyg').isPresent()).toBeTruthy();
            });

            afterAll(function() {
                testdataHelper.deleteUtkast(intygId2);
            });
        });
    });
});
