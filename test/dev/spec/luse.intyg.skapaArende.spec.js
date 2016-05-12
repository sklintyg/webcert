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

xdescribe('Skapa ärende luse intyg', function() {

    var intygId = testdataHelper.generateTestGuid();

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            "contents":{
                "grundData":{"signeringsdatum":"2016-05-03T13:22:13.000",
                    "skapadAv": {
                        "personId": "TSTNMT2321000156-103F",
                        "fullstandigtNamn": "Leonie Koehl",
                        "forskrivarKod": "0000000",
                        "befattningar": [ ],
                        "specialiteter": [ ],
                        "vardenhet": {
                            "enhetsid": "TSTNMT2321000156-1039",
                            "enhetsnamn": "NMT vg1 ve2",
                            "postadress": "NMT gata 2",
                            "postnummer": "12345",
                            "postort": "Testhult",
                            "telefonnummer": "0101112131415",
                            "epost": "enhet2@webcert.invalid.se",
                            "vardgivare": {
                                "vardgivarid": "TSTNMT2321000156-1002",
                                "vardgivarnamn": "NMT vg1"
                            },
                            "arbetsplatsKod": "1234567890"
                        }
                    },
                    "patient":{"personId":"191212121212","fullstandigtNamn":"Tolvan Tolvansson","fornamn":"Tolvan","efternamn":"Tolvansson","postadress":"Svensson, Storgatan 1, PL 1234","postnummer":"12345","postort":"Småmåla","samordningsNummer":false}
                },
                "textVersion":"1.0","undersokningAvPatienten":"2016-05-03","journaluppgifter":"2016-05-03","anhorigsBeskrivningAvPatienten":"2016-05-03","annatGrundForMU":"2016-05-03","annatGrundForMUBeskrivning":"test","kannedomOmPatient":"2016-05-03","underlagFinns":false,"underlag":[],"sjukdomsforlopp":"test","diagnoser":[{"diagnosKod":"Z65","diagnosKodSystem":"ICD_10_SE","diagnosBeskrivning":"Problem som har samband med andra psykosociala förhållanden","diagnosDisplayName":""}],"diagnosgrund":"test","nyBedomningDiagnosgrund":false,"funktionsnedsattningIntellektuell":"test","funktionsnedsattningKommunikation":"test","funktionsnedsattningKoncentration":"test","funktionsnedsattningPsykisk":"test","funktionsnedsattningSynHorselTal":"test","funktionsnedsattningBalansKoordination":"test","funktionsnedsattningAnnan":"test","aktivitetsbegransning":"test","pagaendeBehandling":"test","avslutadBehandling":"test","planeradBehandling":"test","substansintag":"test","medicinskaForutsattningarForArbete":"test","formagaTrotsBegransning":"test","ovrigt":"test","kontaktMedFk":true,"anledningTillKontakt":"test","tillaggsfragor":[],
                "typ":"luse","id":intygId
            },
            "utkastStatus":"SIGNED",
            "revoked":false,
            "relations":[{"intygsId":intygId,"status":"INTYG"}]
        };
        restTestdataHelper.createWebcertIntyg(intygId, testData).then(function(response) {
        });
    });

    describe('make sure intyg is ready to be sent', function() {
        it('should view fk intyg', function() {
            LuseIntygPage.get(intygId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });
        
        it('should make sure message that intyg must be sent to create new arenden is shown', function() {
            expect(LuseIntygPage.arendeIntygNotSentYetMessage.isDisplayed()).toBeTruthy();
        });
    });
/*
    describe('send intyg', function() {
        it('click send intyg', function() {
            LuseIntygPage.send().then(function(){
                expect(LuseIntygPage.skicka.statusSendInprogress.isDisplayed()).toBeTruthy();
            });
        });
    });

    describe('send new arende', function() {
        it('open new arende panel', function() {
            LuseIntygPage.sendNewArende('Här kommer en liten fråga till FK', 'Övrigt').then(function() {
                expect(LuseIntygPage.arendeSentMessage.isDisplayed()).toBeTruthy();
            });
        });
    });
*/
/*    describe('remove arende', function() {
        it('should clean up created arende after the test', function() {
            restTestdataHelper.deleteAllArenden();
        });
    });

    describe('remove test intyg', function() {
        it('should clean up intyg after the test', function() {
            restTestdataHelper.deleteIntyg(intygId);
        });
    });*/
});
