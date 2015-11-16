var WelcomePage = require('../page/welcome.page.js'),
    SokSkrivPage = require('../page/sokskriv.page.js');
//    UtkastPage = require('utkast.page.js'),
//    IntygPage = require('../intyg/intyg.page.js')
var WebCertRestClient = require('../utils/webcert.restclient.js');
var fkIntyg = require('../data/utkast.fk7263.create.json');

var welcomePage = new WelcomePage(),
    sokSkrivPage = new SokSkrivPage();
//    utkastPage = new UtkastPage(),
//    intygPage = new IntygPage();

//var webCertRestClient = new WebCertRestClient();

describe('Create utkast', function() {

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
            expect(sokSkrivPage.getDoctorText()).toContain('Åsa Andersson');
        });
    });

    xdescribe('create fk', function(){

        var utkasts = [];

        xit('create utkast', function(){

            fkIntyg.id = 'intyg-pro-1';
            fkIntyg.patientPersonnummer = '191212121212';
            fkIntyg.grundData.patient.personId = '191212121212';
console.log('dude');
            webCertRestClient.createUtkast('fk7263', fkIntyg).then(function(utkast){
                utkasts.push(utkast);
                console.log('dudecomplete');
            });
            console.log('dude2');
        });

        it('fill in person number and select', function() {
            sokSkrivPage.selectPersonnummer('191212121212');
        });

        it('verify that the created utkast is in the list', function() {
            sokSkrivPage.selectIntygType('string:fk7263');
            sokSkrivPage.continue();
        });

    });

});