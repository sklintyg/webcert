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

/* globals logger, pages, browser, JSON, Promise */


var tsBasUtkastPage = pages.intyg.ts.bas.utkast;
module.exports = {
    fillIn: function(intyg) {
        'use strict';
        //Returnera Promise kedja
        return new Promise(function(resolve) {
            logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
            browser.ignoreSynchronization = true;
            resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
        }).then(function() {
            logger.info('Fyller i patient address det sista vi gör (common)');
        }).then(function() {
            //Intyget Avser
            return tsBasUtkastPage.fillInKorkortstyper(intyg.korkortstyper, 'intygetAvserForm').then(function() {
                logger.info('OK - fillInKorkortstyper, ' + JSON.stringify(intyg.korkortstyper));
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, fillInKorkortstyper, ' + JSON.stringify(intyg.korkortstyper) + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function() {
                logger.info('OK - fillInIdentitetStyrktGenom:' + intyg.identitetStyrktGenom.toString());
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, fillInIdentitetStyrktGenom,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInSynfunktioner(intyg).then(function() {
                logger.info('(1) OK - fillInSynfunktioner');
            }, function(reason) {
                console.trace(reason);
                throw ('(1) FEL, fillInSynfunktioner,' + reason);
            });
        }).then(function() {
            //Hörsel och balans kräver att körkortstyper är angivna
            return tsBasUtkastPage.fillInHorselOchBalanssinne(intyg.horsel).then(function() {
                logger.info('(2) OK - fillInHorselOchBalanssinne: ' + JSON.stringify(intyg.horsel));
            }, function(reason) {
                console.trace(reason);
                throw ('(2) FEL, fillInHorselOchBalanssinne,' + JSON.stringify(intyg.horsel) + reason);
            });
        }).then(function() {
            //Rörelseorganens funktioner kräver att körkortstyper är angivna
            return tsBasUtkastPage.fillInRorelseorganensFunktioner(intyg.rorelseorganensFunktioner).then(function() {
                logger.info('(3) OK - fillInRorelseorganensFunktioner, ' + JSON.stringify(intyg.rorelseorganensFunktioner));
            }, function(reason) {
                console.trace(reason);
                throw ('(3) FEL, fillInRorelseorganensFunktioner,' + JSON.stringify(intyg.rorelseorganensFunktioner) + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInHjartOchKarlsjukdomar(intyg).then(function() {
                logger.info('(4) OK - fillInHjartOchKarlsjukdomar');
            }, function(reason) {
                console.trace(reason);
                throw ('(4) FEL, fillInHjartOchKarlsjukdomar,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInDiabetes(intyg.diabetes).then(function() {
                logger.info('(5) OK - fillInDiabetes: ' + JSON.stringify(intyg.diabetes));
            }, function(reason) {
                console.trace(reason);
                throw ('(5) FEL, fillInDiabetes:, ' + JSON.stringify(intyg.diabetes) + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInNeurologiskaSjukdomar(intyg).then(function() {
                logger.info('(6) OK - fillInNeurologiskaSjukdomar');
            }, function(reason) {
                console.trace(reason);
                throw ('(6) FEL, fillInNeurologiskaSjukdomar,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInEpilepsi(intyg).then(function() {
                logger.info('(7) OK - fillInEpilepsi');
            }, function(reason) {
                console.trace(reason);
                throw ('(7) FEL, fillInEpilepsi,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInNjursjukdomar(intyg).then(function() {
                logger.info('(8) OK - fillInNjursjukdomar');
            }, function(reason) {
                console.trace(reason);
                throw ('(8) FEL, fillInNjursjukdomar,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInDemens(intyg).then(function() {
                logger.info('(9) OK - fillInDemens');
            }, function(reason) {
                console.trace(reason);
                throw ('(9) FEL, fillInDemens,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInSomnOchVakenhet(intyg).then(function() {
                logger.info('(10) OK - fillInSomnOchVakenhet');
            }, function(reason) {
                console.trace(reason);
                throw ('(10) FEL, fillInSomnOchVakenhet,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInAlkoholNarkotikaLakemedel(intyg).then(function() {
                logger.info('(11) OK - fillInAlkoholNarkotikaLakemedel');
            }, function(reason) {
                console.trace(reason);
                throw ('(11) FEL, fillInAlkoholNarkotikaLakemedel,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInPsykiska(intyg).then(function() {
                logger.info('(12) OK - fillInPsykiska');
            }, function(reason) {
                console.trace(reason);
                throw ('(12) FEL, fillInPsykiska,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInAdhd(intyg).then(function() {
                logger.info('(13) OK - fillInAdhd');
            }, function(reason) {
                console.trace(reason);
                throw ('(13) FEL, fillInAdhd,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInSjukhusvard(intyg).then(function() {
                logger.info('(14) OK - fillInSjukhusvard');
            }, function(reason) {
                console.trace(reason);
                throw ('(14) FEL, fillInSjukhusvard,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInOvrigMedicinering(intyg).then(function() {
                logger.info('(15) OK - fillInOvrigMedicinering');
            }, function(reason) {
                console.trace(reason);
                throw ('(15) FEL, fillInOvrigMedicinering,' + reason);
            });
        }).then(function() {
            return tsBasUtkastPage.fillInBedomning(intyg.bedomning).then(function() {
                logger.info('OK - fillInBedomning: ' + JSON.stringify(intyg.bedomning));
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, fillInBedomning,' + reason);
            });
        });

    }
};
