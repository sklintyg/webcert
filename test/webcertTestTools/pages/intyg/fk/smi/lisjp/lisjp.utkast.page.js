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

/*globals element,by,Promise,protractor,browser*/
'use strict';

var BaseSmiUtkast = require('../smi.base.utkast.page.js');
var pageHelpers = require('../../../../pageHelper.util.js');

var LisjpUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.smittskydd = element(by.id('form_avstangningSmittskydd')).element(by.css('input[type=checkbox]'));

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            telefonkontakt: {
                checkbox: element(by.id('form_telefonkontaktMedPatienten')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_telefonkontaktMedPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('form_journaluppgifter')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('annatGrundForMUBeskrivning')),
                checkbox: element(by.id('form_annatGrundForMU')).element(by.css('input[type=checkbox]')),
                datum: element(by.id('form_annatGrundForMU')).element(by.css('input[type=text]'))
            }
        };
        this.sysselsattning = {
            form: element(by.id('form_sysselsattning')),
            typ: {
                nuvarandeArbete: element(by.id('sysselsattning.typ-1')),
                arbetssokande: element(by.id('sysselsattning.typ-2')),
                foraldraledighet: element(by.id('sysselsattning.typ-3')),
                studier: element(by.id('sysselsattning.typ-4'))
            },
            nuvarandeArbeteBeskrivning: element(by.id('nuvarandeArbete'))
        };
        this.konsekvenser = {
            funktionsnedsattning: element(by.id('funktionsnedsattning')),
            aktivitetsbegransning: element(by.id('aktivitetsbegransning'))
        };
        this.medicinskbehandling = {
            pagaende: element(by.id('pagaendeBehandling')),
            planerad: element(by.id('planeradBehandling'))
        };
        this.sjukskrivning = {
            100: {
                fran: element(by.id('sjukskrivningar-HELT_NEDSATT-from')),
                till: element(by.id('sjukskrivningar-HELT_NEDSATT-tom'))
            },
            75: {
                fran: element(by.id('sjukskrivningar-TRE_FJARDEDEL-from')),
                till: element(by.id('sjukskrivningar-TRE_FJARDEDEL-tom'))
            },
            50: {
                fran: element(by.id('sjukskrivningar-HALFTEN-from')),
                till: element(by.id('sjukskrivningar-HALFTEN-tom'))
            },
            25: {
                fran: element(by.id('sjukskrivningar-EN_FJARDEDEL-from')),
                till: element(by.id('sjukskrivningar-EN_FJARDEDEL-tom'))
            },
            forsakringsmedicinsktBeslutsstodBeskrivning: element(by.id('forsakringsmedicinsktBeslutsstod')),
            arbetstidsforlaggning: {
                nej: element(by.id('arbetstidsforlaggningNo')),
                ja: element(by.id('arbetstidsforlaggningYes')),
                beskrivning: element(by.id('arbetstidsforlaggningMotivering'))
            },
            arbetsresor: element(by.id('form_arbetsresor')).element(by.css('input[type=checkbox]')),
            formagaTrotsBegransningBeskrivning: element(by.id('formagaTrotsBegransning')),
            // prognos: {
            //     typ: {
            //         1: element(by.id('prognos.typ-1')),
            //         3: element(by.id('prognos.typ-3')),
            //         4: element(by.id('prognos.typ-4')),
            //         5: element(by.id('prognos.typ-5'))
            //     },
            //     dagarTillArbete: {
            //         30: element(by.id('prognos.dagarTillArbete-1')),
            //         60: element(by.id('prognos.dagarTillArbete-2')),
            //         90: element(by.id('prognos.dagarTillArbete-3')),
            //         180: element(by.id('prognos.dagarTillArbete-4'))
            //     }
            // }
            prognos: {
                form: element(by.id('form_prognos')),
                inom: element(by.id('prognosDagarTillArbete-1-typ')),
                select: element(by.css('#prognosDagarTillArbete-1-typ > div.ui-select-match > span'))
            }
        };
        this.atgarder = {
            typ: {
                1: element(by.id('arbetslivsinriktadeAtgarder-1')),
                2: element(by.id('arbetslivsinriktadeAtgarder-2')),
                3: element(by.id('arbetslivsinriktadeAtgarder-3')),
                4: element(by.id('arbetslivsinriktadeAtgarder-4')),
                5: element(by.id('arbetslivsinriktadeAtgarder-5')),
                6: element(by.id('arbetslivsinriktadeAtgarder-6')),
                7: element(by.id('arbetslivsinriktadeAtgarder-7')),
                8: element(by.id('arbetslivsinriktadeAtgarder-8')),
                9: element(by.id('arbetslivsinriktadeAtgarder-9')),
                10: element(by.id('arbetslivsinriktadeAtgarder-10')),
                11: element(by.id('arbetslivsinriktadeAtgarder-11'))
            },
            ejAktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderEjAktuelltBeskrivning')),
            aktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderAktuelltBeskrivning')),
            labels: element(by.id('form_arbetslivsinriktadeAtgarder')).all(by.css('label.checkbox'))
        };
        this.arendeQuestion = {
            newArendeButton: element(by.id('askArendeBtn')),
            text: element(by.id('arendeNewModelText')),
            topic: element(by.id('new-question-topic')),
            kontakt: element(by.cssContainingText('option', 'Kontakt')),
            sendButton: element(by.id('sendArendeBtn'))
        };
        this.arbetslivsinriktadeAtgarderBeskrivning = element(by.id('arbetslivsinriktadeAtgarderBeskrivning'));
        var panel = element(by.css('.arende-panel'));
        this.arendePanel = panel;
    },
    selectQuestionTopic: function(amne) {
        this.arendeQuestion.topic.element(by.cssContainingText('option', amne)).click();
    },
    get: function get(intygId) {
        get._super.call(this, 'lisjp', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    },
    angeAktivitetsbegransning: function(text) {
        return this.konsekvenser.aktivitetsbegransning.sendKeys(text);
    },
    angeFunktionsnedsattning: function(text) {
        return this.konsekvenser.funktionsnedsattning.sendKeys(text);
    },
    angeMedicinskBehandling: function(behandling) {
        var fn = this.medicinskbehandling;
        return Promise.all([
            fn.pagaende.sendKeys(behandling.pagaende),
            fn.planerad.sendKeys(behandling.planerad)
        ]);
    },
    angeArbetsformaga: function(arbetsformaga) {
        var el25 = this.sjukskrivning['25'];
        var el50 = this.sjukskrivning['50'];
        var el75 = this.sjukskrivning['75'];
        var el100 = this.sjukskrivning['100'];


        var promisesArr = [];

        if (arbetsformaga.nedsattMed25) {
            promisesArr.push(el25.fran.sendKeys(arbetsformaga.nedsattMed25.from)
                .then(function() {
                    return el25.till.sendKeys(arbetsformaga.nedsattMed25.tom);

                })
            );
        }
        if (arbetsformaga.nedsattMed50) {
            promisesArr.push(el50.fran.sendKeys(arbetsformaga.nedsattMed50.from)
                .then(function() {
                    return el50.till.sendKeys(arbetsformaga.nedsattMed50.tom);
                })
            );
        }
        if (arbetsformaga.nedsattMed75) {
            promisesArr.push(el75.fran.sendKeys(arbetsformaga.nedsattMed75.from)
                .then(function() {
                    return el75.till.sendKeys(arbetsformaga.nedsattMed75.tom);
                }));
        }
        if (arbetsformaga.nedsattMed100) {
            promisesArr.push(el100.fran.sendKeys(arbetsformaga.nedsattMed100.from)
                .then(function() {
                    return el100.till.sendKeys(arbetsformaga.nedsattMed100.tom);
                })
            );
        }
        return Promise.all(promisesArr);

        /*
        return Promise.all([
            el25.fran.clear(),
            el50.fran.clear(),
            el75.fran.clear(),
            el100.fran.clear(),
            el25.till.clear(),
            el50.till.clear(),
            el75.till.clear(),
            el100.till.clear()
        ]).then(function() {



        });*/
    },
    angeResorTillArbete: function(resor) {
        if (resor) {
            return this.sjukskrivning.arbetsresor.sendKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeSmittskydd: function(value) {
        if (value) {
            return this.smittskydd.sendKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeDiagnos: function(diagnos) {
        var el = this.diagnoseCode;
        return el.sendKeys(diagnos.kod).then(function() {
            return el.sendKeys(protractor.Key.TAB);
        });

    },
    angeArbetstidsforlaggning: function(arbetstidsforlaggning) {
        var el = this.sjukskrivning.arbetstidsforlaggning;
        if (arbetstidsforlaggning.val === 'Ja') {
            return el.ja.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    var EC = protractor.ExpectedConditions;
                    return browser.wait(EC.visibilityOf(el.beskrivning), 2000).then(function() {
                        return el.beskrivning.sendKeys(arbetstidsforlaggning.beskrivning);
                    });

                });
        } else {
            return el.nej.sendKeys(protractor.Key.SPACE);
        }
    },
    angeAtgarder: function(atgarder) {
        var atgarderEL = this.atgarder;
        var beskrivningEL = this.arbetslivsinriktadeAtgarderBeskrivning;
        var fillInAtgardBeskrivningar = function(atgarder) {
            var beskrivning = '';
            for (var i = 0; i < atgarder.length; i++) {
                if (atgarder[i].beskrivning) {
                    beskrivning += atgarder[i].beskrivning + '\n';
                }
            }

            if (beskrivning) {
                return beskrivningEL.sendKeys(beskrivning);
            } else {
                return Promise.resolve();
            }
        };

        var atgarderNamn = atgarder.map(function(obj) {
            return obj.namn;
        });

        return pageHelpers.clickAll(atgarderEL.labels, atgarderNamn).then(function() {
            return fillInAtgardBeskrivningar(atgarder);
        });
    },
    angeSysselsattning: function(sysselsattning) {
        var sysselsattningEL = this.sysselsattning;

        return sysselsattningEL.form.element(by.cssContainingText('label', sysselsattning.typ)).sendKeys(protractor.Key.SPACE)
            .then(function() {
                if (sysselsattning.yrkesAktiviteter) {
                    return sysselsattningEL.nuvarandeArbeteBeskrivning.sendKeys(sysselsattning.yrkesAktiviteter);
                } else {
                    return Promise.resolve();
                }
            });
    },
    angePrognosForArbetsformaga: function(prognos) {
        var prognosEL = this.sjukskrivning.prognos;
        return prognosEL.form.element(by.cssContainingText('label', prognos.name)).sendKeys(protractor.Key.SPACE).then(function() {
            browser.executeScript('window.scrollTo(0,3000);');
            if (prognos.within) {

                return prognosEL.select.click().then(function() {
                    return prognosEL.inom.element(by.cssContainingText('span', prognos.within)).click();
                });
            } else {
                return Promise.resolve();
            }
        });

    }

});

module.exports = new LisjpUtkast();
