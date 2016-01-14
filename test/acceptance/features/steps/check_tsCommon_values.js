/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/* globals pages, protractor*/
/* globals browser, intyg, scenario, logg */

'use strict';


var helpers = require('./helpers.js');

var tsDiabIntygPage = pages.intyg.ts.diabetes.intyg;
var tsBasIntygPage = pages.intyg.ts.bas.intyg;


module.exports ={
	checkTsCommonValues:function(intyg, callback){

        var selectedTypes = intyg.korkortstyper.sort(function (a, b) {
            var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'TRAKTOR', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'TAXI'];
            return allTypes.indexOf(a.toUpperCase()) - allTypes.indexOf(b.toUpperCase());
        });
        selectedTypes = selectedTypes.join(', ').toUpperCase();

        expect(tsBasIntygPage.intygetAvser.getText()).to.eventually.contain(selectedTypes).then(function(value) {
            logg('OK - Körkortstyper = '+ value);
            }, function(reason) {
                callback('FEL - Körkortstyper: '+ reason);
            });

        if (intyg.identitetStyrktGenom.indexOf('Försäkran enligt 18 kap') > -1) {     
            var txt = 'Försäkran enligt 18 kap 4 §';
            expect(tsBasIntygPage.idStarktGenom.getText()).to.eventually.contain(txt).then(function(value) {
            logg('OK - Identitet styrkt genom = '+ value);
            }, function(reason) {
                callback('FEL - Identitet styrkt genom: '+ reason);
            });
        } else {
            expect(tsBasIntygPage.idStarktGenom.getText()).to.eventually.contain(intyg.identitetStyrktGenom).then(function(value) {
            logg('OK - Identitet styrkt genom = '+ value);
            }, function(reason) {
                callback('FEL - Identitet styrkt genom: '+ reason);
            });
        }
    }
};