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

/* globals logger, pages, JSON, browser, Promise */


var tsdUtkastPage = pages.intyg.ts.diabetes.utkast;


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
            //Intyget avser körkortstyper
            return tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper).then(function() {
                logger.info('OK - fillInKorkortstyper :' + JSON.stringify(intyg.korkortstyper));
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, fillInKorkortstyper,' + reason);
            });
        }).then(function() {
            //Ange Identitet styrkt genom
            return tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function() {
                logger.info('OK - fillInIdentitetStyrktGenom :' + JSON.stringify(intyg.identitetStyrktGenom));
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, fillInIdentitetStyrktGenom,' + reason);
            });
        }).then(function() {
            //Ange allmänt
            return tsdUtkastPage.fillInAllmant(intyg.allmant).then(function() {
                logger.info('(1) OK - fillInAllmant :' + JSON.stringify(intyg.allmant));
            }, function(reason) {
                console.trace(reason);
                throw ('(1) FEL, fillInAllmant,' + reason);
            });
        }).then(function() {
            //Ange hypoglykemier efter att körkortstyper är ifyllda
            return tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier).then(function() {
                logger.info('(2) OK - fillInHypoglykemier :' + JSON.stringify(intyg.hypoglykemier));
            }, function(reason) {
                console.trace(reason);
                throw ('(2) FEL, fillInHypoglykemier,' + reason);
            });
        }).then(function() {
            return tsdUtkastPage.fillInSynintyg(intyg.synintyg).then(function() {
                logger.info('(3) OK - fillInSynintyg :' + JSON.stringify(intyg.synintyg));
            }, function(reason) {
                console.trace(reason);
                throw ('(3) FEL, fillInSynintyg,' + reason);
            });
        }).then(function() {
            return tsdUtkastPage.fillInBedomning(intyg.bedomning).then(function() {
                logger.info('(4) OK - fillInBedomning :' + JSON.stringify(intyg.bedomning));
            }, function(reason) {
                console.trace(reason);
                throw ('(4) FEL, fillInBedomning,' + reason);
            });
        }).then(function() {
            logger.info('TODO - Övriga kommentarer och upplysningar');
        });
    }
};
