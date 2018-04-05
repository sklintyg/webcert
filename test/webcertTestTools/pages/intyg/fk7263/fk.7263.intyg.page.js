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

/*globals protractor, browser*/

/**
 * Created by bennysce on 09/06/15.
 */

'use strict';

var BaseIntyg = require('../base.intyg.page.js');

var Fk7263Intyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'fk7263';
        this.certficate = element(by.id('certificate'));

        this.field1 = {
            text: element(by.id('avstangningSmittskydd'))
        };

        //Fält 2 | Diagnos/diagnoser för sjukdom som orsakar nedsatt arbetsförmåga
        //Fält 2 | Diagnoskod enligt ICD-10 (huvuddiagnos)
        this.field2 = {
            diagnoskod: element(by.id('diagnosKod')),
            diagnosBeskrivning: element(by.id('diagnosBeskrivning'))
        };

        //Fält 3 | Aktuellt sjukdomsförlopp
        this.field3 = {
            sjukdomsforlopp: element(by.id('sjukdomsforlopp'))
        };

        //Fält 4 | Funktionsnedsättningar – observationer, undersökningsfynd och utredningsresultat
        //Fält 4 | Jag baserar uppgifterna på
        this.field4 = {
            funktionsnedsattning: element(by.id('funktionsnedsattning'))
        };
        this.field4b = {
            undersokningAvPatienten: element(by.id('undersokningAvPatienten')),
            telefonKontakt: element(by.id('telefonkontaktMedPatienten')),
            journaluppgifter: element(by.id('journaluppgifter')),
            annat: element(by.id('annanReferens')),
            annanReferensBeskrivning: element(by.id('annanReferensBeskrivning'))


        };

        //Fält 5 | Aktivitetsbegränsning relaterat till diagnos (fält 2) och funktionsnedsättning (fält 4)
        this.field5 = {
            aktivitetsbegransning: element(by.id('aktivitetsbegransning'))
        };

        //Fält 6a | Rekommendationer 
        this.field6a = {
            kontaktArbetsformedlingen: element(by.id('rekommendationKontaktArbetsformedlingen')),
            kontaktForetagshalsovarden: element(by.id('rekommendationKontaktForetagshalsovarden')),
            ovrigt: element(by.id('rekommendationOvrigt')),
            rekommendationOvrigtBeskrivning: element(by.id('rekommendationOvrigt'))
        };

        //Fält 6b | Planerad eller pågående behandling eller åtgärd 

        // Fält 7 | Är arbetslivsinriktad rehabilitering aktuell?
        this.field7 = {
            text: element(by.id('rehabilitering')),
            block: element(by.id('rehabilitering'))
        };

        //Fält 8a | Patientens arbetsförmåga bedöms i förhållande till
        this.field8a = {
            nuvarandeArbete: element(by.id('nuvarandeArbetsuppgifter')),
            nuvarandeArbeteText: element(by.id('nuvarandeArbetsuppgifter-text')),
            arbetsloshet: element(by.id('arbetsloshet')),
            foraldrarledighet: element(by.id('foraldrarledighet'))
        };

        //Fält 8b | Jag bedömer att patientens arbetsförmåga är
        this.field8b = {
            nedsat25: {
                from: element(by.id('nedsattMed25-row-col1')),
                tom: element(by.id('nedsattMed25-row-col2'))
            },
            nedsat50: {
                from: element(by.id('nedsattMed50-row-col1')),
                tom: element(by.id('nedsattMed50-row-col2'))
            },
            nedsat75: {
                from: element(by.id('nedsattMed75-row-col1')),
                tom: element(by.id('nedsattMed75-row-col2'))
            },
            nedsat100: {
                from: element(by.id('nedsattMed100-row-col1')),
                tom: element(by.id('nedsattMed100-row-col2'))
            }
        };

        //Fält 9 | Patientens arbetsförmåga bedöms nedsatt längre tid än den som det försäkringsmedicinska beslutsstödet anger, därför att

        //Fält 10 | Prognos – kommer patienten att få tillbaka sin arbetsförmåga i nuvarande arbete? (Gäller inte arbetslösa)
        this.field10 = {
            text: element(by.id('prognosBedomning'))
        };

        //Fält 11 | Kan resor till och från arbetet med annat färdsätt än normalt göra det möjligt för patienten att återgå i arbete?
        this.field11 = {
            text: element(by.id('resaTillArbetet')),
            block: element(by.id('resaTillArbetet'))
        };

        //Fält 12 | Kontakt önskas med Försäkringskassan
        this.field12 = {
            text: element(by.id('kontaktMedFk'))
        };

        //Fält 13 | Övriga upplysningar och förtydliganden
        this.field13 = {
            kommentar: element(by.id('kommentar'))
        };

        var panel = element(by.css('.qa-panel'));

        this.qaPanel = panel;
        this.qaPanels = element.all(by.css('.arende-panel'));

        this.FMBprognos = element(by.id('arbetsformagaPrognos'));

        this.prognosGarEJ = element(by.id('arbetsformataPrognosGarInteAttBedoma'));

        this.fornyaBtn = element(by.id('fornyaBtn'));
        this.fornyaDialog = {
            btn: element(by.id('button1fornya-dialog'))
        };

        this.prognosGIAB = element(by.id('arbetsformataPrognosGarInteAttBedoma'));
        this.prognosN = element(by.id('arbetsformataPrognosNej'));
        this.prognosJD = element(by.id('arbetsformataPrognosJaDelvis'));
        this.prognosJ = element(by.id('arbetsformataPrognosJa'));
        this.prognosFortyd = element(by.id('arbetsformagaPrognosGarInteAttBedomaBeskrivning'));
        this.forsKod = element(by.id('forskrivarkodOchArbetsplatskod'));

        this.atgarder = {
            sjukvard: element(by.id('atgardInomSjukvarden')),
            annan: element(by.id('annanAtgard'))
        };

        this.kontaktMedFk = element(by.id('kontaktMedFk'));
        this.answer = {
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };

        this.question = {
            newQuestionButton: element(by.id('askArendeBtn')),
            text: element(by.id('newQuestionText')),
            topic: element(by.id('new-question-topic')),
            kontakt: element(by.cssContainingText('option', 'Kontakt')),
            sendButton: element(by.id('sendQuestionBtn'))
        };
        this.forwardBtn = element(by.css('.btn.btn-default.vidarebefordra-btn.btn-info'));
        this.intygStatus = element(by.id('intyg-vy-laddad'));
        this.komplettera = {
            dialog: {
                modal: element(by.id('komplettering-modal-dialog')),
                modalDialogHeader: element(by.id('komplettering-modal-dialog')),
                lamnaOvrigaUpplysningarButton: element(by.id('komplettering-modal-dialog-answerWithNyttIntyg-button')),
                svaraMedMeddelandeButton: element(by.id('komplettering-modal-dialog-answerWithMessage-button')),
            }
        };
        this.enhetsAdress = {
            postAdress: element(by.id('vardperson_postadress')),
            postNummer: element(by.id('vardperson_postnummer')),
            postOrt: element(by.id('vardperson_postort')),
            enhetsTelefon: element(by.id('vardperson_telefonnummer'))
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    selectQuestionTopic: function(amne) {
        this.question.topic.element(by.cssContainingText('option', amne)).click();
    },
    getMarkAsHandledButtonForID: function(id) {
        return element(by.id('handleCheck-' + id));
    },
    getQAElementByText: function(containingText) {
        var panel = element(by.cssContainingText('.arende-panel', containingText));
        return {
            panel: panel,
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };
    },
    sendAnswerForMessageID: function(id, text) {
        return element(by.id('answerText-' + id)).sendKeys(text).then(function() {
            element(by.id('sendAnswerBtn-' + id)).sendKeys(protractor.Key.SPACE);
        });
    },
    markMessageAsHandled: function(id) {
        return this.getMarkAsHandledButtonForID(id).sendKeys(protractor.Key.SPACE);
    },
    clickKompletteraIntyg: function(id) {
        return element(by.id('komplettera-intyg-' + id)).sendKeys(protractor.Key.SPACE);
    },
    clickKanInteKomplettera: function(id) {
        return element(by.id('kan-inte-komplettera-' + id)).sendKeys(protractor.Key.SPACE);
    },
    clickFortsattPaUtkast: function(id) {
        return element(by.id('komplettera-open-utkast-' + id)).sendKeys(protractor.Key.SPACE);
    },
    getQAById: function(handled, id) {
        var subgroup = 'unhandled';
        if (handled) {
            subgroup = 'handled';
        }
        return element(by.id('arende-' + subgroup + '-' + id));
    },
    getKompletteringsDialog: function() {
        return this.komplettera.dialog;
    },
    veriferaBaseratPa: function(baserasPa) {
        if (baserasPa.journaluppgifter) {
            expect(this.field4b.journaluppgifter.getText()).toBe(baserasPa.journaluppgifter.datum);
        }
        if (baserasPa.minUndersokning) {
            expect(this.field4b.undersokningAvPatienten.getText()).toBe(baserasPa.minUndersokning.datum);
        }
        if (baserasPa.minTelefonkontakt) {
            expect(this.field4b.telefonKontakt.getText()).toBe(baserasPa.minTelefonkontakt.datum);
        }
        if (baserasPa.annat) {
            expect(this.field4b.annat.getText()).toBe(baserasPa.annat.datum);
        }
    },
    verifierOvrig: function(data) {

        var text = '';

        if (data.baserasPa && data.baserasPa.annat.text) {
            text += '4b: ' + data.baserasPa.annat.text + '. ';
        }

        if (data.prognos.val === 'Går inte att bedöma' && data.prognos.fortydligande) {
            text += '10: ' + data.prognos.fortydligande + '. ';
        }

        text += data.ovrigaUpplysningar;

        expect(this.field13.kommentar.getText()).toBe(text);
    },

    verifieraRekommendationer: function(rekommendationer) {
        expect(this.field6a.kontaktArbetsformedlingen.getText()).toBe(rekommendationer.kontaktMedArbetsformedlingen ? 'Ja' : 'Nej');
        expect(this.field6a.kontaktForetagshalsovarden.getText()).toBe(rekommendationer.kontaktForetagshalsovarden ? 'Ja' : 'Nej');

        if (rekommendationer.ovrigt) {
            expect(this.field6a.rekommendationOvrigtBeskrivning.getText()).toBe(rekommendationer.ovrigt);
        }
    },
    verifieraDiagnos: function(diagnos) {
        expect(this.field2.diagnoskod.getText()).toBe(diagnos.diagnoser[0].ICD10);

        var beskrivning = '';

        beskrivning += diagnos.diagnoser[0].diagnosText + '. ';

        if (diagnos.samsjuklighetForeligger) {
            beskrivning += 'Samsjuklighet föreligger. ';
        }

        beskrivning += diagnos.fortydligande;

        expect(this.field2.diagnosBeskrivning.getText()).toBe(beskrivning);
    },
    verifieraArbete: function(arbete) {

        if (arbete.nuvarandeArbete) {
            expect(this.field8a.nuvarandeArbete.isDisplayed()).toBeTruthy();
            expect(this.field8a.nuvarandeArbeteText.getText()).toBe(arbete.nuvarandeArbete.aktuellaArbetsuppgifter);
        }
        if (arbete.arbetsloshet) {
            expect(this.field8a.arbetsloshet.isDisplayed()).toBeTruthy();
        }
        if (arbete.foraldrarledighet) {
            expect(this.field8a.foraldrarledighet.isDisplayed()).toBeTruthy();
        }
    },
    verifieraArbetsformaga: function(arbetsformaga) {

        if (arbetsformaga.nedsattMed25) {
            expect(this.field8b.nedsat25.from.getText()).toBe(arbetsformaga.nedsattMed25.from);
            expect(this.field8b.nedsat25.tom.getText()).toBe(arbetsformaga.nedsattMed25.tom);
        }
        if (arbetsformaga.nedsattMed50) {
            expect(this.field8b.nedsat50.from.getText()).toBe(arbetsformaga.nedsattMed50.from);
            expect(this.field8b.nedsat50.tom.getText()).toBe(arbetsformaga.nedsattMed50.tom);
        }
        if (arbetsformaga.nedsattMed75) {
            expect(this.field8b.nedsat75.from.getText()).toBe(arbetsformaga.nedsattMed75.from);
            expect(this.field8b.nedsat75.tom.getText()).toBe(arbetsformaga.nedsattMed75.tom);
        }
        if (arbetsformaga.nedsattMed100) {
            expect(this.field8b.nedsat100.from.getText()).toBe(arbetsformaga.nedsattMed100.from);
            expect(this.field8b.nedsat100.tom.getText()).toBe(arbetsformaga.nedsattMed100.tom);
        }
    },
    verify: function(data) {

        expect(this.field1.text.getText()).toBe(data.smittskydd ? 'Ja' : 'Nej');

        if (!data.smittskydd) {
            this.veriferaBaseratPa(data.baserasPa);

            this.verifieraDiagnos(data.diagnos);

            expect(this.field3.sjukdomsforlopp.getText()).toBe(data.aktuelltSjukdomsforlopp);
            expect(this.field4.funktionsnedsattning.getText()).toBe(data.funktionsnedsattning);
            expect(this.field5.aktivitetsbegransning.getText()).toBe(data.aktivitetsBegransning);

            this.verifieraArbete(data.arbete);

            expect(this.atgarder.sjukvard.getText()).toBe(data.atgarder.planerad);
            expect(this.atgarder.annan.getText()).toBe(data.atgarder.annan);

            this.verifieraRekommendationer(data.rekommendationer);

            expect(this.field7.text.getText()).toBe(data.rekommendationer.arbetslivsinriktadRehab);
            expect(this.field11.text.getText()).toBe(data.rekommendationer.resor ? 'Ja' : 'Nej');
        }

        this.verifieraArbetsformaga(data.arbetsformaga);

        expect(this.FMBprognos.getText()).toBe(data.arbetsformagaFMB);

        expect(this.field10.text.getText()).toBe(data.prognos.val);

        expect(this.kontaktMedFk.getText()).toBe(data.kontaktOnskasMedFK ? 'Ja' : 'Nej');
        this.verifierOvrig(data);
    },

    whenCertificateLoaded: function() {
        return browser.wait(this.certficate.isDisplayed());
    }
});

module.exports = new Fk7263Intyg();
