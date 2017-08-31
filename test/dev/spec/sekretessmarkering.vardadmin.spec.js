/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

    var utkastId;

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

    it('login through the welcome page with default user', function() {

        WelcomePage.get();
        WelcomePage.login('sture-adminsson_TSTNMT2321000156-1077');

        specHelper.waitForAngularTestability();
    });

    describe('Skapa ett utkast', function() {
        it('Skapa utkast för Tolvansson', function() {
            SokSkrivIntygPage.get();
            specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för sjukersättning');
            browser.getCurrentUrl().then(function(url) {
                if (url.endsWith('/')) {
                    var parts = url.split('/');
                    utkastId = parts[parts.length - 2];
                } else {
                    utkastId = url.split('/').pop();
                }
            });
        });
    });

    describe('Gå till listan över fråga/svar', function() {

        it('Räkna antal obesvarade frågor i headern innan vi sekretessmarkerat', function() {
            element(by.css('a[ng-href="/web/dashboard#/unhandled-qa"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).getText()).toBe('19');
        });

        it('Räkna antal ej signerade utkast i headern innan vi sekretessmarkerat', function() {
            expect(element(by.id('stat-unitstat-unsigned-certs-count')).getText()).toBe('1');
        });
    });

    describe('Sekretessmarkera Tolvan', function() {

        it('set sekr and view patient', function() {
            restHelper.setSekretessmarkering('191212121212', true).then(function() {
                SokSkrivIntygPage.get();
                SokSkrivIntygPage.selectPersonnummer('19121212-1212');
                expect(element(by.id('wc-sekretessmarkering-icon-19121212-1212')).isPresent()).toBe(true);
                expect(element(by.id('wc-sekretessmarkering-text-19121212-1212')).isPresent()).toBe(true);
                element.all(by.css('#intygType option')).then(function(items) {
                    expect(items.length).toBe(1);
                });
                expect(element(by.css('#current-list-noResults-unit strong')).getText()).toBe('Inga intyg hittades.');
            });
        });

        it('Räkna antal obesvarade frågor i headern innan vi sekretessmarkerat', function() {
            element(by.css('a[ng-href="/web/dashboard#/unhandled-qa"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).getText()).toBe('3');
        });

        it('Räkna antal ej signerade utkast i headern innan vi sekretessmarkerat', function() {
            expect(element(by.id('stat-unitstat-unsigned-certs-count')).isPresent()).toBe(false);
        });

        it('Försök öppna utkastet via direktlänk', function() {
            browser.get('/web/dashboard#/luse/edit/' + utkastId + '/').then(function() {
                expect(element(by.id('error-panel')).isPresent()).toBe(true);
            });
        });
    });
});
