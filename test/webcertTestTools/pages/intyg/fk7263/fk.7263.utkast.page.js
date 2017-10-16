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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,browser, protractor, Promise */
'use strict';

var BaseUtkast = require('../base.utkast.page.js');

protractor.ElementFinder.prototype.check = function() {
    var checkbox = this;
    return checkbox.isSelected().then(function(selected) {
        if (!selected) {
            return checkbox.sendKeys(protractor.Key.SPACE);
        }
    });
};

protractor.ElementFinder.prototype.uncheck = function() {
    var checkbox = this;
    return checkbox.isSelected().then(function(selected) {
        if (selected) {
            return checkbox.sendKeys(protractor.Key.SPACE);
        }
    });
};


var FkUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));
        this.smittskyddLabel = element(by.css('[key="fk7263.label.smittskydd"]'));
        this.smittskyddCheckbox = element(by.id('smittskydd'));

        this.nedsattMed25Checkbox = element(by.id('nedsattMed25'));

        this.signeraButton = element(by.id('signera-utkast-button'));
        this.fetchPatientButton = element(by.id('fetchPatientButton'));

        this.travelRadioButtonJa = element(by.id('rekommendationRessatt'));
        this.travelRadioButtonNej = element(by.id('rekommendationRessattEj'));
        this.travelRadioGroupChecked = element(by.css('input[name="recommendationsToFkTravel"]:checked'));

        this.capacityForWorkForecastText = element(by.id('capacityForWorkForecastText'));
        this.diagnosKod = element(by.id('diagnoseCode'));
        this.diagnosKod2 = element(by.id('diagnoseCodeOpt1'));
        this.diagnosKod3 = element(by.id('diagnoseCodeOpt2'));
        this.diagnosBeskrivning = element(by.id('diagnoseDescription'));
        this.diagnosBeskrivning2 = element(by.id('diagnoseDescriptionOpt1'));
        this.diagnosBeskrivning3 = element(by.id('diagnoseDescriptionOpt2'));
        this.funktionsNedsattning = element(by.id('disabilities'));
        this.aktivitetsBegransning = element(by.id('activityLimitation'));
        this.nuvarandeArbete = element(by.id('currentWork'));
        this.faktiskTjanstgoring = element(by.id('capacityForWorkActualWorkingHoursPerWeek'));
        this.aktuelltSjukdomsForlopp = element(by.id('diseaseCause'));
        this.arbetsformagaFMB = element(by.id('capacityForWorkText'));
        this.otherInformation = element(by.id('otherInformation'));

        this.prognos = {
            JA: element(by.id('capacityForWork1')),
            JA_DELVIS: element(by.id('capacityForWork2')),
            NEJ: element(by.id('capacityForWork3')),
            GAR_EJ_ATT_BEDOMA: element(by.id('capacityForWork4')),
            fortydligande: element(by.id('capacityForWorkForecastText'))
        };
        this.rekommendationer = {
            kontaktAf: element(by.id('rekommendationKontaktAf')),
            kontaktFH: element(by.id('rekommendationKontaktForetagshalsovard')),
            ovrigt: {
                checkbox: element(by.id('rekommendationOvrigt')),
                beskrivning: element(by.id('rekommendationOvrigtBeskrivning'))
            },
            rehab: {
                JA: element(by.id('rehabYes')),
                NEJ: element(by.id('rehabNo')),
                GAR_EJ_ATT_BEDOMA: element(by.id('garej'))
            }
        };
        this.arbete = {
            nuvarandeArbete: {
                checkbox: element(by.id('arbeteNuvarande')),
                text: element(by.id('currentWork'))
            },
            arbetslos: {
                checkbox: element(by.id('arbeteArbetslos'))
            },
            foraldraledig: {
                checkbox: element(by.id('arbeteForaldraledig'))
            }
        };

        this.nedsatt = {
            lastEffectiveDateNoticeText: element(by.id('lastEffectiveDateNoticeText')),
            med25: {
                checkbox: element(by.id('nedsattMed25')),
                from: element(by.id('nedsattMed25from')),
                tom: element(by.id('nedsattMed25tom')),
            },
            med50: {
                checkbox: element(by.id('nedsattMed50')),
                from: element(by.id('nedsattMed50from')),
                tom: element(by.id('nedsattMed50tom')),
            },
            med75: {
                checkbox: element(by.id('nedsattMed75')),
                from: element(by.id('nedsattMed75from')),
                tom: element(by.id('nedsattMed75tom')),
            },
            med100: {
                checkbox: element(by.id('nedsattMed100')),
                from: element(by.id('nedsattMed100from')),
                tom: element(by.id('nedsattMed100tom')),
            }
        };
        this.baserasPa = {
            minUndersokning: {
                checkbox: element(by.id('basedOnExamination')),
                datum: element(by.id('undersokningAvPatientenDate'))
            },
            minTelefonkontakt: {
                checkbox: element(by.id('basedOnPhoneContact')),
                datum: element(by.id('telefonkontaktMedPatientenDate'))
            },
            journaluppgifter: {
                checkbox: element(by.id('basedOnJournal')),
                datum: element(by.id('journaluppgifterDate'))
            },
            annat: {
                checkbox: element(by.id('basedOnOther')),
                datum: element(by.id('annanReferensDate')),
                text: element(by.id('informationBasedOnOtherText'))
            }
        };

        this.atgarder = {
            measuresCurrent: element(by.id('measuresCurrent')),
            measuresOther: element(by.id('measuresOther'))
        };

        this.diagnos = {
            beskrivning: element(by.id('diagnoseDescription')),
            fortydligande: element(by.id('diagnoseClarification')),
            samsjuklighetForeligger: element(by.id('diagnoseMultipleDiagnoses'))
        };
        this.kontaktFk = element(by.id('kontaktFk'));
        this.srs = {
            knapp: () => element(by.buttonText('SRS')),
            panel: () => element(by.tagName('wc-srs-content')),
            samtycke: {
                ja: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('ng-change').then(v => v === 'setConsent(true)')).first(),
                nej: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('ng-change').then(v => v === 'setConsent(false)')).first()
            },
            visamer: () => element.all(by.id('questionsCollapser')),
            visaKnapp: () => element(by.buttonText('Visa')),
            fragor: () => element(by.tagName('wc-srs-questionaire')),
            prediktion: () => element(by.id('predictionBox')),
            flik: linkText => element(by.linkText(linkText)),
            atgarder: () => element(by.id('atgarder')),
            statistik: () => element(by.id('statstics')),
            atgarderRek: () => element(by.id('atgarderRek')),
            atgarderObs: () => element(by.id('atgarderObs')),
            questionsCollapser: () => element(by.id('questionsCollapser')),

        }
    },

    setSRSConsent: function(isConsent) {
        if (isConsent) {
            this.srs.samtycke.ja().click()
        } else {
            this.srs.samtycke.nej().click()
        }
    },
    getSRSQuestionnaireStatus: function() {
        return this.srs.fragor().isDisplayed()
            .then(displayed => displayed ? Promise.resolve('maximerad') : Promise.resolve('minimerad'));
    },

    getSRSButtonStatus: function() {
        return Promise.all([
            this.srs.knapp().isDisplayed(),
            this.srs.knapp().element(by.className('glyphicon-plus-sign')).isPresent(),
            this.srs.knapp().element(by.className('glyphicon-minus-sign')).isPresent()
        ]).then(results => {
            const [displayed, closed, open] = results;
            if (!displayed) {
                return Promise.resolve('gömd');
            } else if (closed) {
                return Promise.resolve('stängd');
            } else if (open) {
                return Promise.resolve('öppen');
            } else {
                return Promise.reject('okänd');
            }
        });
    },

    get: function get(intygId) {
        get._super.call(this, 'fk7263', intygId);
    },
    whenSmittskyddIsDisplayed: function() {
        return browser.wait(this.smittskyddLabel.isDisplayed());
    },
    getSmittskyddLabelText: function() {
        return this.smittskyddLabel.getText();
    },
    smittskyddCheckboxClick: function() {
        this.smittskyddCheckbox.check();
    },
    nedsattMed25CheckboxClick: function() {
        return this.nedsattMed25Checkbox.check();
    },
    travelRadioButtonJaClick: function() {
        this.travelRadioButtonJa.check();
    },
    getCheckedTravelRadioButtonValue: function() {
        return this.travelRadioGroupChecked.getAttribute('value');
    },
    getCapacityForWorkForecastText: function() {
        return this.capacityForWorkForecastText;
    },
    minUndersokningAvPatClick: function() {
        this.baserasPa.minUndersokning.check();
    },
    angeDiagnosKod: function(kod, diagnosKodElement) {
        var element = !diagnosKodElement ? this.diagnosKod : diagnosKodElement;

        function sendEnterToElement(el) {
            return function() {
                return el.sendKeys(protractor.Key.ENTER);
            };
        }
        return element.clear().sendKeys(kod).then(function() {
            return browser.sleep(2000);
        }).then(sendEnterToElement(element));

    },
    angeDiagnosFortydligande: function(txt) {
        return this.diagnos.fortydligande.clear().sendKeys(txt);
    },
    angeFunktionsnedsattning: function(txt) {
        if (!txt) {
            return Promise.resolve('Success');
        }
        return this.funktionsNedsattning.clear().sendKeys(txt);
    },
    angeAktivitetsBegransning: function(txt) {
        if (txt) {
            return this.aktivitetsBegransning.clear().sendKeys(txt);
        } else {
            return Promise.resolve('Success');
        }
    },
    angeNuvarandeArbete: function(txt) {
        return this.nuvarandeArbete.clear().sendKeys(txt);
    },
    angeFaktiskTjanstgoring: function(txt) {
        return this.faktiskTjanstgoring.clear().sendKeys(txt);
    },
    angeOvrigaUpplysningar: function(txt) {
        return this.otherInformation.clear().sendKeys(txt);
    },
    angeSmittskydd: function(isSmittskydd) {
        if (isSmittskydd) {
            return this.smittskyddCheckbox.check();
        } else {
            return Promise.resolve('Success');
        }
    },
    angeIntygetBaserasPa: function(intygetBaserasPa) {
        if (!intygetBaserasPa) {
            return Promise.resolve('Success');
        }

        var promisesArr = [];
        if (intygetBaserasPa.minUndersokning) {
            // this.baserasPa.minUndersokning.checkbox.check();
            promisesArr.push(this.baserasPa.minUndersokning.datum.clear().sendKeys(intygetBaserasPa.minUndersokning.datum));
        }
        if (intygetBaserasPa.minTelefonkontakt) {
            // this.baserasPa.minTelefonkontakt.checkbox.check();
            promisesArr.push(this.baserasPa.minTelefonkontakt.datum.clear().sendKeys(intygetBaserasPa.minTelefonkontakt.datum));
        }
        if (intygetBaserasPa.journaluppgifter) {
            // this.baserasPa.journaluppgifter.checkbox.check();
            promisesArr.push(this.baserasPa.journaluppgifter.datum.clear().sendKeys(intygetBaserasPa.journaluppgifter.datum));
        }
        if (intygetBaserasPa.annat) {
            // this.baserasPa.annat.checkbox.check();
            promisesArr.push(this.baserasPa.annat.datum.clear().sendKeys(intygetBaserasPa.annat.datum));
            promisesArr.push(this.baserasPa.annat.text.clear().sendKeys(intygetBaserasPa.annat.text));
        }
        return Promise.all(promisesArr);
    },
    angeDiagnoser: function(diagnos) {
        var promisesArr = [];

        if (diagnos.diagnoser && diagnos.diagnoser[0].ICD10) {
            promisesArr.push(this.angeDiagnosKod(diagnos.diagnoser[0].ICD10));
        }

        if (diagnos.fortydligande) {
            promisesArr.push(this.diagnos.fortydligande.clear().sendKeys(diagnos.fortydligande));
        }
        if (diagnos.samsjuklighetForeligger) {
            promisesArr.push(this.diagnos.samsjuklighetForeligger.check());
        }

        return Promise.all(promisesArr);
    },
    angeArbetsformaga: function(arbetsformaga) {

        var promisesArr = [
            this.nedsatt.med25.from.clear(),
            this.nedsatt.med25.tom.clear(),
            this.nedsatt.med50.from.clear(),
            this.nedsatt.med50.tom.clear(),
            this.nedsatt.med75.from.clear(),
            this.nedsatt.med75.tom.clear(),
            this.nedsatt.med100.from.clear(),
            this.nedsatt.med100.tom.clear()
        ];
        if (arbetsformaga.nedsattMed25) {
            // this.nedsatt.med25.checkbox.click();
            promisesArr.push(this.nedsatt.med25.from.sendKeys(arbetsformaga.nedsattMed25.from));
            promisesArr.push(this.nedsatt.med25.tom.sendKeys(arbetsformaga.nedsattMed25.tom));
        }
        if (arbetsformaga.nedsattMed50) {
            // this.nedsatt.med50.checkbox.click();
            promisesArr.push(this.nedsatt.med50.from.sendKeys(arbetsformaga.nedsattMed50.from));
            promisesArr.push(this.nedsatt.med50.tom.sendKeys(arbetsformaga.nedsattMed50.tom));
        }
        if (arbetsformaga.nedsattMed75) {
            // this.nedsatt.med75.checkbox.click();
            promisesArr.push(this.nedsatt.med75.from.sendKeys(arbetsformaga.nedsattMed75.from));
            promisesArr.push(this.nedsatt.med75.tom.sendKeys(arbetsformaga.nedsattMed75.tom));
        }
        if (arbetsformaga.nedsattMed100) {
            // this.nedsatt.med100.checkbox.click();
            promisesArr.push(this.nedsatt.med100.from.sendKeys(arbetsformaga.nedsattMed100.from));
            promisesArr.push(this.nedsatt.med100.tom.sendKeys(arbetsformaga.nedsattMed100.tom));
        }
        return Promise.all(promisesArr);
    },
    angeAktuelltSjukdomsForlopp: function(txt) {
        if (txt) {
            return this.aktuelltSjukdomsForlopp.clear().sendKeys(txt);
        } else {
            return Promise.resolve('Success');
        }
    },
    angeArbetsformagaFMB: function(txt) {
        return this.arbetsformagaFMB.clear().sendKeys(txt);
    },
    angePrognos: function(prognos) {
        logger.debug(prognos);
        var prognosFortydligande = this.prognos.fortydligande;

        if (prognos.val === 'Ja') {
            return this.prognos.JA.check();
        } else if (prognos.val === 'Ja, delvis') {
            return this.prognos.JA_DELVIS.check();
        } else if (prognos.val === 'Nej') {
            return this.prognos.NEJ.check();
        } else if (prognos.val === 'Går inte att bedöma') {
            return this.prognos.GAR_EJ_ATT_BEDOMA.check().then(function() {
                if (prognos.fortydligande) {
                    return browser.sleep(1500) // Vänta på animering
                        .then(function() {
                            return prognosFortydligande.clear().sendKeys(prognos.fortydligande);
                        });
                } else {
                    return Promise.resolve('Inget förtydligande');
                }
            });
        }
    },
    angeArbete: function(arbete) {
        var arbeteCheckbox = this.arbete.nuvarandeArbete.checkbox;
        var nuvarandeArbeteTextElement = this.arbete.nuvarandeArbete.text;

        function checkArbeteCheckbox() {

            return arbeteCheckbox.check();

        }

        var promisesArr = [];
        if (arbete.nuvarandeArbete) {
            promisesArr.push(checkArbeteCheckbox().then(function() {
                if (arbete.nuvarandeArbete.aktuellaArbetsuppgifter) {
                    return nuvarandeArbeteTextElement.clear().sendKeys(arbete.nuvarandeArbete.aktuellaArbetsuppgifter);
                } else {
                    return Promise.resolve('Success');
                }
            }));
        }
        if (arbete.arbetsloshet) {
            promisesArr.push(this.arbete.arbetslos.checkbox.check());
        }
        if (arbete.foraldraledighet) {
            promisesArr.push(this.arbete.foraldraledig.checkbox.check());
        }

        return Promise.all(promisesArr);
    },
    angeAtgarder: function(atgarder) {

        this.atgarder.measuresCurrent.clear().sendKeys(atgarder.planerad);
        this.atgarder.measuresOther.clear().sendKeys(atgarder.annan);
    },
    angeKontaktOnskasMedFK: function(kontaktOnskas) {
        if (kontaktOnskas) {
            return this.kontaktFk.check();
        } else {
            return this.kontaktFk.uncheck();
        }
    },
    angeRekommendationer: function(rekommendationer) {
        var promisesArr = [];
        if (rekommendationer.resor) {
            promisesArr.push(this.travelRadioButtonJa.check());
        } else {
            promisesArr.push(this.travelRadioButtonNej.check());
        }
        if (rekommendationer.kontaktMedArbetsformedlingen) {
            promisesArr.push(this.rekommendationer.kontaktAf.check());
        }
        if (rekommendationer.kontaktMedForetagshalsovard) {
            promisesArr.push(this.rekommendationer.kontaktFH.check());
        }
        if (rekommendationer.ovrigt) {
            promisesArr.push(this.rekommendationer.ovrigt.checkbox.check());
            promisesArr.push(this.rekommendationer.ovrigt.beskrivning.clear().sendKeys(rekommendationer.ovrigt));
        }

        if (rekommendationer.arbetslivsinriktadRehab) {
            if (rekommendationer.arbetslivsinriktadRehab === 'Ja') {
                promisesArr.push(this.rekommendationer.rehab.JA.check());
            } else if (rekommendationer.arbetslivsinriktadRehab === 'Nej') {
                promisesArr.push(this.rekommendationer.rehab.NEJ.check());
            } else if (rekommendationer.arbetslivsinriktadRehab === 'Går inte att bedöma') {
                promisesArr.push(this.rekommendationer.rehab.GAR_EJ_ATT_BEDOMA.check());
            }
        }
        return Promise.all(promisesArr);
    },
    getQAElementByText: function(containingText) {
        var panel = element(by.cssContainingText('.qa-panel', containingText));
        return {
            panel: panel,
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };
    },

});

module.exports = new FkUtkast();
