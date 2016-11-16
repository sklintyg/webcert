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

/* globals logger, pages, browser, JSON, Promise */


var tsBasUtkastPage = pages.intyg.ts.bas.utkast;
module.exports = {
    fillIn: function(intyg) {
        'use strict';
        var promiseArr = [];
        promiseArr.push(tsBasUtkastPage.fillInKorkortstyper(intyg.korkortstyper, 'intygetAvserForm').then(function() {
                logger.info('OK - fillInKorkortstyper, ' + JSON.stringify(intyg.korkortstyper));

                //Hörsel och balans kräver att körkortstyper är angivna
                return tsBasUtkastPage.fillInHorselOchBalanssinne(intyg.horsel)
                    .then(function() {
                        logger.info('OK - fillInHorselOchBalanssinne: ' + JSON.stringify(intyg.horsel));

                        //Rörelseorganens funktioner kräver att körkortstyper är angivna
                        return tsBasUtkastPage.fillInRorelseorganensFunktioner(intyg.rorelseorganensFunktioner)
                            .then(function() {
                                logger.info('OK - fillInRorelseorganensFunktioner, ' + JSON.stringify(intyg.rorelseorganensFunktioner));
                            }, function(reason) {
                                throw ('FEL, fillInRorelseorganensFunktioner,' + JSON.stringify(intyg.rorelseorganensFunktioner) + reason);
                            });

                    }, function(reason) {
                        throw ('FEL, fillInHorselOchBalanssinne,' + JSON.stringify(intyg.horsel) + reason);
                    });
            },
            function(reason) {
                throw ('FEL, fillInKorkortstyper, ' + JSON.stringify(intyg.korkortstyper) + reason);
            }));

        promiseArr.push(tsBasUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function() {
            logger.info('OK - fillInIdentitetStyrktGenom:' + intyg.identitetStyrktGenom.toString());
        }, function(reason) {
            throw ('FEL, fillInIdentitetStyrktGenom,' + reason);
        }));

        // promiseArr.push(tsBasUtkastPage.fillInPatientAdress(person.adress).then(function() {
        //     logger.info('OK - fillInPatientAdress :' + JSON.stringify(person.adress));
        // }, function(reason) {
        //     throw ('FEL, fillInPatientAdress,' + reason);
        // }));

        browser.ignoreSynchronization = true;

        // Synfunktioner
        promiseArr.push(tsBasUtkastPage.fillInSynfunktioner(intyg).then(function() {
            logger.info('OK - fillInSynfunktioner');
        }, function(reason) {
            throw ('FEL, fillInSynfunktioner,' + reason);
        }));

        promiseArr.push(tsBasUtkastPage.fillInHjartOchKarlsjukdomar(intyg).then(function() {
            logger.info('OK - fillInHjartOchKarlsjukdomar');
        }, function(reason) {
            throw ('FEL, fillInHjartOchKarlsjukdomar,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInDiabetes(intyg.diabetes).then(function() {
            logger.info('OK - fillInDiabetes: ' + JSON.stringify(intyg.diabetes));
        }, function(reason) {
            throw ('FEL, fillInDiabetes:, ' + JSON.stringify(intyg.diabetes) + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInNeurologiskaSjukdomar(intyg).then(function() {
            logger.info('OK - fillInNeurologiskaSjukdomar');
        }, function(reason) {
            throw ('FEL, fillInNeurologiskaSjukdomar,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInEpilepsi(intyg).then(function() {
            logger.info('OK - fillInEpilepsi');
        }, function(reason) {
            throw ('FEL, fillInEpilepsi,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInNjursjukdomar(intyg).then(function() {
            logger.info('OK - fillInNjursjukdomar');
        }, function(reason) {
            throw ('FEL, fillInNjursjukdomar,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInDemens(intyg).then(function() {
            logger.info('OK - fillInDemens');
        }, function(reason) {
            throw ('FEL, fillInDemens,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInSomnOchVakenhet(intyg).then(function() {
            logger.info('OK - fillInSomnOchVakenhet');
        }, function(reason) {
            throw ('FEL, fillInSomnOchVakenhet,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInAlkoholNarkotikaLakemedel(intyg).then(function() {
            logger.info('OK - fillInAlkoholNarkotikaLakemedel');
        }, function(reason) {
            throw ('FEL, fillInAlkoholNarkotikaLakemedel,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInPsykiska(intyg).then(function() {
            logger.info('OK - fillInPsykiska');
        }, function(reason) {
            throw ('FEL, fillInPsykiska,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInAdhd(intyg).then(function() {
            logger.info('OK - fillInAdhd');
        }, function(reason) {
            throw ('FEL, fillInAdhd,' + reason);
        }));
        promiseArr.push(tsBasUtkastPage.fillInSjukhusvard(intyg).then(function() {
            logger.info('OK - fillInSjukhusvard');
        }, function(reason) {
            throw ('FEL, fillInSjukhusvard,' + reason);
        }));

        promiseArr.push(tsBasUtkastPage.fillInOvrigMedicinering(intyg).then(function() {
            logger.info('OK - fillInOvrigMedicinering');
        }, function(reason) {
            throw ('FEL, fillInOvrigMedicinering,' + reason);
        }));

        promiseArr.push(tsBasUtkastPage.fillInBedomning(intyg.bedomning).then(function() {
            logger.info('OK - fillInBedomning');
        }, function(reason) {
            throw ('FEL, fillInBedomning,' + reason);
        }));

        return Promise.all(promiseArr)
            .then(function(value) {
                browser.ignoreSynchronization = false;
            });
    }
};
