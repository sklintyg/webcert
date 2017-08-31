/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

/* globals pages */
'use strict';
let valjUtkast = pages.sokSkrivIntyg.valjUtkastType;
let pickPatient = pages.sokSkrivIntyg.pickPatient;
let fk7263utkast = pages.intyg.fk['7263'].utkast;
let patienter = {
    'inte har givit samtycke': '19121212-1212',
    'har givit samtycke': '19121212-1212'
};

let diagnoskoder = {
    'finns i SRS': 'M75',
    'inte finns i SRS': 'M751'
};

module.exports = function() {

    this.Given(/^att jag är inloggad som läkare på vårdenhet med SRS$/,
        () => pages.welcome.get()
        .then(() => pages.welcome.loginByName('Arnold Johansson'))
    );

    this.Given(/^att jag valt en patient som (inte har givit samtycke|har givit samtycke) till SRS$/,
        samtycke => pickPatient.selectPersonnummer(patienter[samtycke])
    );

    this.Given(/^att jag befinner mig på ett nyskapat Läkarintyg FK 7263$/,
        () => valjUtkast.selectIntygTypeByLabel('Läkarintyg FK 7263')
        .then(valjUtkast.intygTypeButton.click)
    );

    this.When(/^jag fyller i diagnoskod som (finns i SRS|inte finns i SRS)$/, srsStatus => fk7263utkast.angeDiagnosKod(diagnoskoder[srsStatus]));

};
