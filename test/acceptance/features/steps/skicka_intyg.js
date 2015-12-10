/* globals pages */
/* globals browser, intyg, protractor */

'use strict';
var fkIntygPage = pages.intygpages.fkIntyg;

module.exports = function() {

    this.Given(/^jag skickar intyget till Transportstyrelsen/, function(callback) {

        //Fånga intygets id
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            logg('Intygsid: ' + intyg.id);
        });

        fkIntygPage.skicka.knapp.click();
        fkIntygPage.skicka.samtyckeCheckbox.click();
        fkIntygPage.skicka.dialogKnapp.click();
        callback();
    });

    this.Given(/^jag skickar intyget till Försäkringskassan$/, function(callback) {

    	browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            intyg.id = intyg.id.replace('?signed', '');
        });

        fkIntygPage.skicka.knapp.click();
        fkIntygPage.skicka.samtyckeCheckbox.click();
        fkIntygPage.skicka.dialogKnapp.click();
        callback();
    });

};