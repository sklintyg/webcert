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

/* globals logger, pages, Promise, wcTestTools */

'use strict';

var lusePage = pages.intyg.luse.intyg;
var testdataHelper = wcTestTools.helpers.testdata;


function checkBaseratPa(baseratPa) {
    var minUndersokningText = testdataHelper.dateToText((baseratPa.minUndersokningAvPatienten));
    var journaluppgifterText = testdataHelper.dateToText((baseratPa.journaluppgifter));
    var anhorigBeskrivningText = testdataHelper.dateToText((baseratPa.anhorigsBeskrivning));
    var annatText = testdataHelper.dateToText((baseratPa.annat));
    var annatBeskrivningText = testdataHelper.ejAngivetIfNull(baseratPa.annatBeskrivning);
    var personligKannedomText = testdataHelper.dateToText((baseratPa.personligKannedom));

    return Promise.all([
        expect(lusePage.baseratPa.minUndersokningAvPatienten.getText()).to.eventually.equal(minUndersokningText),
        expect(lusePage.baseratPa.journaluppgifter.getText()).to.eventually.equal(journaluppgifterText),
        expect(lusePage.baseratPa.anhorigsBeskrivning.getText()).to.eventually.equal(anhorigBeskrivningText),
        expect(lusePage.baseratPa.annat.getText()).to.eventually.equal(annatText),
        expect(lusePage.baseratPa.annatBeskrivning.getText()).to.eventually.equal(annatBeskrivningText),
        expect(lusePage.baseratPa.personligKannedom.getText()).to.eventually.equal(personligKannedomText)
    ]);
}

function checkAndraMedicinskaUtredningar(andraMedicinskaUtredningar) {
    if (andraMedicinskaUtredningar) {
        var promiseArr = [];
        for (var i = 0; i < andraMedicinskaUtredningar.length; i++) {
            var utredningEL = lusePage.andraMedicinskaUtredningar.getUtredning(i);
            var utredningDatum = testdataHelper.dateToText(andraMedicinskaUtredningar[i].datum);
            promiseArr.push(expect(utredningEL.typ.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].underlag));
            promiseArr.push(expect(utredningEL.datum.getText()).to.eventually.equal(utredningDatum));
            promiseArr.push(expect(utredningEL.info.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].infoOmUtredningen));
        }
        return Promise.all(promiseArr);
    } else if (!andraMedicinskaUtredningar) {
        return expect(lusePage.andraMedicinskaUtredningar.field.getText()).to.eventually.contain('Nej');
    }
}

function checkSjukdomsforlopp(forlopp) {
    return expect(lusePage.sjukdomsforlopp.getText()).to.eventually.equal(forlopp);
}

function checkDiagnos(diagnos) {
    var diagnoser = diagnos.diagnoser;
    var nyBedomning = testdataHelper.boolTillJaNej(diagnos.nyBedomning);
    var promiseArr = [];
    for (var i = 0; i < diagnoser.length; i++) {
        promiseArr.push(expect(lusePage.diagnoser.getDiagnos(i).kod.getText()).to.eventually.equal(diagnoser[i].kod));
    }
    promiseArr.push(expect(lusePage.diagnoser.grund.getText()).to.eventually.equal(diagnos.narOchVarStalldesDiagnoserna));
    promiseArr.push(expect(lusePage.diagnoser.nyBedomningDiagnosgrund.getText()).to.eventually.contain(nyBedomning));

    return Promise.all(promiseArr);

}

function checkFunktionsnedsattning(nedsattning) {
    return Promise.all([
        expect(lusePage.funktionsnedsattning.intellektuell.getText()).to.eventually.equal(nedsattning.intellektuell),
        expect(lusePage.funktionsnedsattning.kommunikation.getText()).to.eventually.equal(nedsattning.kommunikation),
        expect(lusePage.funktionsnedsattning.uppmarksamhet.getText()).to.eventually.equal(nedsattning.koncentration),
        expect(lusePage.funktionsnedsattning.annanPsykiskFunktion.getText()).to.eventually.equal(nedsattning.psykisk),
        expect(lusePage.funktionsnedsattning.synHorselTal.getText()).to.eventually.equal(nedsattning.synHorselTal),
        expect(lusePage.funktionsnedsattning.balans.getText()).to.eventually.equal(nedsattning.balansKoordination),
        expect(lusePage.funktionsnedsattning.annanKropsligFunktion.getText()).to.eventually.equal(nedsattning.annan)
    ]);
}

function checkAktivitetsbegransning(begr) {
    return expect(lusePage.aktivitetsbegransning.getText()).to.eventually.equal(begr);
}


function checkMedicinskBehandling(behandl) {
    return Promise.all([
        expect(lusePage.behandling.avslutad.getText()).to.eventually.equal(behandl.avslutad),
        expect(lusePage.behandling.pagaende.getText()).to.eventually.equal(behandl.pagaende),
        expect(lusePage.behandling.planerad.getText()).to.eventually.equal(behandl.planerad),
        expect(lusePage.behandling.substansintag.getText()).to.eventually.equal(behandl.substansintag)
    ]);
}

