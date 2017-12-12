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

/* globals browser*/

'use strict';

var WebcertBasePage = require('./webcert.base.page.js');

var FragorOchSvarPage = WebcertBasePage._extend({
    init: function init() {
        init._super.call(this);
        this.qaTable = element(by.css('table.wc-table-striped'));
        this.atgardSelect = element(by.id('qp-showStatus'));
        this.searchBtn = element(by.id('filter-arende-btn'));
    },
    get: function() {
        return browser.get('/web/dashboard#/enhet-arenden');
    }
});

module.exports = new FragorOchSvarPage();
