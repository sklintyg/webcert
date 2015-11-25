'use strict';
var testUtil = require('../../lib/testdataHelper.util.js');

var WelcomePage = require(pages.welcome),
    SokSkrivIntygPage = require(pages.app.views.sokSkrivIntyg),
    FkIntygPage = require(pages.intygpages.fkIntyg),
    FkUtkastPage = require(pages.intygpages.fkUtkast);

describe('Generate fk intyg', function() {

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            testUtil.createIntygFromTemplate('fkMax').then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });

    describe('Login through the welcome page', function() {
        it('can select user IFV1239877878-104B_IFV1239877878-1042', function() {
            WelcomePage.get();
            WelcomePage.login('IFV1239877878-104B_IFV1239877878-1042');
        });

        it('wait for dashboard', function() {
            browser.sleep(500);
        });

        it('and make sure the correct doctor is logged in', function() {
            expect(SokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
        });
    });

    describe('copy fk intyg to new utkast', function() {
        it('should view fk intyg', function() {
            FkIntygPage.get(intygTemplates.fkMax.intygId);
        });

        it('should copy intyg and view resulting utkast', function() {
            FkIntygPage.copy();
            FkIntygPage.copyDialogConfirm();
            expect(FkUtkastPage.at()).toBeTruthy();
        });

        it('fill missing text capacityForWorkForecastText', function() {
            FkUtkastPage.capacityForWorkForecastText().sendKeys('Litet förtydligande');
            expect(FkUtkastPage.capacityForWorkForecastText().getAttribute('value')).toContain('Litet förtydligande');
        });

        it('should sign copy', function() {
            FkUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                FkUtkastPage.signeraButtonClick();
                expect(FkIntygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
            });
        });
    });
});
