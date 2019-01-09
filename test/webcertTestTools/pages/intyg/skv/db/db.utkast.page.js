/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

var BaseSkvUtkast = require('../skv.base.utkast.page.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init('certificate-content-container');

var moveAndSendKeys = testTools.protractorHelpers.moveAndSendKeys;

var DbUtkast = BaseSkvUtkast._extend({
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
                month: {
                    dropDown: element(by.id('dodsdatum-month')),
                    option: function(month) {
                        return element(by.id('dodsdatum-month-' + month));
                    }
                },
                year: {
                    dropDown: element(by.id('dodsdatum-year')),
                    option: function(year) {
                        return element(by.id('dodsdatum-year-' + year));
                    }
                },
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
        this.explosivImplantat = {
            container: element(by.id('form_explosivImplantat')),
            ja: element(by.id('explosivImplantatYes')),
            nej: element(by.id('explosivImplantatNo')),
            avlagsnat: {
                ja: element(by.id('explosivAvlagsnatYes')),
                nej: element(by.id('explosivAvlagsnatNo'))
            }
        };
        this.yttreUndersokning = {
            container: element(by.id('form_undersokningYttre')),
            ja: element(by.id('undersokningYttre-JA')),
            nejUndersokningSkaGoras: element(by.id('undersokningYttre-UNDERSOKNING_SKA_GORAS')),
            nejUndersokningGjortKortFore: {
                checkbox: element(by.id('undersokningYttre-UNDERSOKNING_GJORT_KORT_FORE_DODEN')),
                datePicker: element(by.id('datepicker_undersokningDatum'))
            }
        };
        this.polisanmalan = {
            container: element(by.id('form_polisanmalan')),
            ja: element(by.id('polisanmalanYes')),
            nej: element(by.id('polisanmalanNo'))
        };
        this.enhetensAdress = {
            postAdress: element(by.id('grundData-skapadAv-vardenhet-postadress')),
            postNummer: element(by.id('grundData-skapadAv-vardenhet-postnummer')),
            postOrt: element(by.id('grundData-skapadAv-vardenhet-postort')),
            enhetsTelefon: element(by.id('grundData-skapadAv-vardenhet-telefonnummer'))
        };
        this.skrivDoi = {
            knapp: element(by.id('createFromTemplateBtn')),
            fortsatt: element(by.id('button1ersatt-dialog'))
        };
        this.makulera = {
            bekrafta: element(by.id('button1makulera-dialog'))
        };
    },
    angeIdentitetStyrktGenom: function angeIdentitetStyrktGenom(identitetStyrktGenom) {
        var identitetStyrktGenomElm = this.identitetStyrktGenom.inputText;

        return moveAndSendKeys(identitetStyrktGenomElm, identitetStyrktGenom);
    },
    angeDodsdatum: function angeDodsdatum(dodsdatum) {
        var dodsdatumElm = this.dodsdatum;

        if (dodsdatum.sakert) {
            return moveAndSendKeys(dodsdatumElm.sakert.checkbox, protractor.Key.SPACE).then(function() {
                return moveAndSendKeys(dodsdatumElm.sakert.datePicker, dodsdatum.sakert.datum);
            });
        } else {
            return moveAndSendKeys(dodsdatumElm.inteSakert.checkbox, protractor.Key.SPACE).then(function() {
                return browser.sleep(500);
            }).then(function() {
                return dodsdatumElm.inteSakert.year.dropDown.click();

            }).then(function() {
                //Väntar på drop-down att öppnas
                return browser.sleep(500);
            }).then(function() {
                return dodsdatumElm.inteSakert.year.option(dodsdatum.inteSakert.year).click();
            }).then(function() {
                if (dodsdatum.inteSakert.year !== '0000') {
                    return dodsdatumElm.inteSakert.month.dropDown.click().then(function() {
                        return browser.sleep(500);
                    }).then(function() {
                        return dodsdatumElm.inteSakert.month.option(dodsdatum.inteSakert.month).click();
                    });
                }
                return;
            }).then(function() {
                return moveAndSendKeys(dodsdatumElm.inteSakert.antraffadDod, dodsdatum.inteSakert.antraffadDod);
            });
        }
    },
    angeDodsPlats: function angeDodsPlats(dodsPlats) {
        var dodsPlatsElm = this.dodsPlats;

        return moveAndSendKeys(dodsPlatsElm.kommun.inputText, dodsPlats.kommun)
            .then(function() {
                switch (dodsPlats.boende) {
                    case 'Sjukhus':
                        return moveAndSendKeys(dodsPlatsElm.boende.sjukhus, protractor.Key.SPACE);
                    case 'Ordinärt boende':
                        return moveAndSendKeys(dodsPlatsElm.boende.ordinartBoende, protractor.Key.SPACE);
                    case 'Särskilt boende':
                        return moveAndSendKeys(dodsPlatsElm.boende.sarskiltBoende, protractor.Key.SPACE);
                    case 'Annan/okänd':
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
    angeExplosivImplantat: function angeExplosivImplantat(explosivImplantat) {
        var explosivImplantatElm = this.explosivImplantat;
        if (explosivImplantat !== false) {
            return moveAndSendKeys(explosivImplantatElm.ja, protractor.Key.SPACE)
                .then(function() {
                    if (explosivImplantat.avlagsnat === true) {
                        return moveAndSendKeys(explosivImplantatElm.avlagsnat.ja, protractor.Key.SPACE);
                    } else {
                        return moveAndSendKeys(explosivImplantatElm.avlagsnat.nej, protractor.Key.SPACE);
                    }
                });
        } else {
            return moveAndSendKeys(explosivImplantatElm.nej, protractor.Key.SPACE);
        }
    },
    angeYttreUndersokning: function angeYttreUndersokning(yttreUndersokning) {
        var yttreUndersokningElm = this.yttreUndersokning;

        switch (yttreUndersokning.value) {
            case 'Ja':
                return moveAndSendKeys(yttreUndersokningElm.ja, protractor.Key.SPACE);
            case 'Nej, rättsmedicinsk undersökning ska göras':
                return moveAndSendKeys(yttreUndersokningElm.nejUndersokningSkaGoras, protractor.Key.SPACE);
            case 'Nej, den avlidne undersökt kort före döden':
                return moveAndSendKeys(yttreUndersokningElm.nejUndersokningGjortKortFore.checkbox, protractor.Key.SPACE).then(function() {
                    return moveAndSendKeys(yttreUndersokningElm.nejUndersokningGjortKortFore.datePicker, yttreUndersokning.datum);
                });
            default:
                throw ('Ingen testdata för yttreUndersokning hittades');
        }
    },
    angePolisanmalan: function angePolisanmalan(polisanmalan) {
        var polisanmalanElm = this.polisanmalan;

        if (typeof polisanmalan !== 'undefined') {
            if (polisanmalan === true) {
                return moveAndSendKeys(polisanmalanElm.ja, protractor.Key.SPACE);
            } else {
                return moveAndSendKeys(polisanmalanElm.nej, protractor.Key.SPACE);
            }
        } else {
            return Promise.resolve();
        }
    }

});

module.exports = new DbUtkast();
