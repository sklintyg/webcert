/*globals pages */
/*globals describe,it,browser */
'use strict';

var WelcomePage = require(pages.welcome),
    SokSkrivIntygPage = require(pages.app.views.sokSkrivIntyg),
    UtkastPage = require(pages.intygpages.fkUtkast),
    IntygPage = require(pages.intygpages.fkIntyg);

describe('Create and Sign FK utkast', function() {

    describe('Login through the welcome page', function() {
        it('can select user IFV1239877878-104B_IFV1239877878-1042', function() {
            WelcomePage.get();

            // login id IFV1239877878-104B_IFV1239877878-1042
            var id = 'IFV1239877878-104B_IFV1239877878-1042';
            WelcomePage.login(id);
        });

        it('wait for dashboard', function() {
            browser.sleep(500);
        });

        it('and make sure the correct doctor is logged in', function() {
            expect(SokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
        });
    });

    describe('create fk', function(){

        it('fill in person number and select', function() {
            SokSkrivIntygPage.selectPersonnummer('191212121212');
        });
//
        it('select fk intyg', function() {
            SokSkrivIntygPage.selectIntygType('string:fk7263');
            SokSkrivIntygPage.continueToUtkast();
        });

        describe('interact with utkast', function() {

            it('check that smittskydd is displayed', function() {

                UtkastPage.whenSmittskyddIsDisplayed().then(function() {
                    expect(UtkastPage.getSmittskyddLabelText()).toContain('Avstängning enligt smittskyddslagen på grund av smitta');
                });

            });

            describe('fill in fk intyg', function() {

                it('nedsatt form8b', function() {
                    UtkastPage.smittskyddCheckboxClick();
                    UtkastPage.nedsattMed25CheckboxClick();
                });

                it('resor form 6a', function() {
                    UtkastPage.travelRadioButtonJaClick();
                    var val = UtkastPage.getCheckedTravelRadioButtonValue();
                    expect(val).toBe('JA');
                });

                it('can sign', function() {
                    UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                        UtkastPage.signeraButtonClick();
                        expect(IntygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
                    });
                });
            });
        });
    });

});