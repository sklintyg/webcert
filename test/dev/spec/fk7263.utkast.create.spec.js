/*globals pages */
/*globals describe,it,helpers */
'use strict';

var specHelper = helpers.spec;
var testdataHelper = helpers.testdata;
var UtkastPage = pages.intygpages.fk7263Utkast;
var IntygPage = pages.intygpages.fkIntyg;

describe('Create and Sign FK utkast', function() {

    describe('Login through the welcome page', function() {
        it('with user', function() {
            specHelper.login();
            specHelper.createUtkastForPatient('191212121212', 'fk7263');
        });
    });

    describe('create fk', function(){

        describe('interact with utkast', function() {

            it('check that smittskydd is displayed', function() {

                UtkastPage.whenSmittskyddIsDisplayed().then(function() {
                    expect(UtkastPage.getSmittskyddLabelText()).toContain('Avstängning enligt smittskyddslagen på grund av smitta');
                });

            });

            describe('fill in fk intyg', function() {

                // speeds up utkast filling by not waiting for angular events, promises etc.
                browser.ignoreSynchronization = true;

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

                    // reset
                    browser.ignoreSynchronization = false;

                    UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                        UtkastPage.signeraButtonClick();
                        expect(IntygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
                    });
                });
            });
        });
    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            testdataHelper.deleteAllIntyg();
            testdataHelper.deleteAllUtkast();
        });
    });

});