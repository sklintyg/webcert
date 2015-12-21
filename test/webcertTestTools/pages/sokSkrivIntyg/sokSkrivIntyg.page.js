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

var WebcertBasePage = require('../webcert.base.page.js');

var SokSkrivIntyg = WebcertBasePage._extend({
    init: function init() {
        init._super.call(this);

        this.personnummer = element(by.id('pnr'));
        this.pnButton = element(by.id('skapapersonnummerfortsatt'));
        this.sekretessmarkering = element(by.id('sekretessmarkering'));
        this.puerror = element(by.id('puerror'));
        this.fornamn = element(by.id('fornamn'));
        this.efternamn = element(by.id('efternamn'));
        this.namnFortsatt = element(by.id('namnFortsatt'));
    },
    get: function() {
        browser.get('web/dashboard#/create/choose-cert-type/index');
    },
    setPersonnummer: function(pn) {
        this.personnummer.sendKeys(pn);
    },
    selectPersonnummer: function(pn) {
        this.setPersonnummer(pn);
        this.pnButton.click();
    },
    sekretessmarkering: this.sekretessmarkering,
    puerror: this.puerror,
    fornamn: this.fornamn,
    efternamn: this.efternamn,
    namnFortsatt: this.namnFortsatt
});

module.exports = new SokSkrivIntyg()
