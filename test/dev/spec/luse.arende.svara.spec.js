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
 * Created by bennysce on 09/06/15.
 */
/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var intygGenerator = wcTestTools.intygGenerator;

describe('answer arende on luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var arendeId = 'luse-arende-arbtid';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false,
            'relations': [
                {'intygsId': intygId, 'status': 'INTYG'}
            ]
        };
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.createWebcertIntyg(testData).then(function() {
            restTestdataHelper.createArendeFromTemplate('luse', intygId, arendeId, 'Hur är det med arbetstiden?',
                'ARBTID', 'PENDING_INTERNAL_ACTION');
        });
        restTestdataHelper.createIntygFromTemplate('luseMax', intygId);
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteIntyg(intygId);
    });

    describe('make sure intyg page has been loaded', function() {
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

        it('fill text', function() {
            var arende = LuseIntygPage.getAnswerBox(arendeId).sendKeys('Låt oss slänga in ett svar och se vad som händer.');
            expect(arende.isDisplayed()).toBeTruthy();
        });

        it('push answer button and make sure answered arende is now in the handled list', function() {
            LuseIntygPage.getAnswerButton(arendeId).click().then(function() {
                var arende = LuseIntygPage.getArendeById(true, arendeId); // true = handled list
                expect(arende.isDisplayed()).toBeTruthy();
            });
        });
    });

});
