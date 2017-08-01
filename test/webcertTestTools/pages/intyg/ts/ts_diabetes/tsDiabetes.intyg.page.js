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

var TsDiabetesIntyg = TsBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-diabetes';

        this.period = element(by.id('diabetes-observationsperiod'));
        this.insulPeriod = element(by.id('diabetes-insulinBehandlingsperiod'));
        this.dTyp = element(by.id('diabetes-diabetestyp'));

        this.kunskapOmAtgarder = element(by.id('hypoglykemier-kunskapOmAtgarder'));
        this.teckenNedsattHjarnfunktion = element(by.id('hypoglykemier-teckenNedsattHjarnfunktion'));
        this.saknarFormagaKannaVarningstecken = element(by.id('hypoglykemier-saknarFormagaKannaVarningstecken'));
        this.allvarligForekomst = element(by.id('hypoglykemier-allvarligForekomst'));
        this.allvarligForekomstBeskrivning = element(by.id('hypoglykemier-allvarligForekomstBeskrivning'));
        this.allvarligForekomstTrafiken = element(by.id('hypoglykemier-allvarligForekomstTrafiken'));
        this.allvarligForekomstTrafikenBeskrivning = element(by.id('hypoglykemier-allvarligForekomstTrafikBeskrivning'));
        this.egenkontrollBlodsocker = element(by.id('hypoglykemier-egenkontrollBlodsocker'));
        this.allvarligForekomstVakenTid = element(by.id('hypoglykemier-allvarligForekomstVakenTid'));
        this.vakenTidObservationsTid = element(by.id('hypoglykemier-allvarligForekomstVakenTidObservationstid'));

        this.synIntyg = element(by.id('syn-separatOgonlakarintyg'));

        this.falt1 = {
            bedomning: element(by.id('bedomning-korkortstyp')),
            annanBehandling: element(by.id('diabetes-annanBehandlingBeskrivning'))
        };

        this.getBehandlingsTyp = function(index) {
            return element(by.id('diabetes-endastKost-diabetes-tabletter-diabetes-insulin-' + index));
        };

        this.specKomp = element(by.id('bedomning-lakareSpecialKompetens'));

        this.intygetAvser = element(by.id('intygAvser'));
    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verifieraAllmant: function(allmant) {
        var that = this;
        expect(this.period.getText()).toBe(allmant.year + '');
        expect(this.dTyp.getText()).toBe(allmant.typ);

        var sorted = _.sortBy(allmant.behandling.typer, function(x) {
            return _.indexOf(testValues.diabetesbehandlingtyper, x);
        });

        sorted.forEach(function(typ, index) {
            expect(that.getBehandlingsTyp(index).getText()).toBe(typ);
        });

        if (allmant.behandling.typer.indexOf('Insulin') > -1) {
            expect(this.insulPeriod.getText()).toBe(allmant.behandling.insulinYear + '');
        }

        expect(this.falt1.annanBehandling.getText()).toBe(allmant.annanbehandling);


    },
    verifieraHypoglykemier: function(hypoglykemier, korkortstyper) {
        expect(this.kunskapOmAtgarder.getText()).toBe(hypoglykemier.a);
        expect(this.teckenNedsattHjarnfunktion.getText()).toBe(hypoglykemier.b);

        if (hypoglykemier.b === 'Ja') {
            expect(this.saknarFormagaKannaVarningstecken.getText()).toBe(hypoglykemier.c);

            expect(this.allvarligForekomst.getText()).toBe(hypoglykemier.d);
            if (hypoglykemier.d === 'Ja') {
                expect(this.allvarligForekomstBeskrivning.getText()).toBe(hypoglykemier.dAntalEpisoder);
            }

            expect(this.allvarligForekomstTrafiken.getText()).toBe(hypoglykemier.e);
            if (hypoglykemier.e === 'Ja') {
                expect(this.allvarligForekomstTrafikenBeskrivning.getText()).toBe(hypoglykemier.eAntalEpisoder);
            }
        }

        if (hypoglykemier.g === 'Ja') {
            expect(this.vakenTidObservationsTid.getText()).toBe(hypoglykemier.gDatum);
        }

        if (testValues.hasHogreKorkortsbehorigheter(korkortstyper)) {
            expect(this.egenkontrollBlodsocker.getText()).toBe(hypoglykemier.f);
            expect(this.allvarligForekomstVakenTid.getText()).toBe(hypoglykemier.g);
        }

    },
    verifieraSynintyg : function(synintyg) {
        expect(this.synIntyg.getText()).toBe(synintyg.a);
    },
    verify: function(data) {
        this.verifieraIntygetAvser(data.korkortstyper, testValues.korkortstyper);
        this.verifieraIdKontroll(data.identitetStyrktGenom);
        this.verifieraAllmant(data.allmant);
        this.verifieraHypoglykemier(data.hypoglykemier, data.korkortstyper);
        this.verifieraSynintyg(data.synintyg);
        this.verifieraBedomning(data.bedomning, testValues.korkortstyper);
        expect(this.specKomp.getText()).toBe(data.specialist);
        expect(this.kommentar.getText()).toBe(data.kommentar);
    }
});

module.exports = new TsDiabetesIntyg();
