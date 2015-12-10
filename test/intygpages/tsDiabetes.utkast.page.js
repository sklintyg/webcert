/**
 * Created by bennysce on 09/12/15.
 */
/*globals element,by*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

var TsDiabetesUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.at = element(by.id('edit-ts-diabetes'));

        this.korkortsTyperChecks = element(by.id('intygetAvserForm')).all(by.css('label.checkbox'));

        this.identitetForm = element(by.id('identitetForm'));

        this.allmant = {
            form: element(by.id('allmantForm')),
            insulinbehandlingsperiod: element(by.id('insulinBehandlingsperiod'))
        };
        this.allmant.diabetesyear = this.allmant.form.element(by.id('diabetesyear'));

        this.hypoglykemier = {
            a: {
                yes: element(by.id('hypoay')),
                no: element(by.id('hypoan'))
            },
            b: {
                yes: element(by.id('hypoby')),
                no: element(by.id('hypobn'))
            },
            c: {
                yes: element(by.id('hypocy')),
                no: element(by.id('hypocn'))
            },
            d: {
                yes: element(by.id('hypody')),
                no: element(by.id('hypodn'))
            },
            e: {
                yes: element(by.id('hypoey')),
                no: element(by.id('hypoen'))
            },
            f: {
                yes: element(by.id('hypofy')),
                no: element(by.id('hypofn'))
            },
            g: {
                yes: element(by.id('hypogy')),
                no: element(by.id('hypogn'))
            }
        };

        this.syn = {
            a: {
                yes: element(by.id('synay')),
                no: element(by.id('synan'))
            }
        };

        this.bedomning = {
            form: element(by.id('bedomningForm')),
            yes: element(by.id('bedomningy')),
            no: element(by.id('bedomningn'))
        };
    },
    get: function get(intygId) {
        get._super.call(this, 'ts-diabetes', intygId);
    },
    fillInKorkortstyper: function(typer) {
        console.log('Anger körkortstyper: ' + typer.toString());

        // hittar flera på text körkortstyp A
        this.korkortsTyperChecks.filter(function(elem) {
            //Return the element or elements
            return elem.getText().then(function(text) {
                //Match the text
                return (typer.indexOf(text) >= 0);
            });
        }).then(function(filteredElements) {
            //filteredElements is the list of filtered elements
            for (var i = 0; i < filteredElements.length; i++) {
                filteredElements[i].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    fillInIdentitetStyrktGenom: function(idtyp) {
        console.log('Anger identitet styrkt genom ' + idtyp);
        this.identitetForm.element(by.cssContainingText('label.radio', idtyp)).sendKeys(protractor.Key.SPACE);
    },

    /**
     *
     * @param allmant
     *      {
     *          year,
     *          typ,
     *          behandling: {
     *              typer,
     *              insulinYear
     *          }
     *      }
     */
    fillInAllmant: function(allmant) {

        // Ange år då diagnos ställts
        //console.log('Anger år då diagnos ställts: ' + allmant.year);
        this.allmant.diabetesyear.sendKeys(allmant.year);

        var form = this.allmant.form;

        // Ange diabetestyp
        //console.log('Anger diabetestyp:' + allmant.typ);
        form.element(by.cssContainingText('label.radio', allmant.typ)).sendKeys(protractor.Key.SPACE);

        // Ange behandlingstyp
        var typer = allmant.behandling.typer;
        typer.forEach(function(typ) {
            console.log('Anger behandlingstyp: ' + typ);
            form.element(by.cssContainingText('label.checkbox', typ)).sendKeys(protractor.Key.SPACE);
        });

        if (allmant.behandling.insulinYear) {
            console.log('Anger insulin från år: ' + allmant.behandling.insulinYear);
            this.allmant.insulinbehandlingsperiod.sendKeys(allmant.behandling.insulinYear);
        }
    },
    fillInHypoglykemier: function(hypoglykemierObj) {

        //console.log('Anger hypoglykemier:' + hypoglykemierObj.toString());

        // a)
        if (hypoglykemierObj.a === 'Ja') {
            this.hypoglykemier.a.yes.sendKeys(protractor.Key.SPACE);
        } else {
            this.hypoglykemier.a.no.sendKeys(protractor.Key.SPACE);
        }

        // b)
        if (hypoglykemierObj.b === 'Ja') {
            this.hypoglykemier.b.yes.sendKeys(protractor.Key.SPACE);
        } else {
            this.hypoglykemier.b.no.sendKeys(protractor.Key.SPACE);
        }

        // f)
        if (hypoglykemierObj.f) {
            if (hypoglykemierObj.f === 'Ja') {
                this.hypoglykemier.f.yes.sendKeys(protractor.Key.SPACE);
            } else {
                this.hypoglykemier.f.no.sendKeys(protractor.Key.SPACE);
            }
        }

        // g)
        if (hypoglykemierObj.g) {
            if (hypoglykemierObj.g === 'Ja') {
                this.hypoglykemier.g.yes.sendKeys(protractor.Key.SPACE);
            } else {
                this.hypoglykemier.g.no.sendKeys(protractor.Key.SPACE);
            }
        }
    },
    fillInSynintyg: function(synintygObj) {
        // a)
        if (synintygObj.a === 'Ja') {
            this.syn.a.yes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.a.no.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInBedomning: function(bedomningObj) {
        console.log('Anger bedömning: ' + bedomningObj.stallningstagande);
        this.bedomning.form.element(by.id(bedomningObj.stallningstagande)).sendKeys(protractor.Key.SPACE);

        console.log('Anger körkortstyper: ' + bedomningObj.behorigheter.toString());

        helpers.page.clickAll(this.bedomning.form.all(by.css('label.checkbox')), bedomningObj.behorigheter);

        if (bedomningObj.lamplighet) {
            console.log('Anger lämplighet: ' + bedomningObj.lamplighet);
            if (bedomningObj.lamplighet === 'Ja') {
                this.bedomning.yes.sendKeys(protractor.Key.SPACE);
            } else {
                this.bedomning.no.sendKeys(protractor.Key.SPACE);
            }
        }
    }
});

module.exports = new TsDiabetesUtkast();
