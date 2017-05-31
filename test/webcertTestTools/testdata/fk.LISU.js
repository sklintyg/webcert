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
    var randomLength = Math.floor(Math.random() * atgarder.length) + 1;

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
    getRandom: function(intygsID) {
        var arbetsformaga = fkValues.getRandomArbetsformaga();
        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }
        return {
            id: intygsID,
            typ: 'Läkarintyg för sjukpenning',

            nuvarandeArbeteBeskrivning: testdataHelper.randomTextString(),
            baseratPa: {
                undersokning: today,
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
            funktionsnedsattning: testdataHelper.randomTextString(),
            aktivitetsbegransning: testdataHelper.randomTextString(),
            arbetsformaga: arbetsformaga,
            arbetstidsforlaggning: getRandomArbetstidsforlaggning(arbetsformaga),
            arbetsformagaFMB: testdataHelper.randomTextString(),
            resorTillArbete: shuffle([true, false])[0],
            goraTrotsSjukdom: testdataHelper.randomTextString(),
            // sannolikhetAtergangTillArbete: getRandomSannolikhetAtergang(),
            atgarder: getRandomAtgarder(),
            prognosForArbetsformaga: getRandomPrognosForArbetsformaga()
        };
    }
};
