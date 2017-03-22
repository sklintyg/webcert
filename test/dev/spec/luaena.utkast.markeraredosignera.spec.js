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

/*globals afterAll,beforeAll,describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.luaeNA.utkast;
var restUtil = wcTestTools.restUtil;

fdescribe('Create partially complete luae_na utkast and mark as ready to sign', function() {

    var utkastId = null,
        data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        restUtil.registerEnhetAsDjupintegrerad('TSTNMT2321000156-1039', 'Vårdenhetens namn', 'vgid', 'vgnamn', false,
            true);
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212',
            'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga');
    });

    describe('Skapa luae_na', function() {

        describe('Fyll i utkastet', function() {

            it('Spara undan intygsId från URL', function() {

                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });
                data = wcTestTools.testdata.fk.LUAE_NA.getRandom(utkastId);
            });
        });

        describe('Byt till djupintegrerad vårdadministratör på utkastet och markera som klar för signering', function() {

            it('Bli vårdadmin och djupintegrerad, ladda sedan om sidan', function() {
                browser.ignoreSynchronization = true;
                specHelper.setUserRole("VARDADMINISTRATOR").then(function() {
                    specHelper.setUserOrigin("DJUPINTEGRATION").then(function() {
                        browser.ignoreSynchronization = false;
                        UtkastPage.get(utkastId);
                    });
                });
            });

            it('Verifiera att knappen för Markera som klart att signera syns', function() {
                expect(UtkastPage.isMarkeraSomKlartAttSigneraButtonDisplayed()).toBeTruthy();
            });

            it('Klicka på knappen för Markera som klart att signera syns', function() {
                UtkastPage.markeraSomKlartAttSigneraButtonClick();

                // Vänta på att den modala dialogen öppnas och Yes-knappen blir synlig
                expect(UtkastPage.markeraKlartAttSigneraModalYesButton.isDisplayed()).toBeTruthy();
            });

            it('Klicka knappen för Markera utkast redo att signera', function() {
                UtkastPage.markeraKlartAttSigneraModalYesButton.sendKeys(protractor.Key.SPACE);

                expect(UtkastPage.markeradKlarForSigneringText.isDisplayed()).toBeTruthy();
            });

            it('Gör REST-anrop till notification-stubben, tillse att vår post finns med KFSIGN', function() {
                restUtil.queryNotificationStub().then(function(data) {

                    // Detta borde kunna göras snyggare med jsonPath...
                    for (var a = 0; a < data.body.length; a++) {
                        var statusUppdatering = data.body[a];
                        if (statusUppdatering.intyg['intygs-id'].extension === utkastId &&
                            statusUppdatering.handelse.handelsekod.code === 'KFSIGN') {
                            return true;
                        }
                    }
                    fail('No matching status message was found, failing test!!');
                });
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
        restUtil.deregisterEnhetAsDjupintegrerad('TSTNMT2321000156-1039');
    });

});
