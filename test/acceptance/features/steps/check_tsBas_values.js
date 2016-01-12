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

var tsBasPage = pages.intyg.tsBas.intyg;
// var intygPage = pages.intyg.fk['7263'].utkast;

function boolTillJaNej(val){
	if(val){
		return 'Ja';
	}
	else{
		return 'Nej';
	}
}

module.exports ={
	checkTsBasValues:function(intyg, callback){

	
    // Synfunktioner
    helpers.testElement(intyg.synDonder, 'synfaltsdefekter');
    helpers.testElement(intyg.synNedsattBelysning, 'nattblindhet');
    helpers.testElement(intyg.synOgonsjukdom, 'progressivOgonsjukdom');
    helpers.testElement(intyg.synDubbel, 'diplopi');
    helpers.testElement(intyg.synNystagmus, 'nystagmus');
    
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

    //ADHD, autismspektrumtillstånd och likartade tillstånd samt psykisk utvecklingsstörning
    var psykiskUtvecklingsstorning = element(by.id('psykiskUtvecklingsstorning'));
    logg('Kontrollera att adhd psykisk är: '+ intyg.adhdPsykisk);    
    expect(psykiskUtvecklingsstorning.getText()).to.eventually.equal(intyg.adhdPsykisk);
    var harSyndrom = element(by.id('harSyndrom'));
    logg('Kontrollera att adhd syndrom är: '+ intyg.adhdSyndrom);
    expect(harSyndrom.getText()).to.eventually.equal(intyg.adhdSyndrom);
    //.and.notify(callback)
    //Sjukhusvård
    var sjukhusEllerLakarkontakt = element(by.id('sjukhusEllerLakarkontakt'));

    var tidpunkt = element(by.id('tidpunkt'));
    var vardinrattning = element(by.id('vardinrattning'));
    var sjukhusvardanledning = element(by.id('sjukhusvardanledning'));
    

    if (intyg.sjukhusvard === 'Ja') {
        logg('Kontrollera att sjukhusvard är: '+ intyg.sjukhusvard);
        expect(tidpunkt.getText()).to.eventually.equal('2015-12-13');
        expect(vardinrattning.getText()).to.eventually.equal('Östra sjukhuset.');
        expect(sjukhusvardanledning.getText()).to.eventually.equal('Allmän ysterhet.');
        expect(sjukhusEllerLakarkontakt.getText()).to.eventually.contain('Ja');
    } else {
        expect(sjukhusEllerLakarkontakt.getText()).to.eventually.equal('Nej');
        logg('Kontrollera att sjukhusvard är: '+ intyg.sjukhusvard);
    }

    var stadigvarandeMedicinering = element(by.id('stadigvarandeMedicinering'));
    var medicineringbeskrivning = element(by.id('medicineringbeskrivning'));
    if (intyg.ovrigMedicin === 'Ja') {
        logg('Kontrollera att stadig varande Medicinering är: '+ intyg.ovrigMedicin);
        // expect(stadigvarandeMedicinering.getText()).to.eventually.contain('Ja');
        logg('Kontrollera att kommentar är: \"beskrivning övrig medicinering\"');
        expect(medicineringbeskrivning.getText()).to.eventually.equal('beskrivning övrig medicinering').and.notify(callback);
    }
    else if (intyg.ovrigMedicin === 'Nej'){
        // expect(stadigvarandeMedicinering.getText()).to.eventually.equal('Nej').and.notify(callback);
        logg('Kontrollera att stadig varande Medicinering är: '+ intyg.ovrigMedicin);
    }
}
};