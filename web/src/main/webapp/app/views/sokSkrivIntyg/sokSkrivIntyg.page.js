/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var doctor = element(by.css('.logged-in')),
    personnummer = element(by.id('pnr')),
    pnButton = element(by.id('skapapersonnummerfortsatt')),
    intygTypeSelector = element(by.id('intygType')),
    intygTypeButton = element(by.id('intygTypeFortsatt')),
    sekretessmarkering = element(by.id('sekretessmarkering')),
    puerror = element(by.id('puerror')),
    fornamn = element(by.id('fornamn')),
    efternamn = element(by.id('efternamn')),
    namnFortsatt = element(by.id('namnFortsatt'));

module.exports = {
    get: function () {
        browser.get('web/dashboard#/create/choose-cert-type/index');
    },
    getDoctorText: function () {
        return doctor.getText();
    },
    setPersonnummer: function (pn) {
        personnummer.sendKeys(pn);
    },
    selectPersonnummer: function (pn) {
        this.setPersonnummer(pn);
        pnButton.click();
    },
    selectIntygType: function (index) {
        intygTypeSelector.all(by.css('option[value="' + index + '"]')).click();
    },
    selectIntygTypeByLabel: function (val) {
        intygTypeSelector.all(by.css('option[label="' + val + '"]')).click();
    },
    continueToUtkast: function () {
        intygTypeButton.click();
    },
    sekretessmarkering: sekretessmarkering,
    puerror: puerror,
    intygTypeSelector: intygTypeSelector,
    intygTypeButton: intygTypeButton,
    fornamn: fornamn,
    efternamn: efternamn,
    namnFortsatt: namnFortsatt
};
