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

/* globals person, Promise,pages*/

'use strict';
var lusePage = pages.intyg.luse.intyg;
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

};
