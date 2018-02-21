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

/*globals element, by */
'use strict';

/**
 * This is a base (view) page for fk SOC SKV family of intyg (db, doi).
 * Only things relevant to ALL such types should end up here.
 */

var BaseIntyg = require('../base.intyg.page.js');

var skvBaseIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.id('viewCertAndQA'));
        this.enhetsAdress = {
            postAdress: element(by.id('vardperson_postadress')),
            postNummer: element(by.id('vardperson_postnummer')),
            postOrt: element(by.id('vardperson_postort')),
            enhetsTelefon: element(by.id('vardperson_telefonnummer'))
        };
    },
    somefunction: function(txt) {
        return txt;
    }
});

module.exports = skvBaseIntyg;
