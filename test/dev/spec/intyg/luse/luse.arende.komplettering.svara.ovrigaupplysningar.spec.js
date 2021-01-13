/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

/*globals browser,beforeAll,afterAll,protractor*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var intygGenerator = wcTestTools.intygGenerator;
describe('svaranyttintyg (övriga upplysningar) - arende on luse intyg', function() {

  var utkastId;
  var intygId = 'luse-arende-intyg-2';
  var meddelandeId = 'luse-arende-komplt';

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
    var testData = {
      'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
      'utkastStatus': 'SIGNED',
      'revoked': false
    };

    restTestdataHelper.deleteAllArenden().then(function() {
      restTestdataHelper.createWebcertIntyg(testData).then(function() {
        restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
          // Intygstatus is sorted by timestamps with second resolution (no milliseconds)
          // Sleep here to make sure arende timestamp is after signed timestamp
          browser.sleep(1500);
          restTestdataHelper.createArendeFromTemplate('luse', intygId, meddelandeId, 'Hur är det med arbetstiden?',
              'KOMPLT', 'PENDING_INTERNAL_ACTION', [
                {
                  'frageId': '1',
                  'instans': 1,
                  'text': 'Fixa.'
                }
              ],
              'test'
          );

        });
      });
    });
  });

  afterAll(function() {
    restTestdataHelper.deleteArende(meddelandeId);
    restTestdataHelper.deleteUtkast(intygId);
    restTestdataHelper.deleteUtkast(utkastId);
  });

  describe('make sure intyg page has been loaded', function() {
    it('and showing luse intyg', function() {
      LuseIntygPage.get(intygId);
      expect(LuseIntygPage.isAt()).toBeTruthy();
    });
  });

  describe('make sure', function() {
    it('pushed arende is visible', function() {
      var arende = LuseIntygPage.getArendeById(true, meddelandeId);
      expect(arende.isDisplayed()).toBeTruthy();

    });

    it('should display message that intyg has komplettering', function() {
      expect(LuseIntygPage.getIntygHasKompletteringMessage().isDisplayed()).toBeTruthy();
    });

    it('click kanInteKompletteraButton should open dialog', function() {
      LuseIntygPage.kanInteKompletteraButton.click();
      expect(LuseIntygPage.kompletteringsAtgardDialog.isDisplayed()).toBeTruthy();
      expect(LuseIntygPage.kanInteKompletteraModalOvrigaUpplysningar.isPresent()).toBeFalsy();
      LuseIntygPage.kanInteKompletteraModalAnledning1.click();
      expect(LuseIntygPage.kanInteKompletteraModalOvrigaUpplysningar.isDisplayed()).toBeTruthy();
    });

    it('Enter övriga upplysningar text, answer and continue to new utkast', function() {
      LuseIntygPage.kanInteKompletteraModalOvrigaUpplysningar.sendKeys('Den här texten skall finnas i kompletterande utkastets övrigtfält');
      LuseIntygPage.kanInteKompletteraModalSkickaSvarButton.click().then(function() {
        expect(LuseUtkastPage.isAt()).toBeTruthy();
        expect(LuseUtkastPage.ovrigt.isDisplayed()).toBeTruthy();
        expect(LuseUtkastPage.ovrigt.getAttribute("value")).toContain('Den här texten skall finnas i kompletterande utkastets övrigtfält');

      });
    });
  });

  describe('make sure "Svara med nytt intyg" button have changed to "Fortsätt på intygsutkast" on original intyg', function() {
    it('Is showing the Fortsatt button in arende view', function() {
      LuseIntygPage.get(intygId);
      expect(LuseIntygPage.isAt()).toBeTruthy();
      expect(LuseIntygPage.kompletteringUtkastLink.isPresent()).toBeTruthy();
      LuseIntygPage.kompletteringUtkastLink.click();
      expect(LuseUtkastPage.isAt()).toBeTruthy();
      // Extract ID of new utkast so we can delete it when we're done.
      // Save id so it can be removed in cleanup stage.
      specHelper.getUtkastIdFromUrl().then(function(id) {
        utkastId = id;
      });
    });
  });

});
