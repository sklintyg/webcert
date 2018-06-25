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

/* globals browser, protractor, logger, JSON, wcTestTools, Promise*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


const fkIntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;
const fkLusePage = wcTestTools.pages.intyg.luse.intyg;
const hogerfaltet = wcTestTools.pages.intyg.hogerfaltet;
const fragaSvar = hogerfaltet.fragaSvar;
const helpers = require('./helpers');
const soap = require('soap');
const soapMessageBodies = require('./soap');
const testdataHelper = wcTestTools.helpers.testdata;
const testTools = require('common-testtools');

let messageID;


testTools.protractorHelpers.init();
/*
 *	Stödfunktioner
 *
 */

function sendQuestionToFK(amne, intyg) {
    var testString = testdataHelper.generateTestGuid();
    var fragaText = 'En ' + amne + '-fråga';

    return fragaSvar.administrativFraga.menyVal.click().then(function() {
        return fragaSvar.administrativFraga.nyfraga.text.typeKeys(fragaText + ' ' + testString);
    }).then(function() {
        return fragaSvar.selectQuestionTopic(amne);
    }).then(function() {
        return fragaSvar.administrativFraga.nyfraga.sendButton.typeKeys(protractor.Key.SPACE);
    }).then(function() {
        //return lisjpUtkastPage.arendePanel.getAttribute('id');
        return fragaSvar.getMessageIdFrom.arendePanel(1);
    }).then(function(fragaId) {
        /*var element = result.split('-');
        var splitIndex = element[0].length + element[1].length + 2;
        var fragaId = result.substr(splitIndex, result.length);

        logger.debug('Frågans ID: ' + fragaId);*/

        if (!intyg.messages) {
            intyg.messages = [];
        }
        messageID = fragaId;
        intyg.messages.unshift({
            typ: 'Fråga',
            amne: helpers.subjectCodes[amne],
            id: fragaId,
            text: fragaText,
            testString: testString
        });
        return;
    });
}

function hamtaAllaTraffar() {
    return element(by.id('hamtaFler')).isDisplayed().then(function(present) {
        if (present) {
            return helpers.moveAndtypeKeys(element(by.id('hamtaFler')), protractor.Key.SPACE).then(function() {
                return helpers.smallDelay();
            });
        } else {
            return false;
        }
    }).then(function(loop) {
        if (loop !== false) {
            return hamtaAllaTraffar();
        } else {
            return;
        }
    });
}



/*
 *	Test steg
 *
 */

Given(/^jag skickar en fråga med ämnet "([^"]*)" till Försäkringskassan$/, function(amne) {
    return sendQuestionToFK(amne, this.intyg);
});
Given(/^jag väljer att svara med ett nytt intyg$/, function() {
    helpers.updateEnhetAdressForNewIntyg(this.user);
    const page = fkLusePage;
    let intyg = this.intyg;

    let world = this;

    if (!intyg.messages || intyg.messages.length <= 0) {
        throw ('Inga frågor hittades');
    } else if (intyg.messages.length > 1) {
        throw ('Fler än en fråga hittades, Granska teststegen!');
    } else {

        return browser.getCurrentUrl().then(function(url) {

            global.behoverKompletterasLink = url;

            return page.clickKompletteraIntyg(intyg.messages[0].id)
                .then(function() {
                    //Fulhack för att inte global ska innehålla en referens
                    logger.info('OK clickKompletteraIntyg(' + intyg.messages[0].id + ');');
                    world.ursprungligtIntyg = JSON.parse(JSON.stringify(intyg));
                    return;
                });

        });
    }
});
Given(/^jag går tillbaka till intyget som behöver kompletteras$/, function() {
    return helpers.getUrl(global.behoverKompletterasLink);

    // Denna funktionalitet användes när relations-valen fanns kvar
    // return element(by.id('wc-intyg-relations-button')).click().then(function() {
    //     return element(by.id('wc-intyg-relations-list')).element(by.cssContainingText('.btn', 'Visa')).click();
    // });

});
Given(/^ska det finnas en knapp med texten "([^"]*)"$/, function(texten) {
    return expect(element(by.cssContainingText('.btn', texten)).isPresent()).to.become(true);
});
Given(/^ska det inte finnas en knapp med texten "([^"]*)"$/, function(texten) {
    return expect(element(by.cssContainingText('.btn', texten)).isPresent()).to.become(false);
});

