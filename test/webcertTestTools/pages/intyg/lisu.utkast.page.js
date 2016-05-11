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

var LisuUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('formly_1_date_undersokningAvPatienten_3')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            telefonkontakt: {
                checkbox: element(by.id('formly_1_date_telefonkontaktMedPatienten_4')),
                datum: element(by.id('form_telefonkontaktMedPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('formly_1_date_journaluppgifter_5')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('formly_1_single-text_annatGrundForMUBeskrivning_7')),
                checkbox: element(by.id('formly_1_date_annatGrundForMU_6')),
                datum: element(by.id('form_annatGrundForMU')).element(by.css('input[type=text]'))
            }
        };
        this.sysselsattning = {
            typ: {
                nuvarandeArbete: element(by.id('sysselsattning.typ-1')),
                arbetssokande:element(by.id('sysselsattning.typ-2')),
                foraldraledighet:element(by.id('sysselsattning.typ-3')),
                studier:element(by.id('sysselsattning.typ-4')),
                arbetmarknadspolitisktProgram:element(by.id('sysselsattning.typ-5')),
            },
            nuvarandeArbeteBeskrivning:element(by.id('nuvarandeArbete')),
            arbetsmarknadspolitisktProgramBeskrivning:element(by.id('arbetsmarknadspolitisktProgram'))
        };
        this.konsekvenser = {
            funktionsnedsattning:element(by.id('funktionsnedsattning')),
            aktivitetsbegransning:element(by.id('aktivitetsbegransning'))
        };
        this.behandling = {
            pagaendeBehandling:element(by.id('pagaendeBehandling')),
            planeradBehandling:element(by.id('planeradBehandling'))
        };
        this.sjukskrivning = {
            100: {
                fran: element(by.id('sjukskrivningar-1-from')),
                till: element(by.id('sjukskrivningar-1-tom'))
            },
            75: {
                fran: element(by.id('sjukskrivningar-2-from')),
                till: element(by.id('sjukskrivningar-2-tom'))
            },
            50: {
                fran: element(by.id('sjukskrivningar-3-from')),
                till: element(by.id('sjukskrivningar-3-tom'))
            },
            25: {
                fran: element(by.id('sjukskrivningar-4-from')),
                till: element(by.id('sjukskrivningar-4-tom'))
            },
            forsakringsmedicinsktBeslutsstodBeskrivning: element(by.id('forsakringsmedicinsktBeslutsstod')),
            arbetstidsforlaggning: {
                nej: element(by.id('arbetstidsforlaggningNo')),
                ja: element(by.id('arbetstidsforlaggningYes')),
                beskrivning: element(by.id('arbetstidsforlaggningMotivering'))
            },
            arbetsresor : {
                nej: element(by.id('arbetsresorNo')),
                ja: element(by.id('arbetsresorYes'))
            },
            formagaTrotsBegransningBeskrivning: element(by.id('formagaTrotsBegransning')),
            prognos: {
                typ: {
                    1: element(by.id('prognos.typ-1')),
                    3: element(by.id('prognos.typ-3')),
                    4: element(by.id('prognos.typ-4')),
                    5: element(by.id('prognos.typ-5'))
                },
                dagarTillArbete: {
                    30: element(by.id('prognos.dagarTillArbete-1')),
                    60: element(by.id('prognos.dagarTillArbete-2')),
                    90: element(by.id('prognos.dagarTillArbete-3')),
                    180: element(by.id('prognos.dagarTillArbete-4'))
                }
            }
        };
        this.atgarder = {
            typ: {
                1: element(by.id('arbetslivsinriktadeAtgarder-1')),
                2: element(by.id('arbetslivsinriktadeAtgarder-2')),
                3: element(by.id('arbetslivsinriktadeAtgarder-3')),
                4: element(by.id('arbetslivsinriktadeAtgarder-4')),
                5: element(by.id('arbetslivsinriktadeAtgarder-5')),
                6: element(by.id('arbetslivsinriktadeAtgarder-6')),
                7: element(by.id('arbetslivsinriktadeAtgarder-7')),
                8: element(by.id('arbetslivsinriktadeAtgarder-8')),
                9: element(by.id('arbetslivsinriktadeAtgarder-9')),
                10: element(by.id('arbetslivsinriktadeAtgarder-10')),
                11: element(by.id('arbetslivsinriktadeAtgarder-11'))
            },
            arbetslivsinriktadeAtgarderEjAktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderEjAktuelltBeskrivning')),
            arbetslivsinriktadeAtgarderAktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderAktuelltBeskrivning'))
        };
    },
    get: function get(intygId) {
        get._super.call(this, 'lisu', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    },
    getTillaggsfraga: function(i) {
        return element(by.id('form_tillaggsfragor_' + i + '__svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar textarea')).getAttribute('value');
    }
});

module.exports = new LisuUtkast();
