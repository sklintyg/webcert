/*global
browser, testdata
*/
'use strict';

module.exports = function() {
    this.Given(/^jag fyller i alla nödvändiga fält enligt mall för ett Diabetes\-MIN\-intyg$/, function(callback) {
        var intyg = testdata.getRandomTsDiabetesIntyg();



        browser.ignoreSynchronization = true;
        // Intyget avser
        fillInKorkortstyper(intyg.korkortstyper);

        // Identiteten är styrkt genom
        fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);

        // Allmänt
        fillInAllmant(intyg.allmant);

        // Hypoglykemier
        fillInHypoglykemier(intyg.hypoglykemier, intyg.korkortstyper);

        //Synintyg
        fillInSynintyg(intyg.synintyg);

        //Bedömning
        fillInBedomning(intyg.bedomning);


        browser.ignoreSynchronization = false;

        callback();
    });
};


function fillInKorkortstyper(typer) {
    console.log('Anger körkortstyper: ');
    typer.forEach(function(typ) {
        process.stdout.write(typ + '..');
        element.all(by.cssContainingText('label.checkbox', typ)).first().click();
    });
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

    console.log('Anger hypoglykemier:' + hypoglykemierObj);

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

function fillInBedomning(bedomningObj) {
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