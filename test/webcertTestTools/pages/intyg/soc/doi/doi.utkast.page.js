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

/*globals element,by, Promise, protractor, browser*/
'use strict';

var BaseSocUtkast = require('../soc.base.utkast.page.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init('certificate-content-container');

var moveAndSendKeys = testTools.protractorHelpers.moveAndSendKeys;
var scrollElm = testTools.protractorHelpers.scrollContainer;

var doiUtkast = BaseSocUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.identitetStyrktGenom = { //identitetStyrktGenom är inte samma element som i TS intyg
            container: element(by.id('form_identitetStyrkt')),
            inputText: element(by.id('identitetStyrkt'))
        };
        this.dodsdatum = {
            container: element(by.id('form_dodsdatumSakert')),
            sakert: {
                checkbox: element(by.id('dodsdatumSakertYes')),
                datePicker: element(by.id('datepicker_dodsdatum'))
            },
            inteSakert: {
                checkbox: element(by.id('dodsdatumSakertNo')),
                month: element(by.css('#dodsdatum-month > div.ui-select-match > span.btn > span.ui-select-match-text > span.ng-binding')),
                year: element(by.css('#dodsdatum-year > div.ui-select-match > span.btn > span.ui-select-match-text > span.ng-binding')),
                options: element.all(by.css('.ui-select-choices-row-inner')),
                antraffadDod: element(by.id('datepicker_antraffatDodDatum'))
            }
        };
        this.dodsPlats = {
            kommun: {
                container: element(by.id('form_dodsplatsKommun')),
                inputText: element(by.id('dodsplatsKommun'))
            },
            boende: {
                container: element(by.id('form_dodsplatsBoende')),
                sjukhus: element(by.id('dodsplatsBoende-SJUKHUS')),
                ordinartBoende: element(by.id('dodsplatsBoende-ORDINART_BOENDE')),
                sarskiltBoende: element(by.id('dodsplatsBoende-SARSKILT_BOENDE')),
                annan: element(by.id('dodsplatsBoende-ANNAN'))
            }
        };
        this.barn = {
            container: element(by.id('form_barn')),
            ja: element(by.id('barnYes')),
            nej: element(by.id('barnNo'))
        };
        this.utlatandeOmDodsorsak = {
            a: {
                beskrivning: element(by.id('orsak--beskrivning')),
                datum: element(by.id('orsak--datum')),
                specifikation: {
                    dropDown: element(by.id('orsak--specifikation')),
                    akut: element(by.id('ui-select-choices-row-0-1')),
                    kronisk: element(by.id('ui-select-choices-row-0-2')),
                    uppgiftSaknas: element(by.id('ui-select-choices-row-0-3'))
                }
            },
            b: {
                beskrivning: element(by.id('orsak-0-beskrivning')),
                datum: element(by.id('orsak-0-datum')),
                specifikation: {
                    dropDown: element(by.id('orsak-0-specifikation')),
                    akut: element(by.id('ui-select-choices-row-2-1')),
                    kronisk: element(by.id('ui-select-choices-row-2-2')),
                    uppgiftSaknas: element(by.id('ui-select-choices-row-2-3'))
                }
            },
            c: {
                beskrivning: element(by.id('orsak-1-beskrivning')),
                datum: element(by.id('orsak-1-datum')),
                specifikation: {
                    dropDown: element(by.id('orsak-1-specifikation')),
                    akut: element(by.id('ui-select-choices-row-3-1')),
                    kronisk: element(by.id('ui-select-choices-row-3-2')),
                    uppgiftSaknas: element(by.id('ui-select-choices-row-3-3'))
                }
            },
            d: {
                beskrivning: element(by.id('orsak-2-beskrivning')),
                datum: element(by.id('orsak-2-datum')),
                specifikation: {
                    dropDown: element(by.id('orsak-2-specifikation')),
                    akut: element(by.id('ui-select-choices-row-4-1')),
                    kronisk: element(by.id('ui-select-choices-row-4-2')),
                    uppgiftSaknas: element(by.id('ui-select-choices-row-4-3'))
                }
            },
            andraSjukdomarSkador: {
                beskrivning: element(by.id('orsak-multi-0-beskrivning')),
                datum: element(by.id('orsak-multi-0-datum')),
                specifikation: {
                    dropDown: element(by.id('orsak-0-specifikation')),
                    akut: element(by.id('ui-select-choices-row-1-1')),
                    kronisk: element(by.id('ui-select-choices-row-1-2')),
                    uppgiftSaknas: element(by.id('ui-select-choices-row-1-3'))
                }
            }
        };
        this.operation = {
            ja: {
                checkbox: element(by.id('operation-JA')),
                datePicker: element(by.id('datepicker_operationDatum')),
                inputText: element(by.id('operationAnledning'))
            },
            nej: element(by.id('operation-NEJ')),
            uppgiftSaknas: element(by.id('operation-UPPGIFT_SAKNAS'))
        };
        this.skadaForgiftning = {
            ja: element(by.id('forgiftningYes')),
            nej: element(by.id('forgiftningNo'))
        };
        this.dodsorsaksuppgifter = {
            foreDoden: element(by.id('grunder-UNDERSOKNING_FORE_DODEN')),
            efterDoden: element(by.id('grunder-UNDERSOKNING_EFTER_DODEN')),
            kliniskObduktion: element(by.id('grunder-KLINISK_OBDUKTION')),
            rattsmedicinskObduktion: element(by.id('grunder-RATTSMEDICINSK_OBDUKTION')),
            rattsmedicinskBesiktning: element(by.id('grunder-RATTSMEDICINSK_BESIKTNING'))
        };
        this.enhetensAdress = {
            postAdress: element(by.id('grundData-skapadAv-vardenhet-postadress')),
            postNummer: element(by.id('grundData-skapadAv-vardenhet-postnummer')),
            postOrt: element(by.id('grundData-skapadAv-vardenhet-postort')),
            enhetsTelefon: element(by.id('grundData-skapadAv-vardenhet-telefonnummer'))
        };
    },
    angeIdentitetStyrktGenom: function angeIdentitetStyrktGenom(identitetStyrktGenom) {
        var identitetStyrktGenomElm = this.identitetStyrktGenom.inputText;

        return moveAndSendKeys(identitetStyrktGenomElm, identitetStyrktGenom);
    },
    angeDodsdatum: function angeDodsdatum(dodsdatum) {
        var dodsdatumElm = this.dodsdatum;

        console.log(dodsdatum.sakert);

        if (dodsdatum.sakert) {
            return moveAndSendKeys(dodsdatumElm.sakert.checkbox, protractor.Key.SPACE).then(function() {
                return moveAndSendKeys(dodsdatumElm.sakert.datePicker, dodsdatum.sakert.datum);
            });
        } else {
            console.log(dodsdatum.inteSakert);
            return moveAndSendKeys(dodsdatumElm.inteSakert.checkbox, protractor.Key.SPACE)
                .then(function() {
                    return dodsdatumElm.inteSakert.year.click().then(function() {
                        return dodsdatumElm.inteSakert.options.getByText(dodsdatum.inteSakert.year)
                            .then(function(elm) {
                                return elm.click();
                            });
                    });
                })
                .then(function() {
                    if (dodsdatum.inteSakert.year !== '0000 (ej känt)') {
                        return dodsdatumElm.inteSakert.month.click()
                            .then(function() {
                                return dodsdatumElm.inteSakert.options.getByText(dodsdatum.inteSakert.month).then(function(monthElm) {
                                    return scrollElm(monthElm, 2)
                                        .then(function() {
                                            return monthElm.click();
                                        })
                                        .then(function() {
                                            return moveAndSendKeys(dodsdatumElm.inteSakert.antraffadDod, dodsdatum.inteSakert.antraffadDod);
                                        });
                                });
                            });
                    }
                    return;
                });
        }
    },
    angeDodsPlats: function angeDodsPlats(dodsPlats) {
        var dodsPlatsElm = this.dodsPlats;

        return moveAndSendKeys(dodsPlatsElm.kommun.inputText, dodsPlats.kommun)
            .then(function() {
                switch (dodsPlats.boende) {
                    case 'sjukhus':
                        return moveAndSendKeys(dodsPlatsElm.boende.sjukhus, protractor.Key.SPACE);
                    case 'ordinartBoende':
                        return moveAndSendKeys(dodsPlatsElm.boende.ordinartBoende, protractor.Key.SPACE);
                    case 'sarskiltBoende':
                        return moveAndSendKeys(dodsPlatsElm.boende.sarskiltBoende, protractor.Key.SPACE);
                    case 'annan':
                        return moveAndSendKeys(dodsPlatsElm.boende.annan, protractor.Key.SPACE);
                    default:
                        throw ('dodsPlats.boende hittades inte');
                }
            });
    },
    angeBarn: function angeBarn(barn) {
        var barnElm = this.barn;

        if (typeof barn !== 'undefined') {
            if (barn === true) {
                return moveAndSendKeys(barnElm.ja, protractor.Key.SPACE);
            } else {
                return moveAndSendKeys(barnElm.nej, protractor.Key.SPACE);
            }
        } else {
            return Promise.resolve();
        }

    },
    angeUtlatandeOmDodsorsak: function angeUtlatandeOmDodsorsak(dodsorsak) {
        var utlatandeOmDodsorsakElm = this.utlatandeOmDodsorsak;
        console.log(dodsorsak);

        return moveAndSendKeys(utlatandeOmDodsorsakElm.a.beskrivning, dodsorsak.a.beskrivning).then(function() {
            return moveAndSendKeys(utlatandeOmDodsorsakElm.a.datum, dodsorsak.a.datum);
        }).then(function() {
            browser.ignoreSynchronization = false;
            return utlatandeOmDodsorsakElm.a.specifikation.dropDown.click().then(function() {

                if (dodsorsak.a.tillstandSpec === 'Akut') {
                    return utlatandeOmDodsorsakElm.a.specifikation.akut.click();
                } else if (dodsorsak.a.tillstandSpec === 'Kronisk') {
                    return utlatandeOmDodsorsakElm.a.specifikation.kronisk.click();
                } else if (dodsorsak.a.tillstandSpec === 'Uppgift saknas') {
                    return utlatandeOmDodsorsakElm.a.specifikation.uppgiftSaknas.click();
                } else {
                    throw ('Klarade inte att matcha ' + dodsorsak.a.tillstandSpec);
                }
            }).then(function() {
                //return browser.sleep(10000);
            }).then(function() {
                browser.ignoreSynchronization = true;
                return;
            });
        });
    },
    angeOperation: function angeOperation(operation) {
        var operationElm = this.operation;

        if (operation.ja) {
            return moveAndSendKeys(operationElm.ja.checkbox, protractor.Key.SPACE).then(function() {
                return moveAndSendKeys(operationElm.ja.datePicker, operation.ja.datum);
            }).then(function() {
                return moveAndSendKeys(operationElm.ja.inputText, operation.ja.beskrivning);
            });
        } else if (operation === 'Nej') {
            return moveAndSendKeys(operationElm.nej, protractor.Key.SPACE);
        } else {
            //'Uppgift om operation saknas'
            return moveAndSendKeys(operationElm.uppgiftSaknas, protractor.Key.SPACE);
        }
    },
    angeSkadaForgiftning: function angeSkadaForgiftning(skadaForgiftning) {
        if (skadaForgiftning === true) {
            return moveAndSendKeys(this.skadaForgiftning.ja, protractor.Key.SPACE);
        } else {
            return moveAndSendKeys(this.skadaForgiftning.nej, protractor.Key.SPACE);
        }
    },
    angeDodsorsaksuppgifterna: function angeDodsorsaksuppgifterna(dodsorsaksuppgifter) {
        var dodsorsaksuppgifterElm = this.dodsorsaksuppgifter;

        return new Promise(function(resolve) {
            resolve();
        }).then(function() {
            if (dodsorsaksuppgifter.foreDoden === true) {
                return moveAndSendKeys(dodsorsaksuppgifterElm.foreDoden, protractor.Key.SPACE);
            } else {
                return;
            }
        }).then(function() {
            if (dodsorsaksuppgifter.efterDoden === true) {
                return moveAndSendKeys(dodsorsaksuppgifterElm.efterDoden, protractor.Key.SPACE);
            } else {
                return;
            }
        }).then(function() {
            if (dodsorsaksuppgifter.kliniskObduktion === true) {
                return moveAndSendKeys(dodsorsaksuppgifterElm.kliniskObduktion, protractor.Key.SPACE);
            } else {
                return;
            }
        }).then(function() {
            if (dodsorsaksuppgifter.rattsmedicinskObduktion === true) {
                return moveAndSendKeys(dodsorsaksuppgifterElm.rattsmedicinskObduktion, protractor.Key.SPACE);
            } else {
                return;
            }
        }).then(function() {
            if (dodsorsaksuppgifter.rattsmedicinskBesiktning === true) {
                return moveAndSendKeys(dodsorsaksuppgifterElm.rattsmedicinskBesiktning, protractor.Key.SPACE);
            } else {
                return;
            }
        });
    }
});

module.exports = new doiUtkast();
