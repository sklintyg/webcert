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

var Af00251Utkast = AfBaseUtkast._extend({
        init: function init() {
            init._super.call(this);
            this.intygType = 'af00251';
            this.intygTypeVersion = '1.0';

            this.minUndersokning = {
                checkbox: element(by.id('checkbox_undersokningsDatum')),
                datum: element(by.id('datepicker_undersokningsDatum'))
            };
            this.annat = {
                checkbox: element(by.id('checkbox_annatDatum')),
                datum: element(by.id('datepicker_annatDatum')),
                text: element(by.id('annatBeskrivning')),
            };

            this.arbetsmarknadspolitisktProgram = {
                text: element(by.id('arbetsmarknadspolitisktProgram-medicinskBedomning')),
                heltidRadio: element(by.id('arbetsmarknadspolitisktProgram.omfattning-HELTID')),
                deltidRadio: element(by.id('arbetsmarknadspolitisktProgram.omfattning-DELTID')),
                okandRadio: element(by.id('arbetsmarknadspolitisktProgram.omfattning-OKAND')),
                deltidText: element(by.id('arbetsmarknadspolitisktProgram-omfattningDeltid')),
            };

            this.funktionsnedsattning = element(by.id('funktionsnedsattning'));

            this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

            this.harForhinder = {
                yes: element(by.id('harForhinderYes')),
                no: element(by.id('harForhinderNo'))
            };

            this.sjukfranvaro = {
                checkbox: function (index) {
                    return element(by.id('sjukfranvaro-' + index + '-checked'))
                },
                niva: function (index) {
                    return element(by.id('sjukfranvaro-' + index + '-niva'))
                },
                from: function (index) {
                    return element(by.id('sjukfranvaro-' + index + '-from'))
                },
                tom: function (index) {
                    return element(by.id('sjukfranvaro-' + index + '-tom'))
                },
                addRow: element(by.id('sjukfranvaro-addRow')),
                deleteRow: function (index) {
                    return element(by.id('sjukfranvaro-' + index + '-deleteRow'))
                }
            };

            this.begransningSjukfranvaro = {
                yes: element(by.id('begransningSjukfranvaro-kanBegransasYes')),
                no: element(by.id('begransningSjukfranvaro-kanBegransasNo')),
                text: element(by.id('begransningSjukfranvaro-beskrivning'))
            };

            this.prognosAtergang = {
                utanAnpassning: element(by.id('prognosAtergang.prognos-ATERGA_UTAN_ANPASSNING')),
                kanEjAterga: element(by.id('prognosAtergang.prognos-KAN_EJ_ATERGA')),
                ejMojligtAvgora: element(by.id('prognosAtergang.prognos-EJ_MOJLIGT_AVGORA')),
                atergaMedAnpassning: element(by.id('prognosAtergang.prognos-ATERGA_MED_ANPASSNING')),
                text: element(by.id('prognosAtergang-anpassningar'))
            }
        },
        angeMinUndersokning: function (undersokning) {
            var promises = [];
            if (undersokning) {
                if (undersokning.checked) {
                    var el = this.minUndersokning;
                    if (undersokning.datum) {
                        promises.push(pageHelpers.moveAndSendKeys(el.datum, undersokning.datum));
                    } else {
                        promises.push(pageHelpers.moveAndSendKeys(el.checkbox, protractor.Key.SPACE));
                    }
                }
            }
            return Promise.all(promises);
        },
        angeAnnanUndersokning: function (undersokning) {
            var promises = [];
            if (undersokning) {
                if (undersokning.checked) {
                    var el = this.annat;
                    if (undersokning.datum) {
                        promises.push(pageHelpers.moveAndSendKeys(el.datum, undersokning.datum)
                            .then(pageHelpers.moveAndSendKeys(el.text, undersokning.text)));
                    } else {
                        promises.push(pageHelpers.moveAndSendKeys(el.checkbox, protractor.Key.SPACE)
                            .then(pageHelpers.moveAndSendKeys(el.text, undersokning.text)));
                    }
                }
            }
            return Promise.all(promises);
        }
        ,
        angeArbetmarksnadsPolitisktProgram: function (program) {
            if (!program) {
                return Promise.resolve();
            } else {
                var el = this.arbetsmarknadspolitisktProgram;
                var promise = Promise.resolve();
                if (program.text) {
                    promise = promise.then(pageHelpers.moveAndSendKeys(el.text, program.text));
                }
                if (program.radio == 'HELTID') {
                    promise = promise.then(pageHelpers.moveAndSendKeys(el.heltidRadio, protractor.Key.SPACE));
                } else if (program.radio == 'DELTID') {
                    promise = promise.then(pageHelpers.moveAndSendKeys(el.deltidRadio, protractor.Key.SPACE))
                        .then(pageHelpers.moveAndSendKeys(el.deltidText, program.deltidText));
                } else if (program.radio == 'OKAND') {
                    promise = promise.then(pageHelpers.moveAndSendKeys(el.okandRadio, protractor.Key.SPACE));
                }
                return promise;
            }
        }
        ,
        angeFunktionsNedsattning: function (value) {
            if (!value) {
                return Promise.resolve();
            } else {
                var el = this.funktionsnedsattning;
                return pageHelpers.moveAndSendKeys(el, value);
            }
        }
        ,
        angeAktivitetsBegransning: function (value) {
            if (!value) {
                return Promise.resolve();
            } else {
                var el = this.aktivitetsbegransning;
                return pageHelpers.moveAndSendKeys(el, value);
            }
        }
        ,
        angeHarForhinder: function (value) {
            var el = this.harForhinder;
            if (value) {
                return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
            } else {
                return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE)
            }
        }
        ,
        angeSjukfranvaro: function (franvaro) {
            var promises = [];
            if (franvaro && franvaro.length > 0) {
                var el = this.sjukfranvaro;

                for (var i = 0; i < franvaro.length; i++) {
                    if (i > 1) {
                        promises.push(el.addRow.click());
                    }
                    var row = franvaro[i];
                    if (!row.checked) {
                        continue;
                    }
                    // if (row.checked) {
                    //     promises.push(pageHelpers.moveAndSendKeys(el.checkbox(i), protractor.Key.SPACE));
                    // }
                    if (row.niva && i > 0) {
                        promises.push(pageHelpers.moveAndSendKeys(el.niva(i), row.niva));
                    }
                    if (row.from) {
                        promises.push(pageHelpers.moveAndSendKeys(el.from(i), row.from));
                    }
                    if (row.tom) {
                        promises.push(pageHelpers.moveAndSendKeys(el.tom(i), row.tom));
                    }
                }
            }
            return Promise.all(promises);
        }
        ,
        angeBegransningSjukfranvaro: function (begransning) {
            if (!begransning) {
                return Promise.resolve();
            } else {
                var el = this.begransningSjukfranvaro;
                if (begransning.value) {
                    return pageHelpers.moveAndSendKeys(el.yes, protractor.Key.SPACE)
                        .then(pageHelpers.moveAndSendKeys(el.text, begransning.text));
                } else {
                    return pageHelpers.moveAndSendKeys(el.no, protractor.Key.SPACE);
                }
            }
        }
        ,
        angePrognosAtergang: function (prognos) {
            if (!prognos) {
                return Promise.resolve();
            } else {
                var el = this.prognosAtergang;
                if (prognos.radio === 'ATERGA_UTAN_ANPASSNING') {
                    return pageHelpers.moveAndSendKeys(el.utanAnpassning, protractor.Key.SPACE);
                } else if (prognos.radio === 'KAN_EJ_ATERGA') {
                    return pageHelpers.moveAndSendKeys(el.kanEjAterga, protractor.Key.SPACE);
                } else if (prognos.radio === 'EJ_MOJLIGT_AVGORA') {
                    return pageHelpers.moveAndSendKeys(el.ejMojligtAvgora, protractor.Key.SPACE);
                } else {
                    return pageHelpers.moveAndSendKeys(el.atergaMedAnpassning, protractor.Key.SPACE)
                        .then(pageHelpers.moveAndSendKeys(el.text, prognos.text));
                }
            }
        }
        ,
        get: function get(intygId) {
            get._super.call(this, 'af00251', intygId);
        }
    })
;

module.exports = new Af00251Utkast();
