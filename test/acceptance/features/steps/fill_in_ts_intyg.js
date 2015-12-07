/*global
testdata, intyg, browser
*/
'use strict';

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält för ett Diabetes\-intyg$/, function (callback) {
        global.intyg = testdata.getRandomTsDiabetesIntyg();

        // browser.ignoreSynchronization = true;
        // Intyget avser
        fillInKorkortstyper(intyg.korkortstyper, 'intygetAvserForm');

        // Identiteten är styrkt genom
        fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);

        // Allmänt
        fillInAllmant(intyg.allmant);

        // Hypoglykemier
        fillInHypoglykemier(intyg.hypoglykemier, intyg.korkortstyper);

        //Synintyg
        fillInSynintyg(intyg.synintyg);

        //Bedömning
        fillInBedomningDiabetes(intyg.bedomning);

        // browser.ignoreSynchronization = false;

        callback();
    });

    this.Given(/^jag fyller i alla nödvändiga fält för ett läkarintyg$/, function (callback) {
        global.intyg = testdata.getRandomTsBasIntyg();
        global.intyg.typ = 'Transportstyrelsens läkarintyg';
        
        // browser.ignoreSynchronization = true;
        // Intyget avser
        fillInKorkortstyper(intyg.korkortstyper, 'intygetAvserForm');
        // Identiteten är styrkt genom
        fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);
        //Synintyg
        fillInSynintyg(intyg.synintyg);
        // Synfunktioner

        browser.ignoreSynchronization = true;
        fillInSynfunktioner();
        fillInHorselOchBalanssinne();
        fillInRorelseorganensFunktioner();
        fillInHjartOchKarlsjukdomar();
        fillInDiabetes();
        fillInHorselOchBalanssinne();
        fillInNeurologiskaSjukdomar();
        fillInEpilepsi();
        fillInNjursjukdomar();
        fillInDemens();
        fillInSomnOchVakenhet();
        fillInAlkoholNarkotikaLakemedel();
        fillInPsykiska();
        fillInAdhd();
        fillInSjukhusvard();
        fillInOvrigMedicinering();
        fillInOvrigKommentar();
        browser.ignoreSynchronization = false;
        fillInBedomning(intyg.bedomning);
        callback();
    });
};


function fillInKorkortstyper(typer, formName) {
    console.log('Anger körkortstyper: '+ typer.toString());
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
    console.log('');
}

function fillInIdentitetStyrktGenom(idtyp) {
    console.log('Anger identitet styrkt genom ' + idtyp);
    var identitetForm = element(by.id('identitetForm'));
    identitetForm.element(by.cssContainingText('label.radio', idtyp)).click();
}

function fillInAllmant(allmantObj) {
    var allmantForm = element(by.id('allmantForm'));

    // Ange år då diagnos ställts
    console.log('Anger år då diagnos ställts: ' + allmantObj.year);
    allmantForm.element(by.id('diabetesyear')).sendKeys(allmantObj.year);

    // Ange diabetestyp
    console.log('Anger diabetestyp:' + allmantObj.typ);
    allmantForm.element(by.cssContainingText('label.radio', allmantObj.typ)).click();

    // Ange behandlingstyp
    var typer = allmantObj.behandling.typer;
    typer.forEach(function(typ) {
        console.log('Anger behandlingstyp: ' + typ);
        allmantForm.element(by.cssContainingText('label.checkbox', typ)).click();
    });

    if (allmantObj.behandling.insulinYear) {
        console.log('Anger insulin från år: ' + allmantObj.behandling.insulinYear);
        element(by.id('insulinBehandlingsperiod')).sendKeys(allmantObj.behandling.insulinYear);
    }

}

