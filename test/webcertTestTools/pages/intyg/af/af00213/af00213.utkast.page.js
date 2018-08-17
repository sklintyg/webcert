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

/* globals protractor */
/**
 * Created by bennysce on 09/06/15.
 */

'use strict';

const BaseAfUtkast = require('../af.base.utkast.page.js');
const pageHelpers = require('../../../pageHelper.util.js');


const Af00213Utkast = BaseAfUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'af00213';
        this.ovrigt = element(by.id('ovrigt'));
        this.funktionsnedsattning = {
            ja: element(by.id('harFunktionsnedsattningYes')),
            nej: element(by.id('harFunktionsnedsattningNo')),
            text: element(by.id('funktionsnedsattning'))
        };
        this.aktivitetsbegransning = {
            ja: element(by.id('harAktivitetsbegransningYes')),
            nej: element(by.id('harAktivitetsbegransningNo')),
            text: element(by.id('aktivitetsbegransning'))
        };
        this.utredningBehandling = {
            ja: element(by.id('harUtredningBehandlingYes')),
            nej: element(by.id('harUtredningBehandlingNo')),
            text: element(by.id('utredningBehandling'))
        };
        this.arbetetsPaverkan = {
            ja: element(by.id('harArbetetsPaverkanYes')),
            nej: element(by.id('harArbetetsPaverkanNo')),
            text: element(by.id('arbetetsPaverkan'))
        };
    },
    angeOvrigt: function(ovrigt) {
        return this.ovrigt.sendKeys(ovrigt);
    },
    angeFunktionsnedsattning: function(funktionsnedsattning) {
        let elm = {
            funktionsnedsattning: this.funktionsnedsattning
        };

        if (funktionsnedsattning === false) {
            return pageHelpers.moveAndSendKeys(elm.funktionsnedsattning.nej, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(elm.funktionsnedsattning.ja, protractor.Key.SPACE).then(function() {
                return pageHelpers.moveAndSendKeys(elm.funktionsnedsattning.text, funktionsnedsattning);
            });
        }
    },
    angeAktivitetsbegransning: function(aktivitetsbegransning) {
        let elm = {
            aktivitetsbegransning: this.aktivitetsbegransning
        };

        if (typeof(aktivitetsbegransning) === 'undefined') {
            return;
        }

        if (aktivitetsbegransning === false) {
            return pageHelpers.moveAndSendKeys(elm.aktivitetsbegransning.nej, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(elm.aktivitetsbegransning.ja, protractor.Key.SPACE).then(function() {
                return pageHelpers.moveAndSendKeys(elm.aktivitetsbegransning.text, aktivitetsbegransning);
            });
        }
    },
    angeUtredningBehandling: function(utredningBehandling) {
        let elm = {
            utredningBehandling: this.utredningBehandling
        };
        if (typeof(utredningBehandling) === 'undefined') {
            return;
        }

        if (utredningBehandling === false) {
            return pageHelpers.moveAndSendKeys(elm.utredningBehandling.nej, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(elm.utredningBehandling.ja, protractor.Key.SPACE).then(function() {
                return pageHelpers.moveAndSendKeys(elm.utredningBehandling.text, utredningBehandling);
            });
        }
    },
    angeArbetetsPaverkan: function(arbetetsPaverkan) {
        let elm = {
            arbetetsPaverkan: this.arbetetsPaverkan
        };
        if (arbetetsPaverkan === false) {
            return pageHelpers.moveAndSendKeys(elm.arbetetsPaverkan.nej, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(elm.arbetetsPaverkan.ja, protractor.Key.SPACE).then(function() {
                return pageHelpers.moveAndSendKeys(elm.arbetetsPaverkan.text, arbetetsPaverkan);
            });
        }
    }
});

module.exports = new Af00213Utkast();
