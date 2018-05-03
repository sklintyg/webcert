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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element, by ,browser, protractor, Promise, logger */
'use strict';

var BaseUtkast = require('../base.utkast.page.js');
var pageHelpers = require('../../pageHelper.util.js');

protractor.ElementFinder.prototype.check = function() {
    var checkbox = this;
    return checkbox.isSelected().then(function(selected) {
        if (!selected) {
            return pageHelpers.moveAndSendKeys(checkbox, protractor.Key.SPACE, 'checkbox is now checked');
        }
    });
};

protractor.ElementFinder.prototype.uncheck = function() {
    var checkbox = this;
    return checkbox.isSelected().then(function(selected) {
        if (selected) {
            return pageHelpers.moveAndSendKeys(checkbox, protractor.Key.SPACE, 'checkbox is now uncheck');
        }
    });
};


var FkBaseUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);


        this.enhetensAdress = {
            postAdress: element(by.id('clinicInfoPostalAddress')),
            postNummer: element(by.id('clinicInfoPostalCode')),
            postOrt: element(by.id('clinicInfoPostalCity')),
            enhetsTelefon: element(by.id('clinicInfoPhone'))
        };
        this.at = element(by.css('#view-fk7263'));
        this.smittskyddLabel = element(by.css('[key="fk7263.label.smittskydd"]'));
        this.smittskyddCheckbox = element(by.id('smittskydd'));

        this.nedsattMed25Checkbox = element(by.id('nedsattMed25'));

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
                ja: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('value').then(v => v === 'JA')).first(),
                nej: () => element.all(by.css('input[type=radio]')).filter(el => el.getAttribute('value').then(v => v === 'NEJ')).first()
            },
            visamer: () => element.all(by.id('questionsCollapser')),
            visaKnapp: () => element(by.buttonText('Visa')),
            fragor: () => element(by.tagName('wc-srs-questionaire')),
            prediktion: () => element(by.id('predictionBox')),
            flik: linkText => element(by.linkText(linkText)),
            atgarder: () => element(by.id('atgarder2')), //SRS rutan vid diagnos antas
            statistik: () => element(by.id('statstics2')), //SRS rutan vid diagnos antas
            atgarderRek: () => element(by.id('atgarderRek')),
            atgarderObs: () => element(by.id('atgarderObs')),
            questionsCollapser: () => element(by.id('questionsCollapser')),

        };
    },

    setSRSConsent: function(isConsent) {
        if (isConsent) {
            return pageHelpers.moveAndSendKeys(this.srs.samtycke.ja(), protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.srs.samtycke.nej(), protractor.Key.SPACE);
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
        return this.smittskyddCheckbox.check();
    },
    nedsattMed25CheckboxClick: function() {
        return this.nedsattMed25Checkbox.check();
    },
    travelRadioButtonJaClick: function() {
        return this.travelRadioButtonJa.check();
    },
    getCheckedTravelRadioButtonValue: function() {
        return this.travelRadioGroupChecked.getAttribute('value');
    },
    getCapacityForWorkForecastText: function() {
        return this.capacityForWorkForecastText;
    },
    minUndersokningAvPatClick: function() {
        return this.baserasPa.minUndersokning.check();
    },
    angeDiagnosKod: function(kod, diagnosKodElement) {
        var element = !diagnosKodElement ? this.diagnosKod : diagnosKodElement;

        return element.clear()
            .then(function() {
                return pageHelpers.moveAndSendKeys(element, kod);
            })
            .then(function() {
                return browser.sleep(200);
            }).then(function() {
                return pageHelpers.moveAndSendKeys(element, protractor.Key.ENTER);
            });

    },
    angeDiagnosFortydligande: function(txt) {
        var elm = this.diagnos.fortydligande;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });
    },
    angeFunktionsnedsattning: function(txt) {
        if (!txt) {
            return Promise.resolve('Success');
        }
        var elm = this.funktionsNedsattning;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });

    },
    angeAktivitetsBegransning: function(txt) {
        if (!txt) {
            return Promise.resolve('Success');
        }
        var elm = this.aktivitetsBegransning;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });
    },
    angeNuvarandeArbete: function(txt) {
        var elm = this.nuvarandeArbete;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });
    },
    angeFaktiskTjanstgoring: function(txt) {
        var elm = this.faktiskTjanstgoring;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });
    },
    angeOvrigaUpplysningar: function(txt) {
        var elm = this.otherInformation;
        return elm.clear().then(function() {
            return pageHelpers.moveAndSendKeys(elm, txt);
        });
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
        } else {
            var baserasPa = this.baserasPa;
            return new Promise(function(resolve) {
                resolve('anger BaseratPa');
            }).then(function() {
                if (intygetBaserasPa.minUndersokning) {
                    return baserasPa.minUndersokning.datum.clear().then(function() {
                        return pageHelpers.moveAndSendKeys(baserasPa.minUndersokning.datum, intygetBaserasPa.minUndersokning.datum);
                    });
                } else {
                    return;
                }
            }).then(function() {
                if (intygetBaserasPa.minTelefonkontakt) {
                    return baserasPa.minTelefonkontakt.datum.clear().then(function() {
                        return pageHelpers.moveAndSendKeys(baserasPa.minTelefonkontakt.datum, intygetBaserasPa.minTelefonkontakt.datum);
                    });
                } else {
                    return;
                }
            }).then(function() {
                if (intygetBaserasPa.journaluppgifter) {
                    return baserasPa.journaluppgifter.datum.clear().then(function() {
                        return pageHelpers.moveAndSendKeys(baserasPa.journaluppgifter.datum, intygetBaserasPa.journaluppgifter.datum);
                    });
                } else {
                    return;
                }
            }).then(function() {
                if (intygetBaserasPa.annat) {
                    return baserasPa.annat.datum.clear().then(function() {
                        return pageHelpers.moveAndSendKeys(baserasPa.annat.datum, intygetBaserasPa.annat.datum);
                    }).then(function() {
                        return baserasPa.annat.text.clear();
                    }).then(function() {
                        return pageHelpers.moveAndSendKeys(baserasPa.annat.text, intygetBaserasPa.annat.text);
                    });
                } else {
                    return;
                }
            });
        }
    },
    angeDiagnoser: function(diagnos) {
        var angeDiagnosKod = this.angeDiagnosKod;
        var diagnosKodElm = this.diagnosKod;
        var diagnosElm = this.diagnos;

        return new Promise(function(resolve) {
            resolve('anger Diagnoser');
        }).then(function() {
            if (diagnos.diagnoser && diagnos.diagnoser[0].ICD10) {
                return angeDiagnosKod(diagnos.diagnoser[0].ICD10, diagnosKodElm);
            } else {
                return;
            }
        }).then(function() {
            if (diagnos.fortydligande) {
                return diagnosElm.fortydligande.clear().then(function() {
                    return pageHelpers.moveAndSendKeys(diagnosElm.fortydligande, diagnos.fortydligande);
                });
            } else {
                return;
            }
        }).then(function() {
            if (diagnos.samsjuklighetForeligger) {
                return diagnosElm.samsjuklighetForeligger.check();
            } else {
                return;
            }
        });
    },
    angeArbetsformaga: function(arbetsformaga) {
        var nedsatt = this.nedsatt;

        return new Promise(function(resolve) {
            resolve('anger Arbetsformaga');
        }).then(function() {
            var promisesArr = [
                nedsatt.med25.from.clear(),
                nedsatt.med25.tom.clear(),
                nedsatt.med50.from.clear(),
                nedsatt.med50.tom.clear(),
                nedsatt.med75.from.clear(),
                nedsatt.med75.tom.clear(),
                nedsatt.med100.from.clear(),
                nedsatt.med100.tom.clear()
            ];
            return Promise.all(promisesArr);
        }).then(function() {
            if (arbetsformaga.nedsattMed25) {
                return pageHelpers.moveAndSendKeys(nedsatt.med25.from, arbetsformaga.nedsattMed25.from).then(function() {
                    return pageHelpers.moveAndSendKeys(nedsatt.med25.tom, arbetsformaga.nedsattMed25.tom);
                });
            } else {
                return;
            }
        }).then(function() {
            if (arbetsformaga.nedsattMed50) {
                return pageHelpers.moveAndSendKeys(nedsatt.med50.from, arbetsformaga.nedsattMed50.from).then(function() {
                    return pageHelpers.moveAndSendKeys(nedsatt.med50.tom, arbetsformaga.nedsattMed50.tom);
                });
            } else {
                return;
            }
        }).then(function() {
            if (arbetsformaga.nedsattMed75) {
                return pageHelpers.moveAndSendKeys(nedsatt.med75.from, arbetsformaga.nedsattMed75.from).then(function() {
                    return pageHelpers.moveAndSendKeys(nedsatt.med75.tom, arbetsformaga.nedsattMed75.tom);
                });
            } else {
                return;
            }
        }).then(function() {
            if (arbetsformaga.nedsattMed100) {
                return pageHelpers.moveAndSendKeys(nedsatt.med100.from, arbetsformaga.nedsattMed100.from).then(function() {
                    return pageHelpers.moveAndSendKeys(nedsatt.med100.tom, arbetsformaga.nedsattMed100.tom);
                });
            }
        });
    },
    angeAktuelltSjukdomsForlopp: function(txt) {
        if (txt) {
            var aktuelltSjukdomsForlopp = this.aktuelltSjukdomsForlopp;
            return aktuelltSjukdomsForlopp.clear().then(function() {
                return pageHelpers.moveAndSendKeys(aktuelltSjukdomsForlopp, txt);
            });
        } else {
            return Promise.resolve('Success');
        }
    },
    angeArbetsformagaFMB: function(txt) {
        var arbetsformagaFMB = this.arbetsformagaFMB;
        return arbetsformagaFMB.clear().then(function() {
            return pageHelpers.moveAndSendKeys(arbetsformagaFMB, txt);
        });
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
                    return prognosFortydligande.clear()
                        .then(function() {
                            return pageHelpers.smallDelay();
                        })
                        .then(function() {
                            return pageHelpers.moveAndSendKeys(prognosFortydligande, prognos.fortydligande);
                        });
                } else {
                    return Promise.resolve('Inget förtydligande');
                }
            });
        }
    },
    angeArbete: function(arbete) {
        var arbeteElemenet = this.arbete;
        var nuvarandeArbeteTextElement = this.arbete.nuvarandeArbete.text;

        return new Promise(function(resolve) {
            resolve('anger Arbete');
        }).then(function() {
            if (arbete.nuvarandeArbete) {
                return arbeteElemenet.nuvarandeArbete.checkbox.check().then(function() {
                    if (arbete.nuvarandeArbete.aktuellaArbetsuppgifter) {
                        return nuvarandeArbeteTextElement.clear().then(function() {
                            return pageHelpers.moveAndSendKeys(nuvarandeArbeteTextElement, arbete.nuvarandeArbete.aktuellaArbetsuppgifter);
                        });
                    } else {
                        return Promise.resolve('Success');
                    }
                });
            } else {
                return Promise.resolve('Success');
            }
        }).then(function() {
            if (arbete.arbetsloshet) {
                return arbeteElemenet.arbetslos.checkbox.check();
            } else {
                return Promise.resolve('Success');
            }
        }).then(function() {
            if (arbete.foraldraledighet) {
                return arbeteElemenet.foraldraledig.checkbox.check();
            } else {
                return Promise.resolve('Success');
            }
        });
    },
    angeAtgarder: function(atgarder) {
        var atgarderElement = this.atgarder;

        return atgarderElement.measuresCurrent.clear().then(function() {
            return pageHelpers.moveAndSendKeys(atgarderElement.measuresCurrent, atgarder.planerad);
        }).then(function() {
            return atgarderElement.measuresOther.clear();
        }).then(function() {
            return pageHelpers.moveAndSendKeys(atgarderElement.measuresOther, atgarder.annan);
        });
    },
    angeKontaktOnskasMedFK: function(kontaktOnskas) {
        if (kontaktOnskas) {
            return this.kontaktFk.check();
        } else {
            return this.kontaktFk.uncheck();
        }
    },
    angeRekommendationer: function(rekommendationer) {

        var rekommendationerElement = this.rekommendationer;
        var travelRadioButtonJa = this.travelRadioButtonJa;
        var travelRadioButtonNej = this.travelRadioButtonNej;

        return new Promise(function(resolve) {
            resolve('anger Arbete');
        }).then(function() {
            if (rekommendationer.resor) {
                return travelRadioButtonJa.check();
            } else {
                return travelRadioButtonNej.check();
            }
        }).then(function() {
            if (rekommendationer.kontaktMedArbetsformedlingen) {
                return rekommendationerElement.kontaktAf.check();
            } else {
                return;
            }
        }).then(function() {
            if (rekommendationer.kontaktMedForetagshalsovard) {
                return rekommendationerElement.kontaktFH.check();
            } else {
                return;
            }
        }).then(function() {
            if (rekommendationer.ovrigt) {
                return rekommendationerElement.ovrigt.checkbox.check().then(function() {
                    return rekommendationerElement.ovrigt.beskrivning.clear();
                }).then(function() {
                    return pageHelpers.moveAndSendKeys(rekommendationerElement.ovrigt.beskrivning, rekommendationer.ovrigt);
                });
            }
        }).then(function() {
            if (rekommendationer.arbetslivsinriktadRehab) {
                if (rekommendationer.arbetslivsinriktadRehab === 'Ja') {
                    return rekommendationerElement.rehab.JA.check();
                } else if (rekommendationer.arbetslivsinriktadRehab === 'Nej') {
                    return rekommendationerElement.rehab.NEJ.check();
                } else if (rekommendationer.arbetslivsinriktadRehab === 'Går inte att bedöma') {
                    return rekommendationerElement.rehab.GAR_EJ_ATT_BEDOMA.check();
                }
            }
        });
    },
    /*TODO getQAElementByText depricated ?*/
    getQAElementByText: function(containingText) {
        var panel = element(by.cssContainingText('.qa-panel', containingText));
        return {
            panel: panel,
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };
    },

});

module.exports = new FkBaseUtkast();
