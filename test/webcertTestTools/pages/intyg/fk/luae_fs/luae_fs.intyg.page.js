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

var FkBaseIntygPage = require('../fk.base.intyg.page.js');

var LuaefsIntyg = FkBaseIntygPage._extend({
    init: function init() {
        init._super.call(this);

        this.intygType = 'luae_fs';

        this.andraMedicinskaUtredningar = {
            field: element(by.id('form_underlagFinns')),
            getUtredning: function(index) {
                return {
                    typ: element(by.id('underlag-' + index + '-typ')),
                    datum: element(by.id('underlag-' + index + '-datum')),
                    info: element(by.id('underlag-' + index + '-hamtasFran'))
                };
            }
        };

        this.diagnoser = {
            getDiagnos: function(index) {
                index = index || 0;
                return {
                    kod: element(by.id('diagnoser-' + index + '-kod')),
                    beskrivning: element(by.id('diagnoser-' + index + '-beskrivning'))
                };
            },
        };

        this.funktionsnedsattning = {
            debut: element(by.id('funktionsnedsattningDebut')),
            paverkan: element(by.id('funktionsnedsattningPaverkan'))
        };

        // Knappar etc. ärvs i första hand från BaseIntygPage

        // Svarstexter
        this.undersokningAvPatienten = element(by.css('#undersokningAvPatienten'));
        this.journaluppgifter = element(by.css('#journaluppgifter'));
        this.anhorigsBeskrivningAvPatienten = element(by.css('#anhorigsBeskrivningAvPatienten'));
        this.annatGrundForMU = element(by.css('#annatGrundForMU'));
        this.annatGrundForMUBeskrivning = element(by.css('#annatGrundForMUBeskrivning'));
        this.kannedomOmPatient = element(by.css('#kannedomOmPatient'));

        this.underlagFinns = element(by.id('underlagFinns'));

        this.underlag0Typ = element(by.id('underlag-row0-col0'));
        this.underlag0Datum = element(by.id('underlag-row0-col1'));
        this.underlag0HamtasFran = element(by.id('underlag-row0-col2'));

        this.underlag1Typ = element(by.id('underlag-row1-col0'));
        this.underlag1Datum = element(by.id('underlag-row1-col1'));
        this.underlag1HamtasFran = element(by.id('underlag-row1-col2'));

        this.diagnos0Kod = element(by.id('diagnoser-row0-col0'));
        this.diagnos0Beskrivning = element(by.id('diagnoser-row0-col1'));

        this.diagnos1Kod = element(by.id('diagnoser-row1-col0'));
        this.diagnos1Beskrivning = element(by.id('diagnoser-row1-col1'));

        this.diagnos2Kod = element(by.id('diagnoser-row2-col0'));
        this.diagnos2Beskrivning = element(by.id('diagnoser-row2-col1'));

        this.funktionsnedsattningDebut = element(by.id('funktionsnedsattningDebut'));
        this.funktionsnedsattningPaverkan = element(by.id('funktionsnedsattningPaverkan'));

        this.ovrigt = element(by.id('ovrigt'));

        this.kontaktMedFk = element(by.id('kontaktMedFk'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));

        this.tillagsFraga1 = element(by.css('#tillaggsfragor-9001'));
        this.tillagsFraga2 = element(by.css('#tillaggsfragor-9002'));

        this.baseratPa = {
            minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
            anhorigsBeskrivning: element(by.id('anhorigsBeskrivningAvPatienten')),
            annat: element(by.id('annatGrundForMU')),
            annatBeskrivning: element(by.id('annatGrundForMUBeskrivning')),
            personligKannedom: element(by.id('kannedomOmPatient'))
        };

        this.ovrigaUpplysningar = element(by.id('ovrigt'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    }
});
module.exports = new LuaefsIntyg();
