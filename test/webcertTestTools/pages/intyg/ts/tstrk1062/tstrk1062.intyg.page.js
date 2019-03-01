/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

'use strict';

var TsBaseIntyg = require('../ts.base.intyg.page');
var testValues = require('../../../../testdata/testvalues.ts');
var _ = require('lodash');

var Tstrk1062Intyg = TsBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-tstrk1062';
        this.intygTypeVersion = '1.0';

        // this.lakemedelsbehandling: {
        //     harHaft: undefined,
        //         pagar: undefined,
        //         aktuell: undefined,
        //         pagatt: undefined,
        //         effekt: undefined,
        //         foljsamhet: undefined,
        //         avslutadTidpunkt: undefined,
        //         avslutadOrsak: undefined
        // },

        this.lakemedelsbehandling = element(by.id('lakemedelsbehandling-korrektionsglasensStyrka'));

    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verifieraHorsel: function(horsel) {
        expect(this.horselBalansbalansrubbningar.getText()).toBe(horsel.yrsel);
        expect(this.horselSamtal.getText()).toBe(horsel.samtal ? horsel.samtal : 'Ej angivet');
    },
    verifieraRorelseorganensFunktioner: function(rorelseorganensFunktioner) {
        expect(this.funktionsnedsattning.getText()).toBe(rorelseorganensFunktioner.nedsattning);
        if (rorelseorganensFunktioner.nedsattning === 'Ja') {
            expect(this.funktionsnedsattningbeskrivning.getText()).toBe(rorelseorganensFunktioner.nedsattningBeskrivning);
        }
        expect(this.funktionsnedsRorelseformaga.getText()).toBe(rorelseorganensFunktioner.inUtUrFordon ? rorelseorganensFunktioner.inUtUrFordon : 'Ej angivet');
    },
    verifieraHjartOchKarlsjukdomar: function(data) {
        expect(this.hjartKarlSjukdom.getText()).toBe(data.hjartHjarna);
        expect(this.hjarnskadaEfterTrauma.getText()).toBe(data.hjartSkada);
        expect(this.riskfaktorerStroke.getText()).toBe(data.hjartRisk);

        if (data.hjartRisk === 'Ja') {
            expect(this.beskrivningRiskfaktorer.getText()).toBe(data.hjartRiskBeskrivning);
        }
    },
    verifieraDiabetes: function(diabetes) {
        expect(this.harDiabetes.getText()).toBe(diabetes.hasDiabetes);

        if (diabetes.hasDiabetes === 'Ja') {
            expect(this.diabetesTyp.getText()).toBe(diabetes.typ);

            if (diabetes.typ === 'Typ 2') {
                for (var i = 0; diabetes.behandlingsTyper.length > i; i++) {
                    var typ = diabetes.behandlingsTyper[i] === 'Endast kost' ? 'Kost' : diabetes.behandlingsTyper[i];
                    expect(this.getBehandlingsTyp(i).getText()).toBe(typ);
                }
            }
        }
    },
    verifieraEpilepsi: function(data) {
        expect(this.medvetandestorning.getText()).toBe(data.epilepsi);
        if (data.epilepsi === 'Ja') {
            expect(this.medvetandestorningbeskrivning.getText()).toBe(data.epilepsiBeskrivning);
        }
    },
    verifieraMissbruk: function(data) {
        expect(this.teckenMissbruk.getText()).toBe(data.alkoholMissbruk);
        expect(this.foremalForVardinsats.getText()).toBe(data.alkoholVard);
        if (data.alkoholMissbruk === 'Ja' || data.alkoholVard === 'Ja') {
            expect(this.provtagningBehovs.getText()).toBe(data.alkoholProvtagning);
        }
        expect(this.lakarordineratLakemedelsbruk.getText()).toBe(data.alkoholLakemedel);
        if (data.alkoholLakemedel === 'Ja') {
            expect(this.lakemedelOchDos.getText()).toBe(data.alkoholLakemedelBeskrivning);
        }
    },
    verifieraSjukvard: function(data) {
        expect(this.sjukhusEllerLakarkontakt.getText()).toBe(data.sjukhusvard);

        if (data.sjukhusvard === 'Ja') {
            expect(this.tidpunkt.getText()).toBe(data.sjukhusvardTidPunkt);
            expect(this.vardinrattning.getText()).toBe(data.sjukhusvardInrattning);
            expect(this.sjukhusvardanledning.getText()).toBe(data.sjukhusvardAnledning);
        }
    },
    verifieraOvrigMedicin: function(data) {
        expect(this.stadigvarandeMedicinering.getText()).toBe(data.ovrigMedicin);
        if (data.ovrigMedicin === 'Ja') {
            expect(this.medicineringbeskrivning.getText()).toBe(data.ovrigMedicinBeskrivning);
        }
    },

    verifieraSynfunktioner: function(data) {

        expect(this.synfaltsdefekter.getText()).toBe(data.synDonder);
        expect(this.nattblindhet.getText()).toBe(data.synNedsattBelysning);
        expect(this.progressivOgonsjukdom.getText()).toBe(data.synOgonsjukdom);
        expect(this.diplopi.getText()).toBe(data.synDubbel);
        expect(this.nystagmus.getText()).toBe(data.synNystagmus);

        expect(this.hogerOgautanKorrektion.getText()).toBe(this.dotToComma(data.styrkor.houk));
        expect(this.hogerOgamedKorrektion.getText()).toBe(this.dotToComma(data.styrkor.homk));
        expect(this.hogerOgakontaktlins.getText()).toBe(data.linser.hoger);

        expect(this.vansterOgautanKorrektion.getText()).toBe(this.dotToComma(data.styrkor.vouk));
        expect(this.vansterOgamedKorrektion.getText()).toBe(this.dotToComma(data.styrkor.vomk));
        expect(this.vansterOgakontaktlins.getText()).toBe(data.linser.vanster);

        expect(this.binokulartutanKorrektion.getText()).toBe(this.dotToComma(data.styrkor.buk));
        expect(this.binokulartmedKorrektion.getText()).toBe(this.dotToComma(data.styrkor.bmk));
    },
    verifieraBedomning: function(bedomning) {

        if (bedomning.stallningstagande === 'Kan inte ta ställning') {
            expect(this.falt1.bedomning.getText()).toBe(bedomning.stallningstagande);
        } else {
            var sorted = _.sortBy(bedomning.behorigheter, function(x) {
                return _.indexOf(testValues.korkortstyperHogreBehorighet, x);
            });

            var text = _.join(sorted, ', ');

            expect(this.falt1.bedomning.getText()).toBe(text);
        }
    },
    dotToComma: function(value) {
        return value.replace('.', ',');
    },
    verify: function(data) {

        this.verifieraIntygetAvser(data.korkortstyper, testValues.korkortstyperHogreBehorighet);
        this.verifieraIdKontroll(data.identitetStyrktGenom);
        this.verifieraSynfunktioner(data);
        this.verifieraHorsel(data.horsel);
        this.verifieraRorelseorganensFunktioner(data.rorelseorganensFunktioner);
        this.verifieraHjartOchKarlsjukdomar(data);
        this.verifieraDiabetes(data.diabetes);
        expect(this.neurologiskSjukdom.getText()).toBe(data.neurologiska);
        this.verifieraEpilepsi(data);
        expect(this.nedsattNjurfunktion.getText()).toBe(data.njursjukdom);
        expect(this.sviktandeKognitivFunktion.getText()).toBe(data.demens);
        expect(this.teckenSomnstorningar.getText()).toBe(data.somnVakenhet);
        this.verifieraMissbruk(data);
        expect(this.psykiskSjukdom.getText()).toBe(data.psykiskSjukdom);
        expect(this.psykiskUtvecklingsstorning.getText()).toBe(data.adhdPsykisk);
        expect(this.harSyndrom.getText()).toBe(data.adhdSyndrom);
        this.verifieraSjukvard(data);
        this.verifieraOvrigMedicin(data);
        expect(this.comment.getText()).toBe(data.kommentar);
        this.verifieraBedomning(data.bedomning, testValues.korkortstyperHogreBehorighet);
    }
});

module.exports = new Tstrk1062Intyg();
