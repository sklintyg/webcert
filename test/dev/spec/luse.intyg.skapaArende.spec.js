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

fdescribe('Generate fk luse intyg', function() {

    var intygId = 'bad42b7c-c1e4-475d-b0e9-3b6b36a819cb';
//    var intygId = testdataHelper.generateTestGuid();
    var intyg;
/*
    describe('prepare test with intyg', function() {
        it('should generate fk luse intyg', function() {
            browser.ignoreSynchronization = false;
            var testData = {"contents":{"grundData":{"signeringsdatum":"2016-05-03T13:22:13.000","skapadAv":{"personId":"IFV1239877878-1049","fullstandigtNamn":"Jan Nilsson","forskrivarKod":"0000000","befattningar":[],"specialiteter":[],"vardenhet":{"enhetsid":"IFV1239877878-1042","enhetsnamn":"WebCert-Enhet1","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"IFV1239877878-1041","vardgivarnamn":"WebCert-Vårdgivare1"},"arbetsplatsKod":"1234567890"}},"patient":{"personId":"191212121212","fullstandigtNamn":"Tolvan Tolvansson","fornamn":"Tolvan","efternamn":"Tolvansson","postadress":"Svensson, Storgatan 1, PL 1234","postnummer":"12345","postort":"Småmåla","samordningsNummer":false}},"textVersion":"1.0","undersokningAvPatienten":"2016-05-03","journaluppgifter":"2016-05-03","anhorigsBeskrivningAvPatienten":"2016-05-03","annatGrundForMU":"2016-05-03","annatGrundForMUBeskrivning":"test","kannedomOmPatient":"2016-05-03","underlagFinns":false,"underlag":[],"sjukdomsforlopp":"test","diagnoser":[{"diagnosKod":"Z65","diagnosKodSystem":"ICD_10_SE","diagnosBeskrivning":"Problem som har samband med andra psykosociala förhållanden","diagnosDisplayName":""}],"diagnosgrund":"test","nyBedomningDiagnosgrund":false,"funktionsnedsattningIntellektuell":"test","funktionsnedsattningKommunikation":"test","funktionsnedsattningKoncentration":"test","funktionsnedsattningPsykisk":"test","funktionsnedsattningSynHorselTal":"test","funktionsnedsattningBalansKoordination":"test","funktionsnedsattningAnnan":"test","aktivitetsbegransning":"test","pagaendeBehandling":"test","avslutadBehandling":"test","planeradBehandling":"test","substansintag":"test","medicinskaForutsattningarForArbete":"test","formagaTrotsBegransning":"test","ovrigt":"test","kontaktMedFk":true,"anledningTillKontakt":"test","tillaggsfragor":[],
                "typ":"luse"},
                "statuses":[{"type":"RECEIVED","target":"HSVARD","timestamp":"2016-05-03T13:22:13.000"}],
                "revoked":false,"relations":[{"intygsId":"bad42b7c-c1e4-475d-b0e9-3b6b36a819cb","status":"INTYG"}]};
            restTestdataHelper.createWebcertIntyg(intygId, testData).then(function(response) {
                intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });
*/
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
