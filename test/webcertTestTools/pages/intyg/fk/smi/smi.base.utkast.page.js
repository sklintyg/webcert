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

/*globals element, by, Promise, protractor, browser, logger */
'use strict';

var FkBaseUtkast = require('../fk.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util');
var testTools = require('common-testtools');
var moveAndSendKeys = pageHelpers.moveAndSendKeys;

testTools.protractorHelpers.init('certificate-content-container');


function sendEnterToElement(el) {
    return function() {
        return moveAndSendKeys(el, protractor.Key.ENTER);
    };
}

var intellektuellForm = element(by.id('form_check-funktionsnedsattningIntellektuell'));
var kommunikationForm = element(by.id('form_check-funktionsnedsattningKommunikation'));
var koncentrationForm = element(by.id('form_check-funktionsnedsattningKoncentration'));
var psykiskForm = element(by.id('form_check-funktionsnedsattningPsykisk'));
var horselTalForm = element(by.id('form_check-funktionsnedsattningSynHorselTal'));
var balansForm = element(by.id('form_check-funktionsnedsattningBalansKoordination'));
var annanForm = element(by.id('form_check-funktionsnedsattningAnnan'));

var avslutadForm = element(by.id('form_avslutadBehandling'));
var planeradForm = element(by.id('form_planeradBehandling'));
var pagaendeForm = element(by.id('form_pagaendeBehandling'));
var substansintagForm = element(by.id('form_substansintag'));

function getCheckbox(el) {
    return el.element(by.css('input'));
}

function getTextarea(el) {
    return el.element(by.css('textarea'));
}

function checkAndSendTextToForm(checkboxEL, textEL, text) {
    return moveAndSendKeys(checkboxEL, protractor.Key.SPACE)
	.then(function() {
        return moveAndSendKeys(textEL, text);
    });
}

function sendTextToForm(textEL, text) {
    return textEL.sendKeys(text)
        .then(function() {
            logger.debug('OK - Angav: ' + text);
        }, function(reason) {
            throw ('FEL - Angav: ' + text + ' ' + reason);
        });
}

