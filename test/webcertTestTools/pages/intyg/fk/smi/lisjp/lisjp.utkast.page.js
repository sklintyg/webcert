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

/*globals element,by,Promise,protractor,browser*/
'use strict';

var BaseSmiUtkast = require('../smi.base.utkast.page.js');
var pageHelpers = require('../../../../pageHelper.util.js');

var LisjpUtkast = BaseSmiUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'lisjp';
        this.intygTypeVersion = '1.0';
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
                nuvarandeArbete: element(by.id('sysselsattning-NUVARANDE_ARBETE')),
                arbetssokande: element(by.id('sysselsattning-ARBETSSOKANDE')),
                foraldraledighet: element(by.id('sysselsattning-FORALDRALEDIG')),
                studier: element(by.id('sysselsattning-STUDIER'))
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
            prognos: {
                form: element(by.id('form_prognos')),
                inom: element(by.css('#prognosDagarTillArbete-1-typ > div.plate')),
                select: element(by.css('#prognosDagarTillArbete-1-typ > div.dropdown-label'))
            }
        };
        this.atgarder = {

            ejAktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderEjAktuelltBeskrivning')),
            aktuelltBeskrivning: element(by.id('arbetslivsinriktadeAtgarderAktuelltBeskrivning'))

        };
        this.arbetslivsinriktadeAtgarderBeskrivning = element(by.id('arbetslivsinriktadeAtgarderBeskrivning'));
        var panel = element(by.css('.arende-panel'));
        this.arendePanel = panel;
        this.srs = {
            knapp: () => element(by.buttonText('SRS')),
            panel: () => element(by.tagName('wc-srs-content')),
            samtycke: {
                ja: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('value').then(v => v === 'JA')).first(),
                nej: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('value').then(v => v === 'NEJ')).first()
            },
            visamer: () => element.all(by.id('questionsCollapser')),
            visaKnapp: () => element(by.buttonText('Visa')),
            fragor: () => element(by.tagName('wc-srs-questionaire')),
            prediktion: () => element(by.id('predictionBox')),
            flik: linkText => element(by.linkText(linkText)),
            atgarder: () => element(by.id('atgarder2')), //SRS rutan vid diagnos antas
            statistik: () => element(by.id('statstics2')), //SRS rutan vid diagnos antas
            atgarderRek: () => element(by.id('atgarderRek')),
            atgarderObs: () => element(by.id('atgarderObs')),
            questionsCollapser: () => element(by.id('questionsCollapser')),

        };
    },
    get: function get(intygId) {
        get._super.call(this, 'lisjp', intygId);
    },
    angeAktivitetsbegransning: function(text) {
        return pageHelpers.moveAndSendKeys(this.konsekvenser.aktivitetsbegransning, text, text);
    },
    angeFunktionsnedsattning: function(text) {
        return pageHelpers.moveAndSendKeys(this.konsekvenser.funktionsnedsattning, text, text);
    },
    angeMedicinskBehandling: function(behandling) {
        if (behandling) {
            var fn = this.medicinskbehandling;
            return pageHelpers.moveAndSendKeys(fn.pagaende, behandling.pagaende, behandling.pagaende).then(function() {
                return pageHelpers.moveAndSendKeys(fn.planerad, behandling.planerad, behandling.planerad);
            });
        } else {
            return Promise.resolve();
        }
    },
    angeArbetsformaga: function(arbetsformaga) {
        var el25 = this.sjukskrivning['25'];
        var el50 = this.sjukskrivning['50'];
        var el75 = this.sjukskrivning['75'];
        var el100 = this.sjukskrivning['100'];

        var promisesArr = [];

        if (arbetsformaga.nedsattMed25) {
            promisesArr.push(el25.fran.clear());
            promisesArr.push(el25.till.clear());
            promisesArr.push(pageHelpers.moveAndSendKeys(el25.fran, arbetsformaga.nedsattMed25.from, arbetsformaga.nedsattMed25.from)
                .then(function() {
                    return pageHelpers.moveAndSendKeys(el25.till, arbetsformaga.nedsattMed25.tom, arbetsformaga.nedsattMed25.tom);

                })
            );
        }
        if (arbetsformaga.nedsattMed50) {
            promisesArr.push(el50.fran.clear());
            promisesArr.push(el50.till.clear());
            promisesArr.push(pageHelpers.moveAndSendKeys(el50.fran, arbetsformaga.nedsattMed50.from, arbetsformaga.nedsattMed50.from)
                .then(function() {
                    return pageHelpers.moveAndSendKeys(el50.till, arbetsformaga.nedsattMed50.tom, arbetsformaga.nedsattMed50.tom);
                })
            );
        }
        if (arbetsformaga.nedsattMed75) {
            promisesArr.push(el75.fran.clear());
            promisesArr.push(el75.till.clear());
            promisesArr.push(pageHelpers.moveAndSendKeys(el75.fran, arbetsformaga.nedsattMed75.from, arbetsformaga.nedsattMed75.from)
                .then(function() {
                    return pageHelpers.moveAndSendKeys(el75.till, arbetsformaga.nedsattMed75.tom, arbetsformaga.nedsattMed75.tom);
                }));
        }
        if (arbetsformaga.nedsattMed100) {
            promisesArr.push(el100.fran.clear());
            promisesArr.push(el100.till.clear());
            promisesArr.push(pageHelpers.moveAndSendKeys(el100.fran, arbetsformaga.nedsattMed100.from, arbetsformaga.nedsattMed100.from)
                .then(function() {
                    return pageHelpers.moveAndSendKeys(el100.till, arbetsformaga.nedsattMed100.tom, arbetsformaga.nedsattMed100.tom);
                })
            );
        }

        return Promise.all(promisesArr);


    },
    angeResorTillArbete: function(resor) {
        if (resor) {
            return pageHelpers.moveAndSendKeys(this.sjukskrivning.arbetsresor, protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeSmittskydd: function(value) {
        if (value) {
            return pageHelpers.moveAndSendKeys(this.smittskydd, protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeDiagnos: function(diagnos) {
        var el = this.diagnoseCode;
        return pageHelpers.moveAndSendKeys(el, diagnos.kod, diagnos.kod).then(function() {
            return pageHelpers.moveAndSendKeys(el, protractor.Key.TAB, 'TAB');
        });

    },
    angeArbetstidsforlaggning: function(arbetstidsforlaggning) {
        var el = this.sjukskrivning.arbetstidsforlaggning;
        if (!arbetstidsforlaggning) {
            return Promise.resolve();
        } else {
            if (arbetstidsforlaggning.val === 'Ja') {
                return pageHelpers.moveAndSendKeys(el.ja, protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(el.beskrivning, arbetstidsforlaggning.beskrivning, arbetstidsforlaggning.beskrivning);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(el.nej, protractor.Key.SPACE);
            }
        }
    },
    angeAtgarder: function(atgarder) {
        var beskrivningEL = this.arbetslivsinriktadeAtgarderBeskrivning;

        var elementsToCheck = [];
        atgarder.forEach(function(atgard) {
            elementsToCheck.push('arbetslivsinriktadeAtgarder-' + atgard.key);
        });

        return pageHelpers.selectCheckBoxesById(elementsToCheck).then(function() {
            var beskrivning = '';
            atgarder.forEach(function(atgard) {

                if (atgard.beskrivning) {
                    beskrivning += atgard.beskrivning + '\n';
                }
            });
            if (beskrivning) {
                return pageHelpers.moveAndSendKeys(beskrivningEL, beskrivning, beskrivning);
            } else {
                return Promise.resolve();
            }
        });




    },
    angeSysselsattning: function(sysselsattning) {
        var sysselsattningEL = this.sysselsattning;
        return element(by.id('sysselsattning-' + sysselsattning.typ)).click()
            .then(function() {
                if (sysselsattning.yrkesAktiviteter) {
                    return pageHelpers.moveAndSendKeys(sysselsattningEL.nuvarandeArbeteBeskrivning, sysselsattning.yrkesAktiviteter, sysselsattning.yrkesAktiviteter);
                } else {
                    return Promise.resolve();
                }
            });
    },
    angePrognosForArbetsformaga: function(prognos) {
        var prognosEL = this.sjukskrivning.prognos;
        return pageHelpers.moveAndSendKeys(prognosEL.form.element(by.id('prognos-' + prognos.name)), protractor.Key.SPACE, 'angePrognosForArbetsformaga').then(function() {
            if (prognos.within) {

                var frontEndJS = 'Element.prototype.documentOffsetTop = function () {';
                frontEndJS += ' return this.offsetTop + ( this.offsetParent ? this.offsetParent.documentOffsetTop() : 0 );';
                frontEndJS += ' };';
                frontEndJS += 'var top = document.getElementById("prognos-ATER_X_ANTAL_DGR").documentOffsetTop() - (window.innerHeight / 2 );';
                frontEndJS += ' window.scrollTo( 0, top );';

                return browser.executeScript(frontEndJS).then(function() {
                    return prognosEL.select.click();
                }).then(function() {
                    // Vänta på att drop-down meny öppnas.
                    return browser.sleep(500);
                }).then(function() {
                    return prognosEL.inom.element(by.cssContainingText('div', prognos.within)).click();
                });
            } else {
                return Promise.resolve();
            }
        });

    }

});

module.exports = new LisjpUtkast();
