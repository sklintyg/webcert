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

/* globals person, Promise,pages,intyg*/

'use strict';
var lusePage = pages.intyg.luse.intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var luseUtkastPage = pages.intyg.luse.utkast;
var helpers = require('../helpers');
module.exports = function() {

    this.Given(/^ska intyget visa den nya addressen$/, function() {
        return Promise.all([
            expect(lusePage.patientAdress.postadress.getText()).to.eventually.contain(person.adress.postadress),
            expect(lusePage.patientAdress.postort.getText()).to.eventually.contain(person.adress.postort),
            expect(lusePage.patientAdress.postnummer.getText()).to.eventually.contain(person.adress.postnummer)
        ]);
    });

    this.Given(/^ska intyget visa det nya namnet$/, function() {
        return expect(lusePage.patientNamnOchPersonnummer.getText()).to.eventually.contain(person.fornamn + ' ' + person.efternamn);
    });

    this.Given(/^ska intyget visa det nya personnummret$/, function() {
        return expect(lusePage.patientNamnOchPersonnummer.getText()).to.eventually.contain(person.fornamn + ' ' + person.efternamn + ' - ' + person.id);
    });

    this.Given(/^ska adressen kopieras till det kopierade intyget$/, function() {
        var promiseArray = [];
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            promiseArray.push(expect(luseUtkastPage.enhetensAdress.enhetsTelefon.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.telefon));
            promiseArray.push(expect(luseUtkastPage.enhetensAdress.postAdress.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postadress));
            promiseArray.push(expect(luseUtkastPage.enhetensAdress.postNummer.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postnummer));
            promiseArray.push(expect(luseUtkastPage.enhetensAdress.postOrt.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postort));

        } else {

            promiseArray.push(expect(fkUtkastPage.enhetensAdress.postAdress.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postadress));
            promiseArray.push(expect(fkUtkastPage.enhetensAdress.postNummer.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postnummer));
            promiseArray.push(expect(fkUtkastPage.enhetensAdress.postOrt.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.postort));
            promiseArray.push(expect(fkUtkastPage.enhetensAdress.enhetsTelefon.getAttribute('value')).to.eventually.equal(global.user.enhetsAdress.telefon));
        }
        return Promise.all(promiseArray);

    });

};
