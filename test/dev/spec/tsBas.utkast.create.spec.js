/**
 * Created by bennysce on 09/06/15.
 */

/*globals pages,describe,it,helpers,utkastTextmap */
'use strict';

var specHelper = helpers.spec;
var testdataHelper = helpers.testdata;
var tsdBasUtkastPage = pages.intygpages['ts-basUtkast'];
var tsdBasIntygPage = pages.intygpages.tsBasIntyg;

describe('Create and Sign ts-diabetes utkast', function() {

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
            horselYrsel: 'Ja',
            horselSamtal: 'Ja',
            rorOrgNedsattning: 'Ja',
            rorOrgInUt: 'Ja',
            hjartHjarna: 'Ja',
            hjartSkada: 'Ja',
            hjartRisk: 'Ja',
            diabetes: {
                has: true,
                typ: utkastTextmap.ts.diabetes.typ.typ1,
                //year: Math.floor((Math.random() * 20) + 1980),
                behandling: [utkastTextmap.ts.diabetes.behandling.endastkost]
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
            tsdBasUtkastPage.fillInIdentitetStyrktGenom(utkastTextmap.ts.identitetStyrktGenom.foretagstjanstekort);
            tsdBasUtkastPage.fillInSynfunktioner(tsBasUtkast);
            tsdBasUtkastPage.fillInHorselOchBalanssinne(tsBasUtkast);
            tsdBasUtkastPage.fillInRorelseorganensFunktioner(tsBasUtkast);
            tsdBasUtkastPage.fillInHjartOchKarlsjukdomar(tsBasUtkast);
            tsdBasUtkastPage.fillInDiabetes(tsBasUtkast);
            tsdBasUtkastPage.fillInHorselOchBalanssinne(tsBasUtkast);
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
                expect(tsdBasIntygPage.viewCertAndQaIsDisplayed()).toBeTruthy();
            });
        });

        describe('remove test intyg', function() {
            it('should clean up all utkast after the test', function() {
                testdataHelper.deleteAllUtkast();
            });
        });
    });
});