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
var shuffle = testdataHelper.shuffle;

var testValues = require('./testvalues.js').ts;

module.exports = {
    get: function(intygsID) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            "id": intygsID,
            "typ": "Transportstyrelsens läkarintyg högre körkortsbehörighet",
            "korkortstyper": ["C1", "CE", "DE", "C1E", "D", "C", "Taxi", "D1E"],
            "identitetStyrktGenom": "Försäkran enligt 18 kap. 4§",
            "allmant": {
                "year": 1981,
                "behandling": {
                    "typer": ["Tabletter"]
                }
            },
            "synintyg": {
                "a": "Ja"
            },
            "bedomning": {
                "stallningstagande": "Kan inte ta ställning",
                "lamplighet": null
            },
            "synDonder": "Ja",
            "synNedsattBelysning": "Nej",
            "synOgonsjukdom": "Nej",
            "synDubbel": "Nej",
            "synNystagmus": "Nej",
            "horsel": {
                "yrsel": "Nej",
                "samtal": "Ja"
            },
            "linser": {
                "vanster": "Nej",
                "hoger": "Ja"
            },
            "rorelseorganensFunktioner": {
                "nedsattning": "Nej",
                "nedsattningBeskrivning": "0QFqaNgxqRNuÅOMÖ",
                "inUtUrFordon": "Ja"
            },
            "hjartHjarna": "Nej",
            "hjartSkada": "Ja",
            "hjartRisk": "Ja",
            "hjartRiskBeskrivning": "TIA och förmaksflimmer.",
            "diabetes": {
                "hasDiabetes": "Nej",
                "typ": "Typ 1",
                "behandlingsTyper": ["Endast kost", "Tabletter", "Insulin"]
            },
            "neurologiska": "Ja",
            "epilepsi": "Nej",
            "epilepsiBeskrivning": "aKCa7WnLäÅåSA19j",
            "njursjukdom": "Ja",
            "demens": "Ja",
            "somnVakenhet": "Nej",
            "alkoholMissbruk": "Nej",
            "alkoholVard": "Nej",
            "alkoholProvtagning": "Ja",
            "alkoholLakemedel": "Nej",
            "alkoholLakemedelBeskrivning": "2 liter metadon.",
            "psykiskSjukdom": "Ja",
            "adhdPsykisk": "Ja",
            "adhdSyndrom": "Ja",
            "sjukhusvard": "Nej",
            "sjukhusvardTidPunkt": "ThGvhEEAövOqIfJC",
            "sjukhusvardInrattning": "FiqMöIcw9öNi7Pe0",
            "sjukhusvardAnledning": "eQcIma21cDNrasGÄ",
            "ovrigMedicin": "Nej",
            "ovrigMedicinBeskrivning": "I4b80sdÖ0qwC4ÄML",
            "kommentar": "Inget att rapportera",
            "styrkor": {
                "houk": "1.1",
                "homk": "1.3",
                "vouk": "1.6",
                "vomk": "1.7",
                "buk": "1.8",
                "bmk": "1.7"
            },
            "specialist": "SkW4Gdzöl6m3pvyJ"
        };
    },
    getRandom: function(intygsID, patient) {
        var randomKorkortstyper = testValues.getRandomKorkortstyperHogre();

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        var bedomningObj = testValues.getRandomBedomning(randomKorkortstyper);
        // TS Bas behöver inte svar på lämplighet
        bedomningObj.lamplighet = null;

        //Använd patientens födelseår för att ta fram allmant.year (vilket år ställdes diagnosen diabetes får inte vare tidigare än födelseår)
        if (!patient || patient === true) {
            patient = {
                id: '1980'
            };
        }

        return {
            id: intygsID,
            typ: 'Transportstyrelsens läkarintyg högre körkortsbehörighet',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: testValues.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: parseInt(patient.id.substring(0, 4)) + 1,
                behandling: testValues.getRandomBehandling()
            },
            synintyg: {
                a: 'Ja'
            },
            bedomning: bedomningObj,
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
                nedsattningBeskrivning: testdataHelper.randomTextString(),
                inUtUrFordon: testValues.getRandomInUtUrFordon(randomKorkortstyper)
            },
            hjartHjarna: shuffle(testValues.hjartHjarna)[0],
            hjartSkada: shuffle(testValues.hjartSkada)[0],
            hjartRisk: shuffle(testValues.hjartRisk)[0],
            hjartRiskBeskrivning: 'TIA och förmaksflimmer.',
            diabetes: {
                hasDiabetes: shuffle(testValues.diabetes)[0],
                typ: shuffle(testValues.diabetestyp)[0],
                behandlingsTyper: testValues.diabetesbehandlingtyper
            },
            neurologiska: shuffle(testValues.neurologiska)[0],
            epilepsi: shuffle(testValues.epilepsi)[0],
            epilepsiBeskrivning: testdataHelper.randomTextString(),
            njursjukdom: shuffle(testValues.njursjukdom)[0],
            demens: shuffle(testValues.demens)[0],
            somnVakenhet: shuffle(testValues.somnVakenhet)[0],
            alkoholMissbruk: shuffle(testValues.alkoholMissbruk)[0],
            alkoholVard: shuffle(testValues.alkoholVard)[0],
            alkoholProvtagning: shuffle(testValues.alkoholProvtagning)[0],
            alkoholLakemedel: shuffle(testValues.alkoholLakemedel)[0],
            alkoholLakemedelBeskrivning: '2 liter metadon.',
            psykiskSjukdom: shuffle(testValues.psykiskSjukdom)[0],
            adhdPsykisk: shuffle(testValues.adhdPsykisk)[0],
            adhdSyndrom: shuffle(testValues.adhdSyndrom)[0],
            sjukhusvard: shuffle(testValues.sjukhusvard)[0],
            sjukhusvardTidPunkt: testdataHelper.randomTextString(),
            sjukhusvardInrattning: testdataHelper.randomTextString(),
            sjukhusvardAnledning: testdataHelper.randomTextString(),
            ovrigMedicin: shuffle(testValues.ovrigMedicin)[0],
            ovrigMedicinBeskrivning: testdataHelper.randomTextString(),
            kommentar: testValues.comment,
            styrkor: testValues.getRandomStyrka(),
            specialist: testdataHelper.randomTextString()
        };
    }
};
