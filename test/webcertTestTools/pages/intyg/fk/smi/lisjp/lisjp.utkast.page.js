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

var LisuUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);

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
                studier: element(by.id('sysselsattning.typ-4')),
                arbetmarknadspolitisktProgram: element(by.id('sysselsattning.typ-5'))
            },
            nuvarandeArbeteBeskrivning: element(by.id('nuvarandeArbete')),
            arbetsmarknadspolitisktProgramBeskrivning: element(by.id('arbetsmarknadspolitisktProgram'))
        };
        this.konsekvenser = {
            funktionsnedsattning: element(by.id('funktionsnedsattning')),
            aktivitetsbegransning: element(by.id('aktivitetsbegransning'))
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
            arbetsresor: {
                nej: element(by.id('arbetsresorNo')),
                ja: element(by.id('arbetsresorYes'))
            },
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
        get._super.call(this, 'lisu', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    },
    getTillaggsfraga: function(i) {
        return element(by.id('form_tillaggsfragor_' + i + '__svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + '__svar textarea')).getAttribute('value');
    },
    angeBaserasPa: function(intygetBaserasPa) {
        var promisesArr = [];
        if (intygetBaserasPa.undersokning) {
            promisesArr.push(this.baseratPa.minUndersokningAvPatienten.datum.sendKeys(intygetBaserasPa.undersokning));
        }
        if (intygetBaserasPa.telefonkontakt) {
            promisesArr.push(this.baseratPa.telefonkontakt.datum.sendKeys(intygetBaserasPa.telefonkontakt));
        }
        if (intygetBaserasPa.journaluppgifter) {
            promisesArr.push(this.baseratPa.journaluppgifter.datum.sendKeys(intygetBaserasPa.journaluppgifter));
        }
        if (intygetBaserasPa.annat) {
            var annatEl = this.baseratPa.annat;
            promisesArr.push(annatEl.datum.sendKeys(intygetBaserasPa.annat).then(function() {
                return annatEl.beskrivning.sendKeys(intygetBaserasPa.annatBeskrivning);
            }));
        }
        return Promise.all(promisesArr);
    },
    angeArbetsformaga: function(arbetsformaga) {
        var promisesArr = [];

        if (arbetsformaga.nedsattMed25) {
            var el25 = this.sjukskrivning['25'];
            promisesArr.push(el25.fran.sendKeys(arbetsformaga.nedsattMed25.from)
                .then(function() {
                    return el25.till.sendKeys(arbetsformaga.nedsattMed25.tom);

                })
            );
        }
        if (arbetsformaga.nedsattMed50) {
            var el50 = this.sjukskrivning['50'];
            promisesArr.push(el50.fran.sendKeys(arbetsformaga.nedsattMed50.from)
                .then(function() {
                    return el50.till.sendKeys(arbetsformaga.nedsattMed50.tom);
                })
            );
        }
        if (arbetsformaga.nedsattMed75) {
            var el75 = this.sjukskrivning['75'];
            promisesArr.push(el75.fran.sendKeys(arbetsformaga.nedsattMed75.from)
                .then(function() {
                    return el75.till.sendKeys(arbetsformaga.nedsattMed75.tom);
                }));
        }
        if (arbetsformaga.nedsattMed100) {
            var el100 = this.sjukskrivning['100'];
            promisesArr.push(el100.fran.sendKeys(arbetsformaga.nedsattMed100.from)
                .then(function() {
                    return el100.till.sendKeys(arbetsformaga.nedsattMed100.tom);
                })
            );
        }
        return Promise.all(promisesArr);
    },
    angeResorTillArbete: function(resor) {
        if (resor) {
            return this.sjukskrivning.arbetsresor.ja.sendKeys(protractor.Key.SPACE);
        } else {
            return this.sjukskrivning.arbetsresor.nej.sendKeys(protractor.Key.SPACE);
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
            var promisesArr = [];
            for (var i = 0; i < atgarder.length; i++) {
                if (atgarder[i].beskrivning) {
                    promisesArr.push(
                        beskrivningEL.sendKeys(atgarder[i].beskrivning + '\n')
                    );
                }
            }
            return Promise.all(promisesArr);
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
                } else if (sysselsattning.programAktiviteter) {
                    return sysselsattningEL.arbetsmarknadspolitisktProgramBeskrivning.sendKeys(sysselsattning.programAktiviteter);
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

module.exports = new LisuUtkast();
