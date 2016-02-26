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

var testdataHelper = require('./../helpers/testdataHelper.js');
var shuffle = testdataHelper.shuffle;

module.exports = {
    ICD10: ['A00', 'B00', 'C00', 'D00'],

    mediciner: ['Ipren', 'Alvedon', 'Bamyl', 'Snus'],
    funktionsnedsattningar: ['Problem...', 'Inget tal', 'Ingen koncentration', 'Total', 'Blind', 'Svajig i benen', 'Ingen'],

    fk: require('./fk.js'),

    randomTrueFalse: function() {
        return shuffle([true, false])[0];
    },

    randomTextString: function() {
        var text = '';
        var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö0123456789';

        for (var i = 0; i < 16; i++) {
            text += possible.charAt(Math.floor(Math.random() * possible.length));
        }
        return text;
    },

    getRandomLuseIntyg: function(intygsID) {
        return {
            intygId: intygsID,
            typ: 'Läkarutlåtande för sjukersättning',
            diagnos: {
                kod: shuffle(this.ICD10)[0],
                bakgrund: this.randomTextString()
                // bakgrund: 'En slumpmässig bakgrund'
            },
            annanUnderlag: this.randomTrueFalse(),
            sjukdomsForlopp: this.randomTextString(),
            // sjukdomsForlopp: 'Sjukdomsförlopp kommentar',
            nyDiagnosBedom: this.randomTrueFalse(),
            funktionsnedsattning: {
                //funktionsnedsattningar
                intellektuell: shuffle(this.funktionsnedsattningar)[0],
                kommunikation: shuffle(this.funktionsnedsattningar)[0],
                koncentration: shuffle(this.funktionsnedsattningar)[0],
                psykisk: shuffle(this.funktionsnedsattningar)[0],
                synHorselTal: shuffle(this.funktionsnedsattningar)[0],
                balansKoordination: shuffle(this.funktionsnedsattningar)[0],
                annan: shuffle(this.funktionsnedsattningar)[0]
            },
            aktivitetsbegransning: this.randomTextString(),
            // aktivitetsbegransning: 'Total',
            avslutadBehandling: shuffle(this.mediciner)[0],
            pagaendeBehandling: shuffle(this.mediciner)[0],
            planeradBehandling: shuffle(this.mediciner)[0],
            substansintag: shuffle(this.mediciner)[0],
            medicinskaForutsattningarForArbete: this.randomTextString(),
            // medicinskaForutsattningarForArbete: 'Inte speciellt',
            aktivitetsFormaga: this.randomTextString(),
            // aktivitetsFormaga: 'Liten',
            ovrigt: this.randomTextString(),
            // ovrigt: 'Inget',
            kontaktMedFkNo: this.randomTrueFalse(),
            tillaggsfragor0svar: this.randomTextString(),
            // tillaggsfragor0svar: 'Answer',
            tillaggsfragor1svar: this.randomTextString()
            // tillaggsfragor1svar: 'Question'
        };
    }
};