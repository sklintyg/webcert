/*global
browser, intyg,protractor
*/
'use strict';

module.exports = function() {

    // this.Given(/^jag loggar in$/, function(callback) {
    //     console.log('Loggar in som ' + 'Jan Nilsson' + '..');
    //     // Gå till welcome.jsp
    //     browser.get(browser.baseUrl + '/welcome.jsp');

    //     // Välj användare
    //     element(by.id('loginBtn')).click();
    //     callback();
    // });

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

    this.When(/^jag väljer patienten "([^"]*)"$/, function(personnummer, callback) {

        element(by.id('pnr')).sendKeys(personnummer);
        element(by.id('skapapersonnummerfortsatt')).click();

        //Patientuppgifter visas
        var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
        expect(patientUppgifter.getText()).to.eventually.contain(personnummer).and.notify(callback);

    });

    this.Given(/^jag går in på  att skapa ett "([^"]*)" intyg$/, function(intygsTyp, callback) {
        element(by.cssContainingText('option', intygsTyp)).click();
        element(by.id('intygTypeFortsatt')).click().then(callback);
    });

    this.Given(/^signerar intyget$/, {
        timeout: 100 * 1000
    }, function(callback) {
        // expect(element(by.id('signera-utkast-button')).isPresent()).toBe(true);
        var EC = protractor.ExpectedConditions;
        // Waits for the element with id 'abc' to be clickable.
        browser.wait(EC.elementToBeClickable($('#signera-utkast-button')), 10000);
        element(by.id('signera-utkast-button')).click().then(callback);
    });

    this.Then(/^ska intygets status vara "([^"]*)"$/, function(statustext, callback) {
        expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Then(/^jag ska se den data jag angett för intyget$/, function(callback) {
        // // Intyget avser
        var intygetAvser = element(by.id('intygAvser'));

        //Sortera typer till den ordning som Webcert använder
        var selectedTypes = intyg.korkortstyper.sort(function(a, b) {
            var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'TRAKTOR', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'TAXI'];
            return allTypes.indexOf(a.toUpperCase()) - allTypes.indexOf(b.toUpperCase());
        });

        selectedTypes = selectedTypes.join(', ').toUpperCase();
        console.log('Kontrollerar att intyget avser körkortstyper:'+selectedTypes);

        expect(intygetAvser.getText()).to.eventually.contain(selectedTypes);

        // //Identiteten är styrkt genom
        var idStarktGenom = element(by.id('identitet'));
        console.log('Kontrollerar att intyg är styrkt genom: ' + intyg.identitetStyrktGenom);
        expect(idStarktGenom.getText()).to.eventually.contain(intyg.identitetStyrktGenom).and.notify(callback);
    });

};