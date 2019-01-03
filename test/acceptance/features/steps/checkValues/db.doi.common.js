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

/* globals pages, Promise, wcTestTools */

'use strict';
const dbpage = pages.intyg.skv.db.intyg;
const testdataHelper = wcTestTools.helpers.testdata;

module.exports = {
    identitetenStyrkt: data => {
        return expect(dbpage.identitetStyrkt.getText()).to.eventually.equal(data.identitetStyrktGenom);
    },
    dodsdatum: data => {
        let dodsdatum = data.dodsdatum.sakert ? data.dodsdatum.sakert.datum : data.dodsdatum.inteSakert.year + '-' + data.dodsdatum.inteSakert.month + '-' + '00';
        let antraffadDod = data.dodsdatum.inteSakert ? testdataHelper.ejAngivetIfNull(data.dodsdatum.inteSakert.antraffadDod) : 'Ej angivet';

        return Promise.all([
            expect(dbpage.dodsdatum.dodsdatumSakert.getText()).to.eventually.equal(data.dodsdatum.sakert ? 'Säkert' : 'Ej säkert'),
            expect(dbpage.dodsdatum.datum.getText()).to.eventually.equal(dodsdatum),
            expect(dbpage.dodsdatum.antraffatDodDatum.getText()).to.eventually.equal(antraffadDod)
        ]);
    },
    dodsplats: data => {
        return Promise.all([
            expect(dbpage.dodsplats.kommun.getText()).to.eventually.equal(data.dodsPlats.kommun),
            expect(dbpage.dodsplats.boende.getText()).to.eventually.equal(data.dodsPlats.boende)
        ]);
    },
    barn: data => {
        let promiseArr = [];

        if (data.barn) {
            promiseArr.push(expect(dbpage.barn.getText()).to.eventually.equal(testdataHelper.boolTillJaNej(data.barn)));
        } else {
            promiseArr.push(expect(dbpage.barn.isPresent()).to.eventually.equal(true));
        }
        return Promise.all(promiseArr);
    }

};
