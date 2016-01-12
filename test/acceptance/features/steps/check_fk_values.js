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
var intygPage = pages.intyg.fk['7263'].intyg;

function boolTillJaNej(val){
	if(val){
		return 'Ja';
	}
	else{
		return 'Nej';
	}
}

module.exports ={
	checkFKValues:function(intyg, callback){

	// var ejAngivet = 'Ej angivet'; 
    //     var field2 = element(by.xpath('//*[@id=\"field2\"]/span/span[1]/span'));
    //     var certificateDiv3 = element(by.xpath('//*[@id=\"certificate\"]/div/div/div/div[3]/span/span[1]/span'));
    //     var field4b = element(by.xpath('//*[@id=\"field4b\"]/span/span[1]/span'));
    //     var certificateDiv6 = element(by.xpath('//*[@id=\"certificate\"]/div/div/div/div[6]/span/span[1]/span'));
    //     var field6a = element(by.xpath('//*[@id=\"field6a\"]/span/span[1]/span'));
    //     var field6b = element(by.xpath('//*[@id=\"field6b\"]/span/span[1]/span'));
    //     var field7 = element(by.xpath('//*[@id=\"field7\"]/span/span[1]/span'));
    //     var field8 = element(by.xpath('//*[@id=\"field8\"]/span/span[1]/span'));

    //     expect(field2.getText()).to.eventually.equal(ejAngivet);
    //     expect(certificateDiv3.getText()).to.eventually.equal(ejAngivet);
    //     expect(field4b.getText()).to.eventually.equal(ejAngivet);
    //     expect(certificateDiv6.getText()).to.eventually.equal(ejAngivet);
    //     expect(field6a.getText()).to.eventually.equal(ejAngivet);
    //     expect(field6b.getText()).to.eventually.equal(ejAngivet);
    //     expect(field7.getText()).to.eventually.equal(ejAngivet);
    //     expect(field8.getText()).to.eventually.equal(ejAngivet);
            

    //Kontrollera smittskydd
    var smitta = boolTillJaNej(intyg.smittskydd);
    expect(intygPage.field1.text.getText()).to.eventually.equal(smitta).then(function(value) {
        logg('OK - SMITTA = ' +value);
    }, function(reason) {
        callback('FEL, SMITTA,' + reason);
    });

    //Kontrollera diagnos
    if(intyg.diagnos){
        helpers.genericAssert(intyg.diagnos.diagnoser[0].ICD10, 'diagnosKod');
        helpers.genericAssert(intyg.diagnos.fortydligande, 'diagnosBeskrivning');
	}

    //Kontrollera sjukdomsförlopp
    if(intyg.aktuelltSjukdomsforlopp){
    	helpers.genericAssert(intyg.aktuelltSjukdomsforlopp, 'sjukdomsforlopp');
    }
    else{
    	helpers.genericAssert('', 'sjukdomsforlopp');
    }

    //Kontrollera funktionsnedsättning
    if(intyg.funktionsnedsattning){
    	helpers.genericAssert(intyg.funktionsnedsattning, 'funktionsnedsattning');
    }
    else{
    	helpers.genericAssert('', 'funktionsnedsattning');
    }

    //Kontrollera Intyget baseras på
    if(intyg.baserasPa){
    	helpers.genericAssert(helpers.getDateForAssertion(intyg.baserasPa.minUndersokning.datum), 'undersokningAvPatienten');
    	helpers.genericAssert(helpers.getDateForAssertion(intyg.baserasPa.minTelefonkontakt.datum), 'telefonkontaktMedPatienten');
    	helpers.genericAssert(helpers.getDateForAssertion(intyg.baserasPa.journaluppgifter.datum), 'journaluppgifter');
    	helpers.genericAssert(helpers.getDateForAssertion(intyg.baserasPa.annat.datum), 'annanReferens');
    }

    //Kontrollera rekommendationer
    logg('TODO: Kontrollera rekommendationer');
    // var field6a = element(by.xpath('//*[@id=\"field6a\"]/span/span[1]/span'));
    // var field6b = element(by.xpath('//*[@id=\"field6b\"]/span/span[1]/span'));
    // expect(field6a.getText()).to.eventually.equal(ejAngivet);
    // expect(field6b.getText()).to.eventually.equal(ejAngivet);

    
    // Kontrollera rehabilitering aktuell
    logg('TODO: Kontrollera rehabilitering aktuell');
    // var rehabiliteringEjAktuell = element(by.id('rehabiliteringEjAktuell'));
    // logg('(PLACEHOLDER) Kontrollera att aktivitets begränsning är : Nej');
    // expect(rehabiliteringEjAktuell.getText()).to.eventually.equal('Nej');
    
    //Kontrollera arbetsuppgifter
    if(intyg.arbete){
    	helpers.genericAssert(intyg.arbete.nuvarandeArbete.aktuellaArbetsuppgifter, 'nuvarandeArbetsuppgifter');
	}

    // Kontrollera aktivitetsbegränsning
    if(intyg.aktivitetsbegränsning){
    	helpers.genericAssert(intyg.aktivitetsBegransning, 'aktivitetsbegransning');
	}

    // Kontrollera nedsatt arbetsförmåga
    logg('TODO: Kontrollera arbetsförmåga');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed25.from), 'nedsattMed25from');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed25.tom), 'nedsattMed25tom');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed50.from), 'nedsattMed50from');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed50.tom), 'nedsattMed50tom');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed75.from), 'nedsattMed75from');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed75.tom), 'nedsattMed75tom');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed100.from), 'nedsattMed100from');
    // helpers.genericAssert(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed100.tom), 'nedsattMed100tom');
    // helpers.genericAssert(intyg.arbetsformagaFMB,'arbetsformagaPrognos');
    // helpers.genericAssert('Går inte att bedöma','arbetsformataPrognosGarInteAttBedoma');

    // Kontrollera kontakt önskas med FK
    var kontaktMedFk = element(by.id('kontaktMedFk'));
    var kontaktOnskas = boolTillJaNej(intyg.kontaktOnskasMedFK);
    expect(kontaktMedFk.getText()).to.eventually.equal(kontaktOnskas).then(function(value) {
        logg('OK - Kontakt med FK = ' +value);
    }, function(reason) {
        callback('FEL, Kontakt med FK,' + reason);
    });


    // Kontrollera rekommendationer
    logg('TODO: Kontrollera rekommendationer');
    // var ressattTillArbeteAktuellt = element(by.id('ressattTillArbeteAktuellt'));
    // intyg.ressattTillArbeteAktuellt 
    // logg('Kontrollera att ressattTillArbeteAktuellt är: '+ s);
    // expect(ressattTillArbeteAktuellt.getText()).to.eventually.equal(s);


    //Kontrollera övriga upplysningar
    var kommentar = element(by.id('kommentar'));
    expect(kommentar.getText()).to.eventually.contain(intyg.prognos.fortydligande).then(function(value) {
        logg('OK - Kommentar = ' +value);
    }, function(reason) {
        callback('FEL, Kommentar,' + reason);
    }).then(callback);
}
};