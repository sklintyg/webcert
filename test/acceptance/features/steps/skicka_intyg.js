/* globals pages */
/* globals browser, intyg, protractor */

'use strict';

module.exports = function() {

    this.Given(/^jag skickar intyget till Transportstyrelsen/, function(callback) {

        //Fånga intygets id
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            console.log('Intygsid: ' + intyg.id);
        });

        element(by.id('sendBtn')).click();
        element(by.id('patientSamtycke')).click();
        element(by.id('button1send-dialog')).click();
        callback();
    });

    this.Given(/^jag skickar intyget till Försäkringskassan$/, function(callback) {

    	browser.getCurrentUrl().then(function(text) {
            global.intyg.id = text.split('/').slice(-1)[0];
            global.intyg.id = global.intyg.id.replace('?signed', '');
        });
        element(by.id('sendBtn')).click();
        element(by.id('patientSamtycke')).click();
        element(by.id('button1send-dialog')).click();
        callback();
    });

};