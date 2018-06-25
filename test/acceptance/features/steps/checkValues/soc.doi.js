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

/* globals logger, pages, Promise */

'use strict';

const doiPage = pages.intyg.soc.doi.intyg;
const verifyDBDOI = require('./db.doi.common.js');

function verifyTerminalDodsorsak(data) {
    return Promise.all([
        expect(doiPage.terminalDodsorsak.getText()).to.eventually.contain(data.dodsorsak.a.tillstandSpec),
        expect(doiPage.terminalDodsorsak.getText()).to.eventually.contain(data.dodsorsak.a.beskrivning),
        expect(doiPage.terminalDodsorsak.getText()).to.eventually.contain(data.dodsorsak.a.datum)
    ]);
}

function verifyOperation(data) {
    let promiseArr = [];
    if (data.operation.ja) {
        promiseArr.push(expect(doiPage.operation.val.getText()).to.eventually.equal('Ja'));
        promiseArr.push(expect(doiPage.operation.datum.getText()).to.eventually.equal(data.operation.ja.datum));
        promiseArr.push(expect(doiPage.operation.beskrivning.getText()).to.eventually.equal(data.operation.ja.beskrivning));
    } else {
        promiseArr.push(expect(doiPage.operation.val.getText()).to.eventually.equal(data.operation));
    }
    return Promise.all(promiseArr);
}

function verifySkadaForgiftning(data) {
    let promiseArr = [];

    if (data.skadaForgiftning === true) {
        promiseArr.push(expect(doiPage.skadaForgiftning.val.getText()).to.eventually.equal('Ja'));
        promiseArr.push(expect(doiPage.skadaForgiftning.orsak.getText()).to.eventually.equal(data.skadaForgiftning.orsakAvsikt));
        promiseArr.push(expect(doiPage.skadaForgiftning.datum.getText()).to.eventually.equal(data.skadaForgiftning.datum));
        promiseArr.push(expect(doiPage.skadaForgiftning.beskrivning.getText()).to.eventually.equal(data.skadaForgiftning.beskrivning));
    } else {
        promiseArr.push(expect(doiPage.skadaForgiftning.val.getText()).to.eventually.equal('Nej'));
    }
    return Promise.all(promiseArr);
}

function verifyGrunder(data) {
    let promiseArr = [];
    Object.keys(data.dodsorsaksuppgifter).forEach(grund => {
        if (data.dodsorsaksuppgifter[grund] !== false) {
            promiseArr.push(expect(doiPage.grunderLista.getText()).to.eventually.contain(data.dodsorsaksuppgifter[grund]));
        }
    });
    return Promise.all(promiseArr);
}

module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Dödsorsaksintyg --');
        let data = intyg;
        logger.silly(JSON.stringify(intyg));

        return new Promise(resolve => resolve())
            .then(verifyDBDOI.identitetenStyrkt(data).then(value => {
                logger.info('OK - identitetenStyrkt');
            }, reason => {
                console.trace(reason);
                throw ('FEL, identitetenStyrkt: ' + reason);
            }))
            .then(() => verifyDBDOI.dodsdatum(data).then(value => {
                logger.info('OK - Dödsdatum');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Dödsdatum: ' + reason);
            }))
            .then(() => verifyDBDOI.dodsplats(data).then(value => {
                logger.info('OK - Dödsplats');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Dödsplats: ' + reason);
            }))
            .then(() => verifyDBDOI.barn(data).then(value => {
                logger.info('OK - Barn');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Barn: ' + reason);
            }))
            .then(() => verifyTerminalDodsorsak(data).then(value => {
                logger.info('OK - TerminalDödsorsak');
            }, reason => {
                console.trace(reason);
                throw ('FEL, TerminalDödsorsak: ' + reason);
            }))
            .then(() => verifyOperation(data).then(value => {
                logger.info('OK - Operation');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Operation: ' + reason);
            }))
            .then(() => verifySkadaForgiftning(data).then(value => {
                logger.info('OK - Skada/förgiftning');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Skada/förgiftning: ' + reason);
            }))
            .then(() => verifyGrunder(data).then(value => {
                logger.info('OK - Grunder');
            }, reason => {
                console.trace(reason);
                throw ('FEL, Grunder: ' + reason);
            }));
    }
};
