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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restHelper = wcTestTools.helpers.restTestdata;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var UnsignedIntygPage = wcTestTools.pages.unsignedPage;

xdescribe('Testa sekretessmarkering för läkare', function() {

    var intygsId;
    var utkastId;
    var arendeId = 'luse-arende-avstmn-hantera';

    beforeAll(function() {
        browser.ignoreSynchronization = false;

        var intyg = intygFromJsonFactory.defaultLuse();
        intygsId = intyg.id;

        restUtil.createIntyg(intyg).then(function(response) {
            var intyg = JSON.parse(response.request.body);
            expect(intyg.id).not.toBeNull();
        }, function() {
            logger.error('Error calling createIntyg');
        });

    });

    afterAll(function() {
        restUtil.deleteUtkast(utkastId);
        restUtil.deleteIntyg(intygsId);

        // Explicitly make sure the PU-service is enabled and s-markering removed from
        // Tolvansson.
        restUtil.setPuServiceState(true).then(function() {
            restHelper.setSekretessmarkering('191212121212', false).then(function() {
                specHelper.logout();
            });
        });
    });

    it('login through the welcome page with default user', function() {
        specHelper.login();
    });


    describe('Sekretessmarkera Tolvan och testa ett flöde', function() {

        it('set sekr and view patient', function() {
            restHelper.setSekretessmarkering('191212121212', true).then(function() {
                SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            });
        });

        it('set sekr and view patient', function() {

            expect(element(by.id('wc-sekretessmarkering-icon-19121212-1212')).isPresent()).toBe(true);
            expect(element(by.id('wc-sekretessmarkering-text-19121212-1212')).isPresent()).toBe(true);
            expect(element(by.id('wc-avliden-icon-19121212-1212')).isPresent()).toBe(false);
            expect(element(by.id('wc-avliden-text-19121212-1212')).isPresent()).toBe(false);
        });

        it('LUSE-intyget skall ej gå att förnya', function() {
            expect(element(by.id('fornyaBtn-' + intygsId)).isPresent()).toBe(true);
        });

        it('Gå in på LUSE-intyget och verifiera att ikon och text syns', function() {
            element(by.id('showBtn-' + intygsId)).sendKeys(protractor.Key.SPACE);
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(element(by.id('wc-sekretessmarkering-icon-19121212-1212')).isPresent()).toBe(true);
            expect(element(by.id('wc-sekretessmarkering-text-19121212-1212')).isPresent()).toBe(true);
        });

        it('Verifiera varningstext för utskrift av s-märkt', function() {
            element(by.id('downloadprint')).sendKeys(protractor.Key.SPACE);
            expect(element(by.id('button1print-patient-sekretessmarkerad')).isPresent()).toBe(true);
            expect(element(by.id('button2print-patient-sekretessmarkerad')).isPresent()).toBe(true);
        });

        it('Stäng diaglogen', function() {
            element(by.id('button2print-patient-sekretessmarkerad')).sendKeys(protractor.Key.SPACE);
        })
    });


    describe('Avaktivera PU-tjänsten', function() {

        it('Ladda om intyget', function() {
            LuseIntygPage.get(intygsId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });

        it('Avaktivera PU-tjänsten', function() {
            restUtil.setPuServiceState(false).then(function() {

            });
        });


        it('Verifiera felmeddelande vid utskrift och PU-tjänsten är nere', function() {
            element(by.id('downloadprint')).sendKeys(protractor.Key.SPACE);
            expect(element(by.id('dialogErrorText')).isPresent()).toBe(true);
            element(by.id('dialogErrorOkBtn')).sendKeys(protractor.Key.SPACE);
        });

        it('Verifiera felmeddelande visas vid skicka och PU-tjänsten är nere', function() {
            element(by.id('sendBtn')).sendKeys(protractor.Key.SPACE);
            expect(element(by.id('dialogErrorText')).isPresent()).toBe(true);
            element(by.id('dialogErrorOkBtn')).sendKeys(protractor.Key.SPACE);
        });

        it('Ladda om intyget, verifiera att felmeddelande syns', function() {
            LuseIntygPage.get(intygsId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(element(by.id('intyg-load-error')).isPresent()).toBe(true);
        });

        it('Återställ PU-tjänsten och ladda om intyget', function() {
            restUtil.setPuServiceState(true).then(function() {
                LuseIntygPage.get(intygsId);
                expect(LuseIntygPage.isAt()).toBeTruthy();
            });
        });
    });

    describe('Skapa ett LUSE utkast', function() {
        it('Skapa utkast', function() {
            SokSkrivIntygPage.get();
            specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för sjukersättning');

            specHelper.getUtkastIdFromUrl().then(function(id) {
                utkastId = id;
            });
        });

        it('Verifiera att ikon och text för s-märkning syns', function() {
            expect(element(by.id('wc-sekretessmarkering-icon-19121212-1212')).isPresent()).toBe(true);
            expect(element(by.id('wc-sekretessmarkering-text-19121212-1212')).isPresent()).toBe(true);
        });

        it('Verifiera att varningsmeddelande för utskrift syns', function() {
            element(by.id('skriv-ut-utkast')).sendKeys(protractor.Key.SPACE);
            expect(element(by.id('print-patient-sekretessmarkerad')).isPresent()).toBe(true);
            element(by.id('button2print-patient-sekretessmarkerad')).sendKeys(protractor.Key.SPACE);
        });

        it('Gå till ej signerade utkast', function() {
            UnsignedIntygPage.get();
        });

        it('Verifiera att utkastet finns i tabellen med sekretessmarkeringsikon', function() {
            expect(element(by.id('wc-sekretessmarkering-icon-' + utkastId)).isPresent()).toBe(true);
        });
    });

    describe('Skapa en fråga på det signerade intyget', function() {

        it('Gå till intygssidan', function() {
            LuseIntygPage.get(intygsId);
            expect(LuseIntygPage.isAt()).toBeTruthy();
        });

        it('Skicka intyget', function() {
            LuseIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE);
            browser.wait(LuseIntygPage.skicka.dialogKnapp.isDisplayed())
                .then(LuseIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE));


            element.all(by.id('#sendBtn')).then(function(items) {
                expect(items.length).toBe(0);
            });
        });

        it('Skapa ärende på intyget', function() {
            restHelper.createArendeFromTemplate('luse', intygsId, arendeId, 'Hur är det med arbetstiden?',
                'AVSTMN', 'PENDING_INTERNAL_ACTION');
        });

        it('Klicka på tabben för Fråga/svar', function() {
            element(by.css('a[ng-href="/web/dashboard#/enhet-arenden"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).getText()).toBe('1');
            expect(element.all(by.css('.wc-table-striped tr td button')).first().getText()).toBe('Visa');
        });

        it('Verifiera s-markeringsikon bredvid frågan i listan', function() {
            expect(element(by.css('.patient-alert')).isPresent()).toBe(true);
        });
    });
});
