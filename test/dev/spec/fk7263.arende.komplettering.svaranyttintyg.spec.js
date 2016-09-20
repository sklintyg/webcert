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

        it('click svara pa komplettering', function() {
            Fk7263IntygPage.svaraMedNyttIntyg(meddelandeId);
            expect(Fk7263IntygPage.getKompletteringsDialog().modalDialogHeader.isDisplayed()).toBeTruthy();
        });

        it('should go to utkast page after komplettera med nytt intyg button is clicked', function() {
            Fk7263IntygPage.getKompletteringsDialog().svaraMedNyttIntygKnapp.click(); //sendKeys(protractor.Key.SPACE);
            expect(Fk7263UtkastPage.isAt()).toBeTruthy();

            // Extract ID of new utkast so we can delete it when we're done.
            // Save id so it can be removed in cleanup stage.
            browser.getCurrentUrl().then(function(url) {
                utkastId = url.split('/').pop();
            });
        });
    });

    describe('show related intyg', function() {
        it('should not be visible as default', function() {
            expect(Fk7263UtkastPage.relatedIntygList.isDisplayed()).toBeFalsy();
        });

        it('should become visible when toggled', function() {
            Fk7263UtkastPage.togglerelatedIntygList.click();
            expect(Fk7263UtkastPage.relatedIntygList.isDisplayed()).toBeTruthy();
            expect(Fk7263UtkastPage.relatedIntygList.rows().count()).toBe(3);
            expect(Fk7263UtkastPage.relatedIntygList.row(2).visa.getText()).toBe('Visas nu');
            expect(Fk7263UtkastPage.relatedIntygList.row(2).relation.getText()).toBe('Komplettering');
            expect(Fk7263UtkastPage.relatedIntygList.row(2).status.getText()).toBe('Utkast, kan signeras');
            expect(Fk7263UtkastPage.relatedIntygList.row(3).relation.getText()).toBe('');
            expect(Fk7263UtkastPage.relatedIntygList.row(3).status.getText()).toBe('Signerat');
        });

        it('should return to intyg when Visa button is clicked', function() {
            Fk7263UtkastPage.relatedIntygList.row(3).visa.click();
            expect(Fk7263IntygPage.isAt()).toBeTruthy();
        });
    });

    describe('make sure "Fortsätt på intygsutkast" buttons exists', function() {
        it('is showing fk intyg', function() {
            expect(Fk7263IntygPage.isAt()).toBeTruthy();
        });

        it('Click the Fortsatt button in arende view', function() {
            Fk7263IntygPage.fortsattPaIntygsutkast(meddelandeId);
            expect(Fk7263UtkastPage.isAt()).toBeTruthy();
        });
    });

    describe('make sure we go to the previously created utkast when clicking the appropriate button in the modal', function() {
        it('Click related intyg button again, assert is showing fk intyg', function() {
            Fk7263UtkastPage.togglerelatedIntygList.click();
            Fk7263UtkastPage.relatedIntygList.row(3).visa.click();
            expect(Fk7263IntygPage.isAt()).toBeTruthy();
        });

        it('Is showing the Fortsatt button in modal', function() {
            Fk7263IntygPage.svaraMedNyttIntyg(meddelandeId);
            expect(Fk7263IntygPage.getKompletteringsDialog().modalDialogHeader.isDisplayed()).toBeTruthy();
            Fk7263IntygPage.getKompletteringsDialog().fortsattPaIntygsutkastKnapp.click();
            expect(Fk7263UtkastPage.isAt()).toBeTruthy();
        });
    });

});
