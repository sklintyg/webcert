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

describe('uthopp - arende on luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var meddelandeId = 'luse-arende-komplt';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false
        };

        // If were nog ignoring sync while setting user, protractor complains that it cannot sync with angular on the testability page loaded during setUserOrigin
        browser.ignoreSynchronization = true;
        specHelper.setUserOrigin('UTHOPP').then(function() {
            browser.ignoreSynchronization = false;
            restTestdataHelper.deleteUtkast(intygId);
            restTestdataHelper.createWebcertIntyg(testData).then(function() {
                restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
                    restTestdataHelper.createArendeFromTemplate('luse', intygId, meddelandeId, 'Hur är det med arbetstiden?',
                        'KOMPLT', 'PENDING_INTERNAL_ACTION', [
                            {
                                'frageId':'1',
                                'instans':1,
                                'text':'Fixa.'
                            }
                        ]);
                });
            });
        });
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
    });

    describe('make sure intyg page has been loaded', function() {
        it('and showing fk intyg', function() {
            LuseIntygPage.get(intygId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
    });

    describe('make sure', function() {
        it('pushed arende is visible', function() {
            var arende = LuseIntygPage.getArendeById(false, meddelandeId);
            expect(arende.isDisplayed()).toBeTruthy();
        });

        it('click svara pa komplettering', function() {
            expect(LuseIntygPage.getKompletteraIntygButton(meddelandeId).isPresent()).toBeFalsy();
            expect(LuseIntygPage.getUthoppKompletteraSvaraButton(meddelandeId).isPresent()).toBeTruthy();
            LuseIntygPage.getUthoppKompletteraSvaraButton(meddelandeId).click();
            expect(LuseIntygPage.kompletteringsAtgardDialog.isDisplayed()).toBeTruthy();
        });

        it('svara med nytt intyg should not be displayed', function() {
            expect(LuseIntygPage.getKompletteringsDialogLamnaOvrigaUpplysningar().isPresent()).toBeFalsy();
        });

        it('svara med meddelande should be displayed', function() {
            LuseIntygPage.getKompletteringsDialogSvaraMedMeddelandeButton().click();
            expect(LuseIntygPage.getAnswerButton(meddelandeId).isDisplayed()).toBeTruthy();
        });
    });

});
