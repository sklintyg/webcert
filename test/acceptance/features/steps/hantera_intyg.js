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

/* globals pages, intyg, protractor, browser, logger, browser, Promise, wcTestTools*/

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


var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var utkastPage = pages.intyg.base.utkast;
var helpers = require('./helpers.js');
var moveAndSendKeys = helpers.moveAndSendKeys;
var loginHelpers = require('./inloggning/login.helpers');

/*
 *	Stödfunktioner
 *
 */

function signeraUtkast() {

    var uppdateraAdressOmErsattandeIntyg = function() {
        if (global.ersattintyg) {
            logger.info('Intyget ersätter ett annat intyg');
            return require('./fillIn/common.js').setPatientAdressIfNotGiven();
        }

        return Promise.resolve();
    };

    return uppdateraAdressOmErsattandeIntyg().then(function() {
        return browser.sleep(1).then(function() { // fix för nåt med animering?

            return expect(fkUtkastPage.klartAttSigneraStatus.getText()).to.eventually.contain('Klart')
                .then(function() {
                    return moveAndSendKeys(fkUtkastPage.signeraButton, protractor.Key.SPACE);
                })
                .then(function() {
                    // Verifiera att det inte finns valideringsfel
                    var ejKomplettEL = element(by.cssContainingText('h3', 'Utkastet saknar uppgifter i följande avsnitt'));
                    return expect(ejKomplettEL.isPresent()).to.become(false)
                        .then(function(val) {
                            //Elementet finns inte i DOM
                            return Promise.resolve();
                        }, function(val) {
                            //Om elementet finns tillgänligt på sidan så ska det iallafall inte vara synligt!
                            return expect(ejKomplettEL.isDisplayed()).to.become(false)
                                .then(function() {
                                    return Promise.resolve('Elementet är inte tillgänligt och inte synligt');
                                }, function() {
                                    throw ('Utkastet är inte komplett och kunde inte signeras. Se screenshot' + '\n' + val);

                                });

                        });
                });
        });
    });
}

function makuleraIntyget() {
    return browser.getCurrentUrl()
        .then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            intyg.id = intyg.id.split('?')[0];
        })
        .then(function() {
            return moveAndSendKeys(fkIntygPage.makulera.btn, protractor.Key.SPACE);
        })
        .then(function() {
            return helpers.largeDelay(); // fix för animering
        })
        .then(function() {
            return fkIntygPage.pickMakuleraOrsak();
        })
        .then(function() {
            return moveAndSendKeys(fkIntygPage.makulera.dialogMakulera, protractor.Key.SPACE);
        }).then(function() {
            return helpers.largeDelay(); // Sleep p.g.a. page reload.
        });
}

function raderaUtkastet() {
    return moveAndSendKeys(fkUtkastPage.radera.knapp, protractor.Key.SPACE).then(function() {
        return moveAndSendKeys(fkUtkastPage.radera.bekrafta, protractor.Key.SPACE);
    }).then(function() {
        return helpers.largeDelay(); // Page reload
    });
}


function makuleraIntygLoop(intygsTyp) {
    logger.info('Startar makuleraIntygLoop');
    return new Promise(function(resolve, reject) {
        return helpers.getIntygElementRow(intygsTyp, intygsTyp, function(intygRadElement) {
            if (!intygRadElement) {
                logger.info('OK - Inget intyg med intygstyp ' + intygsTyp + ' finns');
                return resolve('OK - Inget intyg med intygstyp ' + intygsTyp + ' finns');
            } else {
                return intygRadElement.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE)
                    .then(function() {
                        return helpers.pageReloadDelay(); // Laddar in intyget
                    })
                    .then(function() {
                        return moveAndSendKeys(fkIntygPage.makulera.btn, protractor.Key.SPACE);
                    })
                    .then(function() {
                        return helpers.largeDelay(); // fix för animering
                    }).then(function() {
                        return fkIntygPage.pickMakuleraOrsak();
                    }).then(function() {
                        return moveAndSendKeys(fkIntygPage.makulera.dialogMakulera, protractor.Key.SPACE);
                    }).then(function() {
                        return makuleraIntygLoop(intygsTyp).then(function() {
                            return resolve();
                        });
                    });
            }
        });
    });
}

