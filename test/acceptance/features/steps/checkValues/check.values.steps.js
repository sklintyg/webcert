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

/* globals person, ursprungligPerson, Promise,pages,intyg*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


var lusePage = pages.intyg.luse.intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var luseUtkastPage = pages.intyg.luse.utkast;
var helpers = require('../helpers');
const checkValues = require('../checkValues');

/*
 *	Test steg
 *
 */

Then(/^(?:ska jag|jag ska) se den data jag angett för intyget$/, function() {
    return checkValues.forIntyg(intyg);
});


Given(/^ska intyget visa den nya addressen$/, function() {
    return Promise.all([
        expect(lusePage.patientAdress.postadress.getText()).to.eventually.contain(person.adress.postadress),
        expect(lusePage.patientAdress.postort.getText()).to.eventually.contain(person.adress.postort),
        expect(lusePage.patientAdress.postnummer.getText()).to.eventually.contain(person.adress.postnummer)
    ]);
});

// Given(/^ska intyget visa det nya namnet$/, function() {
//     return expect(lusePage.patientNamnOchPersonnummer.getText()).to.eventually.contain(person.forNamn + ' ' + person.efterNamn);
// });

Given(/^ska intyget visa det (gamla|nya) person-id:numret$/, function(arg1) {
    let id;
    let elm;

    if (arg1 === 'nya') {
        id = person.id;
        elm = lusePage.patientNamnOchPersonnummer;
    } else {
        id = ursprungligPerson.id;
        elm = lusePage.FdPersonnummer;
    }

    return expect(elm.getText()).to.eventually.contain(helpers.insertDashInPnr(id));

});

Given(/^ska adressen vara ifylld på det förnyade intyget$/, function() {
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

Then(/^ska jag( inte)? se signerande läkare "([^"]*)"$/, function(inte, name) {
    if (typeof(inte) === 'undefined') {
        return expect(fkUtkastPage.signingDoctorName.getText()).to.eventually.equal(name);
    } else {
        return helpers.largeDelay().then(function() {
            return fkUtkastPage.signingDoctorName.isPresent();
        }).then(function(present) {
            return expect(present).to.equal.false;
        });
    }
});
