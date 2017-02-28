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



'use strict';

var BaseSmiIntygPage = require('../smi.base.intyg.page.js');
var testdataHelper = require('common-testtools').testdataHelper;
var LuaeNaIntyg = BaseSmiIntygPage._extend({
    init: function init() {
        init._super.call(this);

        this.medicinskaForutsattningar = {
            utecklasOverTid: element(by.id('medicinskaForutsattningarForArbete')),
            trotsBegransningar: element(by.id('formagaTrotsBegransning')),
            forslagTillAtgard: element(by.id('forslagTillAtgard'))
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        expect(this.baseratPa.minUndersokningAvPatienten.getText()).toBe(testdataHelper.dateToText(data.baseratPa.minUndersokningAvPatienten));
        expect(this.baseratPa.journaluppgifter.getText()).toBe(testdataHelper.dateToText(data.baseratPa.journaluppgifter));
        expect(this.baseratPa.anhorigsBeskrivning.getText()).toBe(testdataHelper.dateToText(data.baseratPa.anhorigsBeskrivning));
        expect(this.baseratPa.annat.getText()).toBe(testdataHelper.dateToText(data.baseratPa.annat));
        expect(this.baseratPa.annatBeskrivning.getText()).toBe(data.baseratPa.annatBeskrivning);
        expect(this.baseratPa.personligKannedom.getText()).toBe(testdataHelper.dateToText(data.baseratPa.personligKannedom));

        if (data.andraMedicinskaUtredningar) {
            for (var i = 0; i < data.andraMedicinskaUtredningar.length; i++) {
                var utredningEL = this.andraMedicinskaUtredningar.getUtredning(i);
                var utredningDatum = testdataHelper.dateToText(data.andraMedicinskaUtredningar[i].datum);
                expect(utredningEL.typ.getText()).toBe(data.andraMedicinskaUtredningar[i].underlag);
                expect(utredningEL.datum.getText()).toBe(utredningDatum);
                expect(utredningEL.info.getText()).toBe(data.andraMedicinskaUtredningar[i].infoOmUtredningen);
            }
        }

        expect(this.diagnoser.grund.getText()).toBe(data.diagnos.narOchVarStalldesDiagnoserna);
        if (data.diagnos.nyBedomning) {
            expect(this.diagnoser.nyBedomningDiagnosgrundJa.isDisplayed()).toBeTruthy();
            expect(this.diagnoser.diagnosForNyBedomning.getText()).toBe(data.diagnos.diagnosForNyBedomning);
        } else {
            expect(this.diagnoser.nyBedomningDiagnosgrundNej.isDisplayed()).toBeTruthy();
            expect(this.diagnoser.diagnosForNyBedomning.getText()).toBe('Ej angivet');
        }
        if (data.diagnos.diagnoser) {
            for (var j = 0; j < data.diagnos.diagnoser.length; j++) {
                expect(this.diagnoser.getDiagnos(j).kod.getText()).toBe(data.diagnos.diagnoser[j].kod);
            }
        }

        expect(this.sjukdomsforlopp.getText()).toBe(data.sjukdomsForlopp);

        expect(this.funktionsnedsattning.intellektuell.getText()).toBe(data.funktionsnedsattning.intellektuell);
        expect(this.funktionsnedsattning.kommunikation.getText()).toBe(data.funktionsnedsattning.kommunikation);
        expect(this.funktionsnedsattning.uppmarksamhet.getText()).toBe(data.funktionsnedsattning.koncentration);
        expect(this.funktionsnedsattning.annanPsykiskFunktion.getText()).toBe(data.funktionsnedsattning.psykisk);
        expect(this.funktionsnedsattning.synHorselTal.getText()).toBe(data.funktionsnedsattning.synHorselTal);
        expect(this.funktionsnedsattning.balans.getText()).toBe(data.funktionsnedsattning.balansKoordination);
        expect(this.funktionsnedsattning.annanKropsligFunktion.getText()).toBe(data.funktionsnedsattning.annan);

        expect(this.aktivitetsbegransning.getText()).toBe(data.aktivitetsbegransning);

        expect(this.behandling.avslutad.getText()).toBe(data.medicinskbehandling.avslutad);
        expect(this.behandling.pagaende.getText()).toBe(data.medicinskbehandling.pagaende);
        expect(this.behandling.planerad.getText()).toBe(data.medicinskbehandling.planerad);
        expect(this.behandling.substansintag.getText()).toBe(data.medicinskbehandling.substansintag);

        expect(this.medicinskaForutsattningar.utecklasOverTid.getText()).toBe(data.medicinskaForutsattningar.utecklasOverTid);
        expect(this.medicinskaForutsattningar.trotsBegransningar.getText()).toBe(data.medicinskaForutsattningar.trotsBegransningar);
        expect(this.medicinskaForutsattningar.forslagTillAtgard.getText()).toBe(data.medicinskaForutsattningar.forslagTillAtgard);

        expect(this.ovrigt.getText()).toBe(data.ovrigt);

        if (data.kontaktMedFk) {
            expect(this.kontaktFK.ja.isDisplayed()).toBeTruthy();
        } else {
            expect(this.kontaktFK.nej.isDisplayed()).toBeTruthy();
        }
    }
});
module.exports = new LuaeNaIntyg();
