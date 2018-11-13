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
var WelcomePage = wcTestTools.pages.welcome;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Testa sekretessmarkering för vårdadmin', function() {

    var utkastId,
        unhandledCertsCount = 0,
        intygTypeVersion = '1.1';

    beforeAll(function() {
        browser.ignoreSynchronization = false;

        // Börja med att säkerställa att Tolvan ej är s-markerad
        restHelper.setSekretessmarkering('191212121212', false);
    });

    afterAll(function() {
        restUtil.deleteUtkast(utkastId);

        // Explicitly make sure the PU-service is enabled and s-markering removed from
        // Tolvansson.
        restUtil.setPuServiceState(true).then(function() {
            restHelper.setSekretessmarkering('191212121212', false).then(function() {
                SokSkrivIntygPage.get();
                specHelper.logout();
            });
        });
    });

    it('login through the welcome page with vardadmin user', function() {

        WelcomePage.get();
        WelcomePage.login('IFV1239877878-104N_IFV1239877878-1045');
        specHelper.waitForAngularTestability();
    });

    describe('Skapa ett utkast', function() {
        it('Skapa utkast för Tolvansson', function() {
            SokSkrivIntygPage.get();

            if(element(by.id('stat-unitstat-unsigned-certs-count')).isPresent()) {
                element(by.id('stat-unitstat-unsigned-certs-count')).getText().then(function(value) {
                    unhandledCertsCount = parseInt(value, 10);
                });
            }

            specHelper.createUtkastForPatient('191212121212', 'luse');
            specHelper.getUtkastIdFromUrl().then(function(id) {
                utkastId = id;
            });
        });
    });

    describe('Gå till listan över fråga/svar', function() {

        it('Räkna antal obesvarade frågor i headern innan vi sekretessmarkerat', function() {
            element(by.css('a[ng-href="/#/enhet-arenden"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).isPresent()).toBe(false);
        });

        it('Räkna antal ej signerade utkast i headern innan vi sekretessmarkerat', function() {
            expect(element(by.id('stat-unitstat-unsigned-certs-count')).getText()).toBe((unhandledCertsCount + 1) + '');
        });
    });

    describe('Sekretessmarkera Tolvan', function() {
        it('set sekr and view patient', function() {
            restHelper.setSekretessmarkering('191212121212', true).then(function() {
                SokSkrivIntygPage.get();
                SokSkrivIntygPage.selectPersonnummer('19121212-1212');
                expect(element(by.id('wc-sekretessmarkering-text-191212121212')).isPresent()).toBe(true);
            });
        });

        it('Räkna antal obesvarade frågor i headern efter vi sekretessmarkerat', function() {
            element(by.css('a[ng-href="/#/enhet-arenden"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).isPresent()).toBe(false);
            expect(element(by.css('wc-no-arenden-message div:last-of-type'))
                .getText()).toBe('Det finns inga ohanterade ärenden för den enhet eller de enheter du är inloggad på.');
        });

        it('Räkna antal ej signerade utkast i headern efter vi sekretessmarkerat', function() {
            expect(element(by.id('stat-unitstat-unsigned-certs-count')).isPresent()).toBe(false);
        });

        it('Försök öppna utkastet via direktlänk', function() {
            browser.get('/#/luse/'+ intygTypeVersion + '/edit/' + utkastId + '/').then(function() {
                expect(element(by.id('error-panel')).isPresent()).toBe(true);
            });
        });
    });
});
