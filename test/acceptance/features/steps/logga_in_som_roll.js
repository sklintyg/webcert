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