var BaseSmiUtkast = FkBaseUtkast._extend({
    init: function init() {
        init._super.call(this);
        // this.fmbButtons = ['diagnos-fmb-button', 'funktionsnedsattning-fmb-button', 'aktivitetsbegransning-fmb-button', 'bedomning-fmb-button'];

        this.fmbButtons = {
            falt2: element(by.id('diagnos-fmb-button')),
            falt4: element(by.id('funktionsnedsattning-fmb-button')),
            falt5: element(by.id('aktivitetsbegransning-fmb-button')),
            falt8: element(by.id('bedomning-fmb-button'))
        };

        this.at = element(by.css('.edit-form'));

        this.nameAddressChangedMsg = element(by.id('intyg-djupintegration-name-and-address-changed'));

        this.diagnoseCode = element(by.id('diagnoseCode-0'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.ovrigt = element(by.id('ovrigt'));
        this.tillaggsfragor0svar = this.getTillaggsfraga(0);
        this.tillaggsfragor1svar = this.getTillaggsfraga(1);

        this.funktionsnedsattning = {
            intellektuell: {
                checkbox: getCheckbox(intellektuellForm),
                text: getTextarea(intellektuellForm)

            },
            kommunikation: {
                checkbox: getCheckbox(kommunikationForm),
                text: getTextarea(kommunikationForm)
            },

            koncentration: {
                checkbox: getCheckbox(koncentrationForm),
                text: getTextarea(koncentrationForm)
            },

            annanPsykisk: {
                checkbox: getCheckbox(psykiskForm),
                text: getTextarea(psykiskForm)
            },

            synHorselTal: {
                checkbox: getCheckbox(horselTalForm),
                text: getTextarea(horselTalForm)

            },

            balansKoordination: {
                checkbox: getCheckbox(balansForm),
                text: getTextarea(balansForm)
            },

            annanKroppslig: {
                checkbox: getCheckbox(annanForm),
                text: getTextarea(annanForm)
            }
        };

        this.medicinskBehandling = {
            avslutad: {
                text: getTextarea(avslutadForm)
            },
            pagaende: {
                text: getTextarea(pagaendeForm)
            },
            planerad: {
                text: getTextarea(planeradForm)
            },
            substansintag: {
                text: getTextarea(substansintagForm)
            }
        };

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                return {
                    underlag: element(by.id('underlag-' + index + '--typ')),
                    datum: element(by.id('datepicker_underlag[' + index + '].datum')),
                    information: element(by.id('underlag-' + index + '--hamtasFran'))
                };
            }
        };

        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnos = {
            diagnosRow: function(index) {
                return {
                    kod: element(by.id('diagnoseCode-' + index)),
                    beskrivning: element(by.id('diagnoseDescription-' + index))
                };

            },
            narOchVarStalldesDiagnoser: element(by.id('diagnosgrund')),
            skalTillNyBedomning: {
                JA: element(by.id('nyBedomningDiagnosgrundYes')),
                NEJ: element(by.id('nyBedomningDiagnosgrundNo'))
            },
            diagnosForNyBedomning: element(by.id('diagnosForNyBedomning'))

        };

        this.ovrigt = element(by.id('ovrigt'));
        this.kontaktMedFK = element(by.id('form_kontaktMedFk')).element(by.css('input'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));

        //enhetsadress lika för alla SMI-intyg
        this.enhetensAdress = {
            postAdress: element(by.id('grundData-skapadAv-vardenhet-postadress')),
            postNummer: element(by.id('grundData-skapadAv-vardenhet-postnummer')),
            postOrt: element(by.id('grundData-skapadAv-vardenhet-postort')),
            enhetsTelefon: element(by.id('grundData-skapadAv-vardenhet-telefonnummer'))
        };
    },
    angeBaseratPa: function(baseratPa) {

        var baseratPaElmObj = this.baseratPa;
        return new Promise(function(resolve) {
                resolve('anger BaseratPa');
            })
            .then(function() {
                if (baseratPa.minUndersokningAvPatienten) {
                    return moveAndSendKeys(baseratPaElmObj.minUndersokningAvPatienten.datum, baseratPa.minUndersokningAvPatienten);
                }
                return;
            })
            .then(function() {
                if (baseratPa.journaluppgifter) {
                    return moveAndSendKeys(baseratPaElmObj.journaluppgifter.datum, baseratPa.journaluppgifter);
                }
                return;
            })
            .then(function() {
                if (baseratPa.telefonkontakt) {
                    return moveAndSendKeys(baseratPaElmObj.telefonkontakt.datum, baseratPa.telefonkontakt);
                }
                return;
            })
            .then(function() {
                if (baseratPa.anhorigsBeskrivning) {
                    return moveAndSendKeys(baseratPaElmObj.anhorigBeskrivning.datum, baseratPa.anhorigsBeskrivning);
                }
                return;
            })
            .then(function() {
                if (baseratPa.annat) {
                    return moveAndSendKeys(baseratPaElmObj.annat.datum, baseratPa.annat)
                        .then(function() {
                            return moveAndSendKeys(baseratPaElmObj.annat.beskrivning, baseratPa.annatBeskrivning);
                        });
                }
                return;
            })
            .then(function() {
                if (baseratPa.personligKannedom) {
                    return moveAndSendKeys(baseratPaElmObj.kannedomOmPatient.datum, baseratPa.personligKannedom);
                }
                return;
            });
    },
    angeFunktionsnedsattning: function(nedsattning) {
        var fn = this.funktionsnedsattning;
        return checkAndSendTextToForm(fn.intellektuell.checkbox, fn.intellektuell.text, nedsattning.intellektuell)
		.then(function() {
            return checkAndSendTextToForm(fn.kommunikation.checkbox, fn.kommunikation.text, nedsattning.kommunikation);
        }).then(function() {
            return checkAndSendTextToForm(fn.koncentration.checkbox, fn.koncentration.text, nedsattning.koncentration);
        }).then(function() {
            return checkAndSendTextToForm(fn.annanPsykisk.checkbox, fn.annanPsykisk.text, nedsattning.psykisk);
        }).then(function() {
            return checkAndSendTextToForm(fn.synHorselTal.checkbox, fn.synHorselTal.text, nedsattning.synHorselTal);
        }).then(function() {
            return checkAndSendTextToForm(fn.balansKoordination.checkbox, fn.balansKoordination.text, nedsattning.balansKoordination);
        }).then(function() {
            return checkAndSendTextToForm(fn.annanKroppslig.checkbox, fn.annanKroppslig.text, nedsattning.annan);
        });
    },

    angeAndraMedicinskaUtredningar: function(utredningar) {
        var utredningarElement = this.andraMedicinskaUtredningar;

        var fillIn = function fillInUtr(val, index) {
            var row = utredningarElement.underlagRow(index);


				browser.ignoreSynchronization = false;
				logger.silly('Klickar på element med id: underlag-' + index + '-typ');
				
				return browser.sleep(1500).then(function(){
					return row.underlag.element(by.css('.ui-select-match')).click()	//sendKeys fungerar inte för elementet på LuaeFS använder .click() istället.
						.then(function() {
							return browser.sleep(1500); //TODO utforska om det finns något sätt att få det fungera för samtliga SMI intyg utan sleep.
						})
						.then(function() {
							return row.underlag.all(by.css('.ui-select-choices-row')).getByText(val.underlag).then(function (elm) {
								return elm.click(); //sendKeys fungerar inte för elementet på LuaeFS använder .click() istället.
						});
					});
				})
				.then(function() {
					return moveAndSendKeys(row.datum, val.datum)
				})
                .then(function() {
					browser.ignoreSynchronization = true;
                    return moveAndSendKeys(row.information, val.infoOmUtredningen);
                });

        };

        if (utredningar) {
            return moveAndSendKeys(utredningarElement.finns.JA, protractor.Key.SPACE)
                .then(function() {
                    return utredningar.map(fillIn);
                });
        } else {
            return moveAndSendKeys(utredningarElement.finns.NEJ, protractor.Key.SPACE);
        }
    },
    angeSjukdomsforlopp: function(forlopp) {
        return this.sjukdomsforlopp.sendKeys(forlopp);
    },
    angeDiagnosKoder: function(diagnoser) {
        var promiseArr = [];
        for (var i = 0; i < diagnoser.length; i++) {
            var row = this.diagnos.diagnosRow(i);
			promiseArr.push(moveAndSendKeys(row.kod,diagnoser[i].kod).then(browser.sleep(1000)).then(sendEnterToElement(row.kod)))
        }
        return Promise.all(promiseArr);

    },
    angeDiagnos: function(diagnosObj) {
        var diagnoser = diagnosObj.diagnoser;
		var diagnosElm = this.diagnos;


        //Ange diagnoser
        return this.angeDiagnosKoder(diagnoser)
		
		.then(function(){
			//Ange när och var diagnoser ställts
			return moveAndSendKeys(diagnosElm.narOchVarStalldesDiagnoser, diagnosObj.narOchVarStalldesDiagnoserna);
			})
		.then(function(){
			//Ange Finns skäl till ny bedömning
			var nyBedomning = diagnosElm.skalTillNyBedomning.NEJ;
			if (diagnosObj.nyBedomning) {
				nyBedomning = diagnosElm.skalTillNyBedomning.JA;
			}
			var diagnosForNyBedomning = diagnosElm.diagnosForNyBedomning;
			
			return moveAndSendKeys(nyBedomning, protractor.Key.SPACE)
			.then(function() {
				if (diagnosObj.nyBedomning) {
						//Ange diagnosForNyBedomning
						return moveAndSendKeys(diagnosForNyBedomning, diagnosObj.diagnosForNyBedomning);
				} else {
					return Promise.resolve();
				}
			});
			
		});
    },
	angeOvrigaUpplysningar: function(ovrigt) {
        var elm = this.ovrigt;
        return elm.clear().then(function() {
            return moveAndSendKeys(elm, ovrigt);
        });
    },
    angeKontaktMedFK: function(kontakt) {
        if (kontakt) {
            return moveAndSendKeys(this.kontaktMedFK, protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeTillaggsfragor: function(svarArr) {
        if (!svarArr) {
            return Promise.resolve();
        } else {
            var promiseArr = [];
            for (var i = 0; i < svarArr.length; i++) {
                promiseArr.push(this.getTillaggsfraga(i).sendKeys(svarArr[i].svar));
            }
            return Promise.all(promiseArr);
        }
    },
    angeTillaggsfragorUE: function(svarArr) {
        if (!svarArr) {
            return Promise.resolve();
        } else {
            var promiseArr = [];
            for (var i = 0; i < svarArr.length; i++) {
                promiseArr.push(this.getTillaggsfragaUE(i).sendKeys(svarArr[i].svar));
            }
            return Promise.all(promiseArr);
        }
    },
    angeMedicinskBehandling: function(behandling) {
        var mb = this.medicinskBehandling;
		
		return sendTextToForm(mb.avslutad.text, behandling.avslutad)
		.then(function(){
			return sendTextToForm(mb.pagaende.text, behandling.pagaende);
		})
		.then(function(){
			return sendTextToForm(mb.planerad.text, behandling.planerad);
		})
		.then(function(){
			return sendTextToForm(mb.substansintag.text, behandling.substansintag);
		});
    },
    getTillaggsfraga: function(i) {
        return element(by.id('tillaggsfragor[' + i + '].svar'));
    },
    getTillaggsfragaUE: function(i) {
        return element(by.id('tillaggsfragor-' + i + '--svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + ' label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + ' textarea')).getAttribute('value');
    }
});

module.exports = BaseSmiUtkast;
