//var pages = require('pages');
var WelcomePage = require(pages.welcome),
    SokSkrivIntygPage = require(pages.app.views.sokSkrivIntyg),
    UtkastPage = require(pages.intygpages.utkast),
    IntygPage = require(pages.intygpages.intyg);

var welcomePage = new WelcomePage(),
    sokSkrivIntygPage = new SokSkrivIntygPage(),
    utkastPage = new UtkastPage(),
    intygPage = new IntygPage();

describe('Create and Sign FK utkast', function() {

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
            expect(sokSkrivIntygPage.getDoctorText()).toContain("Åsa Andersson");
        });
    });

    describe('create fk', function(){

        it('fill in person number and select', function() {
            sokSkrivIntygPage.selectPersonnummer('191212121212');
        });
//
        it('select fk intyg', function() {
            sokSkrivIntygPage.selectIntygType('string:fk7263');
            sokSkrivIntygPage.continue();
        });

        describe('interact with utkast', function() {

            it('check that smittskydd is displayed', function() {

                utkastPage.whenSmittskyddIsDisplayed().then(function() {
                    expect(utkastPage.getSmittskyddLabelText()).toContain("Avstängning enligt smittskyddslagen på grund av smitta");
                });

            });

            describe('fill in fk intyg', function() {

                it('nedsatt form8b', function() {
                    utkastPage.smittskyddCheckboxClick();
                    utkastPage.nedsattMed25CheckboxClick();
                });

                it('resor form 6a', function() {
                    utkastPage.travelRadioButtonJaClick();
                    var val = utkastPage.getCheckedTravelRadioButtonValue();
                    expect(val).toBe('JA');
                });

                it('can sign', function() {
                    utkastPage.whenSigneraButtonIsEnabled().then(function() {
                        utkastPage.signeraButtonClick();
                        expect(intygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
                    });
                });
            });
        });
    });

});