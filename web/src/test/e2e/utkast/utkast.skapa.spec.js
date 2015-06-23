var WelcomePage = require('../welcome/welcome.page.js'),
    SokSkrivPage = require('../sokskriv/sokskriv.page.js'),
    UtkastPage = require('utkast.page.js'),
    IntygPage = require('../intyg/intyg.page.js'),
    WebCertRestClient = require('../utils/webcert.restclient.page.js'),
    fkIntyg = require('../data/fkIntyg.json'),

    welcomePage = new WelcomePage(),
    sokSkrivPage = new SokSkrivPage(),
    utakstPage = new UtkastPage(),
    intygPage = new IntygPage(),
    webCertRestClient = new WebCertRestClient();


xdescribe('Create utkast', function() {

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
            expect(sokSkrivPage.getDoctorText()).toContain("Åsa Andersson");
        });
    });

    describe('create fk', function(){

        var utkasts = [];

        it('create utkast', function(){

            fkIntyg.id = 'intyg-pro-1';
            fkIntyg.patientPersonnummer = '191212121212';
            fkIntyg.grundData.patient.personId = '191212121212';

            webCertRestClient.createUtkast('fk7263', fkIntyg).then(function(utkast){
                utkasts.push(utkast);
            });

        });

        it('fill in person number and select', function() {
            sokSkrivPage.selectPersonnummer('191212121212');
        });

        it('verify that the created utkast is in the list', function() {
            sokSkrivPage.selectIntygType(1);
            sokSkrivPage.continue();
        });

    });

});