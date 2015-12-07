/* globals browser, intyg */

'use strict';

module.exports = function() {
    this.Given(/^jag går till Mina intyg för patienten "([^"]*)"$/, function (pnr, callback) {
        browser.ignoreSynchronization = true;
        browser.get(process.env.MINAINTYG_URL+'/welcome.jsp');
        element(by.id('guid')).sendKeys(pnr);
        element(by.css('input.btn')).click().then(callback);
        
        browser.ignoreSynchronization = false;
        
    });

    this.Given(/^ska intygets status i Mina intyg visa "([^"]*)"$/, function(status, callback) {
    	var intygElement = element(by.id('certificate-'+intyg.id));
        expect(intygElement.getText()).to.eventually.contain(status).and.notify(callback);
    });
};

