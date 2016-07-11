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

/* globals logger, pages, JSON, browser, Promise */


var tsdUtkastPage = pages.intyg.ts.diabetes.utkast;


module.exports = {
    fillIn: function(intyg) {
        'use strict';

        return Promise.all([
            //Ange körkortstyper
            tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper).then(function() {
                logger.info('OK - fillInKorkortstyper :' + JSON.stringify(intyg.korkortstyper));
            }, function(reason) {
                throw ('FEL, fillInKorkortstyper,' + reason);
            }).then(function() {
                //Ange hypoglykemier efter att körkortstyper är ifyllda
                return tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier).then(function() {
                    logger.info('OK - fillInHypoglykemier :' + JSON.stringify(intyg.hypoglykemier));
                }, function(reason) {
                    throw ('FEL, fillInHypoglykemier,' + reason);
                });
            }),

            //Ange Identitet styrkt genom
            tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function() {
                logger.info('OK - fillInIdentitetStyrktGenom :' + JSON.stringify(intyg.identitetStyrktGenom));
            }, function(reason) {
                throw ('FEL, fillInIdentitetStyrktGenom,' + reason);
            }),

            function() {
                browser.ignoreSynchronization = true;
            },

            //Ange postadress osv
            browser.element(by.id('patientPostadress')).sendKeys('Postadress 1'),
            browser.element(by.id('patientPostnummer')).sendKeys('66130'),
            browser.element(by.id('patientPostort')).sendKeys('postort'),

            //Ange allmänt
            tsdUtkastPage.fillInAllmant(intyg.allmant).then(function() {
                logger.info('OK - fillInAllmant :' + JSON.stringify(intyg.allmant));
            }, function(reason) {
                throw ('FEL, fillInAllmant,' + reason);
            }),

            tsdUtkastPage.fillInSynintyg(intyg.synintyg).then(function() {
                logger.info('OK - fillInSynintyg :' + JSON.stringify(intyg.synintyg));
            }, function(reason) {
                throw ('FEL, fillInSynintyg,' + reason);
            }),

            function() {
                browser.ignoreSynchronization = false;
            },

            tsdUtkastPage.fillInBedomning(intyg.bedomning).then(function() {
                logger.info('OK - fillInBedomning :' + JSON.stringify(intyg.bedomning));
            }, function(reason) {
                throw ('FEL, fillInBedomning,' + reason);
            })
        ]);
    }
};
