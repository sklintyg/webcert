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
var fkValues = require('./testvalues.js').fk;
var today = testdataHelper.dateFormat(new Date());

function getRandomSysselsattning() {
    return shuffle([{
        typ: 'Nuvarande arbete',
        yrkesAktiviteter: testdataHelper.randomTextString()
    }, {
        typ: 'Arbetssökande'
    }, {
        typ: 'Föräldraledighet för vård av barn'
    }, {
        typ: 'Studier'
    }])[0];
}

function getRandomSannolikhetAtergang() {
    return shuffle([{
        bedomning: 'Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning efter denna sjukskrivning'
    }, {
        bedomning: 'Patienten kan sannolikt inte återgå i nuvarande sysselsättning'
    }, {
        bedomning: 'Prognos för återgång i nuvarande sysselsättning är oklar'
    }, {
        bedomning: 'Patienten kommer med stor sannolikhet att återgå helt i nuvarande sysselsättning efter x antal dagar',
        antalDagar: shuffle([30, 60, 90, 180])[0]
    }])[0];
}

function getRandomArbetstidsforlaggning(arbetsformaga) {
    if (arbetsformaga.nedsattMed25 || arbetsformaga.nedsattMed50 || arbetsformaga.nedsattMed75) {
        return shuffle([{
            val: 'Ja',
            beskrivning: testdataHelper.randomTextString()
        }, {
            val: 'Nej'
        }])[0];
    }
    return null;
}

function getRandomAtgarder() {
    var atgarder = [{
        namn: 'Arbetsträning',
        beskrivning: 'Arbetsträning-beskrivning',
        key: 'ARBETSTRANING'
    }, {
        namn: 'Arbetsanspassning',
        beskrivning: 'Arbetsanspassning-beskrivning',
        key: 'ARBETSANPASSNING'
    }, {
        namn: 'Söka nytt arbete',
        beskrivning: 'Söka nytt arbete-beskrivning',
        key: 'SOKA_NYTT_ARBETE'
    }, {
        namn: 'Besök på arbetsplatsen',
        beskrivning: 'Besök på arbetsplatsen-beskrivning',
        key: 'BESOK_ARBETSPLATS'
    }, {
        namn: 'Ergonomisk bedömning',
        beskrivning: 'Ergonomisk bedömning-beskrivning',
        key: 'ERGONOMISK'
    }, {
        namn: 'Hjälpmedel',
        beskrivning: 'Hjälpmedel-beskrivning',
        key: 'HJALPMEDEL'
    }, {
        namn: 'Konflikthantering',
        beskrivning: 'Konflikthantering-beskrivning',
        key: 'KONFLIKTHANTERING'
    }, {
        namn: 'Kontakt med företagshälsovård',
        beskrivning: 'Kontakt med företagshälsovård-beskrivning',
        key: 'KONTAKT_FHV'
    }, {
        namn: 'Omfördelning av arbetsuppgifter',
        beskrivning: 'Omfördelning av arbetsuppgifter-beskrivning',
        key: 'OMFORDELNING'
    }, {
        namn: 'Övrigt',
        beskrivning: 'Övrigt-beskrivning',
        key: 'OVRIGA_ATGARDER'
    }];
    var randomLength = Math.floor(Math.random() * 3) + 1;

    // 33% chans för inte aktuellt
    return shuffle([
        [{
            namn: 'Inte aktuellt'
        }], shuffle(atgarder).slice(0, randomLength),
        shuffle(atgarder).slice(0, randomLength)
    ])[0];
}

function getRandomPrognosForArbetsformaga() {
    return shuffle([{
        name: 'Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning efter denna sjukskrivning.'
    }, {
        name: 'Patienten kommer med stor sannolikhet inte att kunna återgå helt i nuvarande sysselsättning inom 12 månader.'
    }, {
        name: 'Återgång i nuvarande sysselsättning är oklar.'
    }, {
        name: 'Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning inom',
        within: shuffle(['1 månad', '2 månader', '3 månader'])[0]
    }])[0];
}

