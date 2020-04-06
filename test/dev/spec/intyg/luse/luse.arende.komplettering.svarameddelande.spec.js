/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('svarameddelande - arende on luse intyg', function() {

  var intygId;
  var meddelandeId = 'luse-arende-komplt';

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();

    var intyg = intygFromJsonFactory.defaultLuse();
    intygId = intyg.id;
    restUtil.createIntyg(intyg);
    var testData = {
      'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
      'utkastStatus': 'SIGNED',
      'revoked': false
    };

    restTestdataHelper.deleteAllArenden().then(function() {
      restTestdataHelper.createWebcertIntyg(testData).then(function() {
        restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
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
      var arende = LuseIntygPage.getArendeById(true, meddelandeId);
      expect(arende.isDisplayed()).toBeTruthy();
    });

    it('click svara pa komplettering', function() {
      LuseIntygPage.kompletteraIntygButton.click();
      expect(LuseUtkastPage.isAt()).toBeTruthy();
      LuseUtkastPage.radera.knapp.click();
      LuseUtkastPage.radera.bekrafta.click();
      expect(LuseIntygPage.utkastDeletedModal.isDisplayed()).toBeTruthy();
      browser.sleep(1500);
      expect(LuseIntygPage.isAt()).toBeTruthy();
    });

    it('click svara med meddelande', function() {
      LuseIntygPage.kanInteKompletteraButton.click();
      expect(LuseIntygPage.kompletteringsAtgardDialog.isDisplayed()).toBeTruthy();
      expect(LuseIntygPage.kanInteKompletteraModalMeddelandeText.isPresent()).toBeFalsy();
      LuseIntygPage.kanInteKompletteraModalAnledning2.click();
      expect(LuseIntygPage.kanInteKompletteraModalMeddelandeText.isDisplayed()).toBeTruthy();
    });

    it('push answer button and make sure answered arende is now in the handled list', function() {
      LuseIntygPage.kanInteKompletteraModalMeddelandeText.sendKeys('Låt oss slänga in ett svar och se vad som händer.');
      LuseIntygPage.kanInteKompletteraModalSkickaSvarButton.click().then(function() {
        var arende = LuseIntygPage.getArendeById(true, meddelandeId); // true = komplettering list
        expect(arende.isDisplayed()).toBeTruthy();
        expect(LuseIntygPage.getKompletteringSvarTextById(meddelandeId)).toBe('Låt oss slänga in ett svar och se vad som händer.');
        expect(LuseIntygPage.kompletteringBesvaradesMedMeddelandeAlert.isDisplayed()).toBeTruthy();
      });
    });

  });

});
