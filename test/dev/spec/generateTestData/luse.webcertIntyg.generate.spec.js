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
var specHelper = wcTestTools.helpers.spec;
var intygGenerator = wcTestTools.intygGenerator;

describe('webcert intyg', function() {

    var intygId = 'luse-arende-test';

    it('generate luse and all arendetypes', function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        restTestdataHelper.deleteUtkast(intygId);

        var intygType = 'luse';
        var intygData = {
            'contents':intygGenerator.getIntygJson({'intygType':intygType,'intygId':intygId}),
            'utkastStatus':'SIGNED',
            'revoked':false,
            'relations':[{'intygsId':intygId,'status':'INTYG'}]
        };
        restTestdataHelper.createWebcertIntyg(intygData).then(function(response){
        });
    });
});