function fillInHypoglykemier(hypoglykemierObj, korkortstyper) {

    console.log('Anger hypoglykemier:' + hypoglykemierObj.toString());

    // a)
    if (hypoglykemierObj.a === 'Ja') {
        element(by.id('hypoay')).click();
    } else {
        element(by.id('hypoan')).click();
    }

    // b)
    if (hypoglykemierObj.b === 'Ja') {
        element(by.id('hypoby')).click();
    } else {
        element(by.id('hypobn')).click();
    }

    // f)
    if (hypoglykemierObj.f) {
        if (hypoglykemierObj.f === 'Ja') {
            element(by.id('hypofy')).click();
        } else {
            element(by.id('hypofn')).click();
        }
    }

    // g)
    if (hypoglykemierObj.g) {
        if (hypoglykemierObj.g === 'Ja') {
            element(by.id('hypogy')).click();
        } else {
            element(by.id('hypogn')).click();
        }
    }
}

function fillInSynintyg(synintygObj) {
    // a)
    if (synintygObj.a === 'Ja') {
        element(by.id('synay')).click();
    } else {
        element(by.id('synan')).click();
    }
}

function fillInBedomningDiabetes(bedomningObj) {
    console.log('Anger bedömning: ' + bedomningObj.stallningstagande);
    var bedomningForm = element(by.id('bedomningForm'));
    bedomningForm.element(by.cssContainingText('label.radio', bedomningObj.stallningstagande)).click();

    if (bedomningObj.lamplighet) {
        console.log('Anger lämplighet: ' + bedomningObj.lamplighet);
        if (bedomningObj.lamplighet === 'Ja') {
            element(by.id('bedomningy')).click();
        } else {
            element(by.id('bedomningn')).click();
        }
    }
}

function fillInBedomning(bedomningObj) {
    console.log('Anger bedömning: ' + bedomningObj.stallningstagande);

    element(by.id('behorighet_bedomning')).click();

    fillInKorkortstyper(intyg.korkortstyper, 'bedomningForm');
}


function fillInSynfunktioner() {
    element(by.id('synan')).click();
    element(by.id('synbn')).click();
    element(by.id('syncn')).click();
    element(by.id('syndn')).click();
    element(by.id('synen')).click();

    element(by.id('synHogerOgaUtanKorrektion')).sendKeys('1,0');
    element(by.id('synVansterOgaUtanKorrektion')).sendKeys('1,0');
    element(by.id('synBinokulartUtanKorrektion')).sendKeys('1,0');
    
}

function fillInHorselOchBalanssinne() {
    element(by.id('horselbalansan')).click();
    element(by.id('horselbalansbn')).click();

}

function fillInRorelseorganensFunktioner() {
    element(by.id('funktionsnedsattningan')).click();
    element(by.id('funktionsnedsattningbn')).click();
}

function fillInHjartOchKarlsjukdomar() {
    element(by.id('hjartkarlan')).click();
    element(by.id('hjartkarlbn')).click();
    element(by.id('hjartkarlcn')).click();
}

function fillInDiabetes() {
    element(by.id('diabetesan')).click();
}

function fillInNeurologiskaSjukdomar() {
    element(by.id('neurologian')).click();
}

function fillInEpilepsi() {
    element(by.id('medvetandestorningan')).click();
}

function fillInNjursjukdomar() {
    element(by.id('njuraran')).click();
}

function fillInDemens() {
    element(by.id('kognitivtan')).click();
}

function fillInSomnOchVakenhet() {
    element.all(by.css('[name="somnvakenheta"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInAlkoholNarkotikaLakemedel() {
    element.all(by.css('[name="narkotikalakemedela"]')).then(function (elm) {
        elm[1].click();
    });
    element.all(by.css('[name="narkotikalakemedelb"]')).then(function (elm) {
        elm[1].click();
    });
    element.all(by.css('[name="narkotikalakemedelc"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInPsykiska() {
    element.all(by.css('[name="psykiskta"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInAdhd() {
    element.all(by.css('[name="utvecklingsstorninga"]')).then(function (elm) {
        elm[1].click();
    });

    element.all(by.css('[name="utvecklingsstorningb"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInSjukhusvard() {
    element.all(by.css('[name="sjukhusvarda"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInOvrigMedicinering() {
    element.all(by.css('[name="medicineringa"]')).then(function (elm) {
        elm[1].click();
    });
}

function fillInOvrigKommentar() {
    element(by.id('kommentar')).sendKeys('Inget att rapportera');
}







