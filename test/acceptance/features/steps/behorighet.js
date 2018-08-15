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

/* globals pages, protractor, logger, Promise, browser */
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


var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var tsIntygPage = pages.intyg.ts.bas.intyg;
var basePage = pages.webcertBase;
var utkastPage = pages.intyg.base.utkast;
var unsignedPage = pages.unsignedPage;
var helpers = require('./helpers');

/*
 *	Test steg
 *
 */

Given(/^går in på Sök\/skriv intyg$/, function() {
    return basePage.flikar.sokSkrivIntyg.click();
});

Given(/^går in på Ej signerade utkast$/, function() {
    return unsignedPage.flikar.notSigned.click();
});

/*Given(/^är förnyaknappen tillgänglig$/, function(callback) {
    logger.debug(basePage);
    logger.debug(basePage.fornya);
    logger.debug(basePage.fornya.button);
    expect(basePage.fornya.button.isPresent()).to.become(true).then(function() {
        logger.info('OK - Förnya knapp tillgänglig');
        callback();
        // basePage.fornyaBtn.sendKeys(protractor.Key.SPACE).then(callback);
    }, function(reason) {
        logger.warn(reason);
        callback('FEL : ' + reason);
    });
});*/

Then(/^ska det( inte)? finnas en knapp för att förnya intyget$/, function(inte) {
    var skaFinnas = typeof(inte) === 'undefined';
    logger.debug('skaFinnas:' + skaFinnas);

    return expect(fkIntygPage.fornyaBtn.isPresent()).to.become(skaFinnas).then(function() {
        logger.info('OK - Förnya knapp synlig: ' + skaFinnas);
    }, function(reason) {
        throw ('FEL : ' + reason);
    });
});


Then(/^ska det finnas en knapp för att skriva ut utkastet$/, function() {
    return expect(fkUtkastPage.skrivUtBtn.isPresent()).to.become(true);
});

Then(/^ska det finnas en knapp för att skriva ut intyget$/, function() {
    if (this.intyg.typ.indexOf('Transportstyrelsen') >= 0) {
        return expect(tsIntygPage.printBtn.isPresent()).to.become(true);
    } else {
        return expect(fkIntygPage.selectUtskriftButton.isPresent()).to.become(true);
    }
});

/*Given(/^är förnyaknappen inte tillgänglig$/, function() {
    logger.debug(basePage);
    logger.debug(basePage.fornya);
    logger.debug(basePage.fornya.button);
    return expect(basePage.fornya.button.isPresent()).to.become(false).then(function() {
        logger.info('OK - Förnya knappen syns inte');
    }, function(reason) {
        throw ('FEL : ' + reason);
    });
});*/

Given(/^visas Vidarebefodra knappen$/, function() {
    return expect(fkIntygPage.forwardBtn.isPresent()).to.become(true).then(function() {
        logger.info('OK - Vidarebeforda knappen hittad');
    }, function(reason) {
        throw ('FEL : ' + reason);
    });
});

Given(/^väljer att visa sökfilter/, function() {
    return unsignedPage.showSearchFilters();
});

Then(/^ska sökfiltret Sparat av inte vara tillgängligt/, function(callback) {
    expect(unsignedPage.filterSavedBy.form.isPresent()).to.eventually.be.not.ok.then(function(value) {
        logger.info('Filter \"Sparat av\" inte tillgängligt för uthoppsläkare ' + value);
    }, function(reason) {
        callback('FEL, Filter \"Sparat av\" tillgängligt för uthoppsläkare,' + reason);
    }).then(callback);
});

Given(/^att det finns intygsutkast$/, function() {
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
                                return helpers.getUrl(url); // gå tillbaka till översikt
                            });
                    });
            }
        });

});

Then(/^ska Förnya\-knappen inte visas för något utkast$/, function() {
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
            expect(joinedTexts).to.not.include('Förnya')
        ]);
    });
});


