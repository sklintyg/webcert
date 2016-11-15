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
            field: element(by.cssContainingText('.intyg-field', 'andra medicinska utredningar')),
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
            /*grund: element(by.id('diagnosgrund')),
            nyBedomningDiagnosgrundJa: element(by.id('nyBedomningDiagnosgrund-Ja')),
            nyBedomningDiagnosgrundNej: element(by.id('nyBedomningDiagnosgrund-Nej')),
            nyBedomningDiagnosgrund: element(by.cssContainingText('.intyg-field', 'Finns skäl till att revidera/uppdatera tidigare satt diagnos?')),
            diagnosForNyBedomning: element(by.id('diagnosForNyBedomning'))*/
        };

        this.funktionsnedsattning = {
            debut: element(by.id('funktionsnedsattningDebut')),
            paverkan: element(by.id('funktionsnedsattningPaverkan'))
        };

        // Knappar etc. ärvs i första hand från BaseIntygPage

        // Svarstexter
        this.undersokningAvPatienten = element(by.css('span #undersokningAvPatienten'));
        this.journaluppgifter = element(by.css('span #journaluppgifter'));
        this.anhorigsBeskrivningAvPatienten = element(by.css('span #anhorigsBeskrivningAvPatienten'));
        this.annatGrundForMU = element(by.css('span #annatGrundForMU'));
        this.annatGrundForMUBeskrivning = element(by.css('div #annatGrundForMUBeskrivning'));
        this.kannedomOmPatient = element(by.css('span #kannedomOmPatient'));

        this.underlagFinnsJa = element(by.id('underlagFinns-Ja'));

        this.underlag0Typ = element(by.id('underlag-0-typ'));
        this.underlag0Datum = element(by.id('underlag-0-datum'));
        this.underlag0HamtasFran = element(by.id('underlag-0-hamtasFran'));

        this.underlag1Typ = element(by.id('underlag-1-typ'));
        this.underlag1Datum = element(by.id('underlag-1-datum'));
        this.underlag1HamtasFran = element(by.id('underlag-1-hamtasFran'));

        this.diagnos0Kod = element(by.id('diagnoser-0-kod'));
        this.diagnos0Beskrivning = element(by.id('diagnoser-0-beskrivning'));

        this.diagnos1Kod = element(by.id('diagnoser-1-kod'));
        this.diagnos1Beskrivning = element(by.id('diagnoser-1-beskrivning'));

        this.diagnos2Kod = element(by.id('diagnoser-2-kod'));
        this.diagnos2Beskrivning = element(by.id('diagnoser-2-beskrivning'));

        this.funktionsnedsattningDebut = element(by.id('funktionsnedsattningDebut'));
        this.funktionsnedsattningPaverkan = element(by.id('funktionsnedsattningPaverkan'));

        this.ovrigt = element(by.id('ovrigt'));

        this.kontaktMedFkJa = element(by.id('kontaktMedFk-Ja'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));

        this.tillagsFraga1 = element(by.css('#tillaggsfraga-9001 div'));
        this.tillagsFraga2 = element(by.css('#tillaggsfraga-9002 div'));

        this.baseratPa = {
            minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
            anhorigsBeskrivning: element(by.id('anhorigsBeskrivningAvPatienten')),
            annat: element(by.id('annatGrundForMU')),
            annatBeskrivning: element(by.id('annatGrundForMUBeskrivning')),
            personligKannedom: element(by.id('kannedomOmPatient'))
        };

        this.ovrigaUpplysningar = element(by.id('ovrigt'));

        this.kontaktFK = {
            onskas: element(by.cssContainingText('.intyg-field', 'Jag önskar att Försäkringskassan kontaktar mig')),
            ja: element(by.id('kontaktMedFk-Ja')),
            nej: element(by.id('kontaktMedFk-Nej')),
            anledning: element(by.id('anledningTillKontakt'))
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    }
});
module.exports = new LuaefsIntyg();
