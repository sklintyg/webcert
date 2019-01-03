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

/* globals logger, pages, JSON, browser, Promise */

'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
module.exports = {
    fillIn: function(intyg) {
        return new Promise(function(resolve) {
                logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
                browser.ignoreSynchronization = true;
                resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
            })
            .then(function() {
                //Ange smittskydd
                return fkUtkastPage.angeSmittskydd(intyg.smittskydd).then(function() {
                    return logger.info('OK - angeSmittskydd :' + intyg.smittskydd);
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeSmittskydd, ' + intyg.smittskydd + ' , ' + reason);
                });
            })
            .then(function() {
                //Ange baseras på
                return fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa).then(function() {
                    return logger.info('OK - angeIntygetBaserasPa :' + JSON.stringify(intyg.baserasPa));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeIntygetBaserasPa,' + reason);
                });
            })
            .then(function() {
                //Ange funktionsnedsättning
                return fkUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning).then(function() {
                    return logger.info('OK - angeFunktionsnedsattning :' + JSON.stringify(intyg.funktionsnedsattning));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeFunktionsnedsattning,' + reason);
                });
            })
            .then(function() {
                //Ange diagnoser
                return fkUtkastPage.angeDiagnoser(intyg.diagnos).then(function() {
                    return logger.info('OK - angeDiagnoser :' + JSON.stringify(intyg.diagnos));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeDiagnoser,' + reason);
                });
            })
            .then(function() {
                //Ange aktuellt sjukdomsförlopp
                return fkUtkastPage.angeAktuelltSjukdomsForlopp(intyg.aktuelltSjukdomsforlopp).then(function() {
                    return logger.info('OK - angeAktuelltSjukdomsForlopp :' + JSON.stringify(intyg.aktuelltSjukdomsforlopp));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeAktuelltSjukdomsForlopp,' + reason);
                });
            })
            .then(function() {
                //Ange aktivitetsbegränsning
                return fkUtkastPage.angeAktivitetsBegransning(intyg.aktivitetsBegransning).then(function() {
                    return logger.info('OK - angeAktivitetsBegransning :' + JSON.stringify(intyg.aktivitetsBegransning));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeAktivitetsBegransning,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeArbete(intyg.arbete).then(function() {
                    return logger.info('OK - angeArbete :' + JSON.stringify(intyg.arbete));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeArbete,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function() {
                    return logger.info('OK - angeArbetsformaga :' + JSON.stringify(intyg.arbetsformaga));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeArbetsformaga,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeArbetsformagaFMB(intyg.arbetsformagaFMB).then(function() {
                    return logger.info('OK - angeArbetsformagaFMB :' + JSON.stringify(intyg.arbetsformagaFMB));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeArbetsformagaFMB,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angePrognos(intyg.prognos).then(function() {
                    return logger.info('OK - angePrognos :' + JSON.stringify(intyg.prognos));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angePrognos, value:' + JSON.stringify(intyg.prognos) + ' ,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeKontaktOnskasMedFK(intyg.kontaktOnskasMedFK).then(function() {
                    return logger.info('OK - angeKontaktOnskasMedFK :' + JSON.stringify(intyg.kontaktOnskasMedFK));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeKontaktOnskasMedFK,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeRekommendationer(intyg.rekommendationer).then(function() {
                    return logger.info('OK - angeRekommendationer :' + JSON.stringify(intyg.rekommendationer));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeRekommendationer,' + reason);
                });
            })
            .then(function() {
                return fkUtkastPage.angeFaktiskTjanstgoring('37,5').then(function() {
                    return logger.info('OK - angeFaktiskTjanstgoring :' + JSON.stringify('37,5'));
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeFaktiskTjanstgoring,' + reason);
                });
            });
    }
};
