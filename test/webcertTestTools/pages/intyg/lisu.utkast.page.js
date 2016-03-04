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

/*globals element,by*/
'use strict';

var BaseSmiUtkast = require('./smi.base.utkast.page.js');

var LisuUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.baseratPa = {
            typAvUnderlag: {
                undersokning: element(by.id('formly_1_date_undersokningAvPatienten_3')),
                journal: element(by.id('formly_1_date_journaluppgifter_4')),
                anhorig: element(by.id('formly_1_date_anhorigsBeskrivningAvPatienten_5')),
                annat: element(by.id('formly_1_date_annatGrundForMU_6')),
                annatText: element(by.id('formly_1_single-text_annatGrundForMUBeskrivning_7'))
            }
        };
    },

    get: function get(intygId) {
        get._super.call(this, 'luse', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
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

module.exports = new LisuUtkast();
