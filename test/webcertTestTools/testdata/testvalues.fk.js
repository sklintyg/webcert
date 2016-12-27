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

var fkValues = {
    ICD10: ['A00', 'B00', 'C00', 'D00', 'Z720B', 'Z413'],
    mediciner: ['Ipren', 'Alvedon', 'Bamyl'],
    medicinskaBehandlingar: ['Lågkaloridiet', 'Motionsrådgivning', 'Kostrådgivning', 'Kognitiv beteendeinriktad terapi', 'Elektrokonvulsiv behandling'],
    funktionsnedsattningar: ['Problem', 'Inget tal', 'Ingen koncentration', 'Total', 'Blind', 'Svajig i benen', 'Ingen'],
    getRandomMedicinskaUtredningar: function() {
        return shuffle(
            [
                [
                    getRandomUtredning()
                    //getRandomUtredning() TODO: Fixa stöd för flera utredningar. Detta misslyckas ibland
                ],
                false
            ])[0];
    },
    getRandomDiagnoskod: function() {
        return shuffle(this.ICD10)[0];
    },
    getRandomBaserasPa: function(smittskydd) {
        if (smittskydd) {
            return false;
        }
        return {
            minUndersokning: {
                datum: '2015-12-10'
            },
            minTelefonkontakt: {
                datum: '2015-12-10'
            },
            journaluppgifter: {
                datum: '2015-12-10'
            },
            annat: {
                datum: '2015-12-10',
                text: 'Annat text'
            }
        };
    },
    getRandomArbetsformaga: function() {
        var today = new Date();
        var todayPlus5Days = new Date();
        var todayPlus6Days = new Date();
        var todayPlus10Days = new Date();
        var todayPlus11Days = new Date();
        var todayPlus20Days = new Date();
        var todayPlus21Days = new Date();
        var todayPlus30Days = new Date();

        todayPlus5Days.setDate(today.getDate() + 5);
        todayPlus6Days.setDate(today.getDate() + 6);
        todayPlus10Days.setDate(today.getDate() + 10);
        todayPlus11Days.setDate(today.getDate() + 11);
        todayPlus20Days.setDate(today.getDate() + 20);
        todayPlus21Days.setDate(today.getDate() + 21);
        todayPlus30Days.setDate(today.getDate() + 30);

        return {
            nedsattMed25: {
                from: testdataHelper.dateFormat(today),
                tom: testdataHelper.dateFormat(todayPlus5Days)
            },
            nedsattMed50: {
                from: testdataHelper.dateFormat(todayPlus6Days),
                tom: testdataHelper.dateFormat(todayPlus10Days)
            },
            nedsattMed75: {
                from: testdataHelper.dateFormat(todayPlus11Days),
                tom: testdataHelper.dateFormat(todayPlus20Days)
            },
            nedsattMed100: {
                from: testdataHelper.dateFormat(todayPlus21Days),
                tom: testdataHelper.dateFormat(todayPlus30Days)
            }
        };
    },
    getRandomKontaktOnskasMedFK: function() {
        return shuffle([true, false])[0];
    }
};

function getRandomUtredning() {
    return {
        underlag: shuffle([
            'Neuropsykiatriskt utlåtande',
            'Underlag från habiliteringen',
            'Underlag från arbetsterapeut',
            'Underlag från fysioterapeut',
            'Underlag från logoped',
            'Underlag från psykolog',
            'Underlag från företagshälsovård',
            'Utredning av annan specialistklinik',
            'Utredning från vårdinrättning utomlands',
            'Övrigt'
        ])[0],
        datum: '2016-04-09',
        infoOmUtredningen: testdataHelper.randomTextString()
    };
}

module.exports = fkValues;
