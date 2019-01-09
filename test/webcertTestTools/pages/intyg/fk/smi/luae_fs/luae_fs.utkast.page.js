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

/*globals element,by, Promise*/
'use strict';

var BaseSmiUtkast = require('../smi.base.utkast.page.js');
var pageHelpers = require('../../../../pageHelper.util.js');

var LuaefsUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                return {
                    underlag: element(by.id('underlag-' + index + '--typ')),
                    datum: element(by.id('datepicker_underlag[' + index + '].datum')),
                    information: element(by.id('underlag-' + index + '--hamtasFran'))
                };
            }
        };

        this.funktionsnedsattning = {
            debut: element(by.id('funktionsnedsattningDebut')),
            paverkan: element(by.id('funktionsnedsattningPaverkan'))
        };

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('checkbox_undersokningAvPatienten')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('checkbox_journaluppgifter')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('checkbox_anhorigsBeskrivningAvPatienten')),
                datum: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('annatGrundForMUBeskrivning')),
                checkbox: element(by.id('checkbox_annatGrundForMU')),
                datum: element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first()
            },
            kannedomOmPatient: {
                datum: element(by.id('form_kannedomOmPatient')).element(by.css('input[type=text]'))
            }
        };
    },

    angeDiagnos: function(diagnosObj) {
        var diagnoser = diagnosObj.diagnoser;
        var promiseArr = [];


        //Ange diagnoser
        promiseArr.push(this.angeDiagnosKoder(diagnoser));

        return Promise.all(promiseArr);
    },
    angeFunktionsnedsattning: function(funktionsnedsattning) {
        var fn = this.funktionsnedsattning;

        return pageHelpers.moveAndSendKeys(fn.debut, funktionsnedsattning.debut)
            .then(function() {
                return pageHelpers.moveAndSendKeys(fn.paverkan, funktionsnedsattning.paverkan);
            });
    },

    get: function get(intygId) {
        get._super.call(this, 'luae_fs', intygId);
    }
});

module.exports = new LuaefsUtkast();
