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

var shuffle = require('./../helpers/testdataHelper.js').shuffle;

var testdata =  {
        smittskydd: [true, false]
    };

function randomKontaktMedAF(smittskydd){
    if(smittskydd){return false;}
    else {return shuffle([true,false])[0];}
}
function randomRekommendationOvrigt(smittskydd){
    if(smittskydd){return false;}
    else{
        return shuffle(['Övrig rekommendation beskrivning','Övrig rekommendation beskrivning med lite extra'])[0];

    }
}
function randomRehabAktuell(smittskydd){
    if(smittskydd){return false;}
    else{
        return shuffle(['Ja','Nej','Går inte att bedöma'])[0];
    }
}

function randomPrognosFortydligande(val){
    if(val ==='Går ej att bedöma'){
        return 'Prognos förtydligande text';
    }
}

function dateFormat(date){
    var d = date.toISOString().slice(0,10).replace(/-/g,'-');
    return d;
}

var random = {
    baserasPa:function (smittskydd) {
        if (smittskydd) {return false;}
        return {
                minUndersokning:{datum:'2015-12-10'},
                minTelefonkontakt:{datum:'2015-12-10'},
                journaluppgifter:{datum:'2015-12-10'},
                annat:{datum:'2015-12-10',text: 'Annat text'}
        };
    },
    diagnos:function(smittskydd){
        if (smittskydd) {return false;}
        return {
            diagnoser:
                [{ ICD10:'A00', diagnosText:'Kolera' }],
            fortydligande: 'Förtydligande text',
            samsjuklighetForeligger: true
        };
    },
    aktuelltSjukdomsforlopp: function(smittskydd){
        if (smittskydd) {return '';}
        return 'Aktuellt sjukdomsförlopp text';
    },
    funktionsnedsattning:function(smittskydd){
        if (smittskydd) {return '';}
        return 'Funktionsnedsattning text';
    },
    aktivitestbegransning:function(smittskydd){
        if (smittskydd) {return '';}
        return 'Aktivitetsbegränsning text';
    },
    arbete:function(smittskydd){
        if (smittskydd) {return false;}
        return {
                nuvarandeArbete:{
                    aktuellaArbetsuppgifter:'Aktuella arbetsuppgifter text'
                },
                arbetsloshet:true,
                foraldraledighet:true
        };
    },
    arbetsformaga:function(){
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
                    from: dateFormat(today),
                    tom: dateFormat(todayPlus5Days)
                },
                nedsattMed50: {
                    from: dateFormat(todayPlus6Days),
                    tom: dateFormat(todayPlus10Days)
                },
                nedsattMed75: {
                    from: dateFormat(todayPlus11Days),
                    tom: dateFormat(todayPlus20Days)
                },
                nedsattMed100: {
                    from: dateFormat(todayPlus21Days),
                    tom: dateFormat(todayPlus30Days)
                }
        };
    },
    prognos:function(){
        var val =  shuffle(['Ja','Ja, delvis','Nej','Går ej att bedöma'])[0];
        return {
                    val: val,
                    fortydligande: randomPrognosFortydligande(val)
                };
    },
    atgarder:function(smittskydd){
        if (smittskydd) {return false;}
        return {
                    planerad: 'Planerad eller pågående behandling text',
                    annan: 'Annan åtgärd text'
                };
    },
    rekommendationer:function(smittskydd){
        return {
                    resor:shuffle([true,false])[0],
                    kontaktMedArbetsformedlingen:randomKontaktMedAF(smittskydd),
                    kontaktMedForetagshalsovard: randomKontaktMedAF(smittskydd),
                    ovrigt:shuffle([false,randomRekommendationOvrigt(smittskydd)])[0],
                    arbetslivsinriktadRehab: randomRehabAktuell(smittskydd)
                };
    },
    kontaktOnskasMedFK:function(){
        return shuffle([true,false])[0];
    },
    ovrigaUpplysningar:function(){
        return 'Övriga upplysningar och förtydliganden text';
    },
    arbetsformagaFMB:function(){
        return 'Arbetsförmåga bedöms nedsatt längre tid än FMB anger text';
    }




};

module.exports = {
    data:testdata,

    sjukintyg: {
        getRandom: function() {
            var isSmittskydd = shuffle(testdata.smittskydd)[0];

            return {
                typ:'Läkarintyg FK 7263',
                smittskydd: isSmittskydd,
                baserasPa:random.baserasPa(isSmittskydd),
                diagnos:random.diagnos(isSmittskydd),
                aktuelltSjukdomsforlopp: random.aktuelltSjukdomsforlopp(isSmittskydd),
                funktionsnedsattning: random.funktionsnedsattning(isSmittskydd),
                aktivitetsBegransning: random.aktivitestbegransning(isSmittskydd),
                arbete:random.arbete(isSmittskydd),
                arbetsformaga: random.arbetsformaga(),
                arbetsformagaFMB: random.arbetsformagaFMB(),
                prognos:random.prognos(),
                atgarder:random.atgarder(isSmittskydd),
                rekommendationer:random.rekommendationer(isSmittskydd),
                kontaktOnskasMedFK:random.kontaktOnskasMedFK(),
                ovrigaUpplysningar: random.ovrigaUpplysningar()
            };
        }
    }
};
