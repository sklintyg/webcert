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

/**
 * Created by bennysce on 09/06/15.
 */
'use strict';

var TsBaseIntyg = require('../ts.base.intyg.page');
var testValues = require('../../../../testdata/testvalues.ts');
var _ = require('lodash');

var TsBasIntyg = TsBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-bas';

        this.hogerOgautanKorrektion = element(by.id('syn-row0-col1'));
        this.hogerOgamedKorrektion = element(by.id('syn-row0-col2'));
        this.hogerOgakontaktlins = element(by.id('syn-row0-col3'));

        this.vansterOgautanKorrektion = element(by.id('syn-row1-col1'));
        this.vansterOgamedKorrektion = element(by.id('syn-row1-col2'));
        this.vansterOgakontaktlins = element(by.id('syn-row1-col3'));
        this.binokulartutanKorrektion = element(by.id('syn-row2-col1'));
        this.binokulartmedKorrektion = element(by.id('syn-row2-col2'));

        this.korrektionsglasensStyrka = element(by.id('syn-korrektionsglasensStyrka'));

        this.horselBalansbalansrubbningar = element(by.id('horselBalans-balansrubbningar'));
        this.horselSamtal = element(by.id('horselBalans-svartUppfattaSamtal4Meter'));
        this.funktionsnedsattning = element(by.id('funktionsnedsattning-funktionsnedsattning'));
        this.funktionsnedsattningbeskrivning = element(by.id('funktionsnedsattning-beskrivning'));
        this.funktionsnedsRorelseformaga = element(by.id('funktionsnedsattning-otillrackligRorelseformaga'));

        this.hjartKarlSjukdom = element(by.id('hjartKarl-hjartKarlSjukdom'));
        this.hjarnskadaEfterTrauma = element(by.id('hjartKarl-hjarnskadaEfterTrauma'));
        this.riskfaktorerStroke = element(by.id('hjartKarl-riskfaktorerStroke'));
        this.beskrivningRiskfaktorer = element(by.id('hjartKarl-beskrivningRiskfaktorer'));

        this.synfaltsdefekter = element(by.id('syn-synfaltsdefekter'));
        this.nattblindhet = element(by.id('syn-nattblindhet'));
        this.diplopi = element(by.id('syn-diplopi'));
        this.nystagmus = element(by.id('syn-nystagmus'));
        this.progressivOgonsjukdom = element(by.id('syn-progressivOgonsjukdom'));

        this.harDiabetes = element(by.id('diabetes-harDiabetes'));
        this.kost = element(by.id('kost'));
        this.tabletter = element(by.id('tabletter'));
        this.insulin = element(by.id('insulin'));
        this.diabetesTyp = element(by.id('diabetes-diabetesTyp'));
        this.getBehandlingsTyp = function(index) {
            return element(by.id('diabetes-kost-diabetes-tabletter-diabetes-insulin-' + index));
        };

        this.intygStatus = element(by.id('intyget-sparat-och-ej-komplett-meddelande'));

        this.neurologiskSjukdom = element(by.id('neurologi-neurologiskSjukdom'));

        this.medvetandestorning = element(by.id('medvetandestorning-medvetandestorning'));
        this.medvetandestorningbeskrivning = element(by.id('medvetandestorning-beskrivning'));
        this.nedsattNjurfunktion = element(by.id('njurar-nedsattNjurfunktion'));

        this.sviktandeKognitivFunktion = element(by.id('kognitivt-sviktandeKognitivFunktion'));
        this.teckenSomnstorningar = element(by.id('somnVakenhet-teckenSomnstorningar'));

        this.teckenMissbruk = element(by.id('narkotikaLakemedel-teckenMissbruk'));
        this.foremalForVardinsats = element(by.id('narkotikaLakemedel-foremalForVardinsats'));
        this.provtagningBehovs = element(by.id('narkotikaLakemedel-provtagningBehovs'));
        this.lakarordineratLakemedelsbruk = element(by.id('narkotikaLakemedel-lakarordineratLakemedelsbruk'));
        this.lakemedelOchDos = element(by.id('narkotikaLakemedel-lakemedelOchDos'));

        this.psykiskSjukdom = element(by.id('psykiskt-psykiskSjukdom'));

        this.psykiskUtvecklingsstorning = element(by.id('utvecklingsstorning-psykiskUtvecklingsstorning'));
        this.harSyndrom = element(by.id('utvecklingsstorning-harSyndrom'));

        this.sjukhusEllerLakarkontakt = element(by.id('sjukhusvard-sjukhusEllerLakarkontakt'));
        this.tidpunkt = element(by.id('sjukhusvard-tidpunkt'));
        this.vardinrattning = element(by.id('sjukhusvard-vardinrattning'));
        this.sjukhusvardanledning = element(by.id('sjukhusvard-anledning'));

        this.stadigvarandeMedicinering = element(by.id('medicinering-stadigvarandeMedicinering'));
        this.medicineringbeskrivning = element(by.id('medicinering-beskrivning'));
        this.intygetAvser = element(by.id('intygAvser-korkortstyp'));

        this.printBtn = element(by.id('downloadprint'));
        this.comment = element(by.id('kommentar'));
    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verifieraHorsel: function(horsel) {
        expect(this.horselBalansbalansrubbningar.getText()).toBe(horsel.yrsel);
        expect(this.horselSamtal.getText()).toBe(horsel.samtal ? horsel.samtal : 'Ej Angivet');
    },
    verifieraRorelseorganensFunktioner: function(rorelseorganensFunktioner) {
        expect(this.funktionsnedsattning.getText()).toBe(rorelseorganensFunktioner.nedsattning);
        if (rorelseorganensFunktioner.nedsattning === 'Ja') {
            expect(this.funktionsnedsattningbeskrivning.getText()).toBe(rorelseorganensFunktioner.nedsattningBeskrivning);
        }
        expect(this.funktionsnedsRorelseformaga.getText()).toBe(rorelseorganensFunktioner.inUtUrFordon ? rorelseorganensFunktioner.inUtUrFordon : 'Ej Angivet');
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
                    var typ = diabetes.behandlingsTyper[i]  === 'Endast kost' ? 'Kost' : diabetes.behandlingsTyper[i];
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

module.exports = new TsBasIntyg();
