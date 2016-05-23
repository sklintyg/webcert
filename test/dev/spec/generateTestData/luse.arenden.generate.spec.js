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

/*globals fdescribe, browser*/

'use strict';
var wcTestTools = require('webcert-testtools');
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var arendeFromJsonFactory = wcTestTools.arendeFromJsonFactory;
var specHelper = wcTestTools.helpers.spec;
var intygGenerator = wcTestTools.intygGenerator;

fdescribe('webcert intyg', function() {

    // direct link while intyg doesn't show up in the list: http://localhost:9089/web/dashboard#/intyg/luse/luse-arende-test
    var intygId = 'luse-arende-test';

    it('generate luse and all arendetypes', function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteAllArenden();

        var intygType = 'luse';
        var intygData = {
            'contents':intygGenerator.getIntygJson({'intygType':intygType,'intygId':intygId}),
            'utkastStatus':'SIGNED',
            'revoked':false,
            'relations':[{'intygsId':intygId,'status':'INTYG'}]
        };
        restTestdataHelper.createWebcertIntyg(intygData).then(function(response){

            var index = 1;
            function createArende(meddelande, amne, status, komplettering) {
                console.log('Creating arende:' + amne);
                var arendeId = 'arende-test-' + amne.toLowerCase() + index++;
                var arende = arendeFromJsonFactory.get(meddelande, intygType, intygId, arendeId, amne, status, komplettering);
                restTestdataHelper.createArende(arende).then(function(response){
                    console.log('Response code:' + response.statusCode);
                });
            }

            createArende('Hur är det med arbetstiden?', 'ARBTID', 'PENDING_INTERNAL_ACTION');
            createArende('Vi behöver prata.', 'AVSTMN', 'PENDING_INTERNAL_ACTION');
            createArende('Vi behöver kontakt.', 'KONTKT', 'PENDING_INTERNAL_ACTION');
            createArende('Övriga frågor?', 'OVRIGT', 'PENDING_INTERNAL_ACTION');
            createArende('Komplettera mera.', 'KOMPLT', 'PENDING_INTERNAL_ACTION', [
                {
                    'frageId':'1',
                    'instans':1,
                    'text':'Fixa.'
                },
                {
                    'frageId':'2',
                    'instans':1,
                    'text':'Här har du ett fel.'
                },
                {
                    'frageId':'4',
                    'instans':3,
                    'text':'Här har du ett annat fel.'
                }
            ]);
        });
    });

    // xit this test to keep testdata for manual testing
    xit('clean up intyg and arende', function() {
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteAllArenden();
    });
});
