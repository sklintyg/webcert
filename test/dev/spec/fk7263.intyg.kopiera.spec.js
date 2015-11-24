'use strict';

var restUtil = require('../../lib/rest.util.js'),
    fkIntyg = require('../../lib/testdata/utkast.fk7263.generate.json');

xdescribe('copy fk intyg', function() {

    var intygsId = null;

    it('should generate an fk7263 intyg', function() {
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

        restUtil.deleteAllUtkast().then(function(response){});

        fkIntyg.patientPersonnummer = '191212121212';
        restUtil.createUtkast('fk7263', fkIntyg).then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;
            expect(intygsId).not.toBe(null);
        });
    });

    describe('Login through the welcome page', function() {
        it('can select user IFV1239877878-104B_IFV1239877878-1042', function() {
            welcomePage.get();

            // login id IFV1239877878-104B_IFV1239877878-1042
            var id = 'IFV1239877878-104B_IFV1239877878-1042';
            welcomePage.login(id);
        });

        it('wait for dashboard', function() {
            browser.sleep(500);
        });

        it('and make sure the correct doctor is logged in', function() {
            expect(sokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
        });
    });

    describe('', function() {
        it('can select user IFV1239877878-104B_IFV1239877878-1042', function() {
            welcomePage.get();

            // login id IFV1239877878-104B_IFV1239877878-1042
            var id = 'IFV1239877878-104B_IFV1239877878-1042';
            welcomePage.login(id);
        });

        it('wait for dashboard', function() {
            browser.sleep(500);
        });

        it('and make sure the correct doctor is logged in', function() {
            expect(sokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
        });
    });

});