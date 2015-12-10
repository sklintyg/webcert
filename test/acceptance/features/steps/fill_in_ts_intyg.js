/*global
testdata, intyg, browser, pages, logg
*/
'use strict';

var tsdUtkastPage = pages.intygpages['ts-diabetesUtkast'];

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för ett Diabetes\-intyg$/, function (callback) {
        global.intyg = testdata.getRandomTsDiabetesIntyg();

        tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper);
        tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);
        tsdUtkastPage.fillInAllmant(intyg.allmant);
        tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier);
        tsdUtkastPage.fillInSynintyg(intyg.synintyg);
        tsdUtkastPage.fillInBedomning(intyg.bedomning);
        
        callback();
    });

    this.Given(/^jag fyller i alla nödvändiga fält för ett läkarintyg$/, function (callback) {
        global.intyg = testdata.getRandomTsBasIntyg();
        global.intyg.typ = 'Transportstyrelsens läkarintyg';
        
        // browser.ignoreSynchronization = true;
        // Intyget avser
        fillInKorkortstyper(global.intyg.korkortstyper, 'intygetAvserForm');
        // Identiteten är styrkt genom
        fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);
        //Synintyg
        fillInSynintyg(intyg.synintyg);
        // Synfunktioner

        browser.ignoreSynchronization = true;
        fillInSynfunktioner(global.intyg);
        fillInHorselOchBalanssinne(global.intyg);
        fillInRorelseorganensFunktioner(global.intyg);
        fillInHjartOchKarlsjukdomar(global.intyg);
        fillInDiabetes(global.intyg);
        fillInHorselOchBalanssinne(global.intyg);
        fillInNeurologiskaSjukdomar(global.intyg);
        fillInEpilepsi(global.intyg);
        fillInNjursjukdomar(global.intyg);
        fillInDemens(global.intyg);
        fillInSomnOchVakenhet(global.intyg);
        fillInAlkoholNarkotikaLakemedel(global.intyg);
        fillInPsykiska(global.intyg);
        fillInAdhd(global.intyg);
        fillInSjukhusvard(global.intyg);
        fillInOvrigMedicinering(global.intyg);
        fillInOvrigKommentar(global.intyg);
        browser.ignoreSynchronization = false;
        fillInBedomning(intyg.bedomning);
        callback();
    });
};


function fillInKorkortstyper(typer, formName) {
    logg('Anger körkortstyper: '+ typer.toString());
    // typer.forEach(function(typ) {
    //     process.stdout.write(typ + '..');

    browser.ignoreSynchronization = false;
    // hittar flera på text körkortstyp A
    element(by.id(formName)).all(by.css('label.checkbox')).filter(function(elem) {
        //Return the element or elements
        return elem.getText().then(function(text) {
            //Match the text
            return (typer.indexOf(text)>=0);
        });
    }).then(function(filteredElements) {
        //filteredElements is the list of filtered elements
        for (var i = 0; i < filteredElements.length; i++) {
            filteredElements[i].click();
            //Do something
        }
        // filteredElements[0].click();
    });
    // });
    logg('');
}

function fillInIdentitetStyrktGenom(idtyp) {
    logg('Anger identitet styrkt genom ' + idtyp);
    var identitetForm = element(by.id('identitetForm'));
    identitetForm.element(by.cssContainingText('label.radio', idtyp)).click();
}



function fillInSynintyg(synintygObj) {
    // a)
    if (synintygObj.a === 'Ja') {
        element(by.id('synay')).click();
    } else {
        element(by.id('synan')).click();
    }
}

function fillInBedomning(bedomningObj) {
    logg('Anger bedömning: ' + bedomningObj.stallningstagande);

    element(by.id('behorighet_bedomning')).click();

    fillInKorkortstyper(global.intyg.korkortstyper, 'bedomningForm');
}


function fillInSynfunktioner(intyg) {
    if (intyg.synDonder === 'Ja') {
        element(by.id('synay')).click();
    } else {
        element(by.id('synan')).click();
    }
    if (intyg.synNedsattBelysning === 'Ja') {
        element(by.id('synby')).click();
    } else {
        element(by.id('synbn')).click();
    }
    if (intyg.synOgonsjukdom === 'Ja') {
        element(by.id('syncy')).click();
    } else {
        element(by.id('syncn')).click();
    }
    if (intyg.synDubbel === 'Ja') {
        element(by.id('syndy')).click();
    } else {
        element(by.id('syndn')).click();
    }
    if (intyg.synNystagmus === 'Ja') {
        element(by.id('syney')).click();
    } else {
        element(by.id('synen')).click();
    }

    element(by.id('synHogerOgaUtanKorrektion')).sendKeys('0,8');
    element(by.id('synVansterOgaUtanKorrektion')).sendKeys('0,7');
    element(by.id('synBinokulartUtanKorrektion')).sendKeys('1,0');

    element(by.id('synHogerOgaMedKorrektion')).sendKeys('1,0');
    element(by.id('synVansterOgaMedKorrektion')).sendKeys('1,0');
    element(by.id('synBinokulartMedKorrektion')).sendKeys('1,0');
}

function fillInHorselOchBalanssinne(intyg) {
    if (intyg.horselYrsel === 'Ja') {
        element(by.id('horselbalansay')).click();
    } else {
        element(by.id('horselbalansan')).click();
    }

    if (intyg.korkortstyper.indexOf('D1') > -1 ||
        intyg.korkortstyper.indexOf('D') > -1) {
        if (intyg.horselSamtal === 'Ja') {
            element(by.id('horselbalansby')).click();
        } else {
            element(by.id('horselbalansbn')).click();
        }
    }    
}

function fillInRorelseorganensFunktioner(intyg) {
    if (intyg.rorOrgNedsattning === 'Ja') {
        element(by.id('funktionsnedsattningay')).click();
        element(by.id('funktionsnedsattning')).sendKeys('Amputerad under höger knä.');
    } else {
        element(by.id('funktionsnedsattningan')).click();
    }
    
    if (intyg.korkortstyper.indexOf('D1') > -1 ||
        intyg.korkortstyper.indexOf('D') > -1) {
        if (intyg.rorOrgInUt === 'Ja') {
            element(by.id('funktionsnedsattningby')).click();
        } else {
            element(by.id('funktionsnedsattningbn')).click();
        }
    }
}

function fillInHjartOchKarlsjukdomar(intyg) {
    if (intyg.hjartHjarna === 'Ja') {
        element(by.id('hjartkarlay')).click();
    } else {
        element(by.id('hjartkarlan')).click();
    }
    if (intyg.hjartSkada === 'Ja') {
        element(by.id('hjartkarlby')).click();
    } else {
        element(by.id('hjartkarlbn')).click();
    }
    if (intyg.hjartRisk === 'Ja') {
        element(by.id('hjartkarlcy')).click();
        element(by.id('beskrivningRiskfaktorer')).sendKeys('TIA och förmaksflimmer.');
    } else {
        element(by.id('hjartkarlcn')).click();
    }   
}

function fillInDiabetes(intyg) {
    if (intyg.diabetes === 'Ja') {
        element(by.id('diabetesay')).click();

        if (intyg.allmant.typ === 'Typ 1') {
            element(by.id('diabetestyp1')).click();
        } else {
            element(by.id('diabetestyp2')).click();
        }

        // Ange behandlingstyp
        var typer = intyg.allmant.behandling.typer;

        if (typer.indexOf('Endast kost') > -1) {
            element(by.id('diabetestreat1')).click();
        }
        if (typer.indexOf('Tabletter') > -1) {
            element(by.id('diabetestreat2')).click();
        }
        if (typer.indexOf('Insulin') > -1) {
            element(by.id('diabetestreat3')).click();
        }
    } else {
        element(by.id('diabetesan')).click();
    }
}

function fillInNeurologiskaSjukdomar(intyg) {
    if (intyg.neurologiska === 'Ja') {
        element(by.id('neurologiay')).click();
    } else {
        element(by.id('neurologian')).click();
    }
}

function fillInEpilepsi(intyg) {
    if (intyg.epilepsi === 'Ja') {
        element(by.id('medvetandestorningay')).click();
        element(by.id('beskrivningMedvetandestorning')).sendKeys('Blackout. Midsommarafton.');
    } else {
        element(by.id('medvetandestorningan')).click();
    }
}

function fillInNjursjukdomar(intyg) {
    if (intyg.njursjukdom === 'Ja') {
        element(by.id('njuraray')).click();
    } else {
        element(by.id('njuraran')).click();
    }
}

function fillInDemens(intyg) {
    if (intyg.demens === 'Ja') {
        element(by.id('kognitivtay')).click();
    } else {
        element(by.id('kognitivtan')).click();
    }
}

function fillInSomnOchVakenhet(intyg) {
    element.all(by.css('[name="somnvakenheta"]')).then(function (elm) {
        if (intyg.somnVakenhet === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });
}

function fillInAlkoholNarkotikaLakemedel(intyg) {
    element.all(by.css('[name="narkotikalakemedela"]')).then(function (elm) {
        if (intyg.alkoholMissbruk === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });
    element.all(by.css('[name="narkotikalakemedelb"]')).then(function (elm) {
        if (intyg.alkoholVard === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });
    element.all(by.css('[name="narkotikalakemedelc"]')).then(function (elm) {
        if (intyg.alkoholLakemedel === 'Ja') {
            elm[0].click();
            element(by.id('beskrivningNarkotikalakemedel')).sendKeys('2 liter metadon.');
        } else {
            elm[1].click();
        }
    });

    element.all(by.css('[name="narkotikalakemedelb2"]')).then(function (elm) {
        if (intyg.alkoholMissbruk === 'Ja' || intyg.alkoholVard === 'Ja') {
            if (intyg.alkoholProvtagning === 'Ja') {
                elm[0].click();
            } else {
                elm[1].click();
            }
        } 
    });
}

function fillInPsykiska(intyg) {
    element.all(by.css('[name="psykiskta"]')).then(function (elm) {
        if (intyg.psykiskSjukdom === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });
}

function fillInAdhd(intyg) {
    element.all(by.css('[name="utvecklingsstorninga"]')).then(function (elm) {
        if (intyg.adhdPsykisk === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });

    element.all(by.css('[name="utvecklingsstorningb"]')).then(function (elm) {
        if (intyg.adhdSyndrom === 'Ja') {
            elm[0].click();
        } else {
            elm[1].click();
        }
    });
}

function fillInSjukhusvard(intyg) {
    element.all(by.css('[name="sjukhusvarda"]')).then(function (elm) {
        if (intyg.sjukhusvard === 'Ja') {
            elm[0].click();
            element(by.id('tidpunkt')).sendKeys('2015-12-13');
            element(by.id('vardinrattning')).sendKeys('Östra sjukhuset.');
            element(by.id('anledning')).sendKeys('Allmän ysterhet.');
        } else {
            elm[1].click();
        }
    });
}

function fillInOvrigMedicinering(intyg) {
    element.all(by.css('[name="medicineringa"]')).then(function (elm) {
        if (intyg.ovrigMedicin === 'Ja') {
            elm[0].click();
            element(by.id('beskrivningMedicinering')).sendKeys('Xanax FTW');
        } else {
            elm[1].click();
        }
    });
}

function fillInOvrigKommentar(intyg) {
    element(by.id('kommentar')).sendKeys('Inget att rapportera');
}







