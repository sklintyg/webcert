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

/* globals pages, Promise, logger */

'use strict';
let smipage = pages.intyg.smi.intyg;

module.exports = {
    baseratPa: function(data) {

        let promiseArr = [];
        if (data.baseratPa.minUndersokningAvPatienten) {
            promiseArr.push(expect(smipage.baseratPa.minUndersokningAvPatienten.getText()).to.eventually.equal(data.baseratPa.minUndersokningAvPatienten));
        }

        if (data.baseratPa.journaluppgifter) {
            promiseArr.push(expect(smipage.baseratPa.journaluppgifter.getText()).to.eventually.equal(data.baseratPa.journaluppgifter));
        }

        if (data.baseratPa.telefonkontakt) {
            promiseArr.push(expect(smipage.baseratPa.telefonkontakt.getText()).to.eventually.equal(data.baseratPa.telefonkontakt));
        }

        if (data.baseratPa.annat) {
            promiseArr.push(expect(smipage.baseratPa.annat.getText()).to.eventually.equal(data.baseratPa.annat));
        }

        if (data.baseratPa.annatBeskrivning) {
            promiseArr.push(expect(smipage.baseratPa.annatBeskrivning.getText()).to.eventually.equal(data.baseratPa.annatBeskrivning));
        }

        if (data.baseratPa.personligKannedom) {
            promiseArr.push(expect(smipage.baseratPa.personligKannedom.getText()).to.eventually.equal(data.baseratPa.personligKannedom));
        }

        if (data.baseratPa.anhorigsBeskrivning) {
            promiseArr.push(expect(smipage.baseratPa.anhorigsBeskrivning.getText()).to.eventually.equal(data.baseratPa.anhorigsBeskrivning));
        }
        logger.silly('Baserat på: ' + promiseArr.length + ' assertions made');
        return Promise.all([promiseArr]);
    },
    diagnos: function(data) {
        let promiseArr = [];
        if (data.diagnos.narOchVarStalldesDiagnoserna) {
            promiseArr.push(expect(smipage.diagnoser.grund.getText()).to.eventually.equal(data.diagnos.narOchVarStalldesDiagnoserna));
        }

        promiseArr.push(expect(smipage.diagnoser.getDiagnos(0).kod.getText()).to.eventually.equal(data.diagnos.kod));

        if (data.diagnos.beskrivning) {
            promiseArr.push(expect(smipage.diagnoser.getDiagnos(0).beskrivning.getText()).to.eventually.equal(data.diagnos.beskrivning));
        }

        logger.silly('Diagnos' + promiseArr.length + ' assertions made');
        return Promise.all([promiseArr]);
    },
    aktivitetsbegransning: function(data) {
        return expect(smipage.aktivitetsbegransning.getText()).to.eventually.equal(data.aktivitetsbegransning);
    },
    medicinskbehandling: function(data) {
        let promiseArr = [];
        if (data.medicinskbehandling) {
            if (data.medicinskbehandling.avslutad) {
                promiseArr.push(expect(smipage.behandling.avslutad.getText()).to.eventually.equal(data.medicinskbehandling.avslutad));
            }
            if (data.medicinskbehandling.pagaende) {
                promiseArr.push(expect(smipage.behandling.pagaende.getText()).to.eventually.equal(data.medicinskbehandling.pagaende));
            }
            if (data.medicinskbehandling.planerad) {
                promiseArr.push(expect(smipage.behandling.planerad.getText()).to.eventually.equal(data.medicinskbehandling.planerad));
            }
            if (data.medicinskbehandling.substansintag) {
                promiseArr.push(expect(smipage.behandling.substansintag.getText()).to.eventually.equal(data.medicinskbehandling.substansintag));
            }
        }
        logger.silly('Medicinskbehandling: ' + promiseArr.length + ' assertions made');
        return Promise.all([promiseArr]);
    }

};
