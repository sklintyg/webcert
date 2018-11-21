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

'use strict';

var BaseSmiIntygPage = require('../smi.base.intyg.page.js');

var LuaefsIntyg = BaseSmiIntygPage._extend({
    init: function init() {
        init._super.call(this);

        this.intygType = 'luae_fs';

        this.funktionsnedsattning = {
            debut: element(by.id('funktionsnedsattningDebut')),
            paverkan: element(by.id('funktionsnedsattningPaverkan'))
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        this.verifieraBaseratPa(data);

        this.verifieraDiagnos(data);

        this.verifieraAndraMedicinskaUtredningar(data);

        expect(this.funktionsnedsattning.debut.getText()).toBe(data.funktionsnedsattning.debut);
        expect(this.funktionsnedsattning.paverkan.getText()).toBe(data.funktionsnedsattning.paverkan);

        this.verifieraOvrigt(data);

        this.verifieraKontaktFK(data);

        this.verifieraTillaggsfragor(data);
    }
});
module.exports = new LuaefsIntyg();