Then(/^ska Förnya\-knappen visas för( alla)?( aktuella)? signerade eller mottagna "([^"]*)"\-intyg$/, function(alla, aktuella, intygstyp) {

    if (alla) {
        element(by.id('intygFilterSamtliga')).sendKeys(protractor.Key.SPACE);
    }

    function checkRowForBtnWithText(rowText, buttonText, shouldBePresent) {
        var qaTable = element(by.css('.wc-table-striped'));
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
                //logger.silly('kontrollerart att rad ' + notText + ' har en ' + buttonText + '-knapp');
            }

            function onFail(reason) {
                logger.silly(reason.message);
                throw reason;
            }

            function printText(txt) {
                logger.silly(txt);
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

Given(/^är signeraknappen tillgänglig$/, function() {
    return expect(utkastPage.signeraButton.isPresent()).to.eventually.be.ok.then(function(value) {
        logger.info('Signeringsknapp existerar ' + value);
    }, function(reason) {
        throw ('FEL, Signeringsknapp finns inte på sidan,' + reason);
    }).then(expect(utkastPage.signeraButton.isEnabled()).to.eventually.be.ok.then(function(value) {
        logger.info('Signeringsknapp är klickbar' + value);
    }, function(reason) {
        throw ('FEL, Signeringsknapp är inte klickbar,' + reason);
    }));
});

Then(/^ska makuleraknappen inte vara tillgänglig$/, function() {
    return expect(fkIntygPage.makulera.btn.isPresent()).to.eventually.be.not.ok.then(function(value) {
        logger.info('Makuleraknappen syns inte (ok)' + value);
    }, function(reason) {
        throw ('FEL, Makuleraknappen finns tillgänglig,' + reason);
    });
});

Given(/^väljer att byta vårdenhet$/, function(callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
});

Given(/^vårdenhet ska vara "([^"]*)"$/, function(arg1, callback) {
    expect(basePage.header.getText()).to.eventually.contain(arg1).then(function(value) {
        logger.info('OK - vårdenhet = ' + value);
    }, function(reason) {
        callback('FEL - vårdenhet: ' + reason);
    }).then(callback);
});

When(/^jag väljer flik "([^"]*)"$/, function(arg1, callback) {
    expect(basePage.flikar.sokSkrivIntyg.getText()).to.eventually.contain(arg1).then(function(value) {
        element(basePage.flikar.sokSkrivIntyg).sendKeys(protractor.Key.SPACE);
        logger.info('OK - byta flik till = ' + value);
    }, function(reason) {
        callback('FEL - byta flik till: ' + reason);
    }).then(callback);
});

When(/^jag väljer att byta vårdenhet$/, function(callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
});

Given(/^väljer "([^"]*)"$/, function(arg1, callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(function(arg1) {
        element(by.id('select-active-unit-IFV1239877878-1045-modal')).sendKeys(protractor.Key.SPACE).then(callback);
    });
});

Given(/^visas inte signera knappen$/, function(callback) {
    fkUtkastPage.signeraButton.isPresent().then(function(isVisible) {
        if (isVisible) {
            callback('FEL - Signera knapp synlig!');
        } else {
            logger.debug('OK - Signera knapp ej synlig!');
        }
    }).then(callback);
});

Given(/^visas Hämta personuppgifter knappen$/, function(callback) {
    fkUtkastPage.fetchPatientButton.isPresent().then(function(isVisible) {
        if (isVisible) {
            logger.debug('OK - Hämta personuppgifter synlig!');
        } else {
            callback('FEL - Hämta personuppgifter ej synlig!');
        }
    }).then(callback);
});

Given(/^meddelas jag om spärren$/, function(callback) {
    expect(basePage.warnings.protectedInfo).getText()
        .to.eventually.contain('På grund av sekretessmarkeringen går det inte att skriva nya elektroniska intyg.').then(function(value) {
            logger.info('OK - sekretessmarkeringe = ' + value);
        }, function(reason) {
            callback('FEL - sekretessmarkeringe: ' + reason);
        }).then(callback);

});

When(/^jag byter vårdenhet till "([^"]*)"$/, function(id) {
    return helpers.moveAndSendKeys(basePage.expandUnitMenu, protractor.Key.SPACE)
        .then(function() {
            return helpers.mediumDelay();
        })
        .then(function() {
            return basePage.changeUnit.click();
        })
        .then(function() {
            var enhetId = 'select-active-unit-' + id + '-modal';
            logger.silly(enhetId);
            return helpers.moveAndSendKeys(element(by.id(enhetId)), protractor.Key.SPACE);

        });


});
