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
var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
module.exports = function () {


    this.Then(/^vill jag vara inloggad$/, function (callback) {
        expect(element(by.id('wcHeader')).getText()).to.eventually.contain('Logga ut').and.notify(callback);
    });

    this.When(/^jag väljer patienten "([^"]*)"$/, function (personnummer, callback) {
        element(by.id('menu-skrivintyg')).click();
        sokSkrivIntygPage.selectPersonnummer(personnummer);

        //Patientuppgifter visas
        var patientUppgifter = element(by.cssContainingText('.form-group', 'Patientuppgifter'));
        expect(patientUppgifter.getText()).to.eventually.contain(personnummer).and.notify(callback);
    });

    this.Given(/^jag går in på att skapa ett "([^"]*)" intyg$/, function (intygsTyp, callback) {
        intyg.typ = intygsTyp;
        sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intygsTyp);
        sokSkrivIntygUtkastTypePage.continueToUtkast();
        
        // Save INTYGS_ID:
        browser.getCurrentUrl().then(function(text){
          intyg.id = text.split('/').slice(-1)[0];
          logg('Intygsid: '+intyg.id);
        });
        callback();
    });


    this.Then(/^ska intygets status vara "([^"]*)"$/, function (statustext, callback) {
        expect(element(by.id('intyg-vy-laddad')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });


    
    this.Then(/^jag ska se den data jag angett för intyget$/, function (callback) {

        if (intyg.typ === 'Transportstyrelsens läkarintyg, diabetes' || intyg.typ === 'Transportstyrelsens läkarintyg') {
        // // Intyget avser  
        var intygetAvser = element(by.id('intygAvser'));

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
            expect(idStarktGenom.getText()).to.eventually.contain(txt);
        } else {
            expect(idStarktGenom.getText()).to.eventually.contain(intyg.identitetStyrktGenom);
        }
    }
        if(intyg.typ === 'Transportstyrelsens läkarintyg, diabetes'){

        //  Vilket år ställdes diagnosen diabetes?
        var period = element(by.id('observationsperiod'));
        if (intyg.allmant.year !== null){
        logg('Kontrollerar att observationsperiod är: '+intyg.allmant.year);
        expect(period.getText()).to.eventually.equal(intyg.allmant.year.toString());
        }
        //  Insulin sedan år
        var insulPeriod = element(by.id('insulinBehandlingsperiod'));
        if (typeof intyg.allmant.behandling.insulinYear !== 'undefined'){
        logg('Kontrollerar att intyg.insulinBehandlingsperiod är: '+intyg.allmant.behandling.insulinYear);
        expect(insulPeriod.getText()).to.eventually.equal(intyg.allmant.behandling.insulinYear.toString());
        }

        // Kolla Diabetestyp
        var dTyp = element(by.id('diabetestyp'));
        logg('Kontrollerar att diabetestyp är: '+intyg.allmant.typ);
        expect(dTyp.getText()).to.eventually.equal(intyg.allmant.typ);

        helpers.testElement(intyg.hypoglykemier.a, 'kunskapOmAtgarder');
        helpers.testElement(intyg.hypoglykemier.b, 'teckenNedsattHjarnfunktion');
        helpers.testElement(intyg.hypoglykemier.c, 'saknarFormagaKannaVarningstecken');
        helpers.testElement(intyg.hypoglykemier.d, 'allvarligForekomst');
        helpers.testElement(intyg.hypoglykemier.e, 'allvarligForekomstTrafiken');
        helpers.testElement(intyg.hypoglykemier.f, 'egenkontrollBlodsocker');
        helpers.testElement(intyg.hypoglykemier.g, 'allvarligForekomstVakenTid');

        var synIntyg = element(by.id('separatOgonlakarintyg'));
        if (intyg.syn === 'Ja') {
            logg('Kontrollerar att synintyg är:' + intyg.syn);
            expect(synIntyg.getText()).to.eventually.equal(intyg.syn);
        }

        var bed = element(by.id('bedomning'));

        logg('Kontrollerar att bedömningen avser körkortstyper:'+selectedTypes);
        expect(bed.getText()).to.eventually.contain(selectedTypes);
        
        // ============= PLACEHOLDERS:
        var komment = element(by.id('kommentar'));
        expect(komment.getText()).to.eventually.equal('Ej angivet');
        var specKomp = element(by.id('lakareSpecialKompetens'));
        expect(specKomp.getText()).to.eventually.equal('Ej angivet');
        // ==============
        
        var typer = intyg.allmant.behandling.typer;
        typer.forEach(function(typ) {
            console.log('Kontrollerar behandlingstyp: ' + typ);
            if(typ === 'Endast kost')
            {
                var eKost = element(by.id('endastKost'));
                logg('Kontrollerar att behandlingstyp '+typ+'är satt till \"Ja\"');
                expect(eKost.getText()).to.eventually.equal('Ja').and.notify(callback);
            }
            else if(typ === 'Tabletter')
            {
                var tabl = element(by.id('tabletter'));
                logg('Kontrollerar att behandlingstyp '+typ+'är satt till \"Ja\"');
                expect(tabl.getText()).to.eventually.equal('Ja').and.notify(callback);
            }
            else if(typ === 'Insulin')
            {
                var insul = element(by.id('insulin')); 
                logg('Kontrollerar att behandlingstyp '+typ+'är satt till \"Ja\"');
                expect(insul.getText()).to.eventually.equal('Ja').and.notify(callback);
            }
        });
    }
    else if (intyg.typ === 'Transportstyrelsens läkarintyg'){
        logg('inside Transportstyrelsens läkarintyg');
        require('./check_tsBas_values').checkTsBasValues(intyg, callback);

    
    }
    else if (intyg.typ === 'Läkarintyg FK 7263'){
        logg('-- Kontrollerar Läkarintyg FK 7263 --');
        require('./check_fk_values').checkFKValues(intyg, callback);

    }

    });

    this.Given(/^ska signera\-knappen inte vara synlig$/, function (callback) {
        expect(fk7263Utkast.signeraButton.isPresent()).to.eventually.become(false).and.notify(callback);
    });

};
