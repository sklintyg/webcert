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

        //Save INTYGS_ID:
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

        if(intyg.typ === 'Transportstyrelsens läkarintyg, diabetes'){

        //  Vilket år ställdes diagnosen diabetes?
        var period = element(by.id('observationsperiod'));
        if (intyg.allmant.year !== null){
        logg('Kontrollerar att observationsperiod är: '+intyg.allmant.year);
        expect(period.getText()).to.eventually.equal(intyg.allmant.year.toString());
        }
        //  Insulin sedan år
        var insulPeriod = element(by.id('insulinBehandlingsperiod'));
        if (intyg.allmant.behandling.insulinYear !== null){
        logg('Kontrollerar att intyg.insulinBehandlingsperiod är: '+intyg.allmant.behandling.insulinYear);
        expect(insulPeriod.getText()).to.eventually.equal(intyg.allmant.behandling.insulinYear.toString());
        }

        // Kolla Diabetestyp
        var dTyp = element(by.id('diabetestyp'));
        logg('Kontrollerar att diabetestyp är: '+intyg.allmant.typ);
        expect(dTyp.getText()).to.eventually.equal(intyg.allmant.typ);


        // var annanBeh = element(by.id('annanBehandlingBeskrivning'));

        testElement(intyg.hypoglykemier.a, 'kunskapOmAtgarder');
        testElement(intyg.hypoglykemier.b, 'teckenNedsattHjarnfunktion');
        testElement(intyg.hypoglykemier.c, 'saknarFormagaKannaVarningstecken');
        testElement(intyg.hypoglykemier.d, 'allvarligForekomst');
        testElement(intyg.hypoglykemier.e, 'allvarligForekomstTrafiken');
        testElement(intyg.hypoglykemier.f, 'egenkontrollBlodsocker');
        testElement(intyg.hypoglykemier.g, 'allvarligForekomstVakenTid');

        var synIntyg = element(by.id('separatOgonlakarintyg'));
        if (intyg.syn === 'Ja') {
            logg('Kontrollerar att synintyg är:' + intyg.syn);
            expect(synIntyg.getText()).to.eventually.equal(intyg.syn);
        }
        else {
            //Kontrollera det ifyllda synintyget.
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
        
        // var typer = intyg.allmant.behandling.typer;
        var typer = utkast.allmant.behandling;
        typer.forEach(function (typ) {
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
    }
    // Synfunktioner
    testElement(intyg.synDonder, 'synfaltsdefekter');
    testElement(intyg.synNedsattBelysning, 'nattblindhet');
    testElement(intyg.synOgonsjukdom, 'progressivOgonsjukdom');
    testElement(intyg.synDubbel, 'diplopi');
    testElement(intyg.synNystagmus, 'nystagmus');
    // ============= PLACEHOLDERS:
    // Ändra så att man sparar/genererar random värden!:     
    var hogerOgautanKorrektion = element(by.id('hogerOgautanKorrektion'));
    expect(hogerOgautanKorrektion.getText()).to.eventually.equal('0,8');
    var hogerOgamedKorrektion = element(by.id('hogerOgamedKorrektion'));
    expect(hogerOgamedKorrektion.getText()).to.eventually.equal('1,0');
    var vansterOgautanKorrektion = element(by.id('vansterOgautanKorrektion'));
    expect(vansterOgautanKorrektion.getText()).to.eventually.equal('0,7');
    var vansterOgamedKorrektion = element(by.id('vansterOgamedKorrektion'));
    expect(vansterOgamedKorrektion.getText()).to.eventually.equal('1,0');
    var binokulartutanKorrektion = element(by.id('binokulartutanKorrektion'));
    expect(binokulartutanKorrektion.getText()).to.eventually.equal('1,0');
    var binokulartmedKorrektion = element(by.id('binokulartmedKorrektion'));
    expect(binokulartmedKorrektion.getText()).to.eventually.equal('1,0');
    var korrektionsglasensStyrka = element(by.id('korrektionsglasensStyrka'));
    expect(korrektionsglasensStyrka.getText()).to.eventually.equal('Nej');
    // ==============
    // Hörsel och balanssinne:
    var horselBalansbalansrubbningar = element(by.id('horselBalansbalansrubbningar'));
    expect(horselBalansbalansrubbningar.getText()).to.eventually.equal(intyg.horselYrsel);

    // Rörelseorganens funktioner:
    var funktionsnedsattning = element(by.id('funktionsnedsattning'));
    var funktionsnedsattningbeskrivning = element(by.id('funktionsnedsattningbeskrivning'));
    
    logg('Kontrollerar att rörelsehinder är: '+intyg.rorOrgInUt);
    expect(funktionsnedsattning.getText()).to.eventually.equal(intyg.rorOrgInUt).and.notify(callback);
    
    if(intyg.rorOrgNedsattning==='Ja'){
        logg('Kontrollerar att rörelsehinder kommentar');
        expect(funktionsnedsattningbeskrivning.getText()).to.eventually.equal('Amputerad under höger knä.');
    }else{
        logg('Kontrollerar att rörelsehinder kommentar är tom');
        expect(funktionsnedsattningbeskrivning.getText()).to.eventually.equal('');
    }
    
    var funktionsnedsRorelseformaga = element(by.id('funktionsnedsattningotillrackligRorelseformaga'));
    logg('Kontrollerar \"Är rörelseförmågan otillräcklig\": '+intyg.rorOrgInUt);
    expect(funktionsnedsRorelseformaga.getText()).to.eventually.equal(intyg.rorOrgInUt);

    // Hjärt- och kärlsjukdomar:
    var hjartKarlSjukdom = element(by.id('hjartKarlSjukdom'));
    var hjarnskadaEfterTrauma = element(by.id('hjarnskadaEfterTrauma'));
    
    expect(hjartKarlSjukdom.getText()).to.eventually.equal(intyg.hjartHjarna);
    expect(hjarnskadaEfterTrauma.getText()).to.eventually.equal(intyg.hjartHjarna);

    var riskfaktorerStroke = element(by.id('riskfaktorerStroke'));
    var beskrivningRiskfaktorer = element(by.id('beskrivningRiskfaktorer'));

    logg('Kontrollerar \"Föreligger viktiga riskfaktorer för stroke\": '+intyg.hjartHjarna);
    expect(riskfaktorerStroke.getText()).to.eventually.equal(intyg.hjartHjarna);
    if (intyg.hjartHjarna === 'Ja') {
        logg('Kontrollerar \"Föreligger viktiga riskfaktorer för stroke\" kommentar');
        expect(beskrivningRiskfaktorer.getText()).to.eventually.equal('TIA och förmaksflimmer.');
    }
    else{
        logg('Kontrollerar \"Föreligger viktiga riskfaktorer för stroke\" kommentar är tom');
        expect(beskrivningRiskfaktorer.getText()).to.eventually.equal('');
    }

    // Diabetes

    var harDiabetes = element(by.id('harDiabetes'));
    var kost = element(by.id('kost'));
    var tabeltter = element(by.id('tabletter'));
    var insulin = element(by.id('insulin'));

    logg('Kontrollerar att Patient har Diabetes: '+ intyg.diabetes);
    expect(harDiabetes.getText()).to.eventually.equal(intyg.diabetes);


    var diabetesTyp = element(by.id('diabetesTyp'));
    logg('Kontrollerar att Patient diabetes typ: '+ intyg.diabetestyp);
    expect(diabetesTyp.getText()).to.eventually.equal(intyg.diabetestyp);

    if (intyg.diabetestyp === 'Typ 2' && intyg.diabetes === 'Ja') {
        var typer = intyg.dTyper;
        typer.forEach( function (_typ){
            if (_typ === 'Endast kost') {
                logg('Kontrollerar att behandlingstyp är: '+ _typ);
                expect(kost.getText()).to.eventually.equal('Endast kost');
            }else if (_typ === 'Tabletter'){
                logg('Kontrollerar att behandlingstyp är: '+ _typ);
                expect(tabeltter.getText()).to.eventually.equal('Tabletter');
            }else if (_typ === 'Insulin'){
                logg('Kontrollerar att behandlingstyp är: '+ _typ);
                expect(insulin.getText()).to.eventually.equal('Insulin');
            }
        });
    } else if (intyg.diabetestyp === 'Typ 1'){
        expect(kost.getText()).to.eventually.equal('');
        expect(tabeltter.getText()).to.eventually.equal('');
        expect(insulin.getText()).to.eventually.equal('');
    }

    // Neurologiska sjukdomar
    var neurologiskSjukdom = element(by.id('neurologiskSjukdom'));
    logg('Kontrollerar att Neurologiska sjukdomar är: '+ intyg.neurologiska);
    expect(neurologiskSjukdom.getText()).to.eventually.equal(intyg.neurologiska);

    // Epilepsi, epileptiskt anfall och annan medvetandestörning
    var medvetandestorning = element(by.id('medvetandestorning'));
    logg('Kontrollerar om patienten har eller har patienten haft epilepsi: '+ intyg.epilepsi);
    expect(medvetandestorning.getText()).to.eventually.equal(intyg.epilepsi);
    if (intyg.epilepsi === 'Ja') {
        logg('Kontrollerar Kommentar: \"Blackout. Midsommarafton.\"');
        var medvetandestorningbeskrivning = element(by.id('medvetandestorningbeskrivning'));
        expect(medvetandestorningbeskrivning.getText()).to.eventually.equal('Blackout. Midsommarafton.');
    }

    // Njursjukdomar
    var nedsattNjurfunktion = element(by.id('nedsattNjurfunktion'));
    expect(nedsattNjurfunktion.getText()).to.eventually.equal(intyg.njursjukdom);
    logg('Kontrollerar nedsatt njurfunktion är: ' + intyg.njursjukdom);

    // Demens och andra kognitiva störningar
    var sviktandeKognitivFunktion = element(by.id('sviktandeKognitivFunktion'));
    expect(sviktandeKognitivFunktion.getText()).to.eventually.equal(intyg.demens);
    logg('Kontrollerar sviktande kognitiv funktion är: ' + intyg.demens);

    //Sömn- och vakenhetsstörningar
    var teckenSomnstorningar = element(by.id('teckenSomnstorningar'));
    logg('Kontrollerar sömnstörningar är: ' + intyg.somnVakenhet);
    if (intyg.somnVakenhet==='Ja') {
        expect(teckenSomnstorningar.getText()).to.eventually.equal('Ja');
    }else {
        expect(teckenSomnstorningar.getText()).to.eventually.equal(intyg.somnVakenhet);
    }
    // Alkohol, narkotika och läkemedel
    var teckenMissbruk = element(by.id('teckenMissbruk'));
    var foremalForVardinsats = element(by.id('foremalForVardinsats'));
    var provtagningBehovs = element(by.id('provtagningBehovs'));
    var lakarordineratLakemedelsbruk = element(by.id('lakarordineratLakemedelsbruk'));
    
    logg('Kontrollera att  tecken på missbruk eller beroende är: '+ intyg.alkoholMissbruk);
    expect(teckenMissbruk.getText()).to.eventually.equal(intyg.alkoholMissbruk);

    logg('Kontrollera att alkohol vård är: '+ intyg.alkoholVard);
    expect(foremalForVardinsats.getText()).to.eventually.equal(intyg.alkoholVard);

    logg('Kontrollera att alkohol läkemedel är: '+ intyg.alkoholLakemedel);
    expect(provtagningBehovs.getText()).to.eventually.equal(intyg.alkoholLakemedel);   
    
    if (intyg.alkoholLakemedel==='Ja') {
        var lakemedelOchDos = element(by.id('lakemedelOchDos'));
        expect(lakemedelOchDos.getText()).to.eventually.equal('2 liter metadon.');
        logg('Kontrollera att kommentar innehåller: \" 2 liter metadon.\"');
    }
    // Psykiska sjukdomar och störningar
    var psykiskSjukdom = element(by.id('psykiskSjukdom'));
    expect(psykiskSjukdom.getText()).to.eventually.equal(intyg.psykiskSjukdom);
    logg('Kontrollera att psykisk sjukdom är: '+ intyg.psykiskSjukdom);
    // assertEllementYesNo(intyg.psykiskSjukdom,'psykiskSjukdom');

    //ADHD, autismspektrumtillstånd och likartade tillstånd samt psykisk utvecklingsstörning
    var psykiskUtvecklingsstorning = element(by.id('psykiskUtvecklingsstorning'));
    logg('Kontrollera att adhd psykisk är: '+ intyg.adhdPsykisk);    
    expect(psykiskUtvecklingsstorning.getText()).to.eventually.equal(intyg.adhdPsykisk);
    // assertEllementYesNo(intyg.adhdPsykisk,'psykiskUtvecklingsstorning');
    var harSyndrom = element(by.id('harSyndrom'));
    logg('Kontrollera att adhd syndrom är: '+ intyg.adhdSyndrom);
    // assertEllementYesNo(intyg.adhdSyndrom, 'harSyndrom');
    // callback();
    expect(harSyndrom.getText()).to.eventually.equal(intyg.adhdSyndrom).and.notify(callback);
    //.and.notify(callback)
   

    });

    this.Given(/^ska signera\-knappen inte vara synlig$/, function (callback) {
        expect(fk7263Utkast.signeraButton.isPresent()).to.become(false).and.notify(callback);
    });
        function testElement(_typ, _element){
        var ele = element(by.id(_element));
        if(_typ === null ){
            logg('Kontrollerar '+_element+' : '+ _typ);
            expect(ele.getText()).to.eventually.equal('Ej angivet');
        }
        else if(_typ === 'Ja' || _typ === 'Nej'){
            logg('Kontrollerar '+_element+' : '+ _typ);
            expect(ele.getText()).to.eventually.equal(_typ);
        }
    }
    function assertEllementYesNo(_elementVal, _element){
        var ele = element(by.id(_element));
        if(_elementVal === 'Ja' ){
            logg('Kontrollerar '+_element+' : Ja');
            expect(ele.getText()).to.eventually.equal('Ja');
        }
        else if(_elementVal==='Nej'){
            logg('Kontrollerar '+_element+' : Nej');
            expect(ele.getText()).to.eventually.equal('Nej');
        }
    }


};
