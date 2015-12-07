/* globals pages, protractor*/
/* globals browser, intyg */

'use strict';

module.exports = function () {

    this.Then(/^vill jag vara inloggad$/, function (callback) {
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain('Logga ut').and.notify(callback);
    });

    this.Given(/^att jag är inloggad som läkare "([^"]*)"$/, function (anvandarnamn, callback) {
        console.log('Loggar in som ' + anvandarnamn + '..');
        
        browser.ignoreSynchronization = true;
        pages.welcome.get();
        pages.welcome.loginByName(anvandarnamn);
        browser.ignoreSynchronization = false;
        browser.sleep(2000);
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain(anvandarnamn).and.notify(callback);
        
    });

    this.When(/^jag väljer patienten "([^"]*)"$/, function (personnummer, callback) {
        global.pages.app.views.sokSkrivIntyg.selectPersonnummer(personnummer);

        //Patientuppgifter visas
        var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
        expect(patientUppgifter.getText()).to.eventually.contain(personnummer).and.notify(callback);
    });

    this.Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function (intygsTyp, callback) {
        intyg.typ = intygsTyp;
        pages.app.views.sokSkrivIntyg.selectIntygTypeByLabel(intygsTyp);
        pages.app.views.sokSkrivIntyg.continueToUtkast();
        callback();
    });
    
    this.Given(/^signerar intyget$/, function (callback) {
        var EC = protractor.ExpectedConditions;
        browser.wait(EC.elementToBeClickable($('#signera-utkast-button')), 100000);
        element(by.id('signera-utkast-button')).click().then(callback);
    });

    this.Then(/^ska intygets status vara "([^"]*)"$/, function (statustext, callback) {
        console.log(intyg.typ);
        //För FK-intyg
        // if(intyg.typ === 'Läkarintyg FK 7263'){
        //     expect(element(by.id('certificate-is-sent-to-it-message-text')).getText()).to.eventually.contain(statustext).and.notify(callback);
        // } else if (intyg.typ === 'Transportstyrelsens läkarintyg') {
        //     expect(element(by.id('certificate-is-on-sendqueue-to-it-message-text')).getText()).to.eventually.contain(statustext).and.notify(callback);
        // } else {
        expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
        //}

    });

    this.Then(/^jag ska se den data jag angett för intyget$/, function (callback) {
        // // Intyget avser
        var intygetAvser = element(by.id('intygAvser'));

        //Sortera typer till den ordning som Webcert använder
        var selectedTypes = intyg.korkortstyper.sort(function (a, b) {
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
