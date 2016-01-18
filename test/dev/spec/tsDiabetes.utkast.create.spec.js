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
/*globals describe,it */
'use strict';

var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var tsdUtkastPage = wcTestTools.pages.intyg.ts.diabetes.utkast;
var tsdIntygPage = wcTestTools.pages.intyg.ts.diabetes.intyg;

describe('Create and Sign ts-diabetes utkast', function() {

    var utkast = null;

    describe('prepare test with intyg', function() {
        it('should generate ts-diabetes min intyg', function() {
            testdataHelper.createUtkast('ts-diabetes').then(function(response) {
                utkast = response.body;
                expect(utkast.intygsId).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });

    describe('User', function() {
        it('should login and open utkast', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            tsdUtkastPage.get(utkast.intygsId);
        });
    });

    describe('fill utkast', function() {

        it('should be able to sign utkast', function() {

            browser.ignoreSynchronization = true;

            // Intyget avser
            tsdUtkastPage.fillInKorkortstyper(['D']);

            // Identiteten är styrkt genom
            tsdUtkastPage.fillInIdentitetStyrktGenom(wcTestTools.utkastTextmap.ts.identitetStyrktGenom.pass);
        });

        it('allmant', function() {

            var allmant = {
                year: '2015',
                typ: wcTestTools.utkastTextmap.ts.diabetes.typ.typ1,
                behandling: {
                    typer: [wcTestTools.utkastTextmap.ts.diabetes.behandling.endastkost,
                        wcTestTools.utkastTextmap.ts.diabetes.behandling.insulin],
                    insulinYear: '2000'
                }
            };

            // Allmänt
            tsdUtkastPage.fillInAllmant(allmant);
        });

        it('hypo', function() {

            // Hypoglykemier
            tsdUtkastPage.fillInHypoglykemier({
                a: 'Ja',
                b: 'Nej',
                f: 'Ja',
                g: 'Nej'
            });

            //Synintyg
            tsdUtkastPage.fillInSynintyg({ a: 'Ja' });

            //Bedömning
            var bedomning = {
                stallningstagande: 'behorighet_bedomning',
                behorigheter: ['D'],
                lamplighet: 'Ja'
            };

            tsdUtkastPage.fillInBedomning(bedomning);
        });

        it('should be able to sign utkast', function() {
            browser.ignoreSynchronization = false;
            tsdUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                tsdUtkastPage.signeraButtonClick();
                expect(tsdIntygPage.isAt()).toBeTruthy();
            });
        });

        describe('remove test intyg', function() {
            it('should clean up all utkast after the test', function() {
                testdataHelper.deleteUtkast(utkast.intygsId);
                testdataHelper.deleteIntyg(utkast.intygsId);
            });
        });
    });
});
