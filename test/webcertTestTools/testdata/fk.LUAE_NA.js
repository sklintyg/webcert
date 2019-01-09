/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

var testdataHelper = require('common-testtools').testdataHelper;
var fkValues = require('./testvalues.js').fk;
var shuffle = testdataHelper.shuffle;


var today = testdataHelper.dateFormat(new Date());

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            id: intygsID,
            typ: 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            baseratPa: {
                minUndersokningAvPatienten: '2017-09-26',
                journaluppgifter: '2017-09-26',
                anhorigsBeskrivning: '2017-09-26',
                annat: '2017-09-26',
                annatBeskrivning: 'Mliö62f4OrsNgKäB',
                personligKannedom: '2017-09-26'
            },
            andraMedicinskaUtredningar: [{
                underlag: 'Övrigt',
                datum: '2016-04-09',
                infoOmUtredningen: 'tSE0JcyzBKux55rP'
            }],
            diagnos: {
                diagnoser: [{
                    kod: "Z413",
                    bakgrund: "AmÄ4N2DGrzPbDÅaP"
                }],
                narOchVarStalldesDiagnoserna: 'QYCVäåuQWseawWSd',
                nyBedomning: true,
                diagnosForNyBedomning: 'o2ÖD423TOqrTj1hr'
            },
            sjukdomsForlopp: '8XkulDgjrtcHWkxw',
            funktionsnedsattning: {
                intellektuell: 'Svajig i benen',
                kommunikation: 'Total',
                koncentration: 'Svajig i benen',
                psykisk: 'Total',
                synHorselTal: 'Inget tal',
                balansKoordination: 'Ingen koncentration',
                annan: 'Ingen koncentration'
            },
            aktivitetsbegransning: 'D2åUURoA6fODF7iI',
            medicinskbehandling: {
                avslutad: 'Kostrådgivning',
                pagaende: 'Lågkaloridiet',
                planerad: 'Kostrådgivning',
                substansintag: 'Alvedon'
            },
            medicinskaForutsattningar: {
                utecklasOverTid: 'Q5KWnk1äbEvmÄIJö',
                trotsBegransningar: 'ÅÖ2A8ÖE0WCTmEXMa',
                forslagTillAtgard: 'DG2PDZZ4Q3XRzJåq'
            },
            ovrigt: 'bÖBv3EATatnwd211',
            kontaktMedFk: true,
            tillaggsfragor: [{
                svar: 'FKY1E5fijr9NM6SA'
            }, {
                svar: 'ucrQIäf19L3n2k38'
            }]
        };
    },
    getRandom: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            id: intygsID,
            typ: 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            baseratPa: {
                minUndersokningAvPatienten: today,
                journaluppgifter: today,
                anhorigsBeskrivning: today,
                annat: today,
                annatBeskrivning: testdataHelper.randomTextString(),
                personligKannedom: today
            },
            andraMedicinskaUtredningar: fkValues.getRandomMedicinskaUtredningar(),
            diagnos: {
                diagnoser: [{
                    kod: shuffle(fkValues.ICD10)[0],
                    bakgrund: testdataHelper.randomTextString()
                }],
                narOchVarStalldesDiagnoserna: testdataHelper.randomTextString(),
                nyBedomning: true, //testdataHelper.randomTrueFalse(),
                diagnosForNyBedomning: testdataHelper.randomTextString()
            },
            sjukdomsForlopp: testdataHelper.randomTextString(),
            // sjukdomsForlopp: 'Sjukdomsförlopp kommentar',

            funktionsnedsattning: {
                //funktionsnedsattningar
                intellektuell: shuffle(fkValues.funktionsnedsattningar)[0],
                kommunikation: shuffle(fkValues.funktionsnedsattningar)[0],
                koncentration: shuffle(fkValues.funktionsnedsattningar)[0],
                psykisk: shuffle(fkValues.funktionsnedsattningar)[0],
                synHorselTal: shuffle(fkValues.funktionsnedsattningar)[0],
                balansKoordination: shuffle(fkValues.funktionsnedsattningar)[0],
                annan: shuffle(fkValues.funktionsnedsattningar)[0]
            },
            aktivitetsbegransning: testdataHelper.randomTextString(),
            medicinskbehandling: {
                avslutad: shuffle(fkValues.medicinskaBehandlingar)[0],
                pagaende: shuffle(fkValues.medicinskaBehandlingar)[0],
                planerad: shuffle(fkValues.medicinskaBehandlingar)[0],
                substansintag: shuffle(fkValues.mediciner)[0]
            },
            medicinskaForutsattningar: {
                utecklasOverTid: testdataHelper.randomTextString(),
                trotsBegransningar: testdataHelper.randomTextString(),
                forslagTillAtgard: testdataHelper.randomTextString()
            },
            ovrigt: testdataHelper.randomTextString(),
            kontaktMedFk: testdataHelper.randomTrueFalse(),
            tillaggsfragor: [{
                svar: testdataHelper.randomTextString()
            }, {
                svar: testdataHelper.randomTextString()
            }]
        };
    }
};
