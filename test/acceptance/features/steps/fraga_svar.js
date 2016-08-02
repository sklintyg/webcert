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

/* globals pages, intyg, browser, protractor, logger, JSON, wcTestTools*/

'use strict';
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var lisuUtkastPage = pages.intyg.lisu.utkast;
var helpers = require('./helpers');
var soap = require('soap');
var soapMessageBodies = require('./soap');
var testdataHelper = wcTestTools.helpers.testdata;

function kontrolleraKompletteringsFragaHanterad(kontrollnr) {
    return expect(element(by.cssContainingText('.qa-block-handled', kontrollnr)).isPresent()).to.eventually.be.ok;
}

module.exports = function() {
    this.Given(/^jag skickar en fråga med ämnet "([^"]*)" till Försäkringskassan$/, function(amne, callback) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        console.log('isSMIIntyg : ' + isSMIIntyg);

        if (isSMIIntyg) {
            lisuUtkastPage.arendeQuestion.newArendeButton.sendKeys(protractor.Key.SPACE);
            lisuUtkastPage.arendeQuestion.text.sendKeys('En ' + amne + '-fråga');
            lisuUtkastPage.selectQuestionTopic(amne);

            lisuUtkastPage.arendeQuestion.sendButton.sendKeys(protractor.Key.SPACE);

            lisuUtkastPage.arendePanel.getAttribute('id').then(function(result) {
                var element = result.split('-');
                var splitIndex = element[0].length + element[1].length + 2;
                var fragaId = result.substr(splitIndex, result.length);
                intyg.fragaId = fragaId;
                logger.debug('Frågans ID: ' + intyg.fragaId);
            }).then(callback);

        } else {
            fkIntygPage.question.newQuestionButton.sendKeys(protractor.Key.SPACE);
            fkIntygPage.question.text.sendKeys('En ' + amne + '-fråga');
            fkIntygPage.selectQuestionTopic(amne);

            fkIntygPage.question.sendButton.sendKeys(protractor.Key.SPACE);

            fkIntygPage.qaPanel.getAttribute('id').then(function(result) {
                intyg.fragaId = result.split('-')[1];
                logger.debug('Frågans ID: ' + intyg.fragaId);
                callback();
            });
        }

    });

    this.Given(/^jag väljer att svara med ett nytt intyg$/, function(callback) {
        var fragaText = global.intyg.guidcheck;

        if (!intyg.messages || intyg.messages.length <= 0) {
            callback('Inga frågor hittades');
        } else {
            var svaraBtn = fkIntygPage.getQAElementByText(fragaText).panel.element(by.cssContainingText('.btn-success', ' Svara med nytt intyg'));
            svaraBtn.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    //Fulhack för att inte global ska innehålla en referens
                    global.ursprungligtIntyg = JSON.parse(JSON.stringify(intyg));
                    callback();
                });
        }
    });

    this.Given(/^ska jag se kompletteringsfrågan på (intygs|utkast)\-sidan$/, function(typ, callback) {
        var fragaText;

        if (typ === 'intygs') {
            fragaText = global.intyg.guidcheck;
        } else {
            fragaText = global.ursprungligtIntyg.guidcheck;
        }

        console.log('Letar efter fråga som innehåller text: ' + fragaText);
        expect(fkUtkastPage.getQAElementByText(fragaText).panel.isPresent()).to.become(true).then(function() {
            logger.info('OK - hittade fråga med text: ' + fragaText);
            callback();
        }, function(reason) {
            callback('FEL : ' + reason);
        });
    });

    // this.Given(/^ska jag se kompletteringsfrågan på intygs\-sidan$/, function(callback) {

    //     var fragaText = global.intyg.guidcheck;

    //     console.log('Letar efter fråga som innehåller text: ' + fragaText);
    //     expect(fkIntygPage.getQAElementByText(fragaText).panel.isPresent()).to.become(true).then(function() {
    //         logger.info('OK - hittade fråga med text: ' + fragaText);
    //         callback();
    //     }, function(reason) {
    //         callback('FEL : ' + reason);
    //     });
    // });

    this.Given(/^jag ska inte kunna komplettera med nytt intyg från webcert/, function(callback) {
        var answerWithIntygBtnId = 'answerWithIntygBtn-' + global.intyg.messages[0].id;
        expect(element(by.id(answerWithIntygBtnId)).isPresent()).to.eventually.not.be.ok.and.notify(callback);
    });

    this.Given(/^jag ska se en varningstext för svara med nytt intyg/, function(callback) {
        var kompletteringsFraga = fkIntygPage.getQAElementByText(global.intyg.guidcheck).panel;

        expect(kompletteringsFraga.element(by.cssContainingText('.alert-warning',
            'Gå tillbaka till journalsystemet för att svara på kompletteringsbegäran med nytt intyg.')).isPresent()).
        to.eventually.be.ok.and.notify(callback);
    });

    this.Given(/^jag ska kunna svara med textmeddelande/, function() {
        var kompletteringsFraga = fkIntygPage.getQAElementByText(global.intyg.guidcheck).panel;
        var textSvar = 'Ett kompletteringssvar: ' + global.intyg.guidcheck;

        var svaraPaKomplettering = kompletteringsFraga.element(by.model('cannotKomplettera')).sendKeys(protractor.Key.SPACE)
            .then(function() {
                return kompletteringsFraga.element(by.model('qa.svarsText')).sendKeys(textSvar)
                    .then(function() {
                        return kompletteringsFraga.element(by.partialButtonText('Skicka svar')).sendKeys(protractor.Key.SPACE);

                    });
            });

        return svaraPaKomplettering
            .then(function() {
                logger.info('Kontrollerar att fråga är märkt som hanterad..');
                expect(kompletteringsFraga.element(by.css('.qa-block-handled')).getText()).to.eventually.contain(textSvar)
                    .then(function(value) {
                        logger.info('OK - textsvar = ' + value);
                    }, function(reason) {
                        throw ('FEL - textsvar: ' + reason);
                    });

            });
    });


    this.Given(/^jag svarar på frågan$/, function() {
        return browser.refresh()
            .then(function() {
                return helpers.fetchMessageIds(intyg.typ);
            })
            .then(function() {
                return fkIntygPage.sendAnswerForMessageID(intyg.messages[0].id, 'Ett svar till FK, ' + global.intyg.guidcheck);
            });
    });

    this.Given(/^kan jag se mitt svar under hanterade frågor$/, function() {
        return kontrolleraKompletteringsFragaHanterad(global.intyg.guidcheck);
    });

    this.Given(/^kan jag se påminnelse under hanterade frågor$/, function() {
        return browser.refresh().then(function() {
            var fragaText = global.intyg.guidcheck;
            console.log('Letar efter fråga som innehåller text: ' + fragaText);
            return expect(element(by.cssContainingText('.arende-block', fragaText)).isPresent()).to.eventually.become(true);
        });
    });

    this.Given(/^jag markerar frågan från Försäkringskassan som hanterad$/, function(callback) {
        fkIntygPage.markMessageAsHandled(intyg.messages[0].id).then(callback);
    });

    this.Given(/^jag markerar svaret från Försäkringskassan som hanterat$/, function(callback) {
        browser.refresh()
            .then(function() {
                return helpers.fetchMessageIds(intyg.typ);
            })
            .then(function() {
                return fkIntygPage.markMessageAsHandled(intyg.messages[0].id);
            })
            .then(callback);


    });

    this.Given(/^Försäkringskassan (?:har ställt|ställer) en "([^"]*)" fråga om intyget$/, function(amne, callback) {
        global.intyg.guidcheck = testdataHelper.generateTestGuid();

        var url;
        var body;

        var isSMIIntyg;
        if (intyg && intyg.typ) {
            isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        }

        if (isSMIIntyg) {
            var amneCode = helpers.subjectCodes[amne];
            // switch (amne) {
            //     case helpers.sendMessageToCareSubjectCode.KOMPLT:
            //         amneCode = helpers.getSubjectCode(helpers.sendMessageToCareSubjectCode.KOMPLT);
            //         break;
            //     case helpers.sendMessageToCareSubjectCode.PAMINN:
            //         amneCode = helpers.getSubjectCode(helpers.sendMessageToCareSubjectCode.PAMINN);
            //         break;
            //     default:
            //         // default code block
            // }

            body = soapMessageBodies.SendMessageToCare(global.user, global.person, global.intyg, 'Begär ' + amne + ' ' + global.intyg.guidcheck, amneCode);
            var path = '/send-message-to-care/v1.0?wsdl';
            url = process.env.INTYGTJANST_URL + path;
            // var url = 'https://webcert.ip30.nordicmedtest.sjunet.org/services/send-message-to-care/v1.0?wsdl'; //tillsv
            url = url.replace('https', 'http');

            soap.createClient(url, function(err, client) {
                logger.info(url);
                if (err) {
                    callback(err);
                } else {
                    client.SendMessageToCare(body, function(err, result, resBody) {
                        console.log(resBody);
                        if (err) {
                            callback(err);
                        } else {
                            var resultcode = result.result.resultCode;
                            logger.info('ResultCode: ' + resultcode);
                            console.log(result);
                            if (resultcode !== 'OK') {
                                logger.info(result);
                                callback('ResultCode: ' + resultcode + '\n' + resBody);
                            } else {
                                logger.info('ResultCode: ' + resultcode);
                                console.log(JSON.stringify(result));
                                callback();
                            }

                        }
                    });
                }
            });
        } else {
            url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-question/v1.0?wsdl';
            url = url.replace('https', 'http');

            body = soapMessageBodies.ReceiveMedicalCertificateQuestion(
                global.person.id,
                global.user,
                'Enhetsnamn',
                global.intyg.id,
                amne,
                'nytt meddelande: ' + global.intyg.guidcheck);
            soap.createClient(url, function(err, client) {
                if (err) {
                    callback(err);
                }

                client.ReceiveMedicalCertificateQuestion(body, function(err, result, body) {
                    // logger.debug(body);
                    // logger.debug(result);
                    callback(err);
                });
            });
        }
    });

    this.Given(/^Försäkringskassan skickar ett svar$/, function(callback) {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            callback('TODO: skicka SendMessageToRecipient', 'pending');
        } else {
            var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-answer/v1.0?wsdl';
            url = url.replace('https', 'http');
            soap.createClient(url, function(err, client) {
                if (err) {
                    callback(err);
                } else {
                    var body = soapMessageBodies.ReceiveMedicalCertificateAnswer(
                        global.person.id,
                        global.user.hsaId,
                        global.user.fornamn + '' + global.user.efternamn,
                        global.user.enhetId,
                        'WebCert Enhet 1',
                        'Enhetsnamn',
                        intyg.fragaId
                    );
                    console.log(body);
                    client.ReceiveMedicalCertificateAnswer(body, function(err, result, body) {
                        callback(err);
                    });
                }

            });

        }
    });

    this.Given(/^ska frågan vara hanterad$/, function(callback) {
        // Write code here that turns the phrase above into concrete actions
        callback(null, 'pending');
    });

};