Given(/^ska jag se kompletteringsfrågan på (intygs|utkast)\-sidan$/, function(typ) {
    var fragaDeltext;

    if (typ === 'intygs') {
        messageID = this.intyg.messages[0].id;
        fragaDeltext = this.intyg.messages[0].testString;
    } else {
        messageID = this.ursprungligtIntyg.messages[0].id;
        fragaDeltext = this.ursprungligtIntyg.messages[0].testString;
    }
    return expect(fragaSvar.meddelande(messageID).frageText.getText()).to.eventually.contain(fragaDeltext);
});


Given(/^jag ska inte kunna komplettera med nytt intyg från webcert/, function() {
    var komplettera = element(by.id('komplettera-intyg-' + this.intyg.messages[0].id));

    return expect(komplettera.isPresent()).to.become(false);

});

Given(/^ska svara med textmeddelande vara tillgängligt i dialogen/, function() {
    var svaraMedMeddelande = element(by.id('komplettering-modal-dialog-answerWithMessage-button'));

    return expect(svaraMedMeddelande.isDisplayed()).to.become(true);

});

Given(/^ska kompletteringsdialogen innehålla texten "([^"]*)"$/, function(text) {
    return expect(element(by.css('.modal-body')).getText()).to.eventually.contain(text);
});

Given(/^jag klickar på svara knappen, fortfarande i uthoppsläge$/, function() {
    return element(by.id('uthopp-svara-med-meddelande-' + this.intyg.messages[0].id)).typeKeys(protractor.Key.SPACE)
        .then(function() {
            return helpers.pageReloadDelay();
        });
});


