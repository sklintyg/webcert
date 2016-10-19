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

/* globals pages, intyg, browser, protractor, logger, JSON, wcTestTools, Promise, person*/

'use strict';
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkLusePage = pages.intyg.luse.intyg;
// var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var lisuUtkastPage = pages.intyg.lisu.utkast;
var helpers = require('./helpers');
var soap = require('soap');
var soapMessageBodies = require('./soap');
var testdataHelper = wcTestTools.helpers.testdata;

function kontrolleraKompletteringsFragaHanterad(id) {
    var selector = 'arende-handled-' + id;
    logger.info('Letar efter element med id ' + selector);
    return expect(element(by.id(selector)).isPresent()).to.eventually.be.ok;
}

module.exports = function() {
    this.Given(/^jag skickar en fråga med ämnet "([^"]*)" till Försäkringskassan$/, function(amne, callback) {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        console.log('isSMIIntyg : ' + isSMIIntyg);

        var fragaText = 'En ' + amne + '-fråga ' + testdataHelper.generateTestGuid();

        // if (isSMIIntyg) {

        lisuUtkastPage.arendeQuestion.newArendeButton.sendKeys(protractor.Key.SPACE);
        lisuUtkastPage.arendeQuestion.text.sendKeys(fragaText);
        lisuUtkastPage.selectQuestionTopic(amne);

        lisuUtkastPage.arendeQuestion.sendButton.sendKeys(protractor.Key.SPACE);

        lisuUtkastPage.arendePanel.getAttribute('id').then(function(result) {
            var element = result.split('-');
            var splitIndex = element[0].length + element[1].length + 2;
            var fragaId = result.substr(splitIndex, result.length);

            global.meddelanden.push({
                typ: 'Fråga',
                amne: helpers.subjectCodes[amne],
                id: fragaId,
                text: fragaText
            });

            logger.debug('Frågans ID: ' + fragaId);
        }).then(callback);

        // } else {
        //     fkIntygPage.question.newQuestionButton.sendKeys(protractor.Key.SPACE);
        //     fkIntygPage.question.text.sendKeys(fragaText);
        //     fkIntygPage.selectQuestionTopic(amne);

        //     fkIntygPage.question.sendButton.sendKeys(protractor.Key.SPACE);

        //     fkIntygPage.qaPanel.getAttribute('id').then(function(result) {
        //         var fragaId = result.split('-')[1];
        //         global.meddelanden.push({
        //             typ: 'Fråga',
        //             amne: amne,
        //             id: fragaId,
        //             text: fragaText
        //         });
        //         logger.debug('Frågans ID: ' + fragaId);
        //         callback();
        //     });
        // }

    });

    this.Given(/^jag väljer att svara med ett nytt intyg$/, function() {
        var fragaText = global.intyg.guidcheck;
        var page = fkIntygPage;
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            page = fkLusePage;
        }


        if (!intyg.messages || intyg.messages.length <= 0) {
            throw ('Inga frågor hittades');
        } else {
            var svaraBtn = page.getQAElementByText(fragaText).panel.element(by.cssContainingText('.btn-success', 'Svara'));
            return svaraBtn.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    //Fulhack för att inte global ska innehålla en referens
                    global.ursprungligtIntyg = JSON.parse(JSON.stringify(intyg));

                    return page.komplettera.dialog.svaraMedNyttIntygKnapp.sendKeys(protractor.Key.SPACE);
                });
        }
    });


    this.Given(/^ska jag se kompletteringsfrågan på (intygs|utkast)\-sidan$/, function(typ) {
        var fragaText;

        if (typ === 'intygs') {
            fragaText = global.intyg.guidcheck;
        } else {
            fragaText = global.ursprungligtIntyg.guidcheck;
        }

        var page = fkIntygPage;
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            page = fkLusePage;
        }

        console.log('Letar efter fråga som innehåller text: ' + fragaText);
        return expect(page.getQAElementByText(fragaText).panel.isPresent()).to.become(true);
    });

    this.Given(/^jag ska inte kunna komplettera med nytt intyg från webcert/, function() {
        return fkIntygPage.svaraMedNyttIntyg(global.intyg.messages[0].id).then(function() {
            browser.sleep(3000).then(function() {
                return expect(element(by.cssContainingText('.btn', 'Svara med nytt intyg')).isPresent()).to.become(false);
            });
        });

    });

    this.Given(/^ska kompletteringsdialogen innehålla texten "([^"]*)"$/, function(text) {
        return expect(element(by.css('.modal-body')).getText()).to.eventually.contain(text);
    });



    this.Given(/^jag ska kunna svara med textmeddelande/, function() {
        browser.ignoreSynchronization = false;
        var kompletteringsFraga = fkIntygPage.getQAElementByText(global.intyg.guidcheck).panel;
        var textSvar = 'Ett kompletteringssvar: ' + global.intyg.guidcheck;

        var svaraPaKomplettering = kompletteringsFraga.element(by.cssContainingText('.btn-success', 'Svara')).sendKeys(protractor.Key.SPACE)
            .then(function() {
                return fkIntygPage.komplettera.dialog.svaraMedTextKnapp.sendKeys(protractor.Key.SPACE);
            })
            .then(function() {
                return browser.sleep(2000); // Sleep pga animation
            })
            .then(function() {
                return kompletteringsFraga.element(by.model('arendeSvar.meddelande')).sendKeys(textSvar);

            })
            .then(function() {
                return browser.sleep(1000); // Sleep pga animation
            })
            .then(function() {
                return kompletteringsFraga.element(by.partialButtonText('Skicka svar')).sendKeys(protractor.Key.SPACE);

            });

        return svaraPaKomplettering
            .then(function() {
                logger.info('Kontrollerar att fråga är märkt som hanterad..');
                expect(kompletteringsFraga.element(by.css('.arende-block-handled')).getText()).to.eventually.contain(textSvar)
                    .then(function(value) {
                        logger.info('OK - textsvar = ' + value);
                    }, function(reason) {
                        throw ('FEL - textsvar: ' + reason);
                    });

            });
    });

    var messageID;
    this.Given(/^jag svarar på frågan$/, function() {
        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);

        return browser.refresh()
            .then(function() {
                return helpers.fetchMessageIds(intyg.typ);
            })
            .then(function() {
                for (var k = 0; k < intyg.messages.length; k++) {
                    logger.info('jämför: ' + intyg.messages[k].amne + ' och ' + helpers.getSubjectFromCode(global.meddelanden[0].amne, !isSMIIntyg));
                    var amneMatcharSkickadFraga = intyg.messages[k].amne === helpers.getSubjectFromCode(global.meddelanden[0].amne, !isSMIIntyg);
                    if (amneMatcharSkickadFraga && !intyg.messages[k].isHandled) {
                        messageID = intyg.messages[k].id;
                    }
                }
                return fkIntygPage.sendAnswerForMessageID(messageID, 'Ett svar till FK, ' + global.intyg.guidcheck);
            });
    });

    this.Given(/^kan jag se mitt svar under hanterade frågor$/, function() {
        return kontrolleraKompletteringsFragaHanterad(messageID);
    });

    this.Given(/^ska jag se påminnelsen på intygssidan$/, function() {
        var fragaText = global.intyg.guidcheck;
        var panel = element(by.cssContainingText('.arende-panel', fragaText));
        return browser.refresh()
            .then(function() {
                console.log('Letar efter påminnelse som innehåller text: ' + fragaText);
                return expect(panel.isPresent()).to.eventually.become(true);

            })
            .then(function() {
                // chai-as-promised/cucumberjs 1.2 har en bugg där man inte kan använda denna typ av assertions
                // return expect(panel.getText()).to.eventually.contain('Ämne: Påminnelsee'); //
                return panel.getText().then(function(text) {
                    expect(text).to.contain('Ämne: Påminnelse');
                });

            });
    });

    this.Given(/^jag markerar frågan från Försäkringskassan som hanterad$/, function(callback) {
        fkIntygPage.markMessageAsHandled(intyg.messages[0].id).then(callback);
    });

    this.Given(/^jag markerar svaret från Försäkringskassan (?:.*) hanterat$/, function() {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        if (isSMIIntyg) {
            var messageId;
            console.log(global.meddelanden);
            for (var k = 0; k < global.meddelanden.length; k++) {
                if (global.meddelanden[k].typ === 'Fråga') {
                    messageId = global.meddelanden[k].id;
                }
            }
            return element(by.id('handleCheck-' + messageId)).sendKeys(protractor.Key.SPACE);
        } else {
            return browser.refresh()
                .then(function() {
                    return helpers.fetchMessageIds(intyg.typ);
                })
                .then(function() {
                    return fkIntygPage.markMessageAsHandled(intyg.messages[0].id);
                });
        }



    });

    this.Given(/^Försäkringskassan (?:har ställt|ställer) en "([^"]*)" fråga om intyget$/, function(amne, callback) {
        global.intyg.guidcheck = testdataHelper.generateTestGuid();

        var url;
        var body;
        var amneCode = amne;

        var isSMIIntyg;
        if (intyg && intyg.typ) {
            isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        }

        if (isSMIIntyg) {
            body = soapMessageBodies.SendMessageToCare(global.user, global.person, global.intyg, 'Begär ' + amne + ' ' + global.intyg.guidcheck, amneCode);
            console.log(body);
            var path = '/send-message-to-care/v1.0?wsdl';
            url = process.env.INTYGTJANST_URL + path;
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
            amneCode = amne; //helpers.subjectCodesFK7263[amne];
            url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-question/v1.0?wsdl';
            url = url.replace('https', 'http');

            body = soapMessageBodies.ReceiveMedicalCertificateQuestion(
                global.person.id,
                global.user,
                'Enhetsnamn',
                global.intyg.id,
                amneCode,
                'nytt meddelande: ' + global.intyg.guidcheck);
            console.log(body);
            soap.createClient(url, function(err, client) {
                if (err) {
                    callback(err);
                }

                client.ReceiveMedicalCertificateQuestion(body, function(err, result, resBody) {
                    global.meddelanden.push({
                        typ: 'Fråga',
                        amne: amne
                    });
                    var resultcode = result.result.resultCode;
                    if (resultcode !== 'OK') {
                        logger.info(result);
                        callback('ResultCode: ' + resultcode + '\n' + resBody);
                    } else {
                        callback(err);
                    }
                });
            });
        }
    });

    this.Given(/^Försäkringskassan skickar ett svar$/, function(callback) {

        var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        var url = '';
        var body = '';

        if (isSMIIntyg) {

            global.intyg.guidcheck = testdataHelper.generateTestGuid();

            body = soapMessageBodies.SendMessageToCare(global.user, global.person, global.intyg, 'Ett svar ' + global.intyg.guidcheck, false);
            console.log(body);
            var path = '/send-message-to-care/v1.0?wsdl';
            url = process.env.INTYGTJANST_URL + path;
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
                            // console.log(result);
                            if (resultcode !== 'OK') {
                                logger.info(result);
                                callback('ResultCode: ' + resultcode + '\n' + resBody);
                            } else {
                                logger.info('ResultCode: ' + resultcode);
                                callback();
                            }

                        }
                    });
                }
            });


        } else {
            url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-answer/v1.0?wsdl';
            url = url.replace('https', 'http');
            soap.createClient(url, function(err, client) {
                if (err) {
                    callback(err);
                } else {
                    body = soapMessageBodies.ReceiveMedicalCertificateAnswer(
                        global.person.id,
                        global.user.hsaId,
                        global.user.fornamn + '' + global.user.efternamn,
                        global.user.enhetId,
                        'WebCert Enhet 1',
                        'Enhetsnamn',
                        global.meddelanden[0].id
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

    this.Given(/^jag markerar frågan från vården som hanterad$/, function() {
        var fragaText;
        for (var k = 0; k < global.meddelanden.length; k++) {
            if (global.meddelanden[k].typ === 'Fråga') {
                fragaText = global.meddelanden[k].text;
            }
        }
        return fkLusePage.getQAElementByText(fragaText).panel.element(by.css('input[type=checkbox]')).sendKeys(protractor.Key.SPACE);
    });


    this.Given(/^jag går till sidan Frågor och svar$/, function() {
        return pages.fragorOchSvar.get();
    });



    var matchingQARow;
    this.Given(/^ska det (inte )?finnas en rad med texten "([^"]*)" för frågan$/, function(inte, atgard) {

        logger.info('Letar efter rader som innehåller text: ' + atgard + ' + ' + person.id);
        return pages.fragorOchSvar.qaTable.all(by.css('tr')).filter(function(row) {
            return row.all(by.css('td')).getText().then(function(text) {
                console.log(text);

                var hasPersonnummer = (text.indexOf(person.id) > -1);
                var hasAtgard = (text.indexOf(atgard) > -1);
                return hasAtgard && hasPersonnummer;
            });
        }).then(function(rows) {
            matchingQARow = rows[0];
            if (inte) {
                return expect(rows.length).to.equal(0);
            } else {
                return expect(rows).to.have.length.above(0);
            }


        });
    });

    var buttonId;
    this.Given(/^jag väljer att visa intyget som har en fråga att hantera$/, function() {
        var btn = matchingQARow.element(by.cssContainingText('button', 'Visa'));
        return btn.getAttribute('id').then(function(id) {
            logger.info('knapp-id: ' + id);
            buttonId = id;
            return btn.sendKeys(protractor.Key.SPACE);
        });
    });

    this.Given(/^jag lämnar intygssidan$/, function() {
        return fkIntygPage.backBtn.click();
    });

    this.Given(/^ska jag få dialogen "([^"]*)"$/, function(text) {
        return expect(element(by.cssContainingText('.modal-dialog', text)).isPresent()).to.eventually.be.ok;
    });

    this.Given(/^jag väljer valet att markera som hanterade$/, function() {
        return element(by.cssContainingText('button', 'Hanterade')).sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^ska den tidigare raden inte finnas kvar i tabellen för Frågor och svar$/, function() {
        return expect(element(by.id(buttonId)).isPresent()).to.eventually.not.be.ok;
    });

    this.Given(/^jag väljer åtgärden "([^"]*)"$/, function(atgard) {
        var showFilter = element(by.cssContainingText('button', 'Visa sökfilter'));
        showFilter.isPresent().then(function(isPresent) {
            if (isPresent) {
                return showFilter.sendKeys(protractor.Key.SPACE);
            } else {
                return Promise.resolve('Filter visas redan');
            }
        }).then(function() {
            return pages.fragorOchSvar.atgardSelect.element(by.cssContainingText('option', atgard))
                .sendKeys(protractor.Key.SPACE).then(function() {
                    return pages.fragorOchSvar.searchBtn.sendKeys(protractor.Key.SPACE);
                });
        });
    });



};
