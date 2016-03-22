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
                    getRandomUtredning(),
                    getRandomUtredning()
                ],
                null
            ])[0];
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
            'Underlag från skolhälsovård',
            'Utredning av annan specialistklinik',
            'Övrigt'
        ])[0],
        datum: '2016-02-09',
        infoOmUtredningen: testdataHelper.randomTextString()
    };
}

module.exports = fkValues;
