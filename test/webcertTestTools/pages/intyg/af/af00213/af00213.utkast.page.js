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

/*globals element, by, Promise, protractor*/
'use strict';

var AfBaseUtkast = require('../af.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');

var Af00213Utkast = AfBaseUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'af00213';
        this.intygTypeVersion = '2.0';
        this.funktionsnedsattning = {
            yes: element(by.id('harFunktionsnedsattningYes')),
            no: element(by.id('harFunktionsnedsattningNo')),
            text: element(by.id('funktionsnedsattning'))
        };

        this.aktivitetsbegransning = {
            yes: element(by.id('harAktivitetsbegransningYes')),
            no: element(by.id('harAktivitetsbegransningNo')),
            text: element(by.id('aktivitetsbegransning'))
        };

        this.utredningBehandling = {
            yes: element(by.id('harUtredningBehandlingYes')),
            no: element(by.id('harUtredningBehandlingNo')),
            text: element(by.id('utredningBehandling'))
        };

        this.arbetetsPaverkan = {
            yes: element(by.id('harArbetetsPaverkanYes')),
            no: element(by.id('harArbetetsPaverkanNo')),
            text: element(by.id('arbetetsPaverkan'))
        };
        this.skipparBalte = {
            yes: element(by.id('harSkipparBalteYes')),
            no: element(by.id('harSkipparBalteNo')),
            text: element(by.id('skipparBalteMotivering'))
        };
        this.ovrigt = element(by.id('ovrigt'));
    },
    angeFunktionsnedsattning: function(funktionsnedsattning) {
        var el = this.funktionsnedsattning;
        if (!funktionsnedsattning) {
            return Promise.resolve();
        } else {
            if (funktionsnedsattning.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.text, funktionsnedsattning.text);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
            }
        }
    },
    angeAktivitetsbegransning: function(aktivitetsbegransning) {
        if (!aktivitetsbegransning) {
            return Promise.resolve();
        } else {
            var el = this.aktivitetsbegransning;

            if (aktivitetsbegransning.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.text, aktivitetsbegransning.text);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
            }
        }

    },
    angeUtredningBehandling: function(utredningBehandling) {
        if (!utredningBehandling) {
            return Promise.resolve();
        } else {
            var el = this.utredningBehandling;
            if (utredningBehandling.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.text, utredningBehandling.text);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
            }
        }
    },
    angeArbetetsPaverkan: function(arbetetsPaverkan) {
        var el = this.arbetetsPaverkan;
        if (!arbetetsPaverkan) {
            return Promise.resolve();
        } else {
            if (arbetetsPaverkan.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.text, arbetetsPaverkan.text);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
            }
        }
    },
    angeSkipparBalte: function(skipparbalte) {
        var el = this.skipparBalte;
        if (!skipparbalte) {
            return Promise.resolve();
        } else {
            if (skipparbalte.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.text, skipparbalte.text);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
            }
        }
    },
    angeOvrigaUpplysningar: function(ovrigt) {
        var el = this.ovrigt;
        return el.clear().then(function() {
            return pageHelpers.moveAndSendKeys(el, ovrigt);
        });
    },
    get: function get(intygId) {
        get._super.call(this, 'af00213', intygId);
    }

});

module.exports = new Af00213Utkast();
