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

/**
 * Created by stephenwhite on 09/06/15.
 */
/* globals browser, protractor*/

'use strict';

var WebcertBasePage = require('./webcert.base.page.js');

var UnsignedIntygPage = WebcertBasePage._extend({
    init: function init() {
        init._super.call(this);

        this.visasokfilter = element(by.id('show-advanced-filter-btn'));
        this.filterVidarebefordrad = {
            form: element(by.id('filterFormVidarebefordrad')),
            notifiedAll: element(by.id('notifiedAll')),
            notified: element(by.id('notified')),
            notifiedNot: element(by.id('notifiedNot'))
        };
        // form-group saknar id
        this.filterUtkastComplete = {
            completeAll: element(by.id('completeAll')),
            completeNo: element(by.id('completeNo')),
            completeYes: element(by.id('completeYes'))
        };

        this.filterSavedBy = {
            form: element(by.id('filterFormSparatAv')),
            select: element(by.id('uc-savedBy'))
        };
    },
    get: function() {
        return browser.get('/web/dashboard#/unsigned');
    },
    showSearchFilters: function() {
        return this.visasokfilter.sendKeys(protractor.Key.SPACE);
    },
    hideSearchFilters: function() {
        return this.visasokfilter.sendKeys(protractor.Key.SPACE);
    }
});

module.exports = new UnsignedIntygPage();
