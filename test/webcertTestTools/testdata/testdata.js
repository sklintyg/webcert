/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

var shuffle = require('./../helpers/testdataHelper.js').shuffle;

module.exports = {
    ICD10: ['A00', 'B00', 'C00', 'D00'],
    korkortstyper: ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    identitetStyrktGenom: ['ID-kort', 'Företagskort eller tjänstekort', 'Svenskt körkort', 'Personlig kännedom', 'Försäkran enligt 18 kap. 4§', 'Pass'],
    diabetestyp: ['Typ 1', 'Typ 2'],
    diabetesbehandlingtyper: ['Endast kost', 'Tabletter', 'Insulin'],

    // FK7263

    // TS-Bas-attribut
    korkortstyperHogreBehorighet: [ 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi' ],
    synDonder:                    ['Ja', 'Nej'],
    synNedsattBelysning:          ['Ja', 'Nej'],
    synOgonsjukdom:               ['Ja', 'Nej'],
    synDubbel:                    ['Ja', 'Nej'],
    synNystagmus:                 ['Ja', 'Nej'],
    horselYrsel:                  ['Ja', 'Nej'],
    horselSamtal:                 ['Ja', 'Nej'],
    rorOrgNedsattning:            ['Ja', 'Nej'],
    rorOrgInUt:                   ['Ja', 'Nej'],
    hjartHjarna:                  ['Ja', 'Nej'],
    hjartSkada:                   ['Ja', 'Nej'],
    hjartRisk:                    ['Ja', 'Nej'],
    // diabetes:                     ['Ja', 'Ja'],
    diabetes:                     ['Ja', 'Nej'],
    neurologiska:                 ['Ja', 'Nej'],
    epilepsi:                     ['Ja', 'Nej'],
    njursjukdom:                  ['Ja', 'Nej'],
    demens:                       ['Ja', 'Nej'],
    somnVakenhet:                 ['Ja', 'Nej'],
    alkoholMissbruk:              ['Ja', 'Nej'],
    alkoholVard:                  ['Ja', 'Nej'],
    alkoholProvtagning:           ['Ja', 'Nej'],
    alkoholLakemedel:             ['Ja', 'Nej'],
    psykiskSjukdom:               ['Ja', 'Nej'],
    adhdPsykisk:                  ['Ja', 'Nej'],
    adhdSyndrom:                  ['Ja', 'Nej'],
    sjukhusvard:                  ['Ja', 'Nej'],
    ovrigMedicin:                 ['Ja', 'Nej'],

    fk: require('./fk.js'),
    
    getRandomKorkortstyper: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(this.korkortstyper).slice(0, Math.floor(Math.random() * this.korkortstyper.length));
    },
    getRandomKorkortstyperHogre: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(this.korkortstyperHogreBehorighet).slice(0, Math.floor(Math.random() * this.korkortstyperHogreBehorighet.length));
    },

    getRandomIdentitetStyrktGenom: function() {
        return shuffle(this.identitetStyrktGenom)[0];
    },
    getRandomHypoglykemier: function(korkortstyper) {
        var hypoObj = {
            a: 'Nej',
            b: 'Nej'
        };

        //För vissa körkortstyper krävs det svar på f och g
        if (
            korkortstyper.indexOf('C1') > -1 ||
            korkortstyper.indexOf('C1E') > -1 ||
            korkortstyper.indexOf('C') > -1 ||
            korkortstyper.indexOf('CE') > -1 ||
            korkortstyper.indexOf('D1') > -1 ||
            korkortstyper.indexOf('D1E') > -1 ||
            korkortstyper.indexOf('D') > -1 ||
            korkortstyper.indexOf('DE') > -1
        ) {
            hypoObj.f = shuffle(['Ja', 'Nej'])[0];
            hypoObj.g = 'Nej';
        }
        return hypoObj;
    },
    getRandomBehandling: function() {
        var behandlingObj = {
            typer: shuffle(this.diabetesbehandlingtyper).slice(0, Math.floor(Math.random() * this.diabetesbehandlingtyper.length) + 1)
        };

        // Om Insulinbehanling så måste startår anges
        if (behandlingObj.typer.indexOf('Insulin') > -1) {
            behandlingObj.insulinYear = Math.floor((Math.random() * 20) + 1980);
        }

        return behandlingObj;
    },

    getRandomBedomning: function(korkortstyper) {
        var bedomningsObj = {
            stallningstagande: 'behorighet_bedomning',
            behorigheter: korkortstyper,
            lamplighet: shuffle(['Ja', 'Nej'])[0]
        };

        //För vissa körkortstyper krävs det svar lämplighet
        if (
            korkortstyper.indexOf('C1') > -1 ||
            korkortstyper.indexOf('C1E') > -1 ||
            korkortstyper.indexOf('C') > -1 ||
            korkortstyper.indexOf('CE') > -1 ||
            korkortstyper.indexOf('D1') > -1 ||
            korkortstyper.indexOf('D1E') > -1 ||
            korkortstyper.indexOf('D') > -1 ||
            korkortstyper.indexOf('DE') > -1
        ) {
            bedomningsObj.lamplighet = shuffle(['Ja', 'Nej'])[0];
        }
        return bedomningsObj;

    },
    getRandomTsDiabetesIntyg: function() {
        var randomKorkortstyper = this.getRandomKorkortstyper();
        return {
            typ:'Transportstyrelsens läkarintyg, diabetes',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: this.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                typ: shuffle(this.diabetestyp)[0],
                behandling: this.getRandomBehandling()
            },

            // TODO: Gör dessa slumpade likt ovanstående
            hypoglykemier: this.getRandomHypoglykemier(randomKorkortstyper),
            synintyg: {
                a: 'Ja'
            },
            bedomning: this.getRandomBedomning(randomKorkortstyper)
        };
    },
    getRandomTsBasIntyg: function() {
        var randomKorkortstyper = this.getRandomKorkortstyperHogre();
        return {
            typ:'Transportstyrelsens läkarintyg',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: this.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                behandling: this.getRandomBehandling()
                // behandling: this.diabetesbehandlingtyper
            },
            synintyg: {
                a: 'Ja'
            },
            bedomning: this.getRandomBedomning(randomKorkortstyper),
            synDonder:                    shuffle(this.synDonder)[0],                    
            synNedsattBelysning:          shuffle(this.synNedsattBelysning)[0],          
            synOgonsjukdom:               shuffle(this.synOgonsjukdom)[0],               
            synDubbel:                    shuffle(this.synDubbel)[0],                    
            synNystagmus:                 shuffle(this.synNystagmus)[0],                 
            horselYrsel:                  shuffle(this.horselYrsel)[0],                  
            horselSamtal:                 shuffle(this.horselSamtal)[0],                  
            rorOrgNedsattning:            shuffle(this.rorOrgNedsattning)[0],            
            rorOrgInUt:                   shuffle(this.rorOrgInUt)[0],            
            hjartHjarna:                  shuffle(this.hjartHjarna)[0],                  
            hjartSkada:                   shuffle(this.hjartSkada)[0],                   
            hjartRisk:                    shuffle(this.hjartRisk)[0],                    
            diabetes:                     shuffle(this.diabetes)[0],                     
            diabetestyp:                  shuffle(this.diabetestyp)[0],                     
            neurologiska:                 shuffle(this.neurologiska)[0],                 
            epilepsi:                     shuffle(this.epilepsi)[0],                     
            njursjukdom:                  shuffle(this.njursjukdom)[0],                  
            demens:                       shuffle(this.demens)[0],                       
            somnVakenhet:                 shuffle(this.somnVakenhet)[0],                 
            alkoholMissbruk:              shuffle(this.alkoholMissbruk)[0],              
            alkoholVard:                  shuffle(this.alkoholVard)[0],                  
            alkoholProvtagning:           shuffle(this.alkoholProvtagning)[0],           
            alkoholLakemedel:             shuffle(this.alkoholLakemedel)[0],             
            psykiskSjukdom:               shuffle(this.psykiskSjukdom)[0],               
            adhdPsykisk:                  shuffle(this.adhdPsykisk)[0],                  
            adhdSyndrom:                  shuffle(this.adhdSyndrom)[0],                  
            sjukhusvard:                  shuffle(this.sjukhusvard)[0],                  
            ovrigMedicin:                 shuffle(this.ovrigMedicin)[0],
            kommentar:                    'Inget att rapportera',
            dTyper:                       this.diabetesbehandlingtyper

        };
    }
};
