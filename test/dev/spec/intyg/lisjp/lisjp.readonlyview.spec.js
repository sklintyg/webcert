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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var ReadonlyPage = wcTestTools.pages.readonlypage;
var intygGenerator = wcTestTools.intygGenerator;

describe('show lisjp intyg in read-only-view', function() {

  var intygId = 'lisjp-readonly-1';
  var meddelandeId = 'lisjp-arende-kompl';
  var intygContent = intygGenerator.getIntygJson({'intygType': 'lisjp', 'intygId': intygId});

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
    var testData = {
      'contents': intygContent,
      'utkastStatus': 'SIGNED',
      'revoked': false
    };

    restTestdataHelper.deleteUtkast(intygId);

    restTestdataHelper.createWebcertIntyg(testData).then(function() {
      restTestdataHelper.markeraSkickatTillFK(intygId).then(function() {
        restTestdataHelper.createArendeFromTemplate('lisjp', intygId, meddelandeId, 'kompletteringstext',
            'KOMPLT', 'PENDING_INTERNAL_ACTION', [
              {
                'frageId': '1',
                'instans': 1,
                'text': 'Fixa mig.'
              }]);
      });
    });

  });

  afterAll(function() {
    restTestdataHelper.deleteUtkast(intygId);
  });

  describe('make sure intyg-read-only page has been loaded', function() {
    it('should view fk intyg', function() {
      browser.ignoreSynchronization = true;
      specHelper.setUserOrigin('READONLY').then(function() {
        browser.ignoreSynchronization = false;
        ReadonlyPage.get(intygContent.typ, intygContent.textVersion, intygContent.id);
        expect(ReadonlyPage.isAt()).toBeTruthy();
        expect(ReadonlyPage.getKompletteringLinkElement(meddelandeId).isDisplayed()).toBe(true);
        expect(ReadonlyPage.getKompletteringFrageTextElement(meddelandeId).isDisplayed()).toBe(true);
        expect(ReadonlyPage.getKompletteringFrageTextElement(meddelandeId).getText()).toContain('Hur är det med arbetstiden?');
        //After cliking scroll-to-fraga - we should be able to se it
        ReadonlyPage.getKompletteringLinkElement(meddelandeId).click().then(function() {
          expect(ReadonlyPage.getIntygKompletteringFrageContainer(1).getText()).toContain('Fixa mig.');
        });

      });

    });
  });

});
