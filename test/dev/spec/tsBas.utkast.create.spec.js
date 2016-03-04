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

/*globals pages,describe,it,helpers,utkastTextmap */
'use strict';

var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var tsdBasUtkastPage = wcTestTools.pages.intyg.ts.bas.utkast;
var tsdBasIntygPage = wcTestTools.pages.intyg.ts.bas.intyg;

describe('Create and Sign ts-bas utkast', function() {

    var utkast = null;

    describe('prepare test with intyg', function() {
        it('should generate ts-bas min intyg', function() {
            testdataHelper.createUtkast('ts-bas').then(function(response) {
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
            tsdBasUtkastPage.get(utkast.intygsId);
        });
    });

    describe('fill utkast', function() {

        var tsBasUtkast = {
            korkortstyper: ['D'],
            synDonder: 'Ja',
            synNedsattBelysning: 'Ja',
            synOgonsjukdom: 'Ja',
            synDubbel: 'Ja',
            synNystagmus: 'Ja',
            styrkor: wcTestTools.testdata.values.ts.getRandomStyrka(),
            linser: {
                hoger: 'Ja',
                vanster: 'Ja',
            },
            horsel:{
                yrsel: 'Ja',
                samtal: 'Ja'
            }
            rorelseorganensFunktioner:{
                nedsattning: 'Ja',
                inUtUrFordon: ''
            },
            hjartHjarna: 'Ja',
            hjartSkada: 'Ja',
            hjartRisk: 'Ja',
            diabetes: {
                hasDiabetes: true,
                typ: wcTestTools.utkastTextmap.ts.diabetes.typ.typ1,
                //year: Math.floor((Math.random() * 20) + 1980),
                behandlingsTyper: [wcTestTools.utkastTextmap.ts.diabetes.behandling.endastkost]
            },
            neurologiska: 'Ja',
            epilepsi: 'Ja',
            njursjukdom: 'Ja',
            demens: 'Ja',
            somnVakenhet: 'Ja',
            alkoholMissbruk: 'Ja',
            alkoholVard: 'Ja',
            alkoholProvtagning: 'Ja',
            alkoholLakemedel: 'Ja',
            psykiskSjukdom: 'Ja',
            adhdPsykisk: 'Ja',
            adhdSyndrom: 'Ja',
            sjukhusvard: 'Ja',
            ovrigMedicin: 'Ja',
            kommentar: 'Inget att rapportera'    
        };

        it('first half', function() {
            browser.ignoreSynchronization = true;
            tsdBasUtkastPage.fillInKorkortstyper(tsBasUtkast.korkortstyper); // Intyget avser
            tsdBasUtkastPage.fillInIdentitetStyrktGenom(wcTestTools.utkastTextmap.ts.identitetStyrktGenom.foretagstjanstekort);
            tsdBasUtkastPage.fillInSynfunktioner(tsBasUtkast);
            tsdBasUtkastPage.fillInHorselOchBalanssinne(tsBasUtkast.horsel);
            tsdBasUtkastPage.fillInRorelseorganensFunktioner(tsBasUtkast.rorelseorganensFunktioner);
            tsdBasUtkastPage.fillInHjartOchKarlsjukdomar(tsBasUtkast);
            tsdBasUtkastPage.fillInDiabetes(tsBasUtkast.diabetes);
            tsdBasUtkastPage.fillInHorselOchBalanssinne(tsBasUtkast.horsel);
            tsdBasUtkastPage.fillInNeurologiskaSjukdomar(tsBasUtkast);
        });

        it('second half', function() {
            tsdBasUtkastPage.fillInEpilepsi(tsBasUtkast);
            tsdBasUtkastPage.fillInNjursjukdomar(tsBasUtkast);
            tsdBasUtkastPage.fillInDemens(tsBasUtkast);
            tsdBasUtkastPage.fillInSomnOchVakenhet(tsBasUtkast);
            tsdBasUtkastPage.fillInAlkoholNarkotikaLakemedel(tsBasUtkast);
            tsdBasUtkastPage.fillInPsykiska(tsBasUtkast);
            tsdBasUtkastPage.fillInAdhd(tsBasUtkast);
            tsdBasUtkastPage.fillInSjukhusvard(tsBasUtkast);
            tsdBasUtkastPage.fillInOvrigMedicinering(tsBasUtkast);
            tsdBasUtkastPage.fillInOvrigKommentar(tsBasUtkast);

            var bedomning = {
                stallningstagande: 'behorighet_bedomning',
                behorigheter: ['D'],
                lamplighet: 'Ja'
            };
            tsdBasUtkastPage.fillInBedomning(bedomning);
        });

        it('should be able to sign utkast', function() {
            browser.ignoreSynchronization = false;
            tsdBasUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                tsdBasUtkastPage.signeraButtonClick();
                expect(tsdBasIntygPage.isAt()).toBeTruthy();
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
