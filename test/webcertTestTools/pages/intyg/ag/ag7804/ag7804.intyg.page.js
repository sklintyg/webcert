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

'use strict';

var AgBaseIntyg = require('../ag.base.intyg.page.js');

var Ag7804Intyg = AgBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ag7804';
        this.intygTypeVersion = '1.0';
        this.certficate = element(by.id('certificate'));
        this.smittskydd = element(by.id('avstangningSmittskydd'));
        this.baseratPa = {
            minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
            telefonkontakt: element(by.id('telefonkontaktMedPatienten')),
            annat: element(by.id('annatGrundForMU')),
            annatBeskrivning: element(by.id('annatGrundForMUBeskrivning'))
        };
        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.behandling = {
            pagaende: element(by.id('pagaendeBehandling')),
            planerad: element(by.id('planeradBehandling'))
        };
        this.diagnoser = {
            onskarFormedlaDiagnos: element(by.id('onskarFormedlaDiagnos')),
            getDiagnos: function(index) {
                index = index || 0;
                return {
                    kod: element(by.id('diagnoser-row' + index + '-col0')),
                    beskrivning: element(by.id('diagnoser-row' + index + '-col1'))
                };
            }
        };
        this.konsekvenser = {
            onskarFormedlaFunktionsnedsattning: element(by.id('onskarFormedlaFunktionsnedsattning')),
            funktionsnedsattning: element(by.id('funktionsnedsattning')),
            aktivitetsbegransning: element(by.id('aktivitetsbegransning'))
        };
        this.sysselsattning = {
            list: element(by.id('sysselsattning-list'))
        };
        this.sjukskrivningar = {
            grad: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col0'));
            },
            from: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col1'));
            },
            to: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col2'));
            }
        };
        this.arbetsformagaFMB = element(by.id('forsakringsmedicinsktBeslutsstod'));

        this.arbetstidsforlaggning = {
            val: element(by.id('arbetstidsforlaggning')),
            motivering: element(by.id('arbetstidsforlaggningMotivering'))
        };
        this.resorTillArbete = element(by.id('arbetsresor'));
        this.prognosForArbetsformaga = element(by.id('prognos-typ'));
        this.atgarder = element(by.id('arbetslivsinriktadeAtgarder'));
        this.atgarderBeskrivning = element(by.id('arbetslivsinriktadeAtgarderBeskrivning'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.ovrigt = element(by.id('ovrigt'));
        this.kontakt = {
            onskas: element(by.id('kontaktMedAg')),
            anledning: element(by.id('anledningTillKontakt'))
        };

    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        this.verifieraDiagnos(data);

        this.verifieraOvrigt(data);

        this.verifyArbetsformaga(data.arbetsformaga);

        if (!data.smittskydd) {
            this.verifieraBaseratPa(data);

            this.verifieraSysselsattning(data);

            this.verifieraKonsekvenser(data);

            this.verifieraMedicinskbehandling(data);

            this.verifieraKontakt(data);

        }
    },
    verifieraBaseratPa: function(data) {
        if (data.baseratPa.minUndersokningAvPatienten) {
            expect(this.baseratPa.minUndersokningAvPatienten.getText()).toBe(data.baseratPa.minUndersokningAvPatienten);
        }

        if (data.baseratPa.journaluppgifter) {
            expect(this.baseratPa.journaluppgifter.getText()).toBe(data.baseratPa.journaluppgifter);
        }

        if (data.baseratPa.telefonkontakt) {
            expect(this.baseratPa.telefonkontakt.getText()).toBe(data.baseratPa.telefonkontakt);
        }

        if (data.baseratPa.annat) {
            expect(this.baseratPa.annat.getText()).toBe(data.baseratPa.annat);
        }

        if (data.baseratPa.annatBeskrivning) {
            expect(this.baseratPa.annatBeskrivning.getText()).toBe(data.baseratPa.annatBeskrivning);
        }

    },
    verifieraKonsekvenser: function(data) {
        if (data.onskarFormedlaFunktionsnedsattning) {
            expect(this.konsekvenser.onskarFormedlaFunktionsnedsattning.getText()).toBe('Ja');
            expect(this.konsekvenser.funktionsnedsattning.getText()).toBe(data.funktionsnedsattning);
        } else {
            expect(this.konsekvenser.onskarFormedlaDiagnos.getText()).toBe('Ej angivet');
        }
        if (data.aktivitetsbegransning) {
            expect(this.konsekvenser.aktivitetsbegransning.getText()).toBe(data.aktivitetsbegransning);
        } else {
            expect(this.konsekvenser.aktivitetsbegransning.getText()).toBe('Ej angivet');
        }
    },

    verifieraDiagnos: function(data) {

        if (data.diagnos.onskarFormedlaDiagnos) {
            expect(this.diagnoser.onskarFormedlaDiagnos.getText()).toBe('Ja');
            for (var j = 0; j < data.diagnos.rows.length; j++) {
                expect(this.diagnoser.getDiagnos(j).kod.getText()).toBe(data.diagnos.rows[j].kod);
            }
        } else {
            expect(this.diagnoser.onskarFormedlaDiagnos.getText()).toBe('Nej');
        }
    },

    verifieraMedicinskbehandling: function(data) {

        if (data.medicinskbehandling.pagaende) {
            expect(this.behandling.pagaende.getText()).toBe(data.medicinskbehandling.pagaende);
        }

        if (data.medicinskbehandling.planerad) {
            expect(this.behandling.planerad.getText()).toBe(data.medicinskbehandling.planerad);
        }

    },

    verifyArbetsformaga: function(arbetsformaga) {

        var formagor = [];

        if (arbetsformaga.nedsattMed100) {
            formagor.push(arbetsformaga.nedsattMed100);
        }

        if (arbetsformaga.nedsattMed75) {
            formagor.push(arbetsformaga.nedsattMed75);
        }

        if (arbetsformaga.nedsattMed50) {
            formagor.push(arbetsformaga.nedsattMed50);
        }

        if (arbetsformaga.nedsattMed25) {
            formagor.push(arbetsformaga.nedsattMed25);
        }

        for (var i = 0; i < formagor.length; i++) {
            expect(this.sjukskrivningar.from(i).getText()).toBe(formagor[i].from);
            expect(this.sjukskrivningar.to(i).getText()).toBe(formagor[i].tom);
        }
    },
    verifieraKontakt: function(data) {
        if (data.kontaktMedAg) {
            expect(this.kontakt.onskas.getText()).toBe('Ja');
        } else {
            expect(this.kontakt.onskas.getText()).toBe('Ej angivet');
        }

        if (data.anledningTillKontakt) {
            expect(this.kontakt.anledning.getText()).toBe(data.anledningTillKontakt);
        }
    },
    verifieraSysselsattning: function(data) {
        expect(element(by.id('sysselsattning-0')).getText()).toBe(data.sysselsattning.translated);
    },

    verifieraOvrigt: function(data) {
        expect(this.ovrigt.getText()).toBe(data.ovrigt);
    },

    whenCertificateLoaded: function() {
        var that = this;

        return browser.sleep(2000).then(function() {
            //1 sec sleep för GET request och page/angular reload
            return browser.wait(that.certficate.isPresent(), 15000).then(function() {
                //15sec är timeout
                return browser.wait(that.certficate.isDisplayed(), 15000);
            });
        }).catch(function(e) {
            //Debug
            browser.getCurrentUrl().then(function(url) {
                logger.warn('url: ' + url);
                console.trace(e);
                throw (e.message);
            });
        });
    }
});

module.exports = new Ag7804Intyg();
