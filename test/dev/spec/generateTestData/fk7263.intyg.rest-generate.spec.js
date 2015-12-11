'use strict';

var restUtil = require('../../../lib/util/rest.util.js');
var intygGenerator = require('../../../lib/util/intygGenerator.util.js');

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

        restUtil.deleteAllIntyg();//.then(function(response){});

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

        var intygsId = null;
        restUtil.createIntyg('fk7263', intygGenerator.buildIntyg(intygOptions)).then(function(response){
            intygsId = JSON.parse(response.request.body).id;
            expect(intygsId).not.toBe(null);
        }, function(error) {
            console.log('Error calling createIntyg');
        });
    });
});