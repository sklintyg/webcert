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

/* globals logger, pages, Promise, wcTestTools */

'use strict';

var testdataHelper = wcTestTools.helpers.testdata;
var lisjpPage = pages.intyg.lisjp.intyg;
let checkSMICommon = require('./smi.common.js');


function verifyFunktionsnedsattning(nedsattning) {
    return expect(lisjpPage.funktionsnedsattning.getText()).to.eventually.equal(nedsattning);
}

function verifySmittskydd(data) {
    return expect(lisjpPage.smittskydd.getText()).to.eventually.equal(data.smittskydd);
}

function verifySysselsattning(data) {
    let promiseArr = [];

    if (data.sysselsattning.yrkesAktiviteter) {
        promiseArr.push(expect(lisjpPage.sysselsattning.yrkesAktiviteter.getText()).to.eventually.contain(data.sysselsattning.yrkesAktiviteter));
    }

    promiseArr.push(expect(lisjpPage.sysselsattning.list.getText()).to.eventually.contain(data.sysselsattning.typ));


    logger.silly('Sysselsattning: ' + promiseArr.length + ' assertions made');
    return Promise.all([promiseArr]);
}

function verifyArbetsformaga(data) {
    var promiseArr = [];
    var formagor = [];

    if (data.arbetsformaga.nedsattMed100) {
        formagor.push(data.arbetsformaga.nedsattMed100);
    }

    if (data.arbetsformaga.nedsattMed75) {
        formagor.push(data.arbetsformaga.nedsattMed75);
    }

    if (data.arbetsformaga.nedsattMed50) {
        formagor.push(data.arbetsformaga.nedsattMed50);
    }

    if (data.arbetsformaga.nedsattMed25) {
        formagor.push(data.arbetsformaga.nedsattMed25);
    }

    for (var i = 0; i < formagor.length; i++) {
        promiseArr.push(expect(lisjpPage.sjukskrivningar.from(i).getText()).to.eventually.equal(formagor[i].from));
        promiseArr.push(expect(lisjpPage.sjukskrivningar.to(i).getText()).to.eventually.equal(formagor[i].tom));
    }

    logger.silly('Arbetsformaga : ' + promiseArr.length + ' assertions made');
    return Promise.all([promiseArr]);
}

function verifyArbetsformagaFMB(data) {
    return expect(lisjpPage.arbetsformagaFMB.getText()).to.eventually.equal(data.arbetsformagaFMB);
}

function verifyArbetstidsforlaggning(data) {
    let promiseArr = [];

    //@WaitingForFix: Intygsspecifika krav LISJP - SK-002
    //promiseArr.push(expect(lisjpPage.arbetstidsforlaggning.val.getText()).to.eventually.equal(data.arbetstidsforlaggning.val));
    promiseArr.push(expect(lisjpPage.arbetstidsforlaggning.motivering.getText()).to.eventually.equal(data.arbetstidsforlaggning.beskrivning));

    logger.silly('Arbetstidsforlaggning : ' + promiseArr.length + ' assertions made');
    return Promise.all([promiseArr]);
}

function verifyResorTillArbete(data) {

    return expect(lisjpPage.resorTillArbete.getText()).to.eventually.equal(data.resorTillArbete);
}

function verifyPrognosForArbetsformaga(data) {
    let promiseArr = [];

    promiseArr.push(expect(lisjpPage.prognosForArbetsformaga.getText()).to.eventually.equal(data.prognosForArbetsformaga.name));

    if (data.prognosForArbetsformaga.within) {
        promiseArr.push(expect(lisjpPage.prognosForArbetsformaga.getText()).to.eventually.equal(data.prognosForArbetsformaga));
    }
    return Promise.all([promiseArr]);
}


function forkLisjpIntyg(intyg) {
    let data = Object.create(intyg);

    data.baseratPa.minUndersokningAvPatienten = testdataHelper.ejAngivetIfNull(intyg.baseratPa.minUndersokningAvPatienten);
    data.baseratPa.telefonkontakt = testdataHelper.ejAngivetIfNull(intyg.baseratPa.telefonkontakt);
    data.baseratPa.journaluppgifter = testdataHelper.ejAngivetIfNull(intyg.baseratPa.journaluppgifter);
    data.baseratPa.annat = testdataHelper.ejAngivetIfNull(intyg.baseratPa.annat);
    data.baseratPa.annatBeskrivning = testdataHelper.ejAngivetIfNull(intyg.baseratPa.annatBeskrivning);

    data.smittskydd = testdataHelper.ejAngivetIfNull(intyg.smittskydd);

    switch (data.sysselsattning.typ) {
        case 'NUVARANDE_ARBETE':
            data.sysselsattning.typ = 'Nuvarande arbete';
            break;
        case 'ARBETSSOKANDE':
            data.sysselsattning.typ = 'Arbetssökande - att utföra sådant arbete som är normalt förekommande på arbetsmarknaden';
            break;
        case 'FORALDRALEDIG':
            data.sysselsattning.typ = 'Föräldraledighet för vård av barn';
            break;
        case 'STUDIER':
            data.sysselsattning.typ = 'Studier';
            break;
    }

    data.arbetsformagaFMB = testdataHelper.ejAngivetIfNull(intyg.arbetsformagaFMB);

    data.arbetstidsforlaggning = testdataHelper.ejAngivetIfNull(intyg.arbetstidsforlaggning);
    data.arbetstidsforlaggning.beskrivning = testdataHelper.ejAngivetIfNull(intyg.arbetstidsforlaggning.beskrivning);

    data.resorTillArbete = testdataHelper.boolTillJaEllerEjAngivet(intyg.resorTillArbete);
    data.prognosForArbetsformaga = testdataHelper.ejAngivetIfNull(intyg.prognosForArbetsformaga);
    switch (data.prognosForArbetsformaga.name) {
        case 'STOR_SANNOLIKHET':
            data.prognosForArbetsformaga = 'Patienten kommer med stor sannolikhet att kunna återgå helt i nuvarande sysselsättning efter denna sjukskrivning.';
            break;
        case 'SANNOLIKT_INTE':
            data.prognosForArbetsformaga = 'Patienten kommer med stor sannolikhet inte att kunna återgå helt i nuvarande sysselsättning inom 12 månader.';
            break;
        case 'PROGNOS_OKLAR':
            data.prognosForArbetsformaga = '';
            break;
    }

    //TODO HANDLE WITHIN
    //            name: 'ATER_X_ANTAL_DGR',
    //           within: shuffle(['1 månad', '2 månader', '3 månader'])[0]


    return data;
}



module.exports = {
    checkValues: function(intyg, callback) {
        logger.info('-- Kontrollerar Läkarintyg för sjukpenning --');

        let data = forkLisjpIntyg(intyg);

        return verifySmittskydd(data).then(function(value) {
                logger.info('OK - Smittskydd');
            }, function(reason) {
                throw ('FEL, Smittskydd: ' + reason);
            })
            .then(checkSMICommon.baseratPa(data))
            .then(value => {
                logger.info('OK - Baseras på');
            }, function(reason) {
                throw ('FEL, Baseras på: ' + reason);
            })
            .then(checkSMICommon.diagnos(data))
            .then(value => {
                logger.info('OK - Diagnos');
            }, function(reason) {
                throw ('FEL, Diagnos: ' + reason);
            })
            .then(verifySysselsattning(data))
            .then(value => {
                logger.info('OK - Sysselsättning');
            }, function(reason) {
                throw ('FEL, Sysselsättning: ' + reason);
            })
            .then(verifyFunktionsnedsattning(data.funktionsnedsattning))
            .then(value => {
                logger.info('OK - Funktionsnedsättning');
            }, function(reason) {
                throw ('FEL, Funktionsnedsättning: ' + reason);
            })
            .then(checkSMICommon.aktivitetsbegransning(data))
            .then(value => {
                logger.info('OK - Aktivitestbegränsning');
            }, function(reason) {
                throw ('FEL, Aktivitestbegränsning: ' + reason);
            })
            .then(checkSMICommon.medicinskbehandling(data))
            .then(value => {
                logger.info('OK - Medicinskbehandling');
            }, function(reason) {
                throw ('FEL, Medicinskbehandling: ' + reason);
            })
            .then(verifyArbetsformaga(data))
            .then(value => {
                logger.info('OK - Arbetsformaga');
            }, function(reason) {
                throw ('FEL, Arbetsformaga: ' + reason);
            })
            .then(verifyArbetsformagaFMB(data))
            .then(value => {
                logger.info('OK - ArbetsformagaFMB');
            }, function(reason) {
                throw ('FEL, ArbetsformagaFMB: ' + reason);
            })
            .then(verifyArbetstidsforlaggning(data))
            .then(value => {
                logger.info('OK - Arbetstidsforlaggning');
            }, function(reason) {
                throw ('FEL, Arbetstidsforlaggning: ' + reason);
            })
            .then(verifyResorTillArbete(data))
            .then(value => {
                logger.info('OK - ResorTillArbete');
            }, function(reason) {
                throw ('FEL, ResorTillArbete: ' + reason);
            })
            .then(verifyPrognosForArbetsformaga(data))
            .then(value => {
                logger.info('OK - PrognosForArbetsformaga');
            }, function(reason) {
                throw ('FEL, PrognosForArbetsformaga: ' + reason);
            });


        //atgarder
        //övriga upplysningar  innan / efter pagereload
        //kontaktMedFk
    }
};
