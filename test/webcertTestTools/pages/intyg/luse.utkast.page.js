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

/*globals element,by*/
'use strict';

var BaseSmiUtkast = require('./smi.base.utkast.page.js');

var LuseUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.baseratPa = {
            minUndersokningAvPatienten:{
                checkbox:element(by.id('formly_1_date_undersokningAvPatienten_3'))},
                datum:element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox:element(by.id('formly_1_date_journaluppgifter_4')),
                datum:element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox:element(by.id('form_anhorigsBeskrivningAvPatienten')),
                datum:element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            annat:{
                text:element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).last();
                checkbox:element(by.id('formly_1_date_annatGrundForMU_6')),
                datum:element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first();
            }
            kannedomOmPatient: {
                datum:element(by.id('form_kannedomOmPatient')).element(by.css('input[type=text]'))
                checkbox: element(by.id('formly_1_date_kannedomOmPatient_8'))
            }
        };

        this.underlagFinnsNo = element(by.id('underlagFinnsNo'));
        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnosgrund = element(by.id('diagnosgrund'));
        this.nyBedomningDiagnosgrundNo = element(by.id('nyBedomningDiagnosgrundNo'));
        this.funktionsnedsattning = {
            intellektuell: element(by.id('funktionsnedsattningIntellektuell')),
            kommunikation: element(by.id('funktionsnedsattningKommunikation')),
            koncentration: element(by.id('funktionsnedsattningKoncentration')),
            psykisk: element(by.id('funktionsnedsattningPsykisk')),
            synHorselTal: element(by.id('funktionsnedsattningSynHorselTal')),
            balansKoordination: element(by.id('funktionsnedsattningBalansKoordination')),
            annan: element(by.id('funktionsnedsattningAnnan'))
        };
        this.avslutadBehandling = element(by.id('avslutadBehandling'));
        this.pagaendeBehandling = element(by.id('pagaendeBehandling'));
        this.planeradBehandling = element(by.id('planeradBehandling'));
        this.substansintag = element(by.id('substansintag'));
        this.medicinskaForutsattningarForArbete = element(by.id('medicinskaForutsattningarForArbete'));
        this.aktivitetsFormaga = element(by.id('aktivitetsFormaga'));
        this.ovrigt = element(by.id('ovrigt'));
        this.kontaktMedFkNo = element(by.id('formly_1_checkbox-inline_kontaktMedFk_0'));
        // this.kontaktMedFkNo = element(by.id('kontaktMedFkNo'));
        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));
    },

    get: function get(intygId) {
        get._super.call(this, 'luse', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    }
});

module.exports = new LuseUtkast();