function raderaUtkastLoop(intygsTyp) {
    logger.info('Startar raderaUtkastLoop');
    return new Promise(function(resolve, reject) {
        return helpers.getIntygElementRow(intygsTyp, intygsTyp, function(intygRadElement) {
            if (!intygRadElement) {
                logger.info('OK - Inget intyg med intygstyp ' + intygsTyp + ' finns');
                return resolve('OK - Inget intyg med intygstyp ' + intygsTyp + ' finns');
            } else {
                return intygRadElement.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE)
                    .then(function() {
                        return helpers.pageReloadDelay(); // Laddar in intyget
                    })
                    .then(function() {
                        return helpers.pageReloadDelay(); // Laddar in intyget
                    })
                    .then(function() {
                        return moveAndSendKeys(fkUtkastPage.radera.knapp, protractor.Key.SPACE);
                    })
                    .then(function() {
                        return moveAndSendKeys(fkUtkastPage.radera.bekrafta, protractor.Key.SPACE);
                    }).then(function() {
                        return helpers.largeDelay(); // Page reload
                    }).then(function() {
                        return raderaUtkastLoop(intygsTyp).then(function() {
                            return resolve();
                        });
                    });
            }
        });
    });
}

/*
 *	Test steg
 *
 */


Given(/^jag signerar intyget$/, function() {
    return signeraUtkast();
});

Then(/^klickar jag på knappen "([^"]*)"$/, function(knapp) {
    if (knapp === 'Skriv dödsorsaksintyg') {
        intyg.typ = 'Dödsorsaksintyg';

        return pages.intyg.skv.db.utkast.skrivDoiKnapp.click().then(function() {
            return browser.getCurrentUrl().then(function(text) {
                global.dbIntyg = intyg;

                logger.info('global.dbIntyg.id: ' + global.dbIntyg.id);

                intyg.id = text.split('/').slice(-2)[0];
                intyg.id = intyg.id.split('?')[0];

                global.intyg = helpers.generateIntygByType(intyg.typ, intyg.id);
                logger.info('intyg.id: ' + intyg.id);

                return intyg;
            });
            //TODO: Uppdatera testdata så att den matchar med dödsdatum.
        });

    } else {
        return;
    }
});

Given(/^jag signerar och skickar kompletteringen$/, function() {
    return signeraUtkast();
});

Given(/^ska det inte finnas någon knapp för "([^"]*)"$/, function(texten) {
    return expect(element(by.cssContainingText('.btn', texten)).isPresent()).to.become(false);
});

Given(/^jag klickar på signera\-knappen$/, function() {
    return moveAndSendKeys(fkUtkastPage.signeraButton, protractor.Key.SPACE);
});



Given(/^ska signera\-knappen inte vara klickbar$/, function(callback) {
    utkastPage.signeraButton.isEnabled().then(function(isVisible) {
        if (isVisible) {
            callback('FEL - Signera-knappen är klickbar!');
        } else {
            logger.debug('OK Signera-knappen är ej klickbar!');
        }
    }).then(callback);
});

Given(/^jag uppdaterar enhetsaddress$/, function() {
    return require('./fillIn/common.js').fillInEnhetAdress();
});

Given(/^jag makulerar intyget$/, function() {

    return makuleraIntyget();
});

Given(/^jag makulerar tidigare "([^"]*)" intyg$/, function(intygsTyp, txt) {
    return makuleraIntygLoop(intygsTyp).then(function() {
        return raderaUtkastLoop(intygsTyp);
    });
});

Givet(/^jag har raderat alla intyg för "([^"]*)" via testAPI$/, function(personnummer) {
	
});

Givet(/^jag har raderat alla utkast för "([^"]*)" via testAPI$/, function(personnummer) {
	
});

