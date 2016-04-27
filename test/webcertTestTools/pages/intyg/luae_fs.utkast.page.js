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

/*globals element,by, Promise*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

function sendKeysWithBackspaceFix(el, text) {
    return el.sendKeys(text)
        .then(function() {
            return el.sendKeys(protractor.Key.BACK_SPACE);
        })
        .then(function() {
            return el.sendKeys(text.substr(text.length - 1));
        });
}

var LuaefsUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                index = index + 1; //skip header-row
                var row = element.all(by.css('tr.underlagRow')).get(index);
                return {
                    underlag: row.element(by.css('[name="andraUnderlag"]')),
                    datum: row.element(by.css('[name="-Date"]')),
                    information: row.element(by.css('.input-full'))
                };

            },
            laggTillUnderlagKnapp: element(by.cssContainingText('button', 'ytterligare underlag'))

        };

        //this.diagnosKod = element(by.id('diagnoseCode'));

        this.diagnos = {
            laggTillDiagnosKnapp: element(by.cssContainingText('a', 'Lägg till övriga diagnoser')),
            diagnosRow: function(index) {
                var row = element.all(by.css('.diagnosRow')).get(index);
                return {
                    kod: row.element(by.css('#diagnoseCode'))
                };

            }
        };

        this.funktionsnedsattningDebut = element(by.id('funktionsnedsattningDebut'));
        this.funktionsnedsattningPaverkan = element(by.id('funktionsnedsattningPaverkan'));

        this.ovrigt = element(by.id('ovrigt'));

        this.kontaktMedFkNo = element(by.id('formly_1_checkbox-inline_kontaktMedFk_0'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));

        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('formly_1_date_undersokningAvPatienten_3')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('formly_1_date_journaluppgifter_4')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('form_anhorigsBeskrivningAvPatienten')),
                datum: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('formly_1_single-text_annatGrundForMUBeskrivning_7')),
                checkbox: element(by.id('formly_1_date_annatGrundForMU_6')),
                datum: element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first()
            },
            kannedomOmPatient: {
                datum: element(by.id('form_kannedomOmPatient')).element(by.css('input[type=text]')),
                checkbox: element(by.id('formly_1_date_kannedomOmPatient_8'))
            }
        };
    },


    // Helper functions filling, editing etc.
    angeDiagnos: function(diagnosObj) {
        var diagnoser = diagnosObj.diagnoser;
        var promiseArr = [];

        function sendEnterToElement(el) {
            return function() {
                el.sendKeys(protractor.Key.ENTER);
            };
        }
        //Ange diagnoser
        for (var i = 0; i < diagnoser.length; i++) {
            if (i !== 0) {
                promiseArr.push(this.diagnos.laggTillDiagnosKnapp.click());
            }
            var row = this.diagnos.diagnosRow(i);
            promiseArr.push(row.kod.sendKeys(diagnoser[i].kod).then(sendEnterToElement(row.kod)));

        }
        Promise.all(promiseArr);

    },

    taBortDiagnos: function(index) {
        var promiseArr = [];
        var button = element.all(by.css('.deleteDiagnos')).get(index);
        promiseArr.push(button.click());

        Promise.all(promiseArr);
    },

    angeAndraMedicinskaUtredningar: function(utredningar) {
        var utredningarElement = this.andraMedicinskaUtredningar;

        var fillIn = function fillInUtr(val, index) {

            var laggTillUnderlag;
            if (index !== 0) {
                laggTillUnderlag = utredningarElement.laggTillUnderlagKnapp.sendKeys(protractor.Key.SPACE);
            }

            var row = utredningarElement.underlagRow(index);

            return Promise.all([
                laggTillUnderlag,
                sendKeysWithBackspaceFix(row.datum, val.datum),
                row.underlag.element(by.cssContainingText('option', val.underlag)).click(),
                row.information.sendKeys(val.infoOmUtredningen)
            ]);
        };

        if (utredningar) {
            return utredningarElement.finns.JA.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    browser.sleep(2000);
                    var actions = utredningar.map(fillIn);
                    return actions;
                });
        } else {
            return utredningarElement.finns.NEJ.sendKeys(protractor.Key.SPACE);
        }
    },

    clickCreateUnderlag: function() {
        var addBtn = element(by.id('form_underlag')).element(by.css('button[ng-click="createUnderlag()"]'));
        addBtn.sendKeys(protractor.Key.SPACE);
    },

    clickRemoveUnderlag: function(index) {
        element.all(by.css('button[ng-click="removeUnderlag($index)"]')).then(function(items) {
            items[index].sendKeys(protractor.Key.SPACE);
        });
    },

    angeUnderlagFinns: function(underlag) {
        if (!underlag) {
            return Promise.resolve('Success');
        }

        var promisesArr = [];
        promisesArr.push(this.underlagDatePicker1.sendKeys(underlag.datum));
        promisesArr.push(this.underlagSelect1.sendKeys(underlag.typ));
        promisesArr.push(this.underlagTextField1.sendKeys(underlag.hamtasFran));

        Promise.all(promisesArr);
    },

    angeIntygetBaserasPa: function(intygetBaserasPa) {
        if (!intygetBaserasPa) {
            return Promise.resolve('Success');
        }

        var promisesArr = [];

        if (intygetBaserasPa.minUndersokningAvPatienten) {
            promisesArr.push(this.baseratPa.minUndersokningAvPatienten.datum.sendKeys(intygetBaserasPa.minUndersokningAvPatienten.datum));
        }
        if (intygetBaserasPa.journaluppgifter) {
            promisesArr.push(this.baseratPa.journaluppgifter.datum.sendKeys(intygetBaserasPa.journaluppgifter.datum));
        }
        if (intygetBaserasPa.anhorigBeskrivning) {
            promisesArr.push(this.baseratPa.anhorigBeskrivning.datum.sendKeys(intygetBaserasPa.anhorigBeskrivning.datum));
        }
        if (intygetBaserasPa.annat) {
            promisesArr.push(this.baseratPa.annat.datum.sendKeys(intygetBaserasPa.annat.datum));
            promisesArr.push(this.baseratPa.annat.beskrivning.sendKeys(intygetBaserasPa.annat.beskrivning));
        }
        if (intygetBaserasPa.kannedomOmPatient) {
            promisesArr.push(this.baseratPa.kannedomOmPatient.datum.sendKeys(intygetBaserasPa.kannedomOmPatient.datum));
        }
        return Promise.all(promisesArr);
    },

    getNumberOfDiagnosRows: function() {
        return element.all(by.css('.diagnosRow')).then(function(items) {
            return items.length;
        });
    },

    getNumberOfUnderlag: function() {
        return element.all(by.css('.underlagRow td select')).then(function(items) {
            return items.length;
        });
    },

    get: function get(intygId) {
        get._super.call(this, 'luae_fs', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    }
});

module.exports = new LuaefsUtkast();
