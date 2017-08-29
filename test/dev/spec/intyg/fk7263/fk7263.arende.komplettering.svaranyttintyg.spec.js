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
var Fk7263IntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;
var Fk7263UtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var intygGenerator = wcTestTools.intygGenerator;

describe('arende on fk7263 intyg', function() {

    var utkastId;
    var intygId = 'fk7263-arende-intyg-1';
    var meddelandeId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents': intygGenerator.getIntygJson({'intygType': 'fk7263', 'intygId': intygId}),
            'utkastStatus': 'SIGNED',
            'revoked': false
        };

        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.createWebcertIntyg(testData).then(function() {
            restTestdataHelper.createFragasvarFromTemplate(meddelandeId, intygId, '19121212-1212','KOMPLETTERING_AV_LAKARINTYG', 'PENDING_INTERNAL_ACTION',
                 {
                    'falt': '1',
                    'text': 'Fixa detta.'
                 }, false, function(internReferens) {
                    // Spara ner internReferensen.
                    meddelandeId = internReferens;
                });
        });
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteUtkast(utkastId);
    });

    describe('make sure intyg page has been loaded', function() {
        it('and showing fk intyg', function() {
            Fk7263IntygPage.get(intygId);
            expect(Fk7263IntygPage.isAt()).toBeTruthy();
        });
    });

    describe('make sure', function() {
        it('pushed fragasvar is visible', function() {
            var fragasvar = Fk7263IntygPage.getQAById(false, meddelandeId);
            expect(fragasvar.isDisplayed()).toBeTruthy();
        });

        it('click kan inte komplettera', function() {
            Fk7263IntygPage.clickKanInteKomplettera(meddelandeId);
            expect(Fk7263IntygPage.getKompletteringsDialog().modalDialogHeader.isDisplayed()).toBeTruthy();
            Fk7263IntygPage.get(intygId); //Close modal by reloading utkast.
        });

        it('should go to utkast page after komplettera intyg button is clicked', function() {
            Fk7263IntygPage.clickKompletteraIntyg(meddelandeId);
            expect(Fk7263UtkastPage.isAt()).toBeTruthy();

            // Extract ID of new utkast so we can delete it when we're done.
            // Save id so it can be removed in cleanup stage.
            browser.getCurrentUrl().then(function(url) {
                utkastId = url.split('/').pop();
            });
        });
    });

    describe('make sure "Fortsätt på intygsutkast" buttons exists', function() {
        it('Click the Fortsatt button in arende view', function() {
            Fk7263IntygPage.get(intygId);
            Fk7263IntygPage.clickFortsattPaUtkast(meddelandeId);
            expect(Fk7263UtkastPage.isAt()).toBeTruthy();
        });
    });

});
