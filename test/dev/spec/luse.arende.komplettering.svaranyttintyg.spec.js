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
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var intygGenerator = wcTestTools.intygGenerator;

describe('arende on luse intyg', function() {

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

        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.createWebcertIntyg(testData).then(function() {
            restTestdataHelper.createArendeFromTemplate('luse', intygId, meddelandeId, 'Hur är det med arbetstiden?',
                'KOMPLT', 'PENDING_INTERNAL_ACTION', [
                    {
                        'frageId': '1',
                        'instans': 1,
                        'text': 'Fixa.'
                    }
                ]);
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
            LuseIntygPage.getSvaraPaKompletteringButton(meddelandeId).click();
            expect(LuseIntygPage.kompletteringsAtgardDialog.isDisplayed()).toBeTruthy();
        });

        it('should go to utkast page after komplettera med nytt intyg button is clicked', function() {
            LuseIntygPage.kompletteraMedNyttIntygButton.click();
            expect(LuseUtkastPage.isAt()).toBeTruthy();
        });
    });

    describe('show related intyg', function() {
        it('should not be visible as default', function() {
            expect(LuseUtkastPage.relatedIntygList.isDisplayed()).toBeFalsy();
        });

        it('should become visible when toggled', function() {
            LuseUtkastPage.togglerelatedIntygList.click();
            expect(LuseUtkastPage.relatedIntygList.isDisplayed()).toBeTruthy();
            expect(LuseUtkastPage.relatedIntygList.rows().count()).toBe(3);
            expect(LuseUtkastPage.relatedIntygList.row(2).visa.getText()).toBe('Visas nu');
            expect(LuseUtkastPage.relatedIntygList.row(2).relation.getText()).toBe('Komplettering');
            expect(LuseUtkastPage.relatedIntygList.row(2).status.getText()).toBe('Utkast, kan signeras');
            expect(LuseUtkastPage.relatedIntygList.row(3).relation.getText()).toBe('');
            expect(LuseUtkastPage.relatedIntygList.row(3).status.getText()).toBe('Signerat');
        });

        it('should return to intyg when Visa button is clicked', function() {
            LuseUtkastPage.relatedIntygList.row(3).visa.click();
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
    });

});
