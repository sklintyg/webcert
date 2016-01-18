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

/* globals pages*/
/* globals browser, intyg, logg */

'use strict';

var tsBasIntygPage = pages.intyg.ts.bas.intyg;

module.exports ={
	checkTsBasValues:function(intyg, callback){
   
    expect(tsBasIntygPage.synfaltsdefekter.getText()).to.eventually.equal(intyg.synDonder).then(function(value) {
        logg('OK - Synfältsdefekter = ' + value);
    }, function(reason) {
        callback('FEL - Synfältsdefekter : ' + reason);
    });
    expect(tsBasIntygPage.nattblindhet.getText()).to.eventually.equal(intyg.synNedsattBelysning).then(function(value) {
        logg('OK - Nattblindhet = ' + value);
    }, function(reason) {
        callback('FEL - Nattblindhet : ' + reason);
    });
    expect(tsBasIntygPage.progressivOgonsjukdom.getText()).to.eventually.equal(intyg.synOgonsjukdom).then(function(value) {
        logg('OK - Progressiv ögonsjukdom = ' + value);
    }, function(reason) {
        callback('FEL - Progressiv ögonsjukdom : ' + reason);
    });
    expect(tsBasIntygPage.diplopi.getText()).to.eventually.equal(intyg.synDubbel).then(function(value) {
        logg('OK - Diplopi = ' + value);
    }, function(reason) {
        callback('FEL - Diplopi : ' + reason);
    });
    expect(tsBasIntygPage.nystagmus.getText()).to.eventually.equal(intyg.synNystagmus).then(function(value) {
        logg('OK - Nystagmus = ' + value);
    }, function(reason) {
        callback('FEL - Nystagmus : ' + reason);
    });

    expect(tsBasIntygPage.hogerOgautanKorrektion.getText()).to.eventually.equal(intyg.styrkor.houk.toString().replace('.',',')).then(function(value) {
        logg('OK - Höger Öga utan korrektion = ' + value);
    }, function(reason) {
        callback('FEL - Höger Öga utan korrektion : ' + reason);
    });
    expect(tsBasIntygPage.hogerOgamedKorrektion.getText()).to.eventually.equal(intyg.styrkor.homk.toString().replace('.',',')).then(function(value) {
        logg('OK - Höger Öga med korrektion = ' + value);
    }, function(reason) {
        callback('FEL - Höger Öga med korrektion : ' + reason);
    });
    expect(tsBasIntygPage.vansterOgautanKorrektion.getText()).to.eventually.equal(intyg.styrkor.vouk.toString().replace('.',',')).then(function(value) {
        logg('OK - Vänster öga utan korrektion = ' + value);
    }, function(reason) {
        callback('FEL - Vänster öga utan korrektion : ' + reason);
    });
    expect(tsBasIntygPage.vansterOgamedKorrektion.getText()).to.eventually.equal(intyg.styrkor.vomk.toString().replace('.',',')).then(function(value) {
        logg('OK - Höger Öga med korrektion = ' + value);
    }, function(reason) {
        callback('FEL - Höger Öga med korrektion: ' + reason);
    });
    expect(tsBasIntygPage.binokulartutanKorrektion.getText()).to.eventually.equal(intyg.styrkor.buk.toString().replace('.',',')).then(function(value) {
        logg('OK - Binokulärt utan klorrektion = ' + value);
    }, function(reason) {
        callback('FEL - Binokulärt utan klorrektion : ' + reason);
    });
    expect(tsBasIntygPage.binokulartmedKorrektion.getText()).to.eventually.equal(intyg.styrkor.bmk.toString().replace('.',',')).then(function(value) {
        logg('OK - Binokulärt med klorrektion = ' + value);
    }, function(reason) {
        callback('FEL - Binokulärt med klorrektion : ' + reason);
    });
    expect(tsBasIntygPage.vansterOgakontaktlins.getText()).to.eventually.equal(intyg.linser.vanster).then(function(value) {
        logg('OK - Vänster öga kontaktlins = ' + value);
    }, function(reason) {
        callback('FEL - Vänster öga kontaktlins : ' + reason);
    });    
    expect(tsBasIntygPage.hogerOgakontaktlins.getText()).to.eventually.equal(intyg.linser.hoger).then(function(value) {
        logg('OK - Höger öga kontaktlins = ' + value);
    }, function(reason) {
        callback('FEL - Höger öga kontaktlins : ' + reason);
    });    

    var _sum = (+ intyg.styrkor.homk - +intyg.styrkor.houk) +  ( + intyg.styrkor.vomk - + intyg.styrkor.vouk);
    if (_sum < 8) {
        expect(tsBasIntygPage.korrektionsglasensStyrka.getText()).to.eventually.equal('Nej').then(function(value) {
            logg('OK - Korrektionsglasens styrka = ' + value);
        }, function(reason) {
            callback('FEL - Korrektionsglasens styrka : ' + reason);
        });
    } else {
        expect(tsBasIntygPage.korrektionsglasensStyrka.getText()).to.eventually.equal('Ja').then(function(value) {
            logg('OK - Korrektionsglasens styrka = ' + value);
        }, function(reason) {
            callback('FEL - Korrektionsglasens styrka : ' + reason);
        });
    }
    expect(tsBasIntygPage.horselBalansbalansrubbningar.getText()).to.eventually.equal(intyg.horselYrsel).then(function(value) {
        logg('OK - Hörsel balansbalans rubbningar = ' + value);
    }, function(reason) {
        callback('FEL - Hörsel balansbalans rubbningar : ' + reason);
    });

    expect(tsBasIntygPage.funktionsnedsattning.getText()).to.eventually.equal(intyg.rorOrgNedsattning).then(function(value) {
        logg('OK - Rörelsehinder = ' + value);
    }, function(reason) {
        callback('FEL - Rörelsehinder : ' + reason);
    });
    
    if(intyg.rorOrgNedsattning==='Ja'){
        expect(tsBasIntygPage.funktionsnedsattningbeskrivning.getText()).to.eventually.equal('Amputerad under höger knä.').then(function(value) {
            logg('OK - Rörelsehinder kommentar = ' + value);
        }, function(reason) {
            callback('FEL - Rörelsehinder kommentar : ' + reason);
        });
    }else{
        expect(tsBasIntygPage.funktionsnedsattningbeskrivning.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Rörelsehinder kommentar är tom = ' + value);
        }, function(reason) {
            callback('FEL - Rörelsehinder kommentar är tom : ' + reason);
        });
    }
    
    expect(tsBasIntygPage.funktionsnedsRorelseformaga.getText()).to.eventually.equal(intyg.rorOrgInUt).then(function(value) {
            logg('OK - Rörelseförmågan = ' + value);
        }, function(reason) {
            callback('FEL - Rörelseförmågan : ' + reason);
        });

    expect(tsBasIntygPage.hjartKarlSjukdom.getText()).to.eventually.equal(intyg.hjartHjarna).then(function(value) {
        logg('OK - Hjart kärl sjukdom = ' + value);
    }, function(reason) {
        callback('FEL - Hjart kärl sjukdom : ' + reason);
    });

    expect(tsBasIntygPage.hjarnskadaEfterTrauma.getText()).to.eventually.equal(intyg.hjartSkada).then(function(value) {
        logg('OK - Hjärnskada efter trauma = ' + value);
    }, function(reason) {
        callback('FEL - Hjärnskada efter trauma : ' + reason);
    });


    expect(tsBasIntygPage.riskfaktorerStroke.getText()).to.eventually.equal(intyg.hjartRisk).then(function(value) {
        logg('OK - Riskfaktorer för stroke = ' + value);
    }, function(reason) {
        callback('FEL - Riskfaktorer för stroke : ' + reason);
    });

    if (intyg.hjartRisk === 'Ja') {
        expect(tsBasIntygPage.beskrivningRiskfaktorer.getText()).to.eventually.equal('TIA och förmaksflimmer.').then(function(value) {
            logg('OK - Riskfaktorer för stroke (Kommentar) = ' + value);
        }, function(reason) {
            callback('FEL - Riskfaktorer för stroke (Kommentar) : ' + reason);
        });
    }
    else{
        expect(tsBasIntygPage.beskrivningRiskfaktorer.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Riskfaktorer för stroke (Kommentar) = \"TOMT\"');
        }, function(reason) {
            callback('FEL - Riskfaktorer för stroke (Kommentar) : ' + reason);
        });
    }

    expect(tsBasIntygPage.harDiabetes.getText()).to.eventually.equal(intyg.diabetes).then(function(value) {
        logg('OK - Patient har Diabetes = ' + value);
    }, function(reason) {
        callback('FEL - Patient har Diabetes : ' + reason);
    });

    if (intyg.diabetestyp === 'Typ 2' && intyg.diabetes === 'Ja') {
        var typer = intyg.dTyper;
        expect(tsBasIntygPage.diabetesTyp.getText()).to.eventually.equal(intyg.diabetestyp).then(function(value) {
            logg('OK - Patient diabetes typ = ' + value);
        }, function(reason) {
            callback('FEL - Patient diabetes typ : ' + reason);
        });
        typer.forEach( function (_typ){
            if (_typ === 'Endast kost') {
                expect(tsBasIntygPage.kost.getText()).to.eventually.equal('Kost').then(function(value) {
                    logg('OK - Endast kost = ' + value);
                }, function(reason) {
                    callback('FEL - Endast kost : ' + reason);
                });
            }else if (_typ === 'Tabletter'){
                expect(tsBasIntygPage.tabeltter.getText()).to.eventually.equal('Tabletter').then(function(value) {
                    logg('OK - Tabletter = ' + value);
                }, function(reason) {
                    callback('FEL - Tabletter : ' + reason);
                });
            }else if (_typ === 'Insulin'){
                expect(tsBasIntygPage.insulin.getText()).to.eventually.equal('Insulin').then(function(value) {
                    logg('OK - Insulin = ' + value);
                }, function(reason) {
                    callback('FEL - Insulin: ' + reason);
                });
            }
        });
    } else if (intyg.diabetestyp === 'Typ 1'){
        expect(tsBasIntygPage.kost.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Insulin = \"TOMT\"');
                }, function(reason) {
                    callback('FEL - Insulin: \"TOMT\"');
                });
        expect(tsBasIntygPage.tabeltter.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Insulin = \"TOMT\"');
                }, function(reason) {
                    callback('FEL - Insulin: \"TOMT\"');
                });
        expect(tsBasIntygPage.insulin.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Insulin = \"TOMT\"');
                }, function(reason) {
                    callback('FEL - Insulin : \"TOMT\"');
                });
    }

    expect(tsBasIntygPage.neurologiskSjukdom.getText()).to.eventually.equal(intyg.neurologiska).then(function(value) {
        logg('OK - Neurologiska sjukdomar = ' + value);
        }, function(reason) {
            callback('FEL - Neurologiska sjukdomar: ' + reason);
        });

    expect(tsBasIntygPage.medvetandestorning.getText()).to.eventually.equal(intyg.epilepsi).then(function(value) {
            logg('OK - Patienten har eller har patienten haft epilepsi = ' + value);
                }, function(reason) {
                    callback('FEL - Patienten har eller har patienten haft epilepsi: ' + reason);
                });
    if (intyg.epilepsi === 'Ja') {
        expect(tsBasIntygPage.medvetandestorningbeskrivning.getText()).to.eventually.equal('Blackout. Midsommarafton.').then(function(value) {
            logg('OK - Kommentar: \"Blackout. Midsommarafton.\"');
                }, function(reason) {
                    callback('FEL - Kommentar: \"Blackout. Midsommarafton.\" -> ' + reason);
                });
    }

    expect(tsBasIntygPage.nedsattNjurfunktion.getText()).to.eventually.equal(intyg.njursjukdom).then(function(value) {
        logg('OK - Njurfunktion = '+ value);
            }, function(reason) {
                callback('FEL - Njurfunktion = '+ reason);
            });

    expect(tsBasIntygPage.sviktandeKognitivFunktion.getText()).to.eventually.equal(intyg.demens).then(function(value) {
        logg('OK - Kognitiv funktion = '+ value);
            }, function(reason) {
                callback('FEL - Kognitiv funktion = '+ reason);
            });

    expect(tsBasIntygPage.teckenSomnstorningar.getText()).to.eventually.equal(intyg.somnVakenhet).then(function(value) {
        logg('OK - Tecken sömnstörningar = '+ value);
            }, function(reason) {
                callback('FEL - Tecken sömnstörningar = '+ reason);
            });
    
    expect(tsBasIntygPage.teckenMissbruk.getText()).to.eventually.equal(intyg.alkoholMissbruk).then(function(value) {
        logg('OK - Missbruk eller beroende = '+ value);
        }, function(reason) {
            callback('FEL - Missbruk eller beroende = '+ reason);
        });

    expect(tsBasIntygPage.foremalForVardinsats.getText()).to.eventually.equal(intyg.alkoholVard).then(function(value) {
        logg('OK - Alkohol vård = '+ value);
        }, function(reason) {
            callback('FEL - Alkohol vård = '+ reason);
        });

    if(intyg.alkoholMissbruk ==='Ja' || intyg.alkoholVard === 'Ja'){
        expect(tsBasIntygPage.provtagningBehovs.getText()).to.eventually.equal(intyg.alkoholProvtagning).then(function(value) {
            logg('OK - Alkohol provtagning = '+ value);
            }, function(reason) {
                callback('FEL - Alkohol provtagning = '+ reason);
            });   
    } else {
        expect(tsBasIntygPage.provtagningBehovs.getText()).to.eventually.equal('').then(function(value) {
            logg('OK - Alkohol provtagning = '+ value);
            }, function(reason) {
                callback('FEL - Alkohol provtagning = '+ reason);
            });  
    }
    expect(tsBasIntygPage.lakarordineratLakemedelsbruk.getText()).to.eventually.equal(intyg.alkoholLakemedel).then(function(value) {
        logg('OK - Alkohol läkemedel = '+ value);
        }, function(reason) {
            callback('FEL - Alkohol läkemedel = '+ reason);
        });   

    
    if (intyg.alkoholLakemedel==='Ja') {
        expect(tsBasIntygPage.lakemedelOchDos.getText()).to.eventually.equal('2 liter metadon.').then(function(value) {
        logg('OK - Kommentar innehåller = \" 2 liter metadon.\"');
        }, function(reason) {
            callback('FEL - Kommentar innehåller: \" 2 liter metadon.\" ->'+ reason);
        });
    }

    expect(tsBasIntygPage.psykiskSjukdom.getText()).to.eventually.equal(intyg.psykiskSjukdom).then(function(value) {
        logg('OK - Psykisk sjukdom = '+ value);
        }, function(reason) {
            callback('FEL - Psykisk sjukdom: '+ reason);
        }).then(callback);

    expect(tsBasIntygPage.psykiskUtvecklingsstorning.getText()).to.eventually.equal(intyg.adhdPsykisk).then(function(value) {
        logg('OK - ADHD psykisk = '+ value);
        }, function(reason) {
            callback('FEL - ADHD psykisk: '+ reason);
        });

    expect(tsBasIntygPage.harSyndrom.getText()).to.eventually.equal(intyg.adhdSyndrom).then(function(value) {
        logg('OK - ADHD syndrom = '+ value);
        }, function(reason) {
            callback('FEL - ADHD syndrom: '+ reason);
        }).then(callback);

    if (intyg.sjukhusvard === 'Ja') {
        expect(tsBasIntygPage.tidpunkt.getText()).to.eventually.equal('2015-12-13').then(function(value) {
        logg('OK - Tidpunkt = '+ value);
        }, function(reason) {
            callback('FEL - Tidpunkt: '+ reason);
        });

        expect(tsBasIntygPage.vardinrattning.getText()).to.eventually.equal('Östra sjukhuset.').then(function(value) {
        logg('OK - Vårdinrättning = '+ value);
        }, function(reason) {
            callback('FEL - Vårdinrättning: '+ reason);
        });

        expect(tsBasIntygPage.sjukhusvardanledning.getText()).to.eventually.equal('Allmän ysterhet.').then(function(value) {
        logg('OK - Sjukhusvårdanledning = '+ value);
        }, function(reason) {
            callback('FEL - Sjukhusvårdanledning: '+ reason);
        });

        expect(tsBasIntygPage.sjukhusEllerLakarkontakt.getText()).to.eventually.contain('Ja').then(function(value) {
        logg('OK - Sjukhus Eller Läkarkontakt = '+ value);
        }, function(reason) {
            callback('FEL - Sjukhus Eller Läkarkontakt: '+ reason);
        });
    } else {
        expect(tsBasIntygPage.sjukhusEllerLakarkontakt.getText()).to.eventually.equal('Nej').then(function(value) {
        logg('OK - Sjukhus Eller Läkarkontakt = '+ value);
        }, function(reason) {
            callback('FEL - Sjukhus Eller Läkarkontakt: '+ reason);
        }).then(callback);
    }

    if (intyg.ovrigMedicin === 'Ja') {
        expect(tsBasIntygPage.medicineringbeskrivning.getText()).to.eventually.equal('beskrivning övrig medicinering').then(function(value) {
        logg('OK - Stadigvarande medicinering = '+ value);
        }, function(reason) {
            callback('FEL - Stadigvarande medicinering: '+ reason);
        }).then(callback);

    } else if (intyg.ovrigMedicin === 'Nej'){
        expect(tsBasIntygPage.stadigvarandeMedicinering.getText()).to.eventually.equal('Nej').then(function(value) {
        logg('OK - Stadigvarande medicinering = '+ value);
        }, function(reason) {
            callback('FEL - Stadigvarande medicinering: '+ reason);
        }).then(callback);
    }
}
};
