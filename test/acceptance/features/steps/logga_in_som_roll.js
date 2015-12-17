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

module.exports = function() {

    this.Given(/^att jag är inloggad som tandläkare$/, function(callback) {
        var userObj = {
            fornamn:    'Louise',
            efternamn:  'Ericsson',
            hsaId:      'TSTNMT2321000156-103B',
            enhetId:    'TSTNMT2321000156-1039'
        };
        logInAsUserRole(userObj,'Tandläkare',callback);
    });

    this.Given(/^att jag är inloggad som vårdadministratör$/, function(callback) {
        var userObj = {
            fornamn:    'Lena',
            efternamn:  'Karlsson',
            hsaId:      'IFV1239877878-104N',
            enhetId:    'IFV1239877878-1045'
        };
        logInAsUserRole(userObj,'Vårdadministratör',callback);
    });

    this.Given(/^att jag är inloggad som läkare$/, function(callback) {
        var userObj = {
            fornamn:    'Jan',
            efternamn:  'Nilsson',
            hsaId:      'IFV1239877878-1049',
            enhetId:    'IFV1239877878-1042'
        };
        logInAsUserRole(userObj,'Läkare',callback);
    });
};


function logInAsUserRole(userObj,roleName,callback){
        logg('Loggar in som ' + userObj.fornamn+' '+userObj.efternamn + '..');
        browser.ignoreSynchronization = true;
        pages.welcome.get();
        pages.welcome.loginByJSON(JSON.stringify(userObj));
        browser.ignoreSynchronization = false;
        browser.sleep(2000);

        expect(element(by.id('wcHeader')).getText())
        .to.eventually
        .contain(roleName + ' - ' + userObj.fornamn+ ' ' + userObj.efternamn)
        .and.notify(callback);
}
