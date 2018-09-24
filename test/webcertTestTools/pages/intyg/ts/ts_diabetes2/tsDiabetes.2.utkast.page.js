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
/*globals element,by, protractor, Promise*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');
const pageHelpers = require('../../../pageHelper.util.js');
const testTools = require('common-testtools');
testTools.protractorHelpers.init();

var TsDiabetes2Utkast = BaseTsUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-diabetes-2';
        this.at = element(by.id('edit-ts-diabetes-2'));

        // override some form selecting cause id's differ
        this.korkortsTyperChecks = element(by.id('form_intygAvser-kategorier')).all(by.css('label'));
        this.identitetForm = element(by.id('form_identitetStyrktGenom-typ'));
        this.bedomningKorkortsTyperChecks = element(by.id('form_bedomning-uppfyllerBehorighetskrav')).all(by.css('label'));
        this.kommentar = element(by.id('ovrigt'));

        this.allmant = {
            diabetesyear: element(by.id('allmant-diabetesDiagnosAr')),
            formDiabetesTyp: element(by.id('form_allmant-typAvDiabetes')),
            behandling: {
                kost: element(by.id('allmant-behandling-endastKost')),
                tabletter: element(by.id('allmant-behandling-tabletter')),
                insulin: element(by.id('allmant-behandling-insulin')),
                annan: element(by.id('allmant-behandling-annanBehandling'))
            },
            insulinbehandlingsperiod: null,
            insulin: element(by.id('diabetes-insulin')),
            annanbehandling: null
        };

        this.hypoglykemier = {
            a: {
                yes: element(by.id('hypoglykemier-sjukdomenUnderKontrollYes')),
                no: element(by.id('hypoglykemier-sjukdomenUnderKontrollNo'))
            },
            b: {
                yes: element(by.id('hypoglykemier-nedsattHjarnfunktionYes')),
                no: element(by.id('hypoglykemier-nedsattHjarnfunktionNo'))
            },
            c: {
                yes: element(by.id('hypoglykemier-forstarRiskerYes')),
                no: element(by.id('hypoglykemier-forstarRiskerNo'))
            },
            d: {
                yes: element(by.id('hypoglykemier-fortrogenMedSymptomYes')),
                no: element(by.id('hypoglykemier-fortrogenMedSymptomNo'))
            },
            e: {
                yes: element(by.id('hypoglykemier-saknarFormagaVarningsteckenYes')),
                no: element(by.id('hypoglykemier-saknarFormagaVarningsteckenNo'))
            },
            f: {
                yes: element(by.id('hypoglykemier-kunskapLampligaAtgarderYes')),
                no: element(by.id('hypoglykemier-kunskapLampligaAtgarderNo'))
            },
            g: {
                yes: element(by.id('hypoglykemier-egenkontrollBlodsockerYes')),
                no: element(by.id('hypoglykemier-egenkontrollBlodsockerNo'))
            },
            h: {
                yes: element(by.id('hypoglykemier-aterkommandeSenasteAretYes')),
                no: element(by.id('hypoglykemier-aterkommandeSenasteAretNo')),
                datePickerId: 'datepicker_hypoglykemier.aterkommandeSenasteTidpunkt'
            },
            i: {
                yes: element(by.id('hypoglykemier-aterkommandeSenasteKvartaletYes')),
                no: element(by.id('hypoglykemier-aterkommandeSenasteKvartaletNo')),
                datePickerId: 'datepicker_hypoglykemier.senasteTidpunktVaken'
            },
            j: {
                yes: element(by.id('hypoglykemier-forekomstTrafikYes')),
                no: element(by.id('hypoglykemier-forekomstTrafikNo')),
                datePickerId: 'datepicker_hypoglykemier.forekomstTrafikTidpunkt'
            }
        };

        this.syn = {
            a: {
                yes: element(by.id('synfunktion-misstankeOgonsjukdomYes')),
                no: element(by.id('synfunktion-misstankeOgonsjukdomNo'))
            },
            b: {
                yes: element(by.id('synfunktion-ogonbottenFotoSaknasYes')),
                no: element(by.id('synfunktion-ogonbottenFotoSaknasNo'))
            },
            styrkor: {
                houk: element(by.id('synfunktion-hoger-utanKorrektion')),
                homk: element(by.id('synfunktion-hoger-medKorrektion')),
                vouk: element(by.id('synfunktion-vanster-utanKorrektion')),
                vomk: element(by.id('synfunktion-vanster-medKorrektion')),
                buk: element(by.id('synfunktion-binokulart-utanKorrektion')),
                bmk: element(by.id('synfunktion-binokulart-medKorrektion'))
            }
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
        promisesArr.push(this.allmant.diabetesyear.typeKeys(allmant.year));

        // Ange diabetestyp
        promisesArr.push(this.allmant.formDiabetesTyp.element(by.cssContainingText('label', allmant.typ)).click());

        // Ange behandlingstyp
        var typer = allmant.behandling.typer;
        var behandlingForm = this.allmant.behandling;
        typer.forEach(function(typ) {
            if (typ === 'Endast kost') {
                promisesArr.push(behandlingForm.kost.typeKeys(protractor.Key.SPACE));
            } else if (typ === 'Tabletter') {
                promisesArr.push(behandlingForm.tabletter.typeKeys(protractor.Key.SPACE));
                var suffix = allmant.behandling.riskForHypoglykemi.toLowerCase() === 'ja' ? 'Yes' : 'No';
                promisesArr.push(element(by.id('allmant-behandling-tablettRiskHypoglykemi' + suffix)).click());
            } else if (typ === 'Insulin') {
                promisesArr.push(behandlingForm.insulin.typeKeys(protractor.Key.SPACE));
                if (allmant.behandling.insulinYear) {
                    this.allmant.insulinbehandlingsperiod = element(by.id('allmant-behandling-insulinSedanAr'));
                    promisesArr.push(this.allmant.insulinbehandlingsperiod.typeKeys(allmant.behandling.insulinYear));
                }
            } else if (typ === 'Annan behandling') {
                promisesArr.push(behandlingForm.annan.typeKeys(protractor.Key.SPACE));

                this.allmant.annanbehandling = element(by.id('allmant-behandling-annanBehandlingBeskrivning'));
                promisesArr.push(this.allmant.annanbehandling.typeKeys(allmant.annanbehandling));
            }
        }.bind(this));

        return Promise.all(promisesArr);
    },

    fillInHypoglykemier: function(hypoglykemierObj) {
        var promisesArr = [];
        'abcdefghij'.split('').forEach(function(char) {
            if (typeof hypoglykemierObj[char] !== 'undefined') {
                var answer = hypoglykemierObj[char].toLowerCase() === 'ja' ? 'yes' : 'no';
                promisesArr.push(this.hypoglykemier[char][answer].click());
                if(answer === 'yes' && ['h', 'i', 'j'].indexOf(char) > -1) {
                    promisesArr.push(element(by.id(this.hypoglykemier[char].datePickerId)).typeKeys(hypoglykemierObj[char + 'Datum']));
                }
            }
        }.bind(this));
        return Promise.all(promisesArr);
    },

    fillInBedomning: function(bedomning) {
        return pageHelpers.selectAllCheckBoxes(this.bedomningKorkortsTyperChecks, bedomning.behorigheter).then(function() {
            var lampligPromise, borUndersokasPromise;

            borUndersokasPromise = element(by.id('bedomning-borUndersokasBeskrivning')).typeKeys(bedomning.borUndersokasBeskrivning);
            lampligPromise = element(by.id('form_bedomning-lampligtInnehav')).isPresent().then(function() {
                return element(by.id(bedomning.lamplig.toLowerCase() === 'ja' ? 'bedomning-lampligtInnehavYes' : 'bedomning-lampligtInnehavNo')).typeKeys(protractor.Key.SPACE);
            });

            return Promise.all([borUndersokasPromise, lampligPromise]);
        });
    },

    fillInSynfunktion: function(synValues) {
        var promiseList = [];

        // first check basic radio buttons
        ['a', 'b'].forEach(function(prop) {
            if(typeof synValues[prop] !== 'undefined') {
                var synElement = synValues[prop].toLowerCase() === 'ja' ? this.syn[prop].yes : this.syn[prop].no;
                promiseList.push(synElement.typeKeys(protractor.Key.SPACE));
            }
        }.bind(this));

        // fill in styrkor
        Object.keys(this.syn.styrkor).forEach(function(key) {
            if(typeof synValues.styrkor[key].toLowerCase() === 'string') {
                promiseList.push(this.syn.styrkor[key].typeKeys(synValues.styrkor[key]));
            }
        }.bind(this));

        return Promise.all(promiseList);
    }
});

module.exports = new TsDiabetes2Utkast();
