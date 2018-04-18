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

/* globals protractor, browser*/

'use strict';

/* Samlingssida för funktionalitet som finns i högerfältet */

var WebcertBasePage = require('../webcert.base.page.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init();


var hogerfalt = WebcertBasePage._extend({
    init: function init() {
        init._super.call(this);
        let fragaSvar = {
            container: element(by.id('arende-panel-scrollable-body')),
            meddelande: function(messageId) {
                var obj = {};
                obj.frageText = element(by.id('kompletteringar-arende-fragetext-' + messageId));
                obj.komplettering = {
                    hanterad: element(by.id('arende-handled-' + messageId)),
                    ohanterad: element(by.id('arende-unhandled-' + messageId)),
                    button: element(by.id('komplettera-intyg'))
                };
                obj.administrativFraga = {
                    svaraMedTxt: function(text) {
                        return element(by.id('arende-answer-button-' + messageId)).typeKeys(protractor.Key.SPACE).then(function() {
                            return element(by.id('answerText-' + messageId)).typeKeys(text);
                        }).then(function() {
                            return element(by.id('sendAnswerBtn-' + messageId)).typeKeys(protractor.Key.SPACE);
                        });
                    },
                    hanterad: element(by.id('handleCheck-' + messageId)),
                    togglaHanterad: function() {
                        return element(by.id('handleCheck-' + messageId)).typeKeys(protractor.Key.SPACE);
                    }
                };
                return obj;
            },
            administrativFraga: {
                menyVal: element(by.id('arende-filter-administrativafragor')),
                nyfraga: {
                    text: element(by.id('arendeNewModelText')),
                    topic: {
                        selection: element(by.id('new-question-topic-selected-item-label')),
                        kontakt: element(by.id('new-question-topic-KONTKT')),
                        ovrigt: element(by.id('new-question-topic-OVRIGT')),
                        avstamningsmote: element(by.id('new-question-topic-AVSTMN'))
                    },
                    sendButton: element(by.id('sendArendeBtn'))
                },
                vidarebefordra: element(by.id('vidarebefordraEjHanterad'))
            },
            komplettering: {
                menyVal: element(by.id('arende-filter-kompletteringsbegaran'))
            },
            selectQuestionTopic: function(amne) {
                let topic = fragaSvar.administrativFraga.nyfraga.topic;
                return topic.selection.click().then(function() {
                    return browser.sleep(10000);
                }).then(function() {
                    if (amne === 'Avstämningsmöte') {
                        return topic.avstamningsmote.click();
                    } else if (amne === 'Kontakt') {
                        return topic.kontakt.click();
                    } else if (amne === 'Övrigt') {
                        return topic.ovrigt.click();
                    }
                    throw ('Okänt ämne på frågan');
                });
            },
            getMessageIdFrom: {
                arendePanel: function(index) {
                    return fragaSvar.container.all(by.css('.card')).then(function(elments) {
                        return elments[index].getAttribute('id');
                    }).then(function(domId) {
                        var string = domId.split('-');
                        var splitIndex = string[0].length + string[1].length + 2;
                        var fragaId = domId.substr(splitIndex, domId.length);

                        return fragaId;
                    });
                }
            }
        };
        this.fragaSvar = fragaSvar;
        this.tipsOchTricks = { /*TODO*/ };
        this.fmb = { /*TODO*/ };
    }
});



module.exports = new hogerfalt();
