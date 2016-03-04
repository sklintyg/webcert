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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by */
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

var BaseSmiUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));

        this.diagnoseCode = element(by.id('diagnoseCode'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.pagaendeBehandling = element(by.id('pagaendeBehandling'));
        this.planeradBehandling = element(by.id('planeradBehandling'));
        this.ovrigt = element(by.id('ovrigt'));
        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));
    },

    getTillaggsfraga: function(i) {
        return element(by.id('form_tillaggsfragor_' + i + '__svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar textarea')).getAttribute('value');
    }
});

module.exports = BaseSmiUtkast;
