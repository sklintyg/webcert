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

/*globals element,by */
'use strict';

/**
 * This is a base (view) page for fk SIT family of intyg (luse, lusi, luae_fs, luae_na).
 * Only things relevant to ALL such types should end up here.
 */

var BaseIntyg = require('../base.intyg.page.js');
var _ = require('lodash');

var TsBaseIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);

        this.idkontroll = element(by.id('vardkontakt-idkontroll'));
        this.korkortstyp = element(by.id('intygAvser-korkortstyp'));
        this.idStarktGenom = element(by.id('vardkontakt-idkontroll'));

        this.falt1 = {
            bedomning: element(by.id('bedomning-korkortstyp'))
        };

        this.kommentar = element(by.id('kommentar'));
    },
    verifieraIntygetAvser: function(korkortstyper, korkort) {

        var sorted = _.sortBy(korkortstyper, function(x) {
            return _.indexOf(korkort, x);
        });

        var text = _.join(sorted, ', ');

        expect(this.korkortstyp.getText()).toBe(text);
    },
    verifieraIdKontroll: function(identitetStyrktGenom) {

        if (identitetStyrktGenom === 'Försäkran enligt 18 kap. 4§') {
            identitetStyrktGenom = 'Försäkran enligt 18 kap 4 §';
        }

        if (identitetStyrktGenom === 'Företagskort eller tjänstekort') {
            identitetStyrktGenom = 'Företagskort eller tjänstekort.';
        }

        expect(this.idkontroll.getText()).toBe(identitetStyrktGenom);
    },
    verifieraBedomning: function(bedomning, korkort) {

        if (bedomning.stallningstagande === 'Kan inte ta ställning') {
            expect(this.falt1.bedomning.getText()).toBe(bedomning.stallningstagande);
        } else {
            var sorted = _.sortBy(bedomning.behorigheter, function(x) {
                return _.indexOf(korkort, x);
            });

            var text = _.join(sorted, ', ');

            expect(this.falt1.bedomning.getText()).toBe(text);
        }
    }
});

module.exports = TsBaseIntyg;
