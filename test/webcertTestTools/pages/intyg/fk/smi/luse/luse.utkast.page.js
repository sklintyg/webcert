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

/*globals element, by, Promise*/
'use strict';
var BaseSmiUtkast = require('../smi.base.utkast.page.js');
var pageHelpers = require('../../../../pageHelper.util.js');


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

        this.underlag = {
            ett: {
                datum: element(by.id('underlag-0-datum'))
            },
            tva: {
                datum: element(by.id('underlag-1-datum'))
            },
            tre: {
                datum: element(by.id('underlag-2-datum'))
            }

        };


        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('form_journaluppgifter')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=checkbox]')),
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
            pageHelpers.moveAndSendKeys(this.medicinskaForutsattningar.utecklasOverTid, forutsattningar.utecklasOverTid),
            pageHelpers.moveAndSendKeys(this.medicinskaForutsattningar.trotsBegransningar, forutsattningar.trotsBegransningar)
        ]);
    },
    angeMedicinskBehandling: function(behandling) {
        var mb = this.medicinskBehandling;
        return Promise.all([
            pageHelpers.moveAndSendKeys(mb.avslutad.text, behandling.avslutad),
            pageHelpers.moveAndSendKeys(mb.pagaende.text, behandling.pagaende),
            pageHelpers.moveAndSendKeys(mb.planerad.text, behandling.planerad),
            pageHelpers.moveAndSendKeys(mb.substansintag.text, behandling.substansintag)
        ]);
    },

    get: function get(intygId) {
        get._super.call(this, 'luse', intygId);
    }
});

module.exports = new LuseUtkast();
