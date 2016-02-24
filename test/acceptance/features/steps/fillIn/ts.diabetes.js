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

/* globals logg, pages, JSON, browser */

'use strict';
var tsdUtkastPage = pages.intyg.ts.diabetes.utkast;
module.exports = {
    fillIn: function(intyg, cb) {
        //Ange körkortstyper
        tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper).then(function() {
            logg('OK - fillInKorkortstyper :' + JSON.stringify(intyg.korkortstyper));
        }, function(reason) {
            cb('FEL, fillInKorkortstyper,' + reason);
        });

        //Ange Identitet styrkt genom
        tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function() {
            logg('OK - fillInIdentitetStyrktGenom :' + JSON.stringify(intyg.identitetStyrktGenom));
        }, function(reason) {
            cb('FEL, fillInIdentitetStyrktGenom,' + reason);
        });

        browser.ignoreSynchronization = true;

        //Ange allmänt
        tsdUtkastPage.fillInAllmant(intyg.allmant).then(function() {
            logg('OK - fillInAllmant :' + JSON.stringify(intyg.allmant));
        }, function(reason) {
            cb('FEL, fillInAllmant,' + reason);
        });

        //Ange hypoglykemier
        tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier).then(function() {
            logg('OK - fillInHypoglykemier :' + JSON.stringify(intyg.hypoglykemier));
        }, function(reason) {
            cb('FEL, fillInHypoglykemier,' + reason);
        });

        tsdUtkastPage.fillInSynintyg(intyg.synintyg).then(function() {
            logg('OK - fillInSynintyg :' + JSON.stringify(intyg.synintyg));
        }, function(reason) {
            cb('FEL, fillInSynintyg,' + reason);
        });

        browser.ignoreSynchronization = false;

        tsdUtkastPage.fillInBedomning(intyg.bedomning).then(function() {
            logg('OK - fillInBedomning :' + JSON.stringify(intyg.bedomning));
        }, function(reason) {
            cb('FEL, fillInBedomning,' + reason);
        }).then(cb);
    }
};