function checkMedicinskaForutsattningar(forutsattningar) {
    return Promise.all([
        expect(lusePage.medicinskaForutsattningar.kanUtvecklasOverTid.getText()).to.eventually.equal(forutsattningar.utecklasOverTid),
        expect(lusePage.medicinskaForutsattningar.kanGoraTrotsBegransning.getText()).to.eventually.equal(forutsattningar.trotsBegransningar)
    ]);
}

function checkOvrigaUpplysningar(ovriga) {
    return expect(lusePage.ovrigaUpplysningar.getText()).to.eventually.equal(ovriga);
}

function checkKontaktMedFk(kontakt) {
    return expect(lusePage.kontaktFK.onskas.getText()).to.eventually.contain(testdataHelper.boolTillJaNej(kontakt));
}

function checkTillaggsfragor(fragor) {
    var promiseArr = [];

    for (var i = 0; i < fragor.length; i++) {
        promiseArr.push(expect(lusePage.tillaggsfragor.getFraga(i).getText()).to.eventually.equal(fragor[i].svar));
    }

    return Promise.all(promiseArr);
}

module.exports = {
    checkValues: function(intyg, callback) {
        logger.info('-- Kontrollerar Läkarutlåtande för sjukersättning --');
        logger.warn('intyg med typ: ' + intyg.typ + ' saknar vissa funktioner för kontroll av data');

        Promise.all([
                //Baserat på
                checkBaseratPa(intyg.baseratPa)
                .then(function(value) {
                    logger.info('OK - Baseras på');
                }, function(reason) {
                    return Promise.reject('FEL, Baseras på: ' + reason);
                }),

                //Medicinska utredningar
                checkAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
                .then(function(value) {
                    logger.info('OK - Andra medicinska utredningar');
                }, function(reason) {
                    return Promise.reject('FEL, Andra medicinska utredningar: ' + reason);
                }),

                //Sjukdomsförlopp
                checkSjukdomsforlopp(intyg.sjukdomsForlopp)
                .then(function(value) {
                    logger.info('OK - Sjukdomsförlopp');
                }, function(reason) {
                    return Promise.reject('FEL, Sjukdomsförlopp: ' + reason);
                }),

                //Diagnoser
                checkDiagnos(intyg.diagnos)
                .then(function(value) {
                    logger.info('OK - Diagnos');
                }, function(reason) {
                    return Promise.reject('FEL, Diagnos: ' + reason);
                }),

                //Funktionsnedsättning
                checkFunktionsnedsattning(intyg.funktionsnedsattning)
                .then(function(value) {
                    logger.info('OK - Funktionsnedsättning');
                }, function(reason) {
                    return Promise.reject('FEL, Funktionsnedsättning: ' + reason);
                }),

                //Aktivitestbegränsning
                checkAktivitetsbegransning(intyg.aktivitetsbegransning)
                .then(function(value) {
                    logger.info('OK - Aktivitestbegränsning');
                }, function(reason) {
                    return Promise.reject('FEL, Aktivitestbegränsning: ' + reason);
                }),

                //Medicinsk behandling
                checkMedicinskBehandling(intyg.medicinskbehandling)
                .then(function(value) {
                    logger.info('OK - Medicinsk behandling');
                }, function(reason) {
                    return Promise.reject('FEL, Medicinsk behandling: ' + reason);
                }),

                //Medicinska förutsättningar
                checkMedicinskaForutsattningar(intyg.medicinskaForutsattningar)
                .then(function(value) {
                    logger.info('OK - Medicinska förutsättningar');
                }, function(reason) {
                    return Promise.reject('FEL, Medicinska förutsättningar: ' + reason);
                }),

                //Övriga upplysningar
                checkOvrigaUpplysningar(intyg.ovrigt)
                .then(function(value) {
                    logger.info('OK - Övriga upplysningar');
                }, function(reason) {
                    return Promise.reject('FEL, Övriga upplysningar: ' + reason);
                }),

                //Kontakt med FK
                checkKontaktMedFk(intyg.kontaktMedFk)
                .then(function(value) {
                    logger.info('OK - Kontakt med FK');
                }, function(reason) {
                    return Promise.reject('FEL, Kontakt med FK: ' + reason);
                }),

                //Tilläggsfrågor
                checkTillaggsfragor(intyg.tillaggsfragor)
                .then(function(value) {
                    logger.info('OK - Tilläggsfrågor');
                }, function(reason) {
                    return Promise.reject('FEL, Tilläggsfrågor: ' + reason);
                })

            ])
            .then(function(value) {
                logger.info('Alla kontroller utförda OK:' + value);
                callback();
            }, function(reason) {
                callback(reason);
            });
    }
};
