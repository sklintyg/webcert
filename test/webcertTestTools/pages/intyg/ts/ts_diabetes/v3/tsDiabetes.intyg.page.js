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
'use strict';

var TsBaseIntyg = require('../../ts.base.intyg.page');
var testValues = require('../../../../../testdata/testvalues.ts.js');
var _ = require('lodash');

var ejAngivet = 'Ej angivet';

var TsDiabetesIntyg = TsBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-diabetes';
        this.intygTypeVersion = '3.0';

        this.period = element(by.id('allmant-diabetesDiagnosAr'));
        this.insulPeriod = element(by.id('allmant-behandling-insulinSedanAr'));
        this.dTyp = element(by.id('allmant-typAvDiabetes'));

        this.hypoglykemier = {
            a: element(by.id('hypoglykemier-sjukdomenUnderKontroll')),
            b: element(by.id('hypoglykemier-nedsattHjarnfunktion')),
            c: element(by.id('hypoglykemier-forstarRisker')),
            d: element(by.id('hypoglykemier-fortrogenMedSymptom')),
            e: element(by.id('hypoglykemier-saknarFormagaVarningstecken')),
            f: element(by.id('hypoglykemier-kunskapLampligaAtgarder')),
            g: element(by.id('hypoglykemier-egenkontrollBlodsocker')),
            h: element(by.id('hypoglykemier-aterkommandeSenasteAret')),
            i: element(by.id('hypoglykemier-aterkommandeSenasteKvartalet')),
            j: element(by.id('hypoglykemier-forekomstTrafik')),
            hDate: element(by.id('hypoglykemier-aterkommandeSenasteTidpunkt')),
            iDate: element(by.id('hypoglykemier-senasteTidpunktVaken')),
            jDate: element(by.id('hypoglykemier-forekomstTrafikTidpunkt'))
        };

        this.synIntygA = element(by.id('synfunktion-misstankeOgonsjukdom'));
        this.synIntygB = element(by.id('synfunktion-ogonbottenFotoSaknas'));
        this.synStyrkor = {
            houk: element(by.id('synfunktion-row0-col1')),
            homk: element(by.id('synfunktion-row0-col2')),
            vouk: element(by.id('synfunktion-row1-col1')),
            vomk: element(by.id('synfunktion-row1-col2')),
            buk: element(by.id('synfunktion-row2-col1')),
            bmk: element(by.id('synfunktion-row2-col2'))
        };

        this.falt1 = {
            bedomning: element(by.id('bedomning-uppfyllerBehorighetskrav')),
            annanBehandling: element(by.id('allmant-behandling-annanBehandling'))
        };

        this.borUndersokas = element(by.id('bedomning-borUndersokasBeskrivning'));
        this.arLamplig = element(by.id('bedomning-lampligtInnehav'));

        this.getBehandlingsTyp = function(index) {
            return element(by.id('allmant-behandling-endastKost-allmant-behandling-tabletter-allmant-behandling-insulin-allmant-behandling-annanBehandling-' + index));
        };

        this.idkontroll = element(by.id('identitetStyrktGenom-typ'));
        this.korkortstyp = element(by.id('intygAvser-kategorier'));
        this.kommentar = element(by.id('ovrigt'));

    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verifieraAllmant: function(allmant) {
        expect(this.period.getText()).toBe(allmant.year + '');
        expect(this.dTyp.getText()).toBe(allmant.typ);

        var sorted = _.sortBy(allmant.behandling.typer, function(x) {
            return _.indexOf(testValues.diabetesbehandlingtyper, x);
        });

        sorted.forEach(function(typ, index) {
            expect(this.getBehandlingsTyp(index).getText()).toBe(typ);
        }.bind(this));

        if (allmant.behandling.typer.indexOf('Insulin') > -1) {
            expect(this.insulPeriod.getText()).toBe(allmant.behandling.insulinYear + '');
        }

        expect(this.falt1.annanBehandling.getText()).toBe(allmant.annanbehandling);

    },
    verifieraHypoglykemier: function(hypoglykemier, behandling) {
        var hypoglykemierMandatory = (behandling.typer.indexOf('Insulin') > -1 ||
            (behandling.typer.indexOf('Tabletter') > -1 && behandling.riskForHypoglykemi === 'Ja'));

        'abcdefghij'.split('').forEach(function(char) {
            var expectedYesNo = hypoglykemierMandatory ? hypoglykemier[char] : ejAngivet;
            expect(this.hypoglykemier[char].getText()).toBe(expectedYesNo);
            if (['h', 'i', 'j'].indexOf(char) > -1) {
                var expectedDateValue = hypoglykemier[char] === 'Ja' ? hypoglykemier[char + 'Datum'] : ejAngivet;
                expect(this.hypoglykemier[char + 'Date'].getText()).toBe(expectedDateValue);
            }

        }.bind(this));
    },
    verifieraSynfunktion: function(synfunktion) {
        expect(this.synIntygA.getText()).toBe(synfunktion.a);
        expect(this.synIntygB.getText()).toBe(synfunktion.b);

        Object.keys(synfunktion.styrkor).forEach(function(key) {
            expect(this.synStyrkor[key].getText()).toBe(this.dotToComma(synfunktion.styrkor[key]));
        }.bind(this));
    },
    verifieraBedomning: function(bedomning) {

        var dataCopy = bedomning.behorigheter.map(function(item) {
            return item;
        });

        this.falt1.bedomning.getText().then(function(text) {
            expect(text.split(', ').sort().join(', ')).toBe(dataCopy.sort().join(', '));
        });

        expect(this.borUndersokas.getText()).toBe(bedomning.borUndersokasBeskrivning);
        expect(this.arLamplig.getText()).toBe(bedomning.lamplig);
    },
    dotToComma: function(value) {
        return value.replace('.', ',');
    },
    verify: function(data) {
        this.verifieraIntygetAvser(data.korkortstyper, testValues.korkortstyper);
        this.verifieraIdKontroll(data.identitetStyrktGenom);
        this.verifieraAllmant(data.allmant);
        this.verifieraHypoglykemier(data.hypoglykemier, data.allmant.behandling);
        this.verifieraSynfunktion(data.synfunktion);
        this.verifieraBedomning(data.bedomning);
        expect(this.kommentar.getText()).toBe(data.kommentar);
    }
});

module.exports = new TsDiabetesIntyg();
