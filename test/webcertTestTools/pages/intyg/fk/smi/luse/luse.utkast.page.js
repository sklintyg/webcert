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

/*globals element,by, Promise,browser,protractor*/
'use strict';

var BaseSmiUtkast = require('../smi.base.utkast.page.js');

function checkAndSendTextToForm(checkboxEL, textEL, text) {
    return checkboxEL.sendKeys(protractor.Key.SPACE).then(function() {
        return browser.sleep(1000).then(function() {
            return textEL.sendKeys(text)
                .then(function() {
                    console.log('OK - Angav: ' + text);
                }, function(reason) {
                    throw ('FEL - Angav: ' + text + ' ' + reason);
                });
        });
    });
}

var LuseUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.underlagFinnsNo = element(by.id('underlagFinnsNo'));
        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnosgrund = element(by.id('diagnosgrund'));
        this.nyBedomningDiagnosgrundNo = element(by.id('nyBedomningDiagnosgrundNo'));

        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

        this.medicinskaForutsattningar = {
            utecklasOverTid: element(by.id('medicinskaForutsattningarForArbete')),
            trotsBegransningar: element(by.id('formagaTrotsBegransning'))
        };
        this.kontaktMedFkNo = element(by.id('formly_1_checkbox-inline_kontaktMedFk_0'));
        // this.kontaktMedFkNo = element(by.id('kontaktMedFkNo'));
        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));


        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('formly_1_date_undersokningAvPatienten_1')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('formly_1_date_journaluppgifter_2')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('form_anhorigsBeskrivningAvPatienten')),
                datum: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('annatGrundForMUBeskrivning')),
                checkbox: element(by.id('form_annatGrundForMU')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first()
            },
            kannedomOmPatient: {
                datum: element(by.id('form_kannedomOmPatient')).element(by.css('input'))
            }
        };
    },
    angeAktivitetsbegransning: function(aktivitetsbegransning) {
        return this.aktivitetsbegransning.sendKeys(aktivitetsbegransning);
    },

    angeMedicinskaForutsattningar: function(forutsattningar) {
        return Promise.all([
            this.medicinskaForutsattningar.utecklasOverTid.sendKeys(forutsattningar.utecklasOverTid),
            this.medicinskaForutsattningar.trotsBegransningar.sendKeys(forutsattningar.trotsBegransningar)
        ]);
    },
    angeFunktionsnedsattning: function(nedsattning) {
        var fn = this.funktionsnedsattning;
        return Promise.all([
            checkAndSendTextToForm(fn.intellektuell.checkbox, fn.intellektuell.text, nedsattning.intellektuell),
            checkAndSendTextToForm(fn.kommunikation.checkbox, fn.kommunikation.text, nedsattning.kommunikation),
            checkAndSendTextToForm(fn.koncentration.checkbox, fn.koncentration.text, nedsattning.koncentration),
            checkAndSendTextToForm(fn.annanPsykisk.checkbox, fn.annanPsykisk.text, nedsattning.psykisk),
            checkAndSendTextToForm(fn.synHorselTal.checkbox, fn.synHorselTal.text, nedsattning.synHorselTal),
            checkAndSendTextToForm(fn.balansKoordination.checkbox, fn.balansKoordination.text, nedsattning.balansKoordination),
            checkAndSendTextToForm(fn.annanKroppslig.checkbox, fn.annanKroppslig.text, nedsattning.annan)
        ]);
    },
    angeMedicinskBehandling: function(behandling) {
        var mb = this.medicinskBehandling;
        return Promise.all([
            mb.avslutad.text.sendKeys(behandling.avslutad),
            mb.pagaende.text.sendKeys(behandling.pagaende),
            mb.planerad.text.sendKeys(behandling.planerad),
            mb.substansintag.text.sendKeys(behandling.substansintag)
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
