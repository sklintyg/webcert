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

var testValues = require('./testvalues.js').ts;

module.exports = {
    getRandom: function(intygsID) {
        var randomKorkortstyper = testValues.getRandomKorkortstyperHogre();

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Transportstyrelsens läkarintyg',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: testValues.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                behandling: testValues.getRandomBehandling()
            },
            synintyg: {
                a: 'Ja'
            },
            bedomning: testValues.getRandomBedomning(randomKorkortstyper),
            synDonder: shuffle(testValues.synDonder)[0],
            synNedsattBelysning: shuffle(testValues.synNedsattBelysning)[0],
            synOgonsjukdom: shuffle(testValues.synOgonsjukdom)[0],
            synDubbel: shuffle(testValues.synDubbel)[0],
            synNystagmus: shuffle(testValues.synNystagmus)[0],
            horsel: {
                yrsel: shuffle(testValues.horselYrsel)[0],
                samtal: testValues.getRandomHorselSamtal(randomKorkortstyper)
            },
            linser: {
                vanster: shuffle(testValues.synLinser)[0],
                hoger: shuffle(testValues.synLinser)[0]
            },
            rorelseorganensFunktioner: {
                nedsattning: shuffle(testValues.rorOrgNedsattning)[0],
                inUtUrFordon: testValues.getRandomInUtUrFordon(randomKorkortstyper)
            },
            hjartHjarna: shuffle(testValues.hjartHjarna)[0],
            hjartSkada: shuffle(testValues.hjartSkada)[0],
            hjartRisk: shuffle(testValues.hjartRisk)[0],
            diabetes: {
                hasDiabetes: shuffle(testValues.diabetes)[0],
                typ: shuffle(testValues.diabetestyp)[0],
                behandlingsTyper: testValues.diabetesbehandlingtyper
            },
            neurologiska: shuffle(testValues.neurologiska)[0],
            epilepsi: shuffle(testValues.epilepsi)[0],
            njursjukdom: shuffle(testValues.njursjukdom)[0],
            demens: shuffle(testValues.demens)[0],
            somnVakenhet: shuffle(testValues.somnVakenhet)[0],
            alkoholMissbruk: shuffle(testValues.alkoholMissbruk)[0],
            alkoholVard: shuffle(testValues.alkoholVard)[0],
            alkoholProvtagning: shuffle(testValues.alkoholProvtagning)[0],
            alkoholLakemedel: shuffle(testValues.alkoholLakemedel)[0],
            psykiskSjukdom: shuffle(testValues.psykiskSjukdom)[0],
            adhdPsykisk: shuffle(testValues.adhdPsykisk)[0],
            adhdSyndrom: shuffle(testValues.adhdSyndrom)[0],
            sjukhusvard: shuffle(testValues.sjukhusvard)[0],
            ovrigMedicin: shuffle(testValues.ovrigMedicin)[0],
            kommentar: testValues.comment,
            styrkor: testValues.getRandomStyrka()
        };
    }
};
