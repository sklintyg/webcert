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
 * Created by bennysce on 09/06/15.
 */
/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var intygGenerator = wcTestTools.intygGenerator;

xdescribe('answer arende on luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var arendeId = 'luse-arende-avstmn-hantera';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false
        };
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteAllArenden();
        restTestdataHelper.createWebcertIntyg(testData).then(function() {
            restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
            restTestdataHelper.createArendeFromTemplate('luse', intygId, arendeId, 'Hur är det med arbetstiden?',
                'AVSTMN', 'PENDING_EXTERNAL_ACTION');
            });
        });
    });

    afterAll(function() {
       restTestdataHelper.deleteUtkast(intygId);
    });

    describe('make sure intyg page has loaded', function() {
        it('should view fk intyg', function() {
            LuseIntygPage.get(intygId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
    });

    describe('answer arende', function() {
        it('make sure pushed arende is visible', function() {
            var arende = LuseIntygPage.getArendeById(false, arendeId);
            expect(arende.isDisplayed()).toBeTruthy();
        });

        it('mark arende as handled', function() {
            LuseIntygPage.markArendeAsHandled(arendeId).click().then(function() {
                var arende = LuseIntygPage.getArendeById(true, arendeId); // true = handled list
                expect(arende.isDisplayed()).toBeTruthy();
            });
        });

        it('mark arende as unhandled', function() {
            LuseIntygPage.markArendeAsHandled(arendeId).click().then(function() {
                var arende = LuseIntygPage.getArendeById(false, arendeId); // false = unhandled list
                expect(arende.isDisplayed()).toBeTruthy();
            });
        });
    });

});
