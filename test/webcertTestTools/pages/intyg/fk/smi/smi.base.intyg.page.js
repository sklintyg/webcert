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

        this.patientAdress = {
            postadress: element(by.id('patient_postadress')),
            postnummer: element(by.id('patient_postnummer')),
            postort: element(by.id('patient_postort'))
        };

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
            kanGoraTrotsBegransning: element(by.id('formagaTrotsBegransning'))
        };

        this.andraMedicinskaUtredningar = {
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
            anledning: element(by.id('anledningTillKontakt')),
            verify: function(data) {
                if (data.kontaktMedFk) {
                    expect(that.kontaktFK.value.getText()).toBe('Ja');
                } else {
                    expect(that.kontaktFK.value.getText()).toBe('Nej');
                }
            }
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

    whenCertificateLoaded: function() {
        return browser.wait(this.certficate.isDisplayed());
    }

});

module.exports = BaseSmiIntygPage;
