/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

var AgBaseUtkast = require('../ag.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');

var Ag114Utkast = AgBaseUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ag114';
        this.intygTypeVersion = '1.0';
        this.sysselsattning = {
            text: element(by.id('nuvarandeArbete'))
        };
        this.onskarFormedlaDiagnos = {
            no: element(by.id('onskarFormedlaDiagnosNo')),
            yes: element(by.id('onskarFormedlaDiagnosYes')),
            diagnosRow: function (index) {
                return {
                    kod: element(by.id('diagnoseCode-' + index)),
                    beskrivning: element(by.id('diagnoseDescription-' + index))
                };
            },
        };
        this.nedsattArbetsformaga = {
            text: element(by.id('nedsattArbetsformaga')),
            no: element(by.id('arbetsformagaTrotsSjukdomNo')),
            yes: element(by.id('arbetsformagaTrotsSjukdomYes')),
            formaga: element(by.id('arbetsformagaTrotsSjukdomBeskrivning'))
        };
        this.bedomning = {
            sjukskrivningsgrad: element(by.id('sjukskrivningsgrad')),
            from: element(by.id('datepicker_sjukskrivningsperiod.from')),
            tom: element(by.id('datepicker_sjukskrivningsperiod.tom'))
        };
        this.ovrigt = element(by.id('ovrigaUpplysningar'));
    },
    angeSysselsattning: function (sysselsattning) {
        var el = this.sysselsattning.text;
        return el.clear().then(function () {
            return pageHelpers.moveAndSendKeys(el, sysselsattning.text);
        });
    },
    angeOnskarFormedlaDiagnos: function (onskarFormedlaDiagnos) {
        if (!onskarFormedlaDiagnos) {
            return Promise.resolve();
        } else {
            var el = this.onskarFormedlaDiagnos;
            return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                .then(function () {
                    return pageHelpers.moveAndSendKeys(el.diagnosRow(0).kod, onskarFormedlaDiagnos.diagnoser[0])
                        .then(function () {
                            return pageHelpers.moveAndSendKeys(el.diagnosRow(0).kod, protractor.Key.TAB);
                        })
                        .then(function () {
                            return pageHelpers.moveAndSendKeys(el.diagnosRow(1).kod, onskarFormedlaDiagnos.diagnoser[1])
                                .then(function () {
                                    return pageHelpers.moveAndSendKeys(el.diagnosRow(1).kod, protractor.Key.TAB);
                                })
                                .then(function () {
                                    return pageHelpers.moveAndSendKeys(el.diagnosRow(2).kod, onskarFormedlaDiagnos.diagnoser[2])
                                        .then(function () {
                                            return pageHelpers.moveAndSendKeys(el.diagnosRow(2).kod, protractor.Key.TAB);
                                        });
                                })
                        })
                });
        }
    },
    angeDiagnosKoder: function (diagnoser) {
        var promiseArr = [];
        for (var i = 0; i < diagnoser.length; i++) {
            var row = this.diagnos.diagnosRow(i);
            promiseArr.push(moveAndSendKeys(row.kod, diagnoser[i].kod).then(browser.sleep(1000)).then(sendEnterToElement(row.kod)));
        }
        return Promise.all(promiseArr);

    },
    angeNedsattArbetsformaga: function (nedsattArbetsformaga) {
        if (!nedsattArbetsformaga) {
            return Promise.resolve();
        } else {
            var el = this.nedsattArbetsformaga;
            return pageHelpers.moveAndSendKeys(el.text, nedsattArbetsformaga.text)
                .then(function () {
                    return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.formaga, nedsattArbetsformaga.formaga);
                    })

                });
        }
    },
    angeBedomning: function (bedomning) {
        if (!bedomning) {
            return Promise.resolve();
        } else {
            var el = this.bedomning;
            return pageHelpers.moveAndSendKeys(el.sjukskrivningsgrad, bedomning.sjukskrivningsgrad)
                .then(function () {
                    return pageHelpers.moveAndSendKeys(el.from, bedomning.from);
                })
                .then(function () {
                    return pageHelpers.moveAndSendKeys(el.tom, bedomning.tom);
                });
        }
    },
    angeOvrigt: function (ovrigt) {
        var el = this.ovrigt;
        return el.clear().then(function () {
            return pageHelpers.moveAndSendKeys(el, ovrigt);
        });
    },
    get: function get(intygId) {
        get._super.call(this, 'ag114', intygId);
    }

});

module.exports = new Ag114Utkast();
