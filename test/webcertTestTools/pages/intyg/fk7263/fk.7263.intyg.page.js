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

/*globals protractor*/

/**
 * Created by bennysce on 09/06/15.
 */

'use strict';

var BaseIntyg = require('../base.intyg.page.js');

var Fk7263Intyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'fk7263';

        this.field1 = {
            title: element(by.css('div[field-number="1"]')).element(by.css('.title')),
            text: element(by.css('div[field-number="1"]')).element(by.css('.intyg-block__content'))
        };

        this.field2 = {
            diagnoskod: element(by.id('diagnosKod')),
            diagnosBeskrivning: element(by.id('diagnosBeskrivning'))
        };
        this.field3 = {
            sjukdomsforlopp: element(by.id('sjukdomsforlopp'))
        };
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
        this.field5 = {
            aktivitetsbegransning: element(by.id('aktivitetsbegransning'))
        };

        this.field6a = {
            kontaktArbetsformedlingen: element(by.id('rekommendationKontaktArbetsformedlingen')),
            kontaktForetagshalsovarden: element(by.id('rekommendationKontaktForetagshalsovarden')),
            ovrigt: element(by.id('rekommendationOvrigt')),
            rekommendationOvrigtBeskrivning: element(by.id('rekommendationOvrigtBeskrivning'))
        };

        this.field7 = {
            text: element(by.id('field7')).element(by.css('.intyg-block__content'))
        };

        this.field8b = {
            nedsat25: {
                from: element(by.id('nedsattMed25from')),
                tom: element(by.id('nedsattMed25tom'))
            },
            nedsat50: {
                from: element(by.id('nedsattMed50from')),
                tom: element(by.id('nedsattMed50tom'))
            },
            nedsat75: {
                from: element(by.id('nedsattMed75from')),
                tom: element(by.id('nedsattMed75tom'))
            },
            nedsat100: {
                from: element(by.id('nedsattMed100from')),
                tom: element(by.id('nedsattMed100tom'))
            }
        };

        this.field10 = {
            title: element(by.css('div[field-number="10"]')).element(by.css('.title')),
            text: element(by.css('div[field-number="10"]')).element(by.css('.intyg-block__content'))
        };

        this.field11 = {
            title: element(by.css('div[field-number="11"]')).element(by.css('.title')),
            text: element(by.css('div[field-number="11"]')).element(by.css('.intyg-block__content'))
        };

        this.field12 = {
            title: element(by.id('field12')).element(by.css('.title')),
            text: element(by.id('field12')).element(by.css('.intyg-block__content'))
        };

        this.field13 = {
            kommentar: element(by.id('field13')).element(by.id('kommentar'))
        };

        var panel = element(by.css('.qa-panel'));

        this.qaPanel = panel;
        this.qaPanels = element.all(by.css('.qa-panel'));

        this.FMBprognos = element(by.id('arbetsformagaPrognos'));

        this.prognosGarEJ = element(by.id('arbetsformataPrognosGarInteAttBedoma'));

        this.copyBtn = element(by.id('copyBtn'));
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
        this.answer = {
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };

        this.question = {
            newQuestionButton: element(by.id('askQuestionBtn')),
            text: element(by.id('newQuestionText')),
            topic: element(by.id('new-question-topic')),
            kontakt: element(by.cssContainingText('option', 'Kontakt')),
            sendButton: element(by.id('sendQuestionBtn'))
        };
        this.forwardBtn = element(by.css('.btn.btn-default.vidarebefordra-btn.btn-info'));
        this.intygStatus = element(by.id('intyg-vy-laddad'));
        this.komplettera = {
            dialog: {
                //modal: element(by.id('komplettering-dialog')),
                modalDialogHeader: element(by.id('komplettering-dialog')),
                svaraMedNyttIntygKnapp: element(by.id('button1answerintyg-dialog')),
                svaraMedTextKnapp: element(by.id('button2answermessage-dialog')),
                fortsattPaIntygsutkastKnapp: element(by.id('button3gotoutkast-dialog'))
            }
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    selectQuestionTopic: function(amne) {
        this.question.topic.element(by.cssContainingText('option', amne)).click();
    },
    getMarkAsHandledButtonForID: function(id) {
        return element(by.id('qaunhandled-' + id)).element(by.css('input[type="checkbox"]'));
    },
    getQAElementByText: function(containingText) {
        var panel = element(by.cssContainingText('.qa-panel', containingText));
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
    svaraMedNyttIntyg: function(id) {
        return element(by.id('answerWithIntygBtn-' + id)).sendKeys(protractor.Key.SPACE);
    },
    fortsattPaIntygsutkast: function(id) {
        return element(by.id('continueWithUtkastBtn-' + id)).sendKeys(protractor.Key.SPACE);
    },
    getQAById: function(handled, id) {
        var subgroup = 'unhandled';
        if (handled) {
            subgroup = 'handled';
        }
        return element(by.id('qa' + subgroup + '-' + id));
    },
    getKompletteringsDialog: function() {
        return this.komplettera.dialog;
    }
});

module.exports = new Fk7263Intyg();
