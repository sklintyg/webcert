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
 * Created by bennysce on 09/06/15.
 */
'use strict';

var AfBaseIntyg = require('../af.base.intyg.page');
//var testValues = require('../../../../testdata/testvalues.ts');
//var _ = require('lodash');

var Af00213Intyg = AfBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'af00213';


        //this.intygStatus = element(by.id('intyget-sparat-och-ej-komplett-meddelande'));

    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    }
});

module.exports = new Af00213Intyg();
