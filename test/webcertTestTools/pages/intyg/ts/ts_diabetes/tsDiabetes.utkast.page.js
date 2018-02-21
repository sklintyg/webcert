/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/**
 * Created by bennysce on 09/12/15.
 */
/*globals element,by, protractor, Promise*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');

var TsDiabetesUtkast = BaseTsUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-diabetes';
        this.at = element(by.id('edit-ts-diabetes'));

        this.allmant = {
            diabetesyear: element(by.id('diabetes-observationsperiod')),
            formDiabetesTyp: element(by.id('form_diabetes-diabetestyp')),
            behandling: {
                kost: element(by.id('diabetes-endastKost')),
                tabletter: element(by.id('diabetes-tabletter')),
                insulin: element(by.id('diabetes-insulin'))
            },
            insulinbehandlingsperiod: element(by.id('diabetes-insulinBehandlingsperiod')),
            insulin: element(by.id('diabetes-insulin')),
            annanbehandling: element(by.id('diabetes-annanBehandlingBeskrivning'))
        };

        this.hypoglykemier = {
            a: {
                yes: element(by.id('hypoglykemier-kunskapOmAtgarderYes')),
                no: element(by.id('hypoglykemier-kunskapOmAtgarderNo'))
            },
            b: {
                yes: element(by.id('hypoglykemier-teckenNedsattHjarnfunktionYes')),
                no: element(by.id('hypoglykemier-teckenNedsattHjarnfunktionNo'))
            },
            c: {
                yes: element(by.id('hypoglykemier-saknarFormagaKannaVarningsteckenYes')),
                no: element(by.id('hypoglykemier-saknarFormagaKannaVarningsteckenNo'))
            },
            d: {
                yes: element(by.id('hypoglykemier-allvarligForekomstYes')),
                no: element(by.id('hypoglykemier-allvarligForekomstNo')),
                antalEpisoder: element(by.id('hypoglykemier-allvarligForekomstBeskrivning'))
            },
            e: {
                yes: element(by.id('hypoglykemier-allvarligForekomstTrafikenYes')),
                no: element(by.id('hypoglykemier-allvarligForekomstTrafikenNo')),
                antalEpisoder: element(by.id('hypoglykemier-allvarligForekomstTrafikBeskrivning'))
            },
            f: {
                yes: element(by.id('hypoglykemier-egenkontrollBlodsockerYes')),
                no: element(by.id('hypoglykemier-egenkontrollBlodsockerNo'))
            },
            g: {
                yes: element(by.id('hypoglykemier-allvarligForekomstVakenTidYes')),
                no: element(by.id('hypoglykemier-allvarligForekomstVakenTidNo')),
                datum: element(by.id('datepicker_hypoglykemier.allvarligForekomstVakenTidObservationstid'))
            }
        };

        this.validering = {
            intygAvser: element(by.css('[data-validation-section="intygavser"]'))

        };

        this.syn = {
            a: {
                yes: element(by.id('syn-separatOgonlakarintygYes')),
                no: element(by.id('syn-separatOgonlakarintygNo'))
            },
            hoger: {
                utan: element(by.id('syn-hoger-utanKorrektion')),
                med: element(by.id('syn-hoger-medKorrektion'))
            },
            vanster: {
                utan: element(by.id('syn-vanster-utanKorrektion')),
                med: element(by.id('syn-vanster-medKorrektion'))
            },
            binokulart: {
                utan: element(by.id('syn-binokulart-utanKorrektion')),
                med: element(by.id('syn-binokulart-medKorrektion'))
            }

        };

        this.patientAdress = {
            postAdress: element(by.id('patientPostadress')),
            postNummer: element(by.id('patientPostnummer')),
            postOrt: element(by.id('patientPostort'))
        };
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

        var promisesArr = [];

        // Ange år då diagnos ställts
        promisesArr.push(this.allmant.diabetesyear.sendKeys(allmant.year));
        promisesArr.push(this.allmant.annanbehandling.sendKeys(allmant.annanbehandling));

        // Ange diabetestyp
        promisesArr.push(this.allmant.formDiabetesTyp.element(by.cssContainingText('label.big-radio-label', allmant.typ)).sendKeys(protractor.Key.SPACE));

        // Ange behandlingstyp
        var typer = allmant.behandling.typer;
        var behandlingForm = this.allmant.behandling;
        typer.forEach(function(typ) {
            if (typ === 'Endast kost') {
                promisesArr.push(behandlingForm.kost.sendKeys(protractor.Key.SPACE));
            } else if (typ === 'Tabletter') {
                promisesArr.push(behandlingForm.tabletter.sendKeys(protractor.Key.SPACE));
            } else if (typ === 'Insulin') {
                promisesArr.push(behandlingForm.insulin.sendKeys(protractor.Key.SPACE));
            }
        });

        if (allmant.behandling.insulinYear) {
            promisesArr.push(this.allmant.insulinbehandlingsperiod.sendKeys(allmant.behandling.insulinYear));
        }


        return Promise.all(promisesArr);

    },
    fillInHypoglykemier: function(hypoglykemierObj) {
        //TODO: jshint: This function's cyclomatic complexity is too high.
        //logger.info('Anger hypoglykemier:' + hypoglykemierObj.toString());
        var promisesArr = [];
        var hypoglykemierEl = this.hypoglykemier;

        // a)
        if (hypoglykemierObj.a) {
            if (hypoglykemierObj.a === 'Ja') {
                promisesArr.push(hypoglykemierEl.a.yes.sendKeys(protractor.Key.SPACE));
            } else {
                promisesArr.push(hypoglykemierEl.a.no.sendKeys(protractor.Key.SPACE));
            }
        }

        // b)
        if (hypoglykemierObj.b) {
            if (hypoglykemierObj.b === 'Ja') {
                promisesArr.push(hypoglykemierEl.b.yes.sendKeys(protractor.Key.SPACE));
            } else {
                promisesArr.push(hypoglykemierEl.b.no.sendKeys(protractor.Key.SPACE));
            }
        }
        // c)
        if (hypoglykemierObj.c) {
            if (hypoglykemierObj.c === 'Ja') {
                promisesArr.push(hypoglykemierEl.c.yes.sendKeys(protractor.Key.SPACE));
            } else {
                promisesArr.push(hypoglykemierEl.c.no.sendKeys(protractor.Key.SPACE));
            }
        }
        // d)
        if (hypoglykemierObj.d) {
            if (hypoglykemierObj.d === 'Ja') {
                promisesArr.push(hypoglykemierEl.d.yes.sendKeys(protractor.Key.SPACE).then(function() {
                    // d) antal episoder
                    return hypoglykemierEl.d.antalEpisoder.sendKeys(hypoglykemierObj.dAntalEpisoder);
                }));
            } else {
                promisesArr.push(hypoglykemierEl.d.no.sendKeys(protractor.Key.SPACE));
            }
        }

        // e)
        if (hypoglykemierObj.e) {
            if (hypoglykemierObj.e === 'Ja') {
                promisesArr.push(hypoglykemierEl.e.yes.sendKeys(protractor.Key.SPACE).then(function() {
                    // e) antal episoder
                    return hypoglykemierEl.e.antalEpisoder.sendKeys(hypoglykemierObj.eAntalEpisoder);
                }));
            } else {
                promisesArr.push(hypoglykemierEl.e.no.sendKeys(protractor.Key.SPACE));
            }
        }

        // f)
        if (hypoglykemierObj.f) {
            if (hypoglykemierObj.f === 'Ja') {
                promisesArr.push(hypoglykemierEl.f.yes.sendKeys(protractor.Key.SPACE));
            } else {
                promisesArr.push(hypoglykemierEl.f.no.sendKeys(protractor.Key.SPACE));
            }
        }

        // g)
        if (hypoglykemierObj.g) {
            if (hypoglykemierObj.g === 'Ja') {
                promisesArr.push(hypoglykemierEl.g.yes.sendKeys(protractor.Key.SPACE).then(function() {
                    // Datum
                    return hypoglykemierEl.g.datum.sendKeys(hypoglykemierObj.gDatum);
                }));
            } else {
                promisesArr.push(hypoglykemierEl.g.no.sendKeys(protractor.Key.SPACE));
            }
        }

        return Promise.all(promisesArr);
    },
    fillInSynintyg: function(synintygObj) {
        // a)
        if (synintygObj.a === 'Ja') {
            return this.syn.a.yes.sendKeys(protractor.Key.SPACE);
        } else {
            return this.syn.a.no.sendKeys(protractor.Key.SPACE);
        }
    }
});

module.exports = new TsDiabetesUtkast();
