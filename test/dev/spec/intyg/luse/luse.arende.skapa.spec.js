/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
/*globals browser,beforeAll,afterAll,protractor*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var intygGenerator = wcTestTools.intygGenerator;

describe('Skapa ärende luse intyg', function() {

    var intygId = 'luse-arende-intyg-1';
    var meddelandeId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        var testData = {
            'contents':intygGenerator.getIntygJson({'intygType':'luse','intygId':intygId}),
            'utkastStatus':'SIGNED',
            'revoked':false
        };
        restTestdataHelper.deleteUtkast(intygId);
        restTestdataHelper.deleteAllArenden();
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
        it('should switch to arende tab', function() {
            LuseIntygPage.switchToArendeTab();
        });

        it('should make sure message that intyg must be sent to create new arenden is shown', function() {
            expect(LuseIntygPage.arendeIntygNotSentYetMessage.isDisplayed()).toBeTruthy();
            expect(LuseIntygPage.arendeFilterKompletteringsbegaran.isPresent()).toBeFalsy();
            expect(LuseIntygPage.arendeFilterAdministrativafragor.isPresent()).toBeFalsy();
        });
    });

    describe('send intyg', function() {
        it('click send intyg', function() {
            LuseIntygPage.send().then(function(){
                expect(LuseIntygPage.skicka.statusSent.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.arendeIntygNotSentYetMessage.isPresent()).toBeFalsy();
                LuseIntygPage.arendeFilterAdministrativafragor.click();
                expect(LuseIntygPage.arendeText.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.arendeAmne.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.arendeSend.isDisplayed()).toBeTruthy();
                expect(LuseIntygPage.arendeSend.isEnabled()).toBeFalsy();
            });
        });
    });

    describe('send new arende', function() {
        it('open new arende panel', function() {
            LuseIntygPage.sendNewArende('Här kommer en liten fråga till FK', 'Övrigt').then(function(arendeId) {
                // When arende is sent the textarea and subject should be cleared
                expect(LuseIntygPage.arendeText.getText()).toBe('');
                expect(LuseIntygPage.arendeAmneSelected.getText()).toBe('Välj ämne');
                expect(LuseIntygPage.arendeSend.isEnabled()).toBeFalsy();

                expect(LuseIntygPage.getArendeAdministrativaFragorAmneById(arendeId)).toBe('Övrigt');
                expect(LuseIntygPage.getArendeAdministrativaFragorTextById(arendeId)).toBe('Här kommer en liten fråga till FK');
                expect(LuseIntygPage.getArendeHandledCheckbox(arendeId).getAttribute('checked')).toBeFalsy();
            });
        });
    });

});
