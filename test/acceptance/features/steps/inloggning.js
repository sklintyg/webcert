/* globals pages, protractor*/
/* globals browser, intyg, scenario, logg */

'use strict';

var fk7263Utkast = pages.intygpages.fk7263Utkast;

module.exports = function () {


    this.Then(/^vill jag vara inloggad$/, function (callback) {
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain('Logga ut').and.notify(callback);
    });

    this.When(/^jag väljer patienten "([^"]*)"$/, function (personnummer, callback) {
        element(by.id('menu-skrivintyg')).click();
        global.pages.app.views.sokSkrivIntyg.selectPersonnummer(personnummer);

        //Patientuppgifter visas
        var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
        expect(patientUppgifter.getText()).to.eventually.contain(personnummer).and.notify(callback);
    });

    this.Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function (intygsTyp, callback) {
        intyg.typ = intygsTyp;
        pages.app.views.sokSkrivIntyg.selectIntygTypeByLabel(intygsTyp);
        pages.app.views.sokSkrivIntyg.continueToUtkast();

        //Save INTYGS_ID:
        browser.getCurrentUrl().then(function(text){
          intygsid = text.split('/').slice(-1)[0];
          logg('Intygsid: '+intygsid);
        });

        callback();
    });

    this.Given(/^signerar intyget$/, function (callback) {
        var EC = protractor.ExpectedConditions;
        browser.sleep(5000);
        browser.wait(EC.elementToBeClickable($('#signera-utkast-button')), 100000);
        element(by.id('signera-utkast-button')).click().then(callback);
    });

    this.Then(/^ska intygets status vara "([^"]*)"$/, function (statustext, callback) {
        expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Then(/^jag ska se den data jag angett för intyget$/, function (callback) {
        // // Intyget avser
        var intygetAvser = element(by.id('intygAvser'));

        
        // var period = element(by.id('observationsperiod'));

        var insulPeriod = element(by.id('insulinBehandlingsperiod'));
        var besk = element(by.id('annanBehandlingBeskrivning'));

        //Sortera typer till den ordning som Webcert använder
        var selectedTypes = intyg.korkortstyper.sort(function (a, b) {
            var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'TRAKTOR', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'TAXI'];
            return allTypes.indexOf(a.toUpperCase()) - allTypes.indexOf(b.toUpperCase());
        });

        selectedTypes = selectedTypes.join(', ').toUpperCase();
        logg('Kontrollerar att intyget avser körkortstyper:'+selectedTypes);

        expect(intygetAvser.getText()).to.eventually.contain(selectedTypes);

        // //Identiteten är styrkt genom
        var idStarktGenom = element(by.id('identitet'));
        logg('Kontrollerar att intyg är styrkt genom: ' + intyg.identitetStyrktGenom);


        if (intyg.identitetStyrktGenom.indexOf('Försäkran enligt 18 kap') > -1) { 
            // Specialare eftersom status inte innehåller den punkt som utkastet innehåller.
            var txt = 'Försäkran enligt 18 kap 4 §';
            expect(idStarktGenom.getText()).to.eventually.contain(txt).and.notify(callback);
        } else {
            expect(idStarktGenom.getText()).to.eventually.contain(intyg.identitetStyrktGenom).and.notify(callback);
        }

        var period = element(by.id('observationsperiod'));
        period.getText().then(function (_text) {
            expect(_text).to.eventually.equals(intyg.allmant.year).and.notify(callback);
        });
        
        var dTyp = element(by.id('diabetestyp'));

        var eKost = element(by.id('endastKost'));
        var tabl = element(by.id('tabletter'));
        var insul = element(by.id('insulin'));

        var typer = intyg.allmant.behandling.typer;
        typer.forEach(function(typ) {
            if(typ==='Endast kost')
            {
                expect(eKost.getText()).to.eventually.equal(typ).and.notify(callback);
            }
            else if(typ==='Tabletter')
            {
                expect(tabl.getText()).to.eventually.equal(typ).and.notify(callback);
            }
            else if(typ==='Insulin')
            {
                expect(insul.getText()).to.eventually.equal(typ).and.notify(callback);
            }
        });

        // endastKost
        // tabletter
        // insulin

        // intyg.allmant.behandling

        // intyg.allmant.hypoglykemier
        // intyg.allmant.synintyg
        // intyg.allmant.bedomning
        // intyg.identitetStyrktGenom
        // intyg.allmant
        // intyg.hypoglykemier
        // intyg.synintyg
        // intyg.bedomning
        // intyg.korkortstyper

    });

    this.Given(/^ska signera\-knappen inte vara synlig$/, function (callback) {
        expect(fk7263Utkast.signeraButton.isPresent()).to.become(false).and.notify(callback);
    });

};
