/*global
browser
*/
'use strict';

module.exports = function() {

    this.Given(/^jag loggar in$/, function(callback) {
        console.log('Loggar in som ' + 'Jan Nilsson' + '..');
        // Gå till welcome.jsp
        browser.get(browser.baseUrl + '/welcome.jsp');

        // Välj användare
        element(by.id('loginBtn')).click();
        callback();
    });

    this.Then(/^vill jag vara inloggad$/, function(callback) {
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain('Logga ut').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som läkare "([^"]*)"$/, function(anvandarnamn, callback) {
        console.log('Loggar in som ' + anvandarnamn + '..');

        // Gå till welcome.jsp
        browser.get(browser.baseUrl + '/welcome.jsp');

        // Välj användare
        element(by.cssContainingText('option', anvandarnamn)).click();
        element(by.id('loginBtn')).click();

        expect(element(by.id('wcHeader')).getText()).to.eventually.contain(anvandarnamn).and.notify(callback);
    });

    this.Given(/^jag väljer patienten "([^"]*)"$/, function(personnummer, callback) {
        element(by.id('pnr')).sendKeys(personnummer);
        element(by.id('skapapersonnummerfortsatt')).click();

        // Patinetuppgifter visas
        var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
        expect(patientUppgifter.getText()).to.eventually.contain(personnummer).and.notify(callback);

    });

    this.Given(/^jag går in på  att skapa ett "([^"]*)" intyg$/, function(intygsTyp, callback) {
        element(by.cssContainingText('option', intygsTyp)).click();
        element(by.id('intygTypeFortsatt')).click();

        callback();
    });

    this.Given(/^signerar intyget$/, function(callback) {
        element(by.id('signera-utkast-button')).click();
        callback();
    });

    this.Given(/^ska intygets status vara "([^"]*)"$/, function(statustext, callback) {
        var intygVyLaddad = element(by.id('intyg-vy-laddad'));
        expect(intygVyLaddad.getText()).to.eventually.contain(statustext).and.notify(callback);
    });
};