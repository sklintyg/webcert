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

/* globals pages, protractor, logger, Promise, intyg, browser */
'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var tsIntygPage = pages.intyg.ts.bas.intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var basePage = pages.webcertBase;
var utkastPage = pages.intyg.base.utkast;
var unsignedPage = pages.unsignedPage;


module.exports = function() {

    this.Given(/^går in på Sök\/skriv intyg$/, function() {
        return basePage.flikar.sokSkrivIntyg.click();
    });

    this.Given(/^går in på Ej signerade utkast$/, function() {
        return unsignedPage.flikar.notSigned.click();
    });

    this.Given(/^är kopieraknappen tillgänglig$/, function(callback) {
        expect(basePage.copyBtn.isPresent()).to.become(true).then(function() {
            logger.info('OK - Kopiera knapp tillgänglig');
            callback();
            // basePage.copyBtn.sendKeys(protractor.Key.SPACE).then(callback);
        }, function(reason) {
            callback('FEL : ' + reason);
        });
    });

    this.Given(/^ska det( inte)? finnas en knapp för att förnya intyget$/, function(inte) {
        var skaFinnas = typeof(inte) === 'undefined';

        return expect(fkIntygPage.fornyaBtn.isPresent()).to.become(skaFinnas).then(function() {
            logger.info('OK - Förnya knapp synlig: ' + skaFinnas);
        }, function(reason) {
            throw ('FEL : ' + reason);
        });
    });


    this.Given(/^ska det( inte)? finnas en knapp för att kopiera intyget$/, function(inte) {
        var skaFinnas = typeof(inte) === 'undefined';
        return expect(basePage.copyBtn.isPresent()).to.become(skaFinnas).then(function() {
                logger.info('OK - Kopiera knapp synlig:' + skaFinnas);
            },
            function(reason) {
                throw ('FEL : ' + reason);
            });
    });

    this.Given(/^ska det finnas en knapp för att skriva ut utkastet$/, function() {
        return expect(fkUtkastPage.skrivUtBtn.isPresent()).to.become(true);
    });

    this.Given(/^ska det finnas en knapp för att skriva ut intyget$/, function() {
        if (intyg.typ.indexOf('Transportstyrelsen') >= 0) {
            return expect(tsIntygPage.printBtn.isPresent()).to.become(true);
        } else {
            return expect(fkIntygPage.selectUtskriftButton.isPresent()).to.become(true);
        }
    });

    this.Given(/^är kopieraknappen inte tillgänglig$/, function() {
        return expect(basePage.copyBtn.isPresent()).to.become(false).then(function() {
            logger.info('OK - Kopiera knappen syns inte');
        }, function(reason) {
            throw ('FEL : ' + reason);
        });
    });

    this.Given(/^visas Vidarebefodra knappen$/, function() {
        return expect(fkIntygPage.forwardBtn.isPresent()).to.become(true).then(function() {
            logger.info('OK - Vidarebeforda knappen hittad');
        }, function(reason) {
            throw ('FEL : ' + reason);
        });
    });

    this.Given(/^väljer att visa sökfilter/, function() {
        return unsignedPage.showSearchFilters();
    });

    this.Given(/^ska sökfiltret Sparat av inte vara tillgängligt/, function(callback) {
        expect(unsignedPage.filterSavedBy.form.isPresent()).to.eventually.be.not.ok.then(function(value) {
            logger.info('Filter \"Sparat av\" inte tillgängligt för uthoppsläkare ' + value);
        }, function(reason) {
            callback('FEL, Filter \"Sparat av\" tillgängligt för uthoppsläkare,' + reason);
        }).then(callback);
    });

    this.Given(/^att det finns intygsutkast$/, function() {
        var utkastRows = element.all(by.cssContainingText('tr', 'Utkast')).map(function(elm, index) {
            return elm.getText();
        });

        return utkastRows
            .then(function(rows) {
                var url;

                if (rows.length <= 0) {
                    // Skapa slumpat utkast
                    logger.info('Skapar utkast..');
                    var allOptions = element(by.id('intygType')).all(by.tagName('option'));

                    return browser.getCurrentUrl()
                        .then(function(currentURL) {
                            url = currentURL;

                            return allOptions.filter(function(elem, index) {
                                    return elem.getText().then(function(text) {
                                        return text.indexOf('Välj') === -1; // ta bort felaktigt val
                                    });
                                }).count()
                                .then(function(numberOfItems) {
                                    return Math.floor(Math.random() * numberOfItems) + 1;
                                }).then(function(randomNumber) {
                                    return allOptions.get(randomNumber).click();
                                })
                                .then(function() {
                                    return element(by.id('intygTypeFortsatt')).sendKeys(protractor.Key.SPACE);
                                })
                                .then(function() {
                                    return browser.get(url); // gå tillbaka till översikt
                                });
                        });
                }
            });

    });

    this.Given(/^ska Förnya\-knappen inte visas för något utkast$/, function() {
        var utkastRows = element.all(by.cssContainingText('tr', 'Utkast')).map(function(elm, index) {
            return elm.getText();
        });

        return utkastRows.then(function(rowTexts) {
            if (rowTexts.length <= 0) {
                throw ('Hittade inga utkast-rader. Testet kan inte genomföras');
            }
            var joinedTexts = rowTexts.join('\n');
            logger.info('Hittade utkast-rader: ' + joinedTexts);
            return Promise.all([
                expect(joinedTexts).to.not.include('Förnya'),
                expect(joinedTexts).to.not.include('Kopiera')
            ]);
            // Även om inte steget explicit säger det så lägger jag med en check att inte kopiera knappen visas också. TODO: Snygga till
        });
    });


    this.Given(/^ska Förnya\-knappen visas för alla signerade eller mottagna "([^"]*)"\-intyg$/, function(intygstyp) {

        element(by.id('intygFilterSamtliga')).sendKeys(protractor.Key.SPACE);

        function checkRowForBtnWithText(rowText, buttonText, shouldBePresent) {
            var qaTable = element(by.css('table.table-qa'));
            return qaTable.all(by.cssContainingText('tr', rowText)).filter(function(elem, index) {
                return elem.all(by.css('td')).get(2).getText().then(function(text) {
                    return (text === intygstyp);
                });
            }).then(function(els) {
                logger.info('Antal ' + rowText + '-rader som visas: ' + els.length);
                var notText = '';
                if (!shouldBePresent) {
                    notText = ' inte';
                }
                var rowChecks = [];

                function onSucc(value) {
                    //console.log('kontrollerart att rad ' + notText + ' har en ' + buttonText + '-knapp');
                }

                function onFail(reason) {
                    console.log(reason.message);
                    throw reason;
                }

                function printText(txt) {
                    console.log(txt);
                }

                for (var k = 0; k < els.length; k++) {
                    els[k].getText().then(printText);

                    if (shouldBePresent) {
                        rowChecks.push(expect(els[k].getText()).to.eventually.contain(buttonText)
                            .then(onSucc, onFail));
                    } else {
                        rowChecks.push(expect(els[k].getText()).to.eventually.not.contain(buttonText)
                            .then(onSucc, onFail));
                    }

                }
                return Promise.all(rowChecks);
            });
        }
        return Promise.all([
            checkRowForBtnWithText('Signerat', 'Förnya', true),
            checkRowForBtnWithText('Skickat', 'Förnya', true),
            checkRowForBtnWithText('Makulerat', 'Förnya', false)
        ]);
    });

    this.Given(/^är signeraknappen tillgänglig$/, function(callback) {
        expect(utkastPage.signeraButton.isPresent()).to.eventually.be.ok.then(function(value) {
            logger.info('Signeringsknapp existerar ' + value);
        }, function(reason) {
            callback('FEL, Signeringsknapp finns inte på sidan,' + reason);
        });

        expect(utkastPage.signeraButton.isEnabled()).to.eventually.be.ok.then(function(value) {
            logger.info('Signeringsknapp är klickbar' + value);
        }, function(reason) {
            callback('FEL, Signeringsknapp är inte klickbar,' + reason);
        }).then(callback);
    });

    this.Given(/^ska makuleraknappen inte vara tillgänglig$/, function(callback) {
        expect(fkIntygPage.makulera.btn.isPresent()).to.eventually.be.not.ok.then(function(value) {
            logger.info('Makuleraknappen syns inte (ok)' + value);
        }, function(reason) {
            callback('FEL, Makuleraknappen finns tillgänglig,' + reason);
        }).then(callback);
    });

    this.Given(/^väljer att byta vårdenhet$/, function(callback) {
        basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
    });

    this.Given(/^vårdenhet ska vara "([^"]*)"$/, function(arg1, callback) {
        expect(basePage.careUnit.getText()).to.eventually.contain(arg1).then(function(value) {
            logger.info('OK - vårdenhet = ' + value);
        }, function(reason) {
            callback('FEL - vårdenhet: ' + reason);
        }).then(callback);
    });

    this.Given(/^jag väljer flik "([^"]*)"$/, function(arg1, callback) {
        expect(basePage.flikar.sokSkrivIntyg.getText()).to.eventually.contain(arg1).then(function(value) {
            element(basePage.flikar.sokSkrivIntyg).sendKeys(protractor.Key.SPACE);
            logger.info('OK - byta flik till = ' + value);
        }, function(reason) {
            callback('FEL - byta flik till: ' + reason);
        }).then(callback);
    });

    this.Given(/^jag väljer att byta vårdenhet$/, function(callback) {
        basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
    });

    this.Given(/^väljer "([^"]*)"$/, function(arg1, callback) {
        basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(function(arg1) {
            element(by.id('select-active-unit-IFV1239877878-1045-modal')).sendKeys(protractor.Key.SPACE).then(callback);
        });
    });

    this.Given(/^visas inte signera knappen$/, function(callback) {
        fkUtkastPage.signeraButton.isPresent().then(function(isVisible) {
            if (isVisible) {
                callback('FEL - Signera knapp synlig!');
            } else {
                logger.debug('OK - Signera knapp ej synlig!');
            }
        }).then(callback);
    });

    this.Given(/^visas Hämta personuppgifter knappen$/, function(callback) {
        fkUtkastPage.fetchPatientButton.isPresent().then(function(isVisible) {
            if (isVisible) {
                logger.debug('OK - Hämta personuppgifter synlig!');
            } else {
                callback('FEL - Hämta personuppgifter ej synlig!');
            }
        }).then(callback);
    });

    this.Given(/^meddelas jag om spärren$/, function(callback) {
        expect(basePage.warnings.protectedInfo).getText()
            .to.eventually.contain('På grund av sekretessmarkeringen går det inte att skriva nya elektroniska intyg.').then(function(value) {
                logger.info('OK - sekretessmarkeringe = ' + value);
            }, function(reason) {
                callback('FEL - sekretessmarkeringe: ' + reason);
            }).then(callback);

    });

    this.Given(/^jag kan inte gå in på att skapa ett "([^"]*)" intyg$/, function(arg1, callback) {
        sokSkrivIntygUtkastTypePage.intygTypeButton.isDisplayed().then(function(isVisible) {
            if (isVisible) {
                callback('FEL - ' + arg1 + ' synlig!');
            } else {
                logger.debug('OK -' + arg1 + ' ej synlig!');
            }
        }).then(callback);
    });
    this.Given(/^jag byter vårdenhet till "([^"]*)"$/, function(id, callback) {
        //basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(function() 
        basePage.changeUnit.click().then(function() {
            var enhetId = 'select-active-unit-' + id + '-modal';
            console.log(enhetId);
            element(by.id(enhetId)).click().then(callback); //sendKeys(protractor.Key.SPACE).then(callback);

        });


    });


};
