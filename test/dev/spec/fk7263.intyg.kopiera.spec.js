/**
 * Created by bennysce on 09/06/15.
 */
/*globals helpers,pages*/
'use strict';
var specHelper = helpers.spec;
var testdataHelper = helpers.testdata;
var FkIntygPage = pages.intygpages.fkIntyg;
var FkUtkastPage = pages.intygpages.fk7263Utkast;

describe('Generate fk intyg', function() {

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            browser.ignoreSynchronization = false;
            testdataHelper.createIntygFromTemplate('fkMax').then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });

    describe('Login through the welcome page', function() {
        it('with default user', function() {
            specHelper.login();
        });
    });

    describe('copy fk intyg to new utkast', function() {
        it('should view fk intyg', function() {
            FkIntygPage.get(intygTemplates.fkMax.intygId);
        });

        it('should copy intyg and view resulting utkast', function() {
            FkIntygPage.copy();
            FkIntygPage.copyDialogConfirm();
            expect(FkUtkastPage.isAt()).toBeTruthy();
        });

        it('fill missing text capacityForWorkForecastText', function() {
            browser.ignoreSynchronization = true;
            FkUtkastPage.getCapacityForWorkForecastText().sendKeys('Litet förtydligande');
            expect(FkUtkastPage.getCapacityForWorkForecastText().getAttribute('value')).toContain('Litet förtydligande');
        });

        it('should sign copy', function() {
            browser.ignoreSynchronization = false;
            FkUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                FkUtkastPage.signeraButtonClick();
                expect(FkIntygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
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