Given(/^jag har makulerat tidigare "([^"]*)" intyg för "([^"]*)" testpatienten$/, function(intygsTyp, txt) {

    var testpatienter = wcTestTools.testdata.values.patienter;

    var patient;
    if (txt === 'sekretessmarkering') {
        patient = wcTestTools.testdata.values.patienterMedSekretessmarkering[0];
    } else {
        patient = testpatienter[helpers.getIntFromTxt(txt)];
    }


    return helpers.getUtkast(intygsTyp, patient).then(function(data) {
        //logger.silly('data.length: ' + data.length);
        //logger.silly(data);

        if (!data[0]) {
            return logger.info('OK - Inget utkast med intygsTyp ' + intygsTyp + ' finns i databasen för patienten ' + patient.id);
        }

        //Logga in på vårdenhet som skapat intyget:
        var userObj = {
            forNamn: 'x',
            efterNamn: 'y',
            hsaId: data[0].SKAPAD_AV_HSAID,
            enhetId: data[0].ENHETS_ID
        };
        global.intyg = {};
        global.intyg.id = data[0].INTYGS_ID;


        return loginHelpers.logInAsUser(userObj)
            .then(function() {
                return helpers.pageReloadDelay();
            })
            .then(function() {
                var intygUrlShortcode = helpers.getPathShortcode(intygsTyp).toLowerCase();
                var link = '/#/' + intygUrlShortcode + '/edit/' + intyg.id + '/';
                logger.info('Går till ' + link);
                return browser.get(link);
            })
            .then(function() {
                return helpers.pageReloadDelay();
            })
            .then(function() {
                return raderaUtkastet();
            }).then(function() {
                return helpers.getIntyg(intygsTyp, patient, false).then(function(data) {
                    //logger.silly('data.length: ' + data.length);
                    //logger.silly(data);

                    if (!data[0]) {
                        return logger.info('OK - Inget intyg med intygsTyp ' + intygsTyp + ' finns i databasen för patienten ' + patient.id);
                    }

                    //Logga in på vårdenhet som skapat intyget:
                    var userObj = {
                        forNamn: 'x',
                        efterNamn: 'y',
                        hsaId: data[0].SKAPAD_AV_HSAID,
                        enhetId: data[0].ENHETS_ID
                    };
                    global.intyg = {};
                    global.intyg.id = data[0].INTYGS_ID;


                    return loginHelpers.logInAsUser(userObj)
                        .then(function() {
                            return helpers.pageReloadDelay();
                        })
                        .then(function() {
                            var intygUrlShortcode = helpers.getPathShortcode(intygsTyp).toLowerCase();
                            var link = '/#/' + intygUrlShortcode + '/edit/' + intyg.id + '/';
                            logger.info('Går till ' + link);
                            return browser.get(link);
                        })
                        .then(function() {
                            return helpers.pageReloadDelay();
                        })
                        .then(function() {
                            return makuleraIntyget();
                        });
                });
            });
    });
});


Given(/^jag raderar utkastet$/, function() {
    return raderaUtkastet();
});


Given(/^jag skriver ut intyget$/, function() {
    var isFK7263Intyg = helpers.isFK7263Intyg(intyg.typ);

    if (isFK7263Intyg === false) {
        return moveAndSendKeys(element(by.id('downloadprint')), protractor.Key.SPACE);
    } else {
        return fkIntygPage.skrivUtFullstandigtIntyg();
    }
});

Given(/^jag skriver ut utkastet$/, function() {
    return moveAndSendKeys(utkastPage.skrivUtBtn, protractor.Key.SPACE).then(function() {
        return helpers.pageReloadDelay(); // Page reload
    });
});

Given(/^ska det finnas en referens till gamla intyget$/, function() {
    return element(by.id('wc-intyg-relations-button')).click().then(function() { // May not be needed. Only to graphically illustrate normal user behavior.
        return browser.findElement(by.css('.btn-info')).sendKeys(protractor.Key.SPACE).then(function() {
            return browser.getCurrentUrl().then(function(text) {
                logger.info('(%s contain %s) => %s', text, intyg.id, (text.indexOf(intyg.id) !== -1 ? true : false));
                return expect(text).to.contain(intyg.id);
            });
        });
    });
});
Given(/^ska intyget inte innehålla gamla personuppgifter$/, function() {
    var namn = global.intyg.person.forNamn + ' ' + global.intyg.person.efterNamn;
    return expect(element(by.id('patientNamnPersonnummer')).getText()).to.eventually.not.contain(namn);

});

When(/^jag markerar intyget som klart för signering$/, function() {
    return moveAndSendKeys(element(by.id('markeraKlartForSigneringButton')), protractor.Key.SPACE);
});

When(/^ska jag se texten "([^"]*)"$/, function(msg) {
    return expect(element(by.id('draft-marked-ready-text')).getText()).to.eventually.contain(msg);
});

