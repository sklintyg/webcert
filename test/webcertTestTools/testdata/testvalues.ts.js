/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
var shuffle = testdataHelper.shuffle;

function getRandomFloat() {
    return parseFloat(Math.round((Math.random() * (2.0 - 1.0) + 1.0) * 10) / 10).toFixed(1);
}

function findArrayElementsInArray(targetArray, compareArray) {
    // find all elements in targetArray matching any elements in compareArray
    var result = targetArray.filter(function(element) {
        return (compareArray.indexOf(element) >= 0);
    });

    return result;
}

function arrayContains(array, compareArray) {
    var found = findArrayElementsInArray(array, compareArray);
    //logger.info('found:' + JSON.stringify(found));
    return found.length > 0;
}

function isHogreKorkortstyper(typer) {
    return (
        typer.indexOf('C1') > -1 ||
        typer.indexOf('C1E') > -1 ||
        typer.indexOf('C') > -1 ||
        typer.indexOf('CE') > -1 ||
        typer.indexOf('D1') > -1 ||
        typer.indexOf('D1E') > -1 ||
        typer.indexOf('D') > -1 ||
        typer.indexOf('Taxi') > -1 ||
        typer.indexOf('DE') > -1
    );
}

var tsValues = {
    korkortstyper: ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    identitetStyrktGenom: ['ID-kort', 'Företagskort eller tjänstekort', 'Svenskt körkort', 'Personlig kännedom', 'Försäkran enligt 18 kap. 4§', 'Pass'],
    diabetestyp: ['Typ 1', 'Typ 2'],
    diabetesbehandlingtyper: ['Endast kost', 'Tabletter', 'Insulin'],
    comment: 'Inget att rapportera',
    korkortstyperHogreBehorighet: ['C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    synDonder: ['Ja', 'Nej'],
    synNedsattBelysning: ['Ja', 'Nej'],
    synOgonsjukdom: ['Ja', 'Nej'],
    synDubbel: ['Ja', 'Nej'],
    synNystagmus: ['Ja', 'Nej'],
    synLinser: ['Ja', 'Nej'],
    horselYrsel: ['Ja', 'Nej'],
    horselSamtal: ['Ja', 'Nej'],
    rorOrgNedsattning: ['Ja', 'Nej'],
    rorOrgInUt: ['Ja', 'Nej'],
    hjartHjarna: ['Ja', 'Nej'],
    hjartSkada: ['Ja', 'Nej'],
    hjartRisk: ['Ja', 'Nej'],
    diabetes: ['Ja', 'Nej'],
    neurologiska: ['Ja', 'Nej'],
    epilepsi: ['Ja', 'Nej'],
    njursjukdom: ['Ja', 'Nej'],
    demens: ['Ja', 'Nej'],
    somnVakenhet: ['Ja', 'Nej'],
    alkoholMissbruk: ['Ja', 'Nej'],
    alkoholVard: ['Ja', 'Nej'],
    alkoholProvtagning: ['Ja', 'Nej'],
    alkoholLakemedel: ['Ja', 'Nej'],
    psykiskSjukdom: ['Ja', 'Nej'],
    adhdPsykisk: ['Ja', 'Nej'],
    adhdSyndrom: ['Ja', 'Nej'],
    sjukhusvard: ['Ja', 'Nej'],
    ovrigMedicin: ['Ja', 'Nej'],

    getRandomHorselSamtal: function(korkortstyper) {
        var besvarasOm = ['D1', 'D1E', 'D', 'DE', 'Taxi'];
        if (arrayContains(korkortstyper, besvarasOm)) {
            return shuffle(tsValues.horselSamtal)[0];
        } else {
            return false;
        }
    },
    getRandomInUtUrFordon: function(korkortstyper) {
        var besvarasOm = ['D1', 'D1E', 'D', 'DE', 'Taxi'];
        if (arrayContains(korkortstyper, besvarasOm)) {
            return shuffle(tsValues.rorOrgInUt)[0];
        } else {
            return '';
        }
    },

    getRandomKorkortstyper: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(tsValues.korkortstyper.slice(0)).slice(0, Math.floor(Math.random() * tsValues.korkortstyper.length) + 1);
    },

    getRandomKorkortstyperHogre: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(tsValues.korkortstyperHogreBehorighet.slice(0)).slice(0, Math.floor(Math.random() * tsValues.korkortstyperHogreBehorighet.length) + 1);
    },
    getRandomStyrka: function() {
        var styrkor = {
            houk: getRandomFloat(),
            homk: getRandomFloat(),
            vouk: getRandomFloat(),
            vomk: getRandomFloat(),
            buk: getRandomFloat(),
            bmk: getRandomFloat()
        };
        return styrkor;
    },

    getRandomIdentitetStyrktGenom: function() {
        return shuffle(tsValues.identitetStyrktGenom)[0];
    },
    getRandomHypoglykemier: function(korkortstyper) {
        var hypoObj = {
            a: shuffle(['Ja', 'Nej'])[0],
            b: shuffle(['Ja', 'Nej'])[0]
        };

        //För vissa körkortstyper krävs det svar på f och g
        if (isHogreKorkortstyper(korkortstyper)) {
            hypoObj.f = shuffle(['Ja', 'Nej'])[0];
            hypoObj.g = shuffle(['Ja', 'Nej'])[0];
        }

        if (hypoObj.g === 'Ja') {
            hypoObj.gDatum = '2018-07-04';
        }

        if (hypoObj.b === 'Ja') {
            hypoObj.c = shuffle(['Ja', 'Nej'])[0];
            hypoObj.d = shuffle(['Ja', 'Nej'])[0];
            hypoObj.e = shuffle(['Ja', 'Nej'])[0];
            if (hypoObj.d === 'Ja') {
                hypoObj.dAntalEpisoder = testdataHelper.randomTextString();
            }
            if (hypoObj.e === 'Ja') {
                hypoObj.eAntalEpisoder = testdataHelper.randomTextString();
            }
        }

        return hypoObj;
    },
    getRandomBehandling: function() {
        var start = Math.floor(Math.random() * tsValues.diabetesbehandlingtyper.length);
        var end = Math.floor(Math.random() * (tsValues.diabetesbehandlingtyper.length + 1)) + start; // +1 kompenserar för att end inte är inkluderat i splice.

        var behandlingObj = {
            typer: tsValues.diabetesbehandlingtyper.slice(start, end)
        };

        // Om Insulinbehanling så måste startår anges
        if (behandlingObj.typer.indexOf('Insulin') > -1) {
            behandlingObj.insulinYear = Math.floor((Math.random() * 20) + 1980);
        }

        return behandlingObj;
    },
    hasHogreKorkortsbehorigheter: function(korkortstyper) {
        var foundHogreBehorigheter = findArrayElementsInArray(korkortstyper, tsValues.korkortstyperHogreBehorighet);
        return foundHogreBehorigheter.length > 0;
    },
    getRandomBedomning: function(korkortstyper) {
        var stalltagande = shuffle(['Någon av följande behörighet', 'Kan inte ta ställning'])[0];

        // if (!this.hasHogreKorkortsbehorigheter(korkortstyper)) {
        //     lamplighet = null;
        // }


        var bedomningsObj = {
            stallningstagande: stalltagande,
            lamplighet: null

        };


        if (stalltagande === 'Någon av följande behörighet') {
            bedomningsObj.behorigheter = korkortstyper;
        }

        //För vissa körkortstyper krävs det svar lämplighet
        if (isHogreKorkortstyper(korkortstyper)) {
            bedomningsObj.lamplighet = shuffle(['Ja', 'Nej'])[0];
        }
        return bedomningsObj;

    }
};

module.exports = tsValues;
