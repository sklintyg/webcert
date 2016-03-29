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
/*global JSON*/
var restUtil = require('../../../webcertTestTools/util/rest.util.js');
var intygGenerator = require('../../../webcertTestTools/util/intygGenerator.util.js');

describe('Generate fk intyg', function() {

    it('should login rest client and generate an fk7263 intyg', function() {
        // login with doctor Jan Nilsson
        restUtil.login({
            'fornamn': 'Jan',
            'efternamn': 'Nilsson',
            'hsaId': 'IFV1239877878-1049',
            'enhetId': 'IFV1239877878-1042',
            'lakare': true,
            'forskrivarKod': '2481632'
        }).then(function(data) {
            console.log('Login OK');
        });

        var intygOptions = {
            personnr : '19121212-1212',
            patientNamn : 'Tolvan Tolvansson',
            //issuerId : '',
            issuer : 'IFV1239877878-104B',
            issued : '2013-04-01',
            validFrom : '2013-04-01',
            validTo : '2013-04-11',
            enhetId : 'IFV1239877878-1042',
            //enhet : '',
            vardgivarId : 'IFV1239877878-1041',
            intygType : 'fk7263',
            intygId : 'intyg-prtr-1',
            sent : false,
            revoked : false
            //idTemplate : '',
            //mall : 'M',
            //from : '',
            //to : '',
            //deletedByCareGiver : '',
            //responseStatus : '',
            //template : ''
        };

        //console.log("======================================================INTYGOPTIONS");
        //console.log(intygOptions);

        var intygsId = null;
        restUtil.createIntyg(intygGenerator.buildIntyg(intygOptions)).then(function(response){
            intygsId = JSON.parse(response.request.body).id;
            expect(intygsId).not.toBe(null);
        }, function(error) {
            console.log('Error calling createIntyg');
        });
    });
});
