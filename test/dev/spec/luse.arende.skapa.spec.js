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
/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var intygGenerator = wcTestTools.intygGenerator;

describe('Skapa ärende luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var arendeId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents':intygGenerator.getIntygJson({'intygType':'luse','intygId':intygId}),
            'utkastStatus':'SIGNED',
            'revoked':false,
            'relations':[{'intygsId':intygId,'status':'INTYG'}]
        };
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.createWebcertIntyg(testData);
    });

    afterAll(function() {
        restTestdataHelper.deleteUtkast(intygId);
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

    describe('send intyg', function() {
        it('click send intyg', function() {
            LuseIntygPage.send().then(function(){
                expect(LuseIntygPage.skicka.statusSendInprogress.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.newArendeBtn.isPresent()).toBeTruthy();
            });
        });
    });

    describe('send new arende', function() {
        it('open new arende panel', function() {
            LuseIntygPage.sendNewArende('Här kommer en liten fråga till FK', 'Övrigt').then(function() {
                //console.log(1,element(by.repeater('arendeListItem in arendeList').row(0)));
                //console.log(2,element.all(by.repeater('arendeListItem in arendeList').getText()));
                var first = element.all(by.model('arendeListItem.arende.fraga.vidarebefordrad')).first();
                first.getAttribute('id').then(function(id) {
                    var firstPart = id.substring(0,27);
                    var secondPart = id.substring(27);
                    expect(firstPart).toBe('unhandled-mark-as-notified-');
                    arendeId = secondPart;
                });
                expect(LuseIntygPage.arendeSentMessage.isDisplayed()).toBeTruthy();
            });
        });
    });

});
