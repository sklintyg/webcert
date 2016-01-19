/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
            

    // Kontrollera FÄLT 1 : Smittskydd
    var smitta = boolTillJaNej(intyg.smittskydd);
    expect(intygPage.field1.text.getText()).to.eventually.equal(smitta).then(function(value) {
        logg('OK - SMITTA = ' +value);
    }, function(reason) {
        callback('FEL, SMITTA,' + reason);
    });

    //Kontrollera FÄLT 2 : Diagnos
    var field2 = intygPage.field2;
    if(intyg.diagnos){
        expect(field2.diagnoskod.getText()).to.eventually.equal(intyg.diagnos.diagnoser[0].ICD10).then(
            function(value) {
                logg('OK - Diagnoskod = ' +value);
            }, function(reason) {
                callback('FEL, Diagnoskod,' + reason);
            }
        );

        expect(field2.diagnosBeskrivning.getText()).to.eventually.equal(intyg.diagnos.fortydligande).then(
            function(value) {
                logg('OK - Diagnos förtydligande = ' +value);
            }, function(reason) {
                callback('FEL, Diagnos förtydligande,' + reason);
            }
        );
	}

    //Kontrollera FÄLT 3 : Sjukdomsförlopp
    expect(intygPage.field3.sjukdomsforlopp.getText()).to.eventually.equal(intyg.aktuelltSjukdomsforlopp).then(
        function(value) {
            logg('OK - Sjukdomsförlopp = ' +value);
        }, function(reason) {
            callback('FEL, Sjukdomsförlopp,' + reason);
        }
    );

    //Kontrollera FÄLT 4 : Funktionsnedsättning
    expect(intygPage.field4.funktionsnedsattning.getText()).to.eventually.equal(intyg.funktionsnedsattning).then(
        function(value) {
            logg('OK - Funktionsnedsättning = ' +value);
        }, function(reason) {
            callback('FEL, Funktionsnedsättning,' + reason);
        }
    );    

    //Kontrollera FÄLT 4b : Intyget baseras på

    if (intyg.baserasPa.minUndersokning) {
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.baserasPa.minUndersokning.datum)).then(
        function(value) {
            logg('OK - Undersokning av patienten baseras på min Undersokning = ' +value);
        }, function(reason) {
            callback('FEL, Undersokning av patienten baseras på min Undersokning, ' + reason);
        }
    );
    } else if (intyg.baserasPa.minTelefonkontakt) {
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.baserasPa.minTelefonkontakt.datum)).then(
        function(value) {
            logg('OK - Undersokning av patienten baseras på min Telefonkontakt = ' +value);
        }, function(reason) {
            callback('FEL, Undersokning av patienten baseras på min Telefonkontakt, ' + reason);
        }
    );
    } else if (intyg.baserasPa.journaluppgifter) {
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.baserasPa.journaluppgifter.datum)).then(
        function(value) {
            logg('OK - Undersokning av patienten baseras på journaluppgifter = ' +value);
        }, function(reason) {
            callback('FEL, Undersokning av patienten baseras på journaluppgifter, ' + reason);
        }
    );
    } else if (intyg.baserasPa.annat) {
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.baserasPa.annat.datum)).then(
        function(value) {
            logg('OK - Undersokning av patienten baseras på annat = ' +value);
        }, function(reason) {
            callback('FEL, Undersokning av patienten baseras på annat, ' + reason);
        }
    );
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(intyg.baserasPa.annat.text).then(
        function(value) {
            logg('OK - Undersokning av patienten baseras på annat = ' +value);
        }, function(reason) {
            callback('FEL, Undersokning av patienten baseras på annat, ' + reason);
        }
    );
    }
    
    //Kontrollera Fält 5 : Aktivitetsbegränsning
    var field5 = intygPage.field5.aktivitetsbegransning;
    expect(field5.getText()).to.eventually.equal(intyg.aktivitetsBegransning).then(
        function(value) {
            logg('OK - Aktivitetsbegränsning = ' +value);
        }, function(reason) {
            callback('FEL, Aktivitetsbegränsning,' + reason);
        }
    );

    //Kontrollera FÄLT 6a : Rekommendationer
    var field6a = intygPage.field6a;
    //Kontakt med AF
    expect(field6a.kontaktArbetsformedlingen.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedArbetsformedlingen).then(
        function(value) {
            logg('OK - Kontakt med AF = ' +value);
        }, function(reason) {
            callback('FEL, Kontakt med AF,' + reason);
        }
    );
    //Kontakt med Företagshälsovården
    expect(field6a.kontaktForetagshalsovarden.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedForetagshalsovard).then(
        function(value) {
            logg('OK - Kontakt med Företagshälsovård = ' +value);
        }, function(reason) {
            callback('FEL, Kontakt med Företagshälsovård,' + reason);
        }
    );
    //Övrig rekommendation
    if(intyg.rekommendationer.ovrigt){
        expect(field6a.ovrigt.getText()).to.eventually.equal(intyg.rekommendationer.ovrigt).then(
            function(value) {
                logg('OK - Övrig rekommendation= ' +value);
            }, function(reason) {
                callback('FEL, Övrig rekommendation,' + reason);
            }
        );
    }
    
    // Kontrollera FÄLT 7 : Rehabilitering
    if(intyg.rekommendationer.arbetslivsinriktadRehab){
        expect(intygPage.field7.text.getText()).to.eventually.equal(intyg.rekommendationer.arbetslivsinriktadRehab).then(
            function(value) {
                logg('OK - Rehabilitering aktuell = ' +value);
            }, function(reason) {
                callback('FEL, Rehabilitering aktuell,' + reason);
            }
        );
    }
    
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
    // fält 8b
    if (intyg.arbetsformaga.nedsattMed25) {
        expect(intygPage.field8b.nedsat25.from.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed25.from)).then(
        function(value) {
            logg('OK - Nedsatt med 20% from = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 20% from,' + reason);
        });
        expect(intygPage.field8b.nedsat25.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed25.tom)).then(
        function(value) {
            logg('OK - Nedsatt med 20% tom = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 20% tom,' + reason);
        });
    }
    if (intyg.arbetsformaga.nedsattMed50) {
        expect(intygPage.field8b.nedsat50.from.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed50.from)).then(
        function(value) {
            logg('OK - Nedsatt med 50% from = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 50% from,' + reason);
        });
        expect(intygPage.field8b.nedsat50.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed50.tom)).then(
        function(value) {
            logg('OK - Nedsatt med 50% tom = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 50% tom,' + reason);
        });
    }
    if (intyg.arbetsformaga.nedsattMed75) {
        expect(intygPage.field8b.nedsat75.from.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed75.from)).then(
        function(value) {
            logg('OK - Nedsatt med 75% from = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 75% from,' + reason);
        });
        expect(intygPage.field8b.nedsat75.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed75.tom)).then(
        function(value) {
            logg('OK - Nedsatt med 75% tom = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 75% tom,' + reason);
        });
    }
    if (intyg.arbetsformaga.nedsattMed100) {
        expect(intygPage.field8b.nedsat100.from.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed100.from)).then(
        function(value) {
            logg('OK - Nedsatt med 100% from = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 100% from,' + reason);
        });
        expect(intygPage.field8b.nedsat100.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(intyg.arbetsformaga.nedsattMed100.tom)).then(
        function(value) {
            logg('OK - Nedsatt med 100% tom = ' +value);
        }, function(reason) {
            callback('FEL, Nedsatt med 100% tom,' + reason);
        });
    }

    // fält 9
    expect(intygPage.FMBprognos.getText()).to.eventually.equal(intyg.arbetsformagaFMB).then(
    function(value) {
        logg('OK - Arbetsformåga FMB prognos = ' +value);
    }, function(reason) {
        callback('FEL, Arbetsformåga FMB prognos,' + reason);
    });

    // fält 10
    if (!smitta) {
        // if(intyg.prognos.val === 'Ja'){
            expect(intygPage.prognosJ.getText()).to.eventually.equal(intyg.prognos.val).then(
                function(value) {
                logg('OK - Arbetsformåga prognos = ' +value);
            }, function(reason) {
                callback('FEL, Arbetsformåga prognos, ' + reason);
            });
        if(intyg.prognos.fortydligande){
            expect(intygPage.prognosFortyd.getText()).to.eventually.equal(intyg.prognos.fortydligande).then(
                function(value) {
                    logg('OK - Arbetsformåga prognos förtydligande = ' +value);
                }, function(reason) {
                    callback('FEL, Arbetsformåga prognos förtydligande, ' + reason);
                });
            }
        }
        // }
        // else if(intyg.prognos.val === 'Ja, delvis'){
        //     expect(intygPage.prognosJD.getText()).to.eventually.equal(intyg.prognos.val).then(
        //         function(value) {
        //         logg('OK - Arbetsformåga prognos (JA, DELVIS) = ' +value);
        //     }, function(reason) {
        //         callback('FEL, Arbetsformåga prognos (JA, DELVIS), ' + reason);
        //     });
        // }
        // else if(intyg.prognos.val === 'Nej'){
        //     expect(intygPage.prognosN.getText()).to.eventually.equal(intyg.prognos.val).then(
        //         function(value) {
        //         logg('OK - Arbetsformåga prognos (NEJ) = ' +value);
        //     }, function(reason) {
        //         callback('FEL, Arbetsformåga prognos (NEJ), ' + reason);
        //     });
        // }
        // else if(intyg.prognos.val ==='Går ej att bedöma'){
        //     expect(intygPage.prognosGIAB.getText()).to.eventually.equal(intyg.prognos.val).then(
        //         function(value) {
        //         logg('OK - Arbetsformåga prognos (GÅR EJ) = ' +value);
        //     }, function(reason) {
        //         callback('FEL, Arbetsformåga prognos (GÅR EJ), ' + reason);
        //     });
    // }



    // Kontrollera FÄLT 11 : Resa till arbete med annat färdsätt
    expect(intygPage.field11.text.getText()).to.eventually.contain(boolTillJaNej(intyg.rekommendationer.resor)).then(function(value) {
        logg('OK - Resor till arbete med annat färdsätt = ' + value);
    }, function(reason) {
        callback('FEL, Resor till arbete med annat färdsätt,' + reason);
    }).then(callback);

    // // Kontrollera FÄLT 10 : Prognos
    // expect(intygPage.field10.text.getText()).to.eventually.equal(intyg.prognos.val).then(function(value) {
    //     logg('OK - Prognos = ' +value);
    // }, function(reason) {
    //     callback('FEL, Prognos,' + reason);
    // });


    // Kontrollera FÄLT 12 : Kontakt önskas med FK
    var kontaktOnskas = boolTillJaNej(intyg.kontaktOnskasMedFK);
    expect(intygPage.field12.text.getText()).to.eventually.equal(kontaktOnskas).then(function(value) {
        logg('OK - Kontakt med FK = ' +value);
    }, function(reason) {
        callback('FEL, Kontakt med FK,' + reason);
    });

    // TBI!
    // expect(intygPage.forsKod.getText()).not.equal(' ').then(function(value) {
    //     logg('OK - Forskrivarkod = ' +value);
    // }, function(reason) {
    //     callback('FEL, Forskrivarkod,' + reason);
    // });

    //Kontrollera FÄLT13 : Övriga upplysningar
    var kommentar = element(by.id('kommentar'));
    expect(kommentar.getText()).to.eventually.contain(intyg.prognos.fortydligande).then(function(value) {
        logg('OK - Övrig kommentar = ' +value);
    }, function(reason) {
        callback('FEL, Övrig kommentar,' + reason);
    }).then(callback);

}
};
