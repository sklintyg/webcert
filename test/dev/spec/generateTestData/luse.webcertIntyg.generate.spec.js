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

'use strict';
var wcTestTools = require('webcert-testtools');
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var arendeFromJsonFactory = wcTestTools.arendeFromJsonFactory;
var specHelper = wcTestTools.helpers.spec;
var intygGenerator = wcTestTools.intygGenerator;

fdescribe('webcert intyg', function() {

    it('generate luse', function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        var intygId = 'luse-arende-test';
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
            function createArende(amne, status) {
                console.log('Creating arende:' + amne);
                var arendeId = 'arende-test-' + amne.toLowerCase() + index++;
                var arende = arendeFromJsonFactory.get(intygType, intygId, arendeId, amne, status);
                restTestdataHelper.createArende(arende).then(function(response){
                    console.log('Response code:' + response.statusCode);
                });
            }

            createArende('ARBTID', 'PENDING_INTERNAL_ACTION');
            createArende('AVSTMN', 'PENDING_INTERNAL_ACTION');
            createArende('KONTKT', 'PENDING_INTERNAL_ACTION');
            createArende('OVRIGT', 'PENDING_INTERNAL_ACTION');
            createArende('KOMPLT', 'PENDING_INTERNAL_ACTION');
            //createArende('OVRIGT', 'CLOSED');
            //createArende('PAMINN', 'PENDING_INTERNAL_ACTION');
        });
    });
});
