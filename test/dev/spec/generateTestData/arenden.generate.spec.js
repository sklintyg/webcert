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

/*globals fdescribe, browser*/

'use strict';
var wcTestTools = require('webcert-testtools');
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var arendeFromJsonFactory = wcTestTools.arendeFromJsonFactory;
var specHelper = wcTestTools.helpers.spec;
var intygGenerator = wcTestTools.intygGenerator;

describe('webcert intyg', function() {

  var arendeIndex = 1;

  it('generate all intyg and all arendetypes', function() {

    function createIntygWithArenden(intygType) {

      var intygId = intygType + '-arende-test';

      // Delete utkast also removes associated Arenden
      restTestdataHelper.deleteUtkast(intygId);

      var intygData = {
        'contents': intygGenerator.getIntygJson({'intygType': intygType, 'intygId': intygId}),
        'utkastStatus': 'SIGNED',
        'revoked': false
      };
      restTestdataHelper.createWebcertIntyg(intygData).then(function(response) {

        function createArende(arendeOptions) {
          logger.debug('Creating arende:' + arendeOptions.amne);

          arendeOptions.meddelandeId = 'arende-test-' + arendeOptions.amne.toLowerCase() + arendeIndex++;
          arendeOptions.intygType = intygType;
          arendeOptions.intygId = intygId;

          var arende = arendeFromJsonFactory.get(arendeOptions);
          restTestdataHelper.createArende(arende).then(function(response) {
            logger.debug('Response code:' + response.statusCode);
          });

          return arendeOptions.meddelandeId;
        }

        createArende({meddelande: 'Hur är det med arbetstiden?', amne: 'AVSTMN', status: 'PENDING_INTERNAL_ACTION'});
        createArende({meddelande: 'Vi behöver prata.', amne: 'AVSTMN', status: 'PENDING_INTERNAL_ACTION'});
        createArende({meddelande: 'Vi behöver kontakt.', amne: 'KONTKT', status: 'PENDING_INTERNAL_ACTION'});
        createArende({meddelande: 'Övriga frågor?', amne: 'OVRIGT', status: 'PENDING_INTERNAL_ACTION'});

        var meddelandeId = createArende({
          meddelande: 'Komplettera mera.',
          amne: 'KOMPLT',
          status: 'PENDING_INTERNAL_ACTION',
          kompletteringar: [
            {
              'frageId': '1',
              'instans': 1,
              'text': 'Fixa.'
            },
            {
              'frageId': '2',
              'instans': 1,
              'text': 'Här har du ett fel.'
            },
            {
              'frageId': '4',
              'instans': 3,
              'text': 'Här har du ett annat fel.'
            }
          ]
        });

        createArende({
          meddelande: 'Du har facking glömt att komplettera loser!',
          amne: 'PAMINN',
          status: 'PENDING_INTERNAL_ACTION',
          paminnelseMeddelandeId: meddelandeId
        });
      });
    }

    browser.ignoreSynchronization = false;
    specHelper.login();

    createIntygWithArenden('luse');
    createIntygWithArenden('luae_na');
    createIntygWithArenden('luae_fs');
    createIntygWithArenden('lisjp');
  });

  // xit this test to keep testdata for manual testing
  xit('clean up intyg and arende', function() {
    restTestdataHelper.deleteUtkast('luae_na-arende-test');
    restTestdataHelper.deleteUtkast('luae_fs-arende-test');
    restTestdataHelper.deleteUtkast('luse-arende-test');
    restTestdataHelper.deleteUtkast('lisjp-arende-test');
  });
});
