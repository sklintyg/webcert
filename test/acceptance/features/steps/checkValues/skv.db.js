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

/* globals logger, Promise, pages, wcTestTools */

'use strict';

const dbPage = pages.intyg.skv.db.intyg;
const testdataHelper = wcTestTools.helpers.testdata;
const verifyDBDOI = require('./db.doi.common.js');

function verifyExplosivImplantat(data) {
    return Promise.all([
        expect(dbPage.explosivImplantat.value.getText()).to.eventually.equal(testdataHelper.boolTillJaNej(data.explosivImplantat)),
        expect(dbPage.explosivImplantat.avlagsnat.getText()).to.eventually.equal(data.explosivImplantat ? testdataHelper.boolTillJaNej(data.explosivImplantat.avlagsnat) : testdataHelper.ejAngivetIfNull(data.explosivImplantat.avlagsnat))
    ]);
}

function verifyYttreundersokning(data) {
    return Promise.all([
        expect(dbPage.yttreUndersokning.value.getText()).to.eventually.equal(data.yttreUndersokning.value),
        expect(dbPage.yttreUndersokning.datum.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull(data.yttreUndersokning.datum))
    ]);
}

function verifyPolisanmalan(data) {
    if (data.yttreUndersokning.value === 'Nej, rättsmedicinsk undersökning ska göras') {
        data.polisanmalan = 'Ja';
    }

    return Promise.all([
        expect(dbPage.polisanmalan.getText()).to.eventually.contain(testdataHelper.boolTillJaNej(data.polisanmalan))
    ]);
}

module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Dödsbevis --');
        logger.silly(JSON.stringify(intyg));

        let data = intyg;

        return verifyDBDOI.identitetenStyrkt(data).then(value => {
                logger.info('OK - identitetenStyrkt');
            }, reason => {
                throw ('FEL, identitetenStyrkt: ' + reason);
            })
            .then(() => verifyDBDOI.dodsdatum(data))
            .then(value => {
                logger.info('OK - Dödsdatum');
            }, reason => {
                throw ('FEL, Dödsdatum: ' + reason);
            })
            .then(() => verifyDBDOI.dodsplats(data))
            .then(value => {
                logger.info('OK - Dödsplats');
            }, reason => {
                throw ('FEL, Dödsplats: ' + reason);
            })
            .then(() => verifyDBDOI.barn(data))
            .then(value => {
                logger.info('OK - Barn');
            }, reason => {
                throw ('FEL, Barn: ' + reason);
            })
            .then(() => verifyExplosivImplantat(data))
            .then(value => {
                logger.info('OK - ExplosivImplantat');
            }, reason => {
                throw ('FEL, ExplosivImplantat: ' + reason);
            })
            .then(() => verifyYttreundersokning(data))
            .then(value => {
                logger.info('OK - Yttreundersökning');
            }, reason => {
                throw ('FEL, Yttreundersökning: ' + reason);
            })
            .then(() => verifyPolisanmalan(data))
            .then(value => {
                logger.info('OK - Polisanmälan');
            }, reason => {
                throw ('FEL, Polisanmälan: ' + reason);
            });
        //polisanmälan
    }
};