Given(/^jag ska kunna svara med textmeddelande/, function() {
    browser.ignoreSynchronization = false;
    var kompletteringsFraga = fkIntygPage.getQAElementByText(this.intyg.messages[0].testString).panel;
    var textSvar = 'Ett kompletteringssvar: ' + this.intyg.messages[0].testString;

    var svaraPaKomplettering = kompletteringsFraga.element(by.cssContainingText('.btn-default', 'Kan inte komplettera')).typeKeys(protractor.Key.SPACE)
        .then(function() {
            return helpers.largeDelay();
        })
        .then(function() {
            return fkIntygPage.komplettera.dialog.svaraMedMeddelandeButton.typeKeys(protractor.Key.SPACE);
        })
        .then(function() {
            return helpers.largeDelay();
        })
        .then(function() {
            return kompletteringsFraga.element(by.model('arendeSvar.meddelande')).typeKeys(textSvar);

        })
        .then(function() {
            return helpers.largeDelay();
        })
        .then(function() {
            return kompletteringsFraga.element(by.partialButtonText('Skicka svar')).typeKeys(protractor.Key.SPACE);

        })
        .then(function() {
            return helpers.largeDelay();
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

Given(/^jag svarar på frågan$/, function() {
    let intyg = this.intyg;
    messageID = intyg.messages[0].id;

    intyg.messages[0].answer = 'Ett svar till FK, på frågan: ' + intyg.messages[0].testString;

    return browser.refresh()
        .then(function() {
            return fragaSvar.meddelande(messageID).administrativFraga.svaraMedTxt(intyg.messages[0].answer);
        });
});

Given(/^kan jag se mitt svar i högerfältet$/, function() {
    return expect(fragaSvar.container.getText()).to.eventually.contain(this.intyg.messages[0].answer);
});

Given(/^ska jag se påminnelsen på intygssidan$/, function() {
    var fragaText = this.intyg.messages[0].testString;

    return browser.refresh()
        .then(function() {
            return expect(fragaSvar.container.isPresent()).to.eventually.become(true);
        })
        .then(function() {
            logger.silly('Letar efter påminnelse som innehåller text: ' + fragaText);
            return expect(fragaSvar.container.getText()).to.eventually.contain(fragaText); //
        });
});

Given(/^jag markerar (svaret|frågan)? från Försäkringskassan som( INTE)? hanterad$/, function(meddelandeTyp, inte) {

    let tempMessageID;
    if (meddelandeTyp === 'svaret') {
        tempMessageID = this.intyg.messages[1].id;
    } else {
        tempMessageID = this.intyg.messages[0].id;
    }
    return browser.refresh().then(function() {
        return helpers.pageReloadDelay();
    }).then(function() {
        return fragaSvar.administrativFraga.menyVal.click();
    }).then(function() {
        return helpers.smallDelay();
    }).then(function() {
        return fragaSvar.meddelande(tempMessageID).administrativFraga.togglaHanterad();
    });
});

Given(/^Försäkringskassan (?:har ställt|ställer) en "([^"]*)" fråga om intyget$/, function(amne, callback) {

    var url;
    var body;
    var amneCode = amne;

    body = soapMessageBodies.SendMessageToCare(this.user, this.patient, this.intyg, 'Begär ' + amne, testdataHelper.generateTestGuid(), amneCode);
    logger.silly(body);
    var path = '/send-message-to-care/v2.0?wsdl';
    url = process.env.INTYGTJANST_URL + path;
    url = url.replace('https', 'http');

    soap.createClient(url, function(err, client) {
        logger.info(url);
        if (err) {
            callback(err);
        } else {
            client.SendMessageToCare(body, function(err, result, resBody) {
                logger.silly(resBody);
                var resultcode = result.result.resultCode;
                logger.info('ResultCode: ' + resultcode);
                logger.silly(result);
                if (resultcode !== 'OK') {
                    logger.info(result);
                    callback('ResultCode: ' + resultcode + '\n' + resBody);
                } else {
                    logger.info('ResultCode: ' + resultcode);
                    logger.silly(JSON.stringify(result));
                    browser.refresh().then(function() {
                        callback(err);
                    });
                }
            });
        }
    });
});

Given(/^Försäkringskassan skickar ett svar$/, function(callback) {
    let intyg = this.intyg;
    let patient = this.patient;
    let user = this.user;

    function callSendMessageToCare() {
        var url = '';
        var body = '';


        var path = '/send-message-to-care/v2.0?wsdl';
        url = process.env.INTYGTJANST_URL + path;
        url = url.replace('https', 'http');

        body = soapMessageBodies.SendMessageToCare(user, patient, intyg, 'Ett svar ', testdataHelper.generateTestGuid(), false);
        logger.silly(body);

        soap.createClient(url, function(err, client) {
            logger.info(url);
            if (err) {
                callback(err);
            } else {
                client.SendMessageToCare(body, function(err, result, resBody) {
                    logger.silly(resBody);
                    if (err) {
                        callback(err);
                    } else {
                        var resultcode = result.result.resultCode;
                        logger.info('ResultCode: ' + resultcode);
                        // logger.silly(result);
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
    }
    //Vänta 1 sec på att frågan kommer till Intygstjänsten
    setTimeout(callSendMessageToCare, 1000);
});

Given(/^jag markerar frågan från vården som hanterad$/, function() {
    return fragaSvar.meddelande(messageID).administrativFraga.togglaHanterad();
    //return fkLusePage.getQAElementByText(fragaText).panel.element(by.css('input[type=checkbox]')).typeKeys(protractor.Key.SPACE);
});


Given(/^jag går till sidan Frågor och svar$/, function() {
    return wcTestTools.pages.fragorOchSvar.get().then(function() {
        return helpers.pageReloadDelay();
    });
});

Given(/^ska frågan inte finnas i listan$/, function() {
    let intyg = this.intyg;
    return expect(element(by.id('wc-sekretessmarkering-icon-' + intyg.id)).isPresent()).to.become(false).then(function() {

        return expect(element(by.id('showqaBtn-' + intyg.id)).isPresent()).to.become(false);

    });
});

var matchingQARow;
Given(/^ska det (inte )?finnas en rad med texten "([^"]*)" för frågan$/, function(inte, atgard) {
    let patient = this.patient;

    logger.info('Letar efter rader som innehåller text: ' + atgard + ' + ' + patient.id);
    return wcTestTools.pages.fragorOchSvar.qaTable.all(by.css('tr')).filter(function(row) {
        return row.all(by.css('td')).getText().then(function(text) {
            logger.silly(text);
            if (patient.id.indexOf('-') === -1) {
                patient.id = patient.id.replace(/(\d{8})(\d{4})/, '$1-$2');
            }
            var hasPersonnummer = (text.indexOf(patient.id) > -1);
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
Given(/^jag väljer att visa intyget som har en fråga att hantera$/, function() {
    var btn = matchingQARow.element(by.cssContainingText('button', 'Visa'));
    return btn.getAttribute('id').then(function(id) {
        logger.info('knapp-id: ' + id);
        buttonId = id;
        return btn.typeKeys(protractor.Key.SPACE);
    });
});

Given(/^jag väljer att visa intyget med frågan$/, function() {
    logger.silly(this.intyg.messages);
    var atgard = 'Svara';
    let patient = this.patient;

    logger.info('Letar efter rader som innehåller text: ' + atgard + ' + ' + patient.id);
    return wcTestTools.pages.fragorOchSvar.qaTable.all(by.css('tr')).filter(function(row) {
        return row.all(by.css('td')).getText().then(function(text) {
            logger.silly(text);
            var hasPersonnummer = (text.indexOf(patient.id) > -1);
            var hasAtgard = (text.indexOf(atgard) > -1);
            return hasAtgard && hasPersonnummer;
        });
    }).then(function(rows) {
        matchingQARow = rows[0];
        var btn = matchingQARow.element(by.cssContainingText('button', 'Visa'));
        return btn.getAttribute('id').then(function(id) {
            logger.info('knapp-id: ' + id);
            buttonId = id;
            return btn.typeKeys(protractor.Key.SPACE);
        });


    });
});

Given(/^jag lämnar intygssidan$/, function() {
    return fkIntygPage.backBtn.click();
});

Given(/^ska jag få dialogen "([^"]*)"$/, function(text) {
    return expect(element(by.cssContainingText('.modal-dialog', text)).isPresent()).to.eventually.become(true);
});

Given(/^jag väljer valet att markera som hanterade$/, function() {
    return element(by.cssContainingText('button', 'Hanterade')).typeKeys(protractor.Key.SPACE);
});

Given(/^ska den tidigare raden inte finnas kvar i tabellen för Frågor och svar$/, function() {
    return expect(element(by.id(buttonId)).isPresent()).to.eventually.not.be.ok;
});

Given(/^jag väljer åtgärden "([^"]*)"$/, function(atgard) {
    var showFilter = element(by.cssContainingText('button', 'Visa sökfilter'));
    return showFilter.isPresent().then(function(isPresent) {
        if (isPresent) {
            return showFilter.typeKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve('Filter visas redan');
        }
    }).then(function() {
        return wcTestTools.pages.fragorOchSvar.atgardSelect.element(by.cssContainingText('option', atgard))
            .typeKeys(protractor.Key.SPACE).then(function() {
                return wcTestTools.pages.fragorOchSvar.searchBtn.typeKeys(protractor.Key.SPACE).then(function() {
                    return helpers.smallDelay;
                });
            });
    }).then(function() {
        if (atgard === 'Visa alla ej hanterade') {
            return hamtaAllaTraffar();
        } else {
            return;
        }
    });
});



Given(/^ska jag se flera frågor$/, function() {
    return wcTestTools.pages.fragorOchSvar.qaTable.all(by.css('tr')).count().then(function(count) {
        return expect(count).to.be.above(1); // mer än 1 pga att table-header är en rad
    });
});

Given(/^jag väljer att filtrera på läkare "([^"]*)"$/, function(lakare) {
    var showFilter = element(by.cssContainingText('button', 'Visa sökfilter'));
    return showFilter.isPresent().then(function(isPresent) {
        if (isPresent) {
            return showFilter.typeKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve('Filter visas redan');
        }
    }).then(function() {
        return element(by.id('qp-lakareSelector'))
            .element(by.cssContainingText('option', lakare)).click()
            .then(function() {
                return wcTestTools.pages.fragorOchSvar.searchBtn.typeKeys(protractor.Key.SPACE);
            });

    });
});


Given(/^ska jag bara se frågor på intyg signerade av "([^"]*)"$/, function(lakare) {
    logger.silly('Kontrollerar att varje rad innehåller texten ' + lakare);
    return wcTestTools.pages.fragorOchSvar.qaTable.all(by.css('tr')).getText()
        .then(function(textArr) {
            var text = textArr.join('\n');
            logger.info(text);
            if (text.indexOf(lakare) < 0) {
                throw 'Hittade felaktig rad';
            }
        });
});

Given(/^ska jag se min fråga som ohanterad$/, function() {
    return expect(fragaSvar.meddelande(messageID).administrativFraga.hanterad.isSelected()).to.eventually.become(false);
});

Given(/^jag skickar en fråga med slumpat ämne till Försäkringskassan$/, function() {
    return sendQuestionToFK(
        testdataHelper.shuffle(['Arbetstidsförläggning', 'Avstämningsmöte', 'Kontakt', 'Övrigt'])[0],
        this.intyg
    );
});

Given(/^ska jag ha möjlighet att vidarebefordra frågan$/, function() {
    return browser.refresh().then(function() {
        return helpers.pageReloadDelay();
    }).then(function() {
        return fragaSvar.administrativFraga.menyVal.click();
    }).then(function() {
        return expect(fragaSvar.administrativFraga.vidarebefordra.isPresent()).to.eventually.become(true);
    });
});
Then(/^ska det synas vem som svarat$/, function() {
    var name = this.user.forNamn + ' ' + this.user.efterNamn;
    return element.all(by.css('.arende-sender.ng-binding.ng-scope')).map(function(data) {
        return data.getText();
    }).then(function(theNames) {
        return expect(theNames.join('\n')).to.contain(name);
    });

});
