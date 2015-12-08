'use strict';

module.exports = {
    ICD10: ['A00', 'B00', 'C00', 'D00'],
    korkortstyper: ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    identitetStyrktGenom: ['ID-kort', 'Företagskort eller tjänstekort', 'Svenskt körkort', 'Personlig kännedom', 'Försäkran enligt 18 kap. 4§', 'Pass'],
    diabetestyp: ['Typ 1', 'Typ 2'],
    diabetesbehandlingtyper: ['Endast kost', 'Tabletter', 'Insulin'],

    // TS-Bas-attribut
    korkortstyperHogreBehorighet: [ 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi' ],
    synDonder:                    shuffle(['Ja', 'Nej'])[0],
    synNedsattBelysning:          shuffle(['Ja', 'Nej'])[0],
    synOgonsjukdom:               shuffle(['Ja', 'Nej'])[0],
    synDubbel:                    shuffle(['Ja', 'Nej'])[0],
    synNystagmus:                 shuffle(['Ja', 'Nej'])[0],
    horselYrsel:                  shuffle(['Ja', 'Nej'])[0],
    horselSamtal:                 shuffle(['Ja', 'Nej'])[0],
    rorOrgNedsattning:            shuffle(['Ja', 'Nej'])[0],
    rorInUt:                      shuffle(['Ja', 'Nej'])[0],
    hjartHjarna:                  shuffle(['Ja', 'Nej'])[0],
    hjartSkada:                   shuffle(['Ja', 'Nej'])[0],
    hjartRisk:                    shuffle(['Ja', 'Nej'])[0],
    diabetes:                     shuffle(['Ja', 'Nej'])[0],
    neurologiska:                 shuffle(['Ja', 'Nej'])[0],
    epilepsi:                     shuffle(['Ja', 'Nej'])[0],
    njursjukdom:                  shuffle(['Ja', 'Nej'])[0],
    demens:                       shuffle(['Ja', 'Nej'])[0],
    somnVakenhet:                 shuffle(['Ja', 'Nej'])[0],
    alkoholMissbruk:              shuffle(['Ja', 'Nej'])[0],
    alkoholVard:                  shuffle(['Ja', 'Nej'])[0],
    alkoholProvtagning:           shuffle(['Ja', 'Nej'])[0],
    alkoholLakemedel:             shuffle(['Ja', 'Nej'])[0],
    psykiskSjukdom:               shuffle(['Ja', 'Nej'])[0],
    adhdPsykisk:                  shuffle(['Ja', 'Nej'])[0],
    adhdSyndrom:                  shuffle(['Ja', 'Nej'])[0],
    sjukhusvard:                  shuffle(['Ja', 'Nej'])[0],
    ovrigMedicin:                 shuffle(['Ja', 'Nej'])[0],
    
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
            stallningstagande: 'Kan inte ta ställning'
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
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: this.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                behandling: this.getRandomBehandling()
            },
            synintyg: {
                a: 'Ja'
            },
            bedomning: this.getRandomBedomning(randomKorkortstyper),
            synDonder:                    this.synDonder,                    
            synNedsattBelysning:          this.synNedsattBelysning,          
            synOgonsjukdom:               this.synOgonsjukdom,               
            synDubbel:                    this.synDubbel,                    
            synNystagmus:                 this.synNystagmus,                 
            horselYrsel:                  this.horselYrsel,                  
            horselSamtal:                 this.horselSamtal,                  
            rorOrgNedsattning:            this.rorOrgNedsattning,            
            rorOrgInUt:                   this.rorOrgInUt,            
            hjartHjarna:                  this.hjartHjarna,                  
            hjartSkada:                   this.hjartSkada,                   
            hjartRisk:                    this.hjartRisk,                    
            diabetes:                     this.diabetes,                     
            neurologiska:                 this.neurologiska,                 
            epilepsi:                     this.epilepsi,                     
            njursjukdom:                  this.njursjukdom,                  
            demens:                       this.demens,                       
            somnVakenhet:                 this.somnVakenhet,                 
            alkoholMissbruk:              this.alkoholMissbruk,              
            alkoholVard:                  this.alkoholVard,                  
            alkoholProvtagning:           this.alkoholProvtagning,           
            alkoholLakemedel:             this.alkoholLakemedel,             
            psykiskSjukdom:               this.psykiskSjukdom,               
            adhdPsykisk:                  this.adhdPsykisk,                  
            adhdSyndrom:                  this.adhdSyndrom,                  
            sjukhusvard:                  this.sjukhusvard,                  
            ovrigMedicin:                 this.ovrigMedicin
        };
    }
};

function shuffle(o) {
    for (var j, x, i = o.length; i;){
        j = Math.floor(Math.random() * i);
        x = o[--i]; 
        o[i] = o[j]; 
        o[j] = x;
    }
    return o;
}
