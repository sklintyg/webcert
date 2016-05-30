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
/*globals browser,beforeAll,afterAll,protractor*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var intygGenerator = wcTestTools.intygGenerator;

describe('arende on luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var arendeId = 'luse-arende-komplt';

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
                'KOMPLT', 'PENDING_INTERNAL_ACTION', [
                    {
                        'frageId': '1',
                        'instans': 1,
                        'text': 'Fixa.'
                    }
                ]);
        });
        restTestdataHelper.createIntygFromTemplate('luseMax', intygId);
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteIntyg(intygId);
    });

    describe('make sure intyg page has been loaded', function() {
        it('and showing fk intyg', function() {
            LuseIntygPage.get(intygId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
    });

    describe('make sure', function() {
        it('pushed arende is visible', function() {
            var arende = LuseIntygPage.getArendeById(false, arendeId);
            expect(arende.isDisplayed()).toBeTruthy();
        });

        it('click svara med nytt intyg', function() {
            LuseIntygPage.getKanEjSvaraCheck(arendeId).sendKeys(protractor.Key.SPACE);
            expect(LuseIntygPage.getAnswerButton(arendeId).isDisplayed()).toBeTruthy();
        });

        it('push answer button and make sure answered arende is now in the handled list', function() {
            LuseIntygPage.getAnswerBox(arendeId).sendKeys('Låt oss slänga in ett svar och se vad som händer.');
            LuseIntygPage.getAnswerButton(arendeId).sendKeys(protractor.Key.SPACE).then(function() {
                var arende = LuseIntygPage.getArendeById(true, arendeId); // true = handled list
                expect(arende.isDisplayed()).toBeTruthy();
            });
        });
    });

});
