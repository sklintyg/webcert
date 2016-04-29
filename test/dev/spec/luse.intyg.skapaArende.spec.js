/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by bennysce on 09/06/15.
 */
/*globals browser,JSON,protractor*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;

describe('Generate fk luse intyg', function() {

    var intygId = testdataHelper.generateTestGuid();
    var intyg;

    describe('prepare test with intyg', function() {
        it('should generate fk luse intyg', function() {
            browser.ignoreSynchronization = false;
            restTestdataHelper.createIntygFromTemplate('luseMax', intygId).then(function(response) {
                intyg = JSON.parse(response.request.body);
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

    describe('make sure intyg is ready to be sent', function() {
        it('should view fk intyg', function() {
            LuseIntygPage.get(intygId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
        
        it('should make sure message that intyg must be sent to create new arenden is shown', function() {
            expect(LuseIntygPage.notSentMessage.isDisplayed()).toBeTruthy();
        });
    });

    describe('send intyg', function() {
        it('click send intyg', function() {
            LuseIntygPage.send().then(function(){
                expect(LuseIntygPage.sentMessage1.isDisplayed()).toBeTruthy();
            });
        });
    });
    /*
    describe('send new arende', function() {
        it('open new arende panel', function() {
            expect(true).toBeTruthy();
        });
    });
*/
    describe('remove test intyg', function() {
        it('should clean up intyg after the test', function() {
            restTestdataHelper.deleteIntyg(intygId);
        });
    });
});
