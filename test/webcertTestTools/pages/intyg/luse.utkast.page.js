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

/*globals element,by, Promise*/
'use strict';

var BaseSmiUtkast = require('./smi.base.utkast.page.js');

var LuseUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.underlagFinnsNo = element(by.id('underlagFinnsNo'));
        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnosgrund = element(by.id('diagnosgrund'));
        this.nyBedomningDiagnosgrundNo = element(by.id('nyBedomningDiagnosgrundNo'));
        this.funktionsnedsattning = {
            intellektuell: element(by.id('funktionsnedsattningIntellektuell')),
            kommunikation: element(by.id('funktionsnedsattningKommunikation')),
            koncentration: element(by.id('funktionsnedsattningKoncentration')),
            annanPsykisk: element(by.id('funktionsnedsattningPsykisk')),
            synHorselTal: element(by.id('funktionsnedsattningSynHorselTal')),
            balansKoordination: element(by.id('funktionsnedsattningBalansKoordination')),
            annanKroppslig: element(by.id('funktionsnedsattningAnnan'))
        };
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

        this.medicinskBehandling = {
            avslutad: element(by.id('avslutadBehandling')),
            pagaende: element(by.id('pagaendeBehandling')),
            planerad: element(by.id('planeradBehandling')),
            substansintag: element(by.id('substansintag'))
        };
        this.medicinskaForutsattningar = {
            utecklasOverTid: element(by.id('medicinskaForutsattningarForArbete')),
            trotsBegransningar: element(by.id('aktivitetsFormaga'))
        };
        this.kontaktMedFkNo = element(by.id('formly_1_checkbox-inline_kontaktMedFk_0'));
        // this.kontaktMedFkNo = element(by.id('kontaktMedFkNo'));
        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('formly_1_date_undersokningAvPatienten_3')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('formly_1_date_journaluppgifter_4')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('form_anhorigsBeskrivningAvPatienten')),
                datum: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('formly_1_single-text_annatGrundForMUBeskrivning_7')),
                checkbox: element(by.id('formly_1_date_annatGrundForMU_6')),
                datum: element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first()
            },
            kannedomOmPatient: {
                datum: element(by.id('form_kannedomOmPatient')).element(by.css('input[type=text]')),
                checkbox: element(by.id('formly_1_date_kannedomOmPatient_8'))
            }
        };
    },
    angeFunktionsnedsattning: function(nedsattning) {
        return Promise.all([
            this.funktionsnedsattning.intellektuell.sendKeys(nedsattning.intellektuell),
            this.funktionsnedsattning.kommunikation.sendKeys(nedsattning.kommunikation),
            this.funktionsnedsattning.koncentration.sendKeys(nedsattning.koncentration),
            this.funktionsnedsattning.annanPsykisk.sendKeys(nedsattning.psykisk),
            this.funktionsnedsattning.synHorselTal.sendKeys(nedsattning.synHorselTal),
            this.funktionsnedsattning.balansKoordination.sendKeys(nedsattning.balansKoordination),
            this.funktionsnedsattning.annanKroppslig.sendKeys(nedsattning.annan)
        ]);
    },
    angeAktivitetsbegransning: function(aktivitetsbegransning) {
        return this.aktivitetsbegransning.sendKeys(aktivitetsbegransning);
    },
    angeMedicinskBehandling: function(behandling) {
        return Promise.all([
            this.medicinskBehandling.avslutad.sendKeys(behandling.avslutad),
            this.medicinskBehandling.pagaende.sendKeys(behandling.pagaende),
            this.medicinskBehandling.planerad.sendKeys(behandling.planerad),
            this.medicinskBehandling.substansintag.sendKeys(behandling.substansintag)
        ]);
    },

    angeMedicinskaForutsattningar: function(forutsattningar) {
        return Promise.all([
            this.medicinskaForutsattningar.utecklasOverTid.sendKeys(forutsattningar.utecklasOverTid),
            this.medicinskaForutsattningar.trotsBegransningar.sendKeys(forutsattningar.trotsBegransningar)
        ]);
    },

    get: function get(intygId) {
        get._super.call(this, 'luse', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    }
});

module.exports = new LuseUtkast();