Given(/^ska intyget inte finnas i listan$/, function() {

    return expect(element(by.id('wc-sekretessmarkering-icon-' + intyg.id)).isPresent()).to.become(false).then(function() {

        return expect(element(by.id('showBtn-' + intyg.id)).isPresent()).to.become(false);

    });
});

When(/^jag fyller i nödvändig information \( om intygstyp är "([^"]*)"\)$/, function(intygstyp) {

    if (intygstyp !== intyg.typ) {
        logger.info('Intygstyp är inte ' + intygstyp);
        return Promise.resolve();
    } else {
        browser.ignoreSynchronization = true;
        logger.info('Intygstyp är: ' + intyg.typ);
        logger.info(intyg);

        if (intyg.typ === 'Dödsorsaksintyg') {
            var doiUtkastPage = pages.intyg.soc.doi.utkast;

            //Läkarens utlåtande om dödsorsaken 
            return doiUtkastPage.angeUtlatandeOmDodsorsak(intyg.dodsorsak)
                .then(function() {
                    logger.info('OK - angeUtlatandeOmDodsorsak');
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeUtlatandeOmDodsorsak,' + reason);
                }).then(function() {
                    //Opererad inom fyra veckor före döden
                    return doiUtkastPage.angeOperation(intyg.operation)
                        .then(function() {
                            logger.info('OK - angeOperation');
                        }, function(reason) {
                            console.trace(reason);
                            throw ('FEL, angeOperation,' + reason);
                        });
                }).then(function() {
                    //SkadaForgiftning
                    return doiUtkastPage.angeSkadaForgiftning(intyg.skadaForgiftning)
                        .then(function() {
                            logger.info('OK - angeSkadaForgiftning');
                        }, function(reason) {
                            console.trace(reason);
                            throw ('FEL, angeSkadaForgiftning,' + reason);
                        });
                }).then(function() {
                    //Dödsorsaksuppgifter
                    return doiUtkastPage.angeDodsorsaksuppgifterna(intyg.dodsorsaksuppgifter)
                        .then(function() {
                            logger.info('OK - angeDodsorsaksuppgifterna');
                        }, function(reason) {
                            console.trace(reason);
                            throw ('FEL, angeDodsorsaksuppgifterna,' + reason);
                        });
                });


        } else if (intyg.typ === 'Läkarintyg för sjukpenning') {

            if (typeof(intyg.baseratPa) === 'undefined') {
                global.intyg = helpers.generateIntygByType(intyg.typ, intyg.id);
            }
            return pages.intyg.lisjp.utkast.angeBaseratPa(intyg.baseratPa)
                .then(function() {
                    return logger.info('OK - angeBaseratPa');
                }, function(reason) {
                    throw ('FEL, angeBaseratPa,' + reason);
                })
                .then(function() {
                    return pages.intyg.lisjp.utkast.angeArbetsformaga(intyg.arbetsformaga).then(function() {
                        browser.ignoreSynchronization = false;
                        return logger.info('OK - angeArbetsformaga');
                    }, function(reason) {
                        throw ('FEL, angeArbetsformaga,' + reason);
                    });
                })
                .then(function() {
                    return pages.intyg.lisjp.utkast.angeArbetstidsforlaggning(intyg.arbetstidsforlaggning).then(function() {
                        logger.info('OK - angeArbetstidsforlaggning');
                    }, function(reason) {
                        console.trace(reason);
                        throw ('FEL, angeArbetstidsforlaggning,' + reason);
                    });
                })
                .then(function() {
                    return pages.intyg.lisjp.utkast.angePrognosForArbetsformaga(intyg.prognosForArbetsformaga).then(function() {
                        logger.info('OK - prognosForArbetsformaga');
                    }, function(reason) {
                        console.trace(reason);
                        throw ('FEL, prognosForArbetsformaga,' + reason);
                    });
                });
        } else {
            console.trace(intygstyp);
            logger.warn('Kunde inte matcha intygstyp.');
        }
    }
});
