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

/*globals element, by, Promise, protractor */
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

var BaseSmiUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));

        this.diagnoseCode = element(by.id('diagnoseCode'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.pagaendeBehandling = element(by.id('pagaendeBehandling'));
        this.planeradBehandling = element(by.id('planeradBehandling'));
        this.ovrigt = element(by.id('ovrigt'));
        this.tillaggsfragor0svar = this.getTillaggsfraga(0);
        this.tillaggsfragor1svar = this.getTillaggsfraga(1);
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

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                index = index + 1; //skip header-row
                var row = element.all(by.css('tr.underlagRow')).get(index);
                return {
                    underlag: row.element(by.css('select')),
                    datum: row.element(by.css('.ng-valid-date')),
                    information: row.element(by.css('.input-full'))
                };

            },
            laggTillUnderlagKnapp: element(by.cssContainingText('button', 'ytterligare underlag'))

        };

        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnos = {
            laggTillDiagnosKnapp: element(by.cssContainingText('a', 'Lägg till övriga diagnoser')),
            diagnosRow: function(index) {
                var row = element.all(by.css('.diagnosRow')).get(index);
                return {
                    kod: row.element(by.css('#diagnoseCode'))
                };

            },
            narOchVarStalldesDiagnoser: element(by.id('diagnosgrund')),
            skalTillNyBedomning: {
                JA: element(by.id('nyBedomningDiagnosgrundYes')),
                NEJ: element(by.id('nyBedomningDiagnosgrundNo'))
            }

        };

        this.ovrigt = element(by.id('ovrigt'));
        this.kontaktMedFK = element(by.id('form_kontaktMedFk')).element(by.css('input'));
    },
    angeBaseratPa: function(baseratPa) {
        var promiseArr = [];
        if (baseratPa.minUndersokningAvPatienten) {
            promiseArr.push(this.baseratPa.minUndersokningAvPatienten.datum.sendKeys(baseratPa.minUndersokningAvPatienten));
        }
        if (baseratPa.journaluppgifter) {
            promiseArr.push(this.baseratPa.journaluppgifter.datum.sendKeys(baseratPa.journaluppgifter));
        }
        if (baseratPa.anhorigsBeskrivning) {
            promiseArr.push(this.baseratPa.anhorigBeskrivning.datum.sendKeys(baseratPa.anhorigsBeskrivning));
        }
        if (baseratPa.annat) {
            var annatEl = this.baseratPa.annat;
            promiseArr.push(
                annatEl.datum.sendKeys(baseratPa.annat)
                .then(function() {
                    return annatEl.beskrivning.sendKeys(baseratPa.annatBeskrivning);
                })
            );

        }

        if (baseratPa.personligKannedom) {
            promiseArr.push(this.baseratPa.kannedomOmPatient.datum.sendKeys(baseratPa.personligKannedom));
        }
        return Promise.all(promiseArr);

    },

    angeAndraMedicinskaUtredningar: function(utredningar) {
        var utredningarElement = this.andraMedicinskaUtredningar;

        function chooseFinns(finns) {
            if (finns) {
                return utredningarElement.finns.JA.sendKeys(protractor.Key.SPACE);
            } else {
                return utredningarElement.finns.NEJ.sendKeys(protractor.Key.SPACE);
            }
        }

        return chooseFinns(utredningar).then(function() {

            function makeLogTextFunction() {
                return function(text) {
                    console.log(text);
                };
            }

            function makeLogElementText() {
                return function(elm) {
                    return elm.getText().then(makeLogTextFunction());
                };
            }

            var promiseArr = [];
            for (var i = 0; i < utredningar.length; i++) {
                if (i !== 0) {
                    promiseArr.push(utredningarElement.laggTillUnderlagKnapp.sendKeys(protractor.Key.SPACE));
                }
                var row = utredningarElement.underlagRow(i);

                //Skriv ut alla möjliga val, för debug
                promiseArr.push(row.underlag.all(by.css('option')).map(makeLogElementText));

                promiseArr.push(row.underlag.element(by.cssContainingText('option', utredningar[i].underlag)).sendKeys(protractor.Key.SPACE));
                promiseArr.push(row.datum.sendKeys(utredningar[i].datum));
                promiseArr.push(row.information.sendKeys(utredningar[i].infoOmUtredningen));

            }
            return Promise.all(promiseArr);
        });
    },
    angeSjukdomsforlopp: function(forlopp) {
        return this.sjukdomsforlopp.sendKeys(forlopp);
    },
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
                promiseArr.push(this.diagnos.laggTillDiagnosKnapp.sendKeys(protractor.Key.SPACE));
            }
            var row = this.diagnos.diagnosRow(i);
            promiseArr.push(row.kod.sendKeys(diagnoser[i].kod).then(sendEnterToElement(row.kod)));

        }

        //Ange när och var diagnoser ställts
        promiseArr.push(this.diagnos.narOchVarStalldesDiagnoser.sendKeys(diagnosObj.narOchVarStalldesDiagnoserna));

        //Ange Finns skäl till ny bedömning
        var nyBedomning = this.diagnos.skalTillNyBedomning.NEJ;
        if (diagnosObj.nyBedomning) {
            nyBedomning = this.diagnos.skalTillNyBedomning.JA;
        }
        promiseArr.push(nyBedomning.sendKeys(protractor.Key.SPACE));

        return Promise.all(promiseArr);
    },
    angeOvrigaUpplysningar: function(ovrigt) {
        return this.ovrigt.sendKeys(ovrigt);
    },
    angeKontaktMedFK: function(kontakt) {
        if (kontakt) {
            return this.kontaktMedFK.sendKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeTillaggsfragor: function(svarArr) {
        var promiseArr = [];
        for (var i = 0; i < svarArr.length; i++) {
            return this.getTillaggsfraga(i).sendKeys(svarArr[i].svar);
        }
        //return Promise.all(promiseArr);

    },
    getTillaggsfraga: function(i) {
        return element(by.id('form_tillaggsfragor_' + i + '__svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar textarea')).getAttribute('value');
    }
});

module.exports = BaseSmiUtkast;
