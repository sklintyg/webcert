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
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('answer arende on luse intyg', function() {

    var intygId;
    var meddelandeId = 'luse-arende-avstmn';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false
        };

        var intyg = intygFromJsonFactory.defaultLuse();
        intygId = intyg.id;
        restUtil.createIntyg(intyg);

        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.createWebcertIntyg(testData).then(function() {
            restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
                restTestdataHelper.createArendeFromTemplate('luse', intygId, meddelandeId, 'Hur är det med arbetstiden?',
                    'AVSTMN', 'PENDING_INTERNAL_ACTION');
            });
        });

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
            var arende = LuseIntygPage.getArendeById(false, meddelandeId);
            expect(arende.isDisplayed()).toBeTruthy();
        });

        it('fill text', function() {
            LuseIntygPage.getAnswerButton(meddelandeId).click();
            LuseIntygPage.getAnswerBox(meddelandeId).sendKeys('Låt oss slänga in ett svar och se vad som händer.');
        });

        it('push answer button and make sure answered arende is now in the handled list', function() {
            LuseIntygPage.getSendAnswerButton(meddelandeId).click().then(function() {
                var arende = LuseIntygPage.getArendeById(false, meddelandeId);
                expect(arende.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.getArendeAdministrativaSvarTextById(meddelandeId).getText()).toBe('Låt oss slänga in ett svar och se vad som händer.');
            });
        });
    });

});
