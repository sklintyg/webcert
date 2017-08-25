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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,browser */
'use strict';

var FkBaseIntyg = require('../fk.base.intyg.page.js');

//TODO: Det är möjligt att vissa element här endast finns för LUSE, de bör flyttas bort.

var BaseSmiIntygPage = FkBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        var that = this;

        this.certficate = element(by.id('certificate'));
        this.notSentMessage = element(by.id('intyg-is-not-sent-to-fk-message-text'));

        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.ovrigt = element(by.id('ovrigt'));

        this.baseratPa = {
            minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
            telefonkontakt: element(by.id('telefonkontaktMedPatienten')),
            anhorigsBeskrivning: element(by.id('anhorigsBeskrivningAvPatienten')),
            annat: element(by.id('annatGrundForMU')),
            annatBeskrivning: element(by.id('annatGrundForMUBeskrivning')),
            personligKannedom: element(by.id('kannedomOmPatient'))
        };

        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));

        this.diagnoser = {
            getDiagnos: function(index) {
                index = index || 0;
                return {
                    kod: element(by.id('diagnoser-row' + index + '-col0')),
                    beskrivning: element(by.id('diagnoser-row' + index + '-col1'))
                };
            },
            grund: element(by.id('diagnosgrund')),
            nyBedomningDiagnosgrund: element(by.id('nyBedomningDiagnosgrund')),
            diagnosForNyBedomning: element(by.id('diagnosForNyBedomning'))
        };

        this.funktionsnedsattning = {
            intellektuell: element(by.id('funktionsnedsattningIntellektuell')),
            kommunikation: element(by.id('funktionsnedsattningKommunikation')),
            uppmarksamhet: element(by.id('funktionsnedsattningKoncentration')),
            annanPsykiskFunktion: element(by.id('funktionsnedsattningPsykisk')),
            synHorselTal: element(by.id('funktionsnedsattningSynHorselTal')),
            balans: element(by.id('funktionsnedsattningBalansKoordination')),
            annanKropsligFunktion: element(by.id('funktionsnedsattningAnnan'))
        };

        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

        this.behandling = {
            avslutad: element(by.id('avslutadBehandling')),
            pagaende: element(by.id('pagaendeBehandling')),
            planerad: element(by.id('planeradBehandling')),
            substansintag: element(by.id('substansintag'))
        };

        this.medicinskaForutsattningar = {
            kanUtvecklasOverTid: element(by.id('medicinskaForutsattningarForArbete')),
            kanGoraTrotsBegransning: element(by.id('formagaTrotsBegransning')),
            utecklasOverTid: element(by.id('medicinskaForutsattningarForArbete')),
            trotsBegransningar: element(by.id('formagaTrotsBegransning')),
            forslagTillAtgard: element(by.id('forslagTillAtgard'))
        };

        this.andraMedicinskaUtredningar = {
            value: element(by.id('underlagFinns')),
            field: element(by.id('form_underlagFinns')),
            getUtredning: function(index) {
                return {
                    typ: element(by.id('underlag-row' + index + '-col0')),
                    datum: element(by.id('underlag-row' + index + '-col1')),
                    info: element(by.id('underlag-row' + index + '-col2'))
                };
            }
        };

        this.ovrigaUpplysningar = element(by.id('ovrigt'));

        this.kontaktFK = {
            value: element(by.id('kontaktMedFk')),
            onskas: element(by.id('form_kontaktMedFk')),
            anledning: element(by.id('anledningTillKontakt'))
        };

        this.qaPanels = element.all(by.css('.arende-panel'));

        this.tillaggsfragor = {
            getFraga: function(id) {
                return element(by.id('tillaggsfragor-' + id));
            }
        };
        this.komplettera = {
            dialog: {
                svaraMedNyttIntygKnapp: element(by.id('komplettering-modal-dialog-answerWithNyttIntyg-button')),
                svaraMedTextKnapp: element(by.id('komplettering-modal-dialog-answerWithMessage-button'))
            }
        };
        this.kompletteringsAtgardDialog = element(by.id('komplettering-modal-dialog'));
        this.kompletteraMedNyttIntygButton = element(by.id('komplettering-modal-dialog-answerWithNyttIntyg-button'));
        this.kompletteraMedFortsattPaIntygsutkastButton = element(by.id('komplettering-modal-dialog-goToUtkast-button'));
        this.kompletteraMedMeddelandeButton = element(by.id('komplettering-modal-dialog-answerWithMessage-button'));

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

        if (data.baseratPa.personligKannedom) {
            expect(this.baseratPa.personligKannedom.getText()).toBe(data.baseratPa.personligKannedom);
        }

        if (data.baseratPa.anhorigsBeskrivning) {
            expect(this.baseratPa.anhorigsBeskrivning.getText()).toBe(data.baseratPa.anhorigsBeskrivning);
        }
    },

    verifieraDiagnos: function(data) {

        if (data.diagnos.narOchVarStalldesDiagnoserna) {
            expect(this.diagnoser.grund.getText()).toBe(data.diagnos.narOchVarStalldesDiagnoserna);
        }

        if (data.diagnos.diagnoser) {
            for (var j = 0; j < data.diagnos.diagnoser.length; j++) {
                expect(this.diagnoser.getDiagnos(j).kod.getText()).toBe(data.diagnos.diagnoser[j].kod);

                if (data.diagnos.diagnoser[j].beskrivning) {
                    expect(this.diagnoser.getDiagnos(j).beskrivning.getText()).toBe(data.diagnos.diagnoser[j].beskrivning);
                }
            }
        }
    },

    verifieraDiagnosBedomning: function(data) {
        if (data.diagnos.nyBedomning) {
            expect(this.diagnoser.nyBedomningDiagnosgrund.getText()).toBe('Ja');
            expect(this.diagnoser.diagnosForNyBedomning.getText()).toBe(data.diagnos.diagnosForNyBedomning);
        } else {
            expect(this.diagnoser.nyBedomningDiagnosgrund.getText()).toBe('Nej');
            expect(this.diagnoser.diagnosForNyBedomning.getText()).toBe('Ej angivet');
        }
    },

    verifieraAndraMedicinskaUtredningar: function(data) {
        if (data.andraMedicinskaUtredningar) {
            expect(this.andraMedicinskaUtredningar.value.getText()).toBe('Ja');

            for (var i = 0; i < data.andraMedicinskaUtredningar.length; i++) {
                var utredningEL = this.andraMedicinskaUtredningar.getUtredning(i);
                var utredningDatum = data.andraMedicinskaUtredningar[i].datum;
                expect(utredningEL.typ.getText()).toBe(data.andraMedicinskaUtredningar[i].underlag);
                expect(utredningEL.datum.getText()).toBe(utredningDatum);
                expect(utredningEL.info.getText()).toBe(data.andraMedicinskaUtredningar[i].infoOmUtredningen);
            }
        } else {
            expect(this.andraMedicinskaUtredningar.value.getText()).toBe('Nej');
        }
    },

    verifieraSjukdomsforlopp: function(data) {
        expect(this.sjukdomsforlopp.getText()).toBe(data.sjukdomsForlopp);
    },

    verifieraFunktionsnedsattning: function(data) {
        expect(this.funktionsnedsattning.intellektuell.getText()).toBe(data.funktionsnedsattning.intellektuell);
        expect(this.funktionsnedsattning.kommunikation.getText()).toBe(data.funktionsnedsattning.kommunikation);
        expect(this.funktionsnedsattning.uppmarksamhet.getText()).toBe(data.funktionsnedsattning.koncentration);
        expect(this.funktionsnedsattning.annanPsykiskFunktion.getText()).toBe(data.funktionsnedsattning.psykisk);
        expect(this.funktionsnedsattning.synHorselTal.getText()).toBe(data.funktionsnedsattning.synHorselTal);
        expect(this.funktionsnedsattning.balans.getText()).toBe(data.funktionsnedsattning.balansKoordination);
        expect(this.funktionsnedsattning.annanKropsligFunktion.getText()).toBe(data.funktionsnedsattning.annan);
    },

    verifieraAktivitetsbegransning: function(data) {
        expect(this.aktivitetsbegransning.getText()).toBe(data.aktivitetsbegransning);
    },

    verifieraMedicinskbehandling: function(data) {
        if (data.medicinskbehandling.avslutad) {
            expect(this.behandling.avslutad.getText()).toBe(data.medicinskbehandling.avslutad);
        }

        if (data.medicinskbehandling.pagaende) {
            expect(this.behandling.pagaende.getText()).toBe(data.medicinskbehandling.pagaende);
        }

        if (data.medicinskbehandling.planerad) {
            expect(this.behandling.planerad.getText()).toBe(data.medicinskbehandling.planerad);
        }

        if (data.medicinskbehandling.substansintag) {
            expect(this.behandling.substansintag.getText()).toBe(data.medicinskbehandling.substansintag);
        }
    },

    verifieraMedicinskaForutsattningar: function(data) {
        expect(this.medicinskaForutsattningar.utecklasOverTid.getText()).toBe(data.medicinskaForutsattningar.utecklasOverTid);
        expect(this.medicinskaForutsattningar.trotsBegransningar.getText()).toBe(data.medicinskaForutsattningar.trotsBegransningar);

        if (data.medicinskaForutsattningar.forslagTillAtgard) {
            expect(this.medicinskaForutsattningar.forslagTillAtgard.getText()).toBe(data.medicinskaForutsattningar.forslagTillAtgard);
        }
    },

    verifieraKontaktFK: function(data) {
        if (data.kontaktMedFk) {
            expect(this.kontaktFK.value.getText()).toBe('Ja');
        } else {
            expect(this.kontaktFK.value.getText()).toBe('Nej');
        }

        if (data.kontaktAnledning) {
            expect(this.kontaktFK.anledning.getText()).toBe(data.kontaktAnledning);
        }
    },

    verifieraTillaggsfragor: function(data) {
        if (data.tillaggsfragor) {
            for (var i = 0; i < data.tillaggsfragor.length; i++) {
                var fraga = data.tillaggsfragor[i];
                expect(this.tillaggsfragor.getFraga(fraga.id).getText()).toBe(fraga.svar);
            }
        }
    },

    verifieraOvrigt: function(data) {
        expect(this.ovrigt.getText()).toBe(data.ovrigt);
    },

    whenCertificateLoaded: function() {
        return browser.wait(this.certficate.isDisplayed());
    }

});

module.exports = BaseSmiIntygPage;
