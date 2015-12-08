var restUtil = require('../../../lib/util/rest.util.js'),
    fkIntyg = require('../../../lib/testdata/utkast.fk7263.generate.json');

describe('Generate fk utkast', function() {

    it('should generate an fk7263 utkast', function() {
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

        restUtil.deleteAllUtkast();//.then(function(response){});

        var intygsId = null;
        fkIntyg.patientPersonnummer = '191212121212';
        restUtil.createUtkast('fk7263', fkIntyg).then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;
            expect(intygsId).not.toBe(null);
        });
    });
});