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

var testdata =  {
        smittskydd: [true, false]
    };

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
        if (smittskydd) {return false;}
        return 'Aktuellt sjukdomsförlopp text';
    },
    funktionsnedsattning:function(smittskydd){
        if (smittskydd) {return false;}
        return 'Funktionsnedsattning text';
    },
    aktivitestbegransning:function(smittskydd){
        if (smittskydd) {return false;}
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
        return {
                    nedsattMed25: {
                        from: '2015-12-15',
                        tom: '2016-01-20'
                    },
                    nedsattMed50: {
                        from: '2016-01-21',
                        tom: '2016-02-20'
                    },
                    nedsattMed75: {
                        from: '2016-02-21',
                        tom: '2016-03-20'
                    },
                    nedsattMed100: {
                        from: '2016-03-21',
                        tom: '2016-04-20'
                    }
        };
    },
    prognos:function(){
        return {
                    choice: {
                        // JA:1,
                        // JA_DELVIS:1,
                        // NEJ:1,
                        GAR_EJ_ATT_BEDOMA:1
                    },
                    fortydligande: 'Fortydligande text'
                };
    },
    atgarder:function(smittskydd){
        if (smittskydd) {return false;}
        return {
                    planerad: 'Planerad eller pågående behandling text',
                    annan: 'Annan åtgärd text'
                };
    },
    rekommendationer:function(){
        return {
                    resor:true,
                    kontaktMedArbetsformedlingen:true,
                    kontaktMedForetagshalsovard: true,
                    Ovrigt:'Rekomendation övrigt text',
                    arbetslivsinriktadRehab: 'Går ej att bedöma'
                };
    },
    kontaktOnskasMedFK:function(){
        return true;
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
                rekommendationer:random.rekommendationer(),
                kontaktOnskasMedFK:random.kontaktOnskasMedFK(),
                ovrigaUpplysningar: random.ovrigaUpplysningar()
            };
        }
    }
};
