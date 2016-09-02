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

        this.certficate = element(by.id('certificate'));
        this.notSentMessage = element(by.id('intyg-is-not-sent-to-fk-message-text'));

        this.diagnoseCode = element(by.id('diagnoseCode'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.pagaendeBehandling = element(by.id('pagaendeBehandling'));
        this.planeradBehandling = element(by.id('planeradBehandling'));
        this.ovrigt = element(by.id('ovrigt'));
        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));

        this.baseratPa = {
            minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
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
                    kod: element(by.id('diagnoser-' + index + '-kod')),
                    beskrivning: element(by.id('diagnoser-' + index + '-beskrivning'))
                };
            },
            grund: element(by.id('diagnosgrund')),
            nyBedomningDiagnosgrundJa: element(by.id('nyBedomningDiagnosgrund-Ja')),
            nyBedomningDiagnosgrundNej: element(by.id('nyBedomningDiagnosgrund-Nej')),
            nyBedomningDiagnosgrund: element(by.cssContainingText('.intyg-field', 'Finns skäl till att revidera/uppdatera tidigare satt diagnos?')),
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

        this.ovrigaUpplysningar = element(by.id('ovrigt'));

        this.kontaktFK = {
            onskas: element(by.cssContainingText('.intyg-field', 'Jag önskar att Försäkringskassan kontaktar mig')),
            ja: element(by.id('kontaktMedFk-Ja')),
            nej: element(by.id('kontaktMedFk-Nej')),
            anledning: element(by.id('anledningTillKontakt'))
        };

        this.qaPanels = element.all(by.css('.arende-panel'));

        this.tillaggsfragor = {
            getFraga: function(index) {
                index = index + 1 || 1;
                return element(by.id('tillaggsfraga-900' + index));
            }
        };
        this.kompletteringsAtgardDialog = element(by.id('komplettering-modal-dialog'));
        this.kompletteraMedNyttIntygButton = element(by.id('komplettering-modal-dialog-answerWithNyttIntyg-button'));
        this.kompletteraMedMeddelandeButton = element(by.id('komplettering-modal-dialog-answerWithMessage-button'));

    },

    whenCertificateLoaded: function() {
        return browser.wait(this.certficate.isDisplayed());
    }

});

module.exports = BaseSmiIntygPage;