module.exports = {
    get: function(intygsID, smittskydd) {
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        if (smittskydd) {
            return {"id":intygsID,"typ":"Läkarintyg för sjukpenning","smittskydd":true,
                "baseratPa":{"minUndersokningAvPatienten":"2017-09-27","journaluppgifter":"2017-09-27","telefonkontakt":"2017-09-27","annat":"2017-09-27","annatBeskrivning":"tFEöSdJD1yVrIq5D"},
                "diagnos":{"kod":"D00","bakgrund":"cÅtHIlxiS0c1öTÅö"},
                "arbetsformaga":{"nedsattMed25":{"from":"2017-09-27","tom":"2017-10-02"},"nedsattMed50":{"from":"2017-10-03","tom":"2017-10-07"},"nedsattMed75":{"from":"2017-10-08","tom":"2017-10-17"},"nedsattMed100":{"from":"2017-10-18","tom":"2017-10-27"}},
                "arbetstidsforlaggning":{"val":"Nej"},"ovrigt":"EhRgOSC9psäJcÅjt"};
        }
        return {"id":intygsID,"typ":"Läkarintyg för sjukpenning","smittskydd":false,"nuvarandeArbeteBeskrivning":"iÅI3WYZMFe8ÖV5Ox",
            "baseratPa":{"minUndersokningAvPatienten":"2017-09-27","journaluppgifter":"2017-09-27","telefonkontakt":"2017-09-27","annat":"2017-09-27","annatBeskrivning":"klHh4TcWVGHÖQw8K"},
            "sysselsattning":{"typ":"Arbetssökande"},
            "diagnos":{"kod":"Z413","bakgrund":"BRBQOdppäJEBbÖ0i"},
            "medicinskbehandling":{"planerad":"IawIÖIdU8sCfä7Uq","pagaende":"cnEYDuNuqWBd59åY"},
            "funktionsnedsattning":"3wexbyEqÄsoxaWWl","aktivitetsbegransning":"Wpnv0SMoElXnTJXh",
            "arbetsformaga":{"nedsattMed25":{"from":"2017-09-27","tom":"2017-10-02"},"nedsattMed50":{"from":"2017-10-03","tom":"2017-10-07"},"nedsattMed75":{"from":"2017-10-08","tom":"2017-10-17"},"nedsattMed100":{"from":"2017-10-18","tom":"2017-10-27"}},
            "arbetstidsforlaggning":{"val":"Ja","beskrivning":"pB8TPLe3JZÄSV0YN"},
            "arbetsformagaFMB":"WewXÄUgv06gåOmÄH","resorTillArbete":true,
            "atgarder":[{"namn":"Besök på arbetsplatsen","beskrivning":"Besök på arbetsplatsen-beskrivning","key":"BESOK_ARBETSPLATS"},{"namn":"Arbetsträning","beskrivning":"Arbetsträning-beskrivning","key":"ARBETSTRANING"},{"namn":"Kontakt med företagshälsovård","beskrivning":"Kontakt med företagshälsovård-beskrivning","key":"KONTAKT_FHV"}],
            "prognosForArbetsformaga":{"name":"Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning efter denna sjukskrivning."},
            "kontaktMedFk":false,
            "ovrigt":"iÄ0f0cÖ7NY0JEa1Ä",
            "tillaggsfragor":[{"id":9001,"svar":"iQ0zn6Yrh7zYSjGI"},{"id":9002,"svar":"vZMnfG0Z6jäscuY2"}]};
    },
    getRandom: function(intygsID, smittskydd) {
        var arbetsformaga = fkValues.getRandomArbetsformaga();
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        if (smittskydd) {
            return {
                id: intygsID,
                typ: 'Läkarintyg för sjukpenning',

                smittskydd: true,
                baseratPa: {
                    minUndersokningAvPatienten: today,
                    journaluppgifter: today,
                    telefonkontakt: today,
                    annat: today,
                    annatBeskrivning: testdataHelper.randomTextString()
                },

                diagnos: {
                    kod: shuffle(fkValues.ICD10)[0],
                    bakgrund: testdataHelper.randomTextString()
                },
                arbetsformaga: arbetsformaga,
                arbetstidsforlaggning: getRandomArbetstidsforlaggning(arbetsformaga),
                ovrigt: testdataHelper.randomTextString()
            };
        }

        return {
            id: intygsID,
            typ: 'Läkarintyg för sjukpenning',

            smittskydd: false,
            nuvarandeArbeteBeskrivning: testdataHelper.randomTextString(),
            baseratPa: {
                minUndersokningAvPatienten: today,
                journaluppgifter: today,
                telefonkontakt: today,
                annat: today,
                annatBeskrivning: testdataHelper.randomTextString()
            },

            sysselsattning: getRandomSysselsattning(),
            diagnos: {
                kod: shuffle(fkValues.ICD10)[0],
                bakgrund: testdataHelper.randomTextString()
            },
            medicinskbehandling: {
                planerad: testdataHelper.randomTextString(),
                pagaende: testdataHelper.randomTextString()
            },
            funktionsnedsattning: testdataHelper.randomTextString(),
            aktivitetsbegransning: testdataHelper.randomTextString(),
            arbetsformaga: arbetsformaga,
            arbetstidsforlaggning: getRandomArbetstidsforlaggning(arbetsformaga),
            arbetsformagaFMB: testdataHelper.randomTextString(),
            resorTillArbete: testdataHelper.randomTrueFalse(),
            atgarder: getRandomAtgarder(),
            prognosForArbetsformaga: getRandomPrognosForArbetsformaga(),
            kontaktMedFk: testdataHelper.randomTrueFalse(),
            ovrigt: testdataHelper.randomTextString(),
            tillaggsfragor: [
                {
                    id: 9001,
                    svar: testdataHelper.randomTextString()
                }, {
                    id: 9002,
                    svar: testdataHelper.randomTextString()
                }
            ]
        };
    }
};
