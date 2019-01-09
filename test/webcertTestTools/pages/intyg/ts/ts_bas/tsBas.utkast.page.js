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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,protractor, Promise,browser*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');

var TsBasUtkast = BaseTsUtkast._extend({
    init: function init() {

        init._super.call(this);
        this.intygType = 'ts-bas';
        this.at = element(by.id('edit-ts-bas'));
        this.syn = {
            aYes: element(by.id('syn-synfaltsdefekterYes')),
            aNo: element(by.id('syn-synfaltsdefekterNo')),
            bYes: element(by.id('syn-nattblindhetYes')),
            bNo: element(by.id('syn-nattblindhetNo')),
            cYes: element(by.id('syn-progressivOgonsjukdomYes')),
            cNo: element(by.id('syn-progressivOgonsjukdomNo')),
            dYes: element(by.id('syn-diplopiYes')),
            dNo: element(by.id('syn-diplopiNo')),
            eYes: element(by.id('syn-nystagmusYes')),
            eNo: element(by.id('syn-nystagmusNo')),
            hoger: {
                utan: element(by.id('syn-hogerOga-utanKorrektion')),
                med: element(by.id('syn-hogerOga-medKorrektion'))
            },
            vanster: {
                utan: element(by.id('syn-vansterOga-utanKorrektion')),
                med: element(by.id('syn-vansterOga-medKorrektion'))
            },
            binokulart: {
                utan: element(by.id('syn-binokulart-utanKorrektion')),
                med: element(by.id('syn-binokulart-medKorrektion'))
            },
            // utanKorrektion: {
            //     hoger: element(by.id('synHogerOgaUtanKorrektion')),
            //     vanster: element(by.id('synVansterOgaUtanKorrektion')),
            //     binokulart: element(by.id('synBinokulartUtanKorrektion'))
            // },
            // medKorrektion: {
            //     hoger: element(by.id('synHogerOgaMedKorrektion')),
            //     vanster: element(by.id('synVansterOgaMedKorrektion')),
            //     binokulart: element(by.id('synBinokulartMedKorrektion'))
            // },
            kontaktlins: {
                hoger: element(by.id('syn-hogerOga-kontaktlins')),
                vanster: element(by.id('syn-vansterOga-kontaktlins'))
            }
        };

        this.horselBalans = {
            aYes: element(by.id('horselBalans-balansrubbningarYes')),
            aNo: element(by.id('horselBalans-balansrubbningarNo')),
            bYes: element(by.id('horselBalans-svartUppfattaSamtal4MeterYes')),
            bNo: element(by.id('horselBalans-svartUppfattaSamtal4MeterNo'))
        };
        this.funktionsnedsattning = {
            aYes: element(by.id('funktionsnedsattning-funktionsnedsattningYes')),
            aNo: element(by.id('funktionsnedsattning-funktionsnedsattningNo')),
            aText: element(by.id('funktionsnedsattning-beskrivning')),
            bYes: element(by.id('funktionsnedsattning-otillrackligRorelseformagaYes')),
            bNo: element(by.id('funktionsnedsattning-otillrackligRorelseformagaNo'))
        };
        this.hjartKarl = {
            aYes: element(by.id('hjartKarl-hjartKarlSjukdomYes')),
            aNo: element(by.id('hjartKarl-hjartKarlSjukdomNo')),
            bYes: element(by.id('hjartKarl-hjarnskadaEfterTraumaYes')),
            bNo: element(by.id('hjartKarl-hjarnskadaEfterTraumaNo')),
            cYes: element(by.id('hjartKarl-riskfaktorerStrokeYes')),
            cNo: element(by.id('hjartKarl-riskfaktorerStrokeNo')),
            cText: element(by.id('hjartKarl-beskrivningRiskfaktorer'))
        };
        this.diabetes = {
            aYes: element(by.id('diabetes-harDiabetesYes')),
            aNo: element(by.id('diabetes-harDiabetesNo')),
            typ1: element(by.id('diabetes.diabetesTyp-DIABETES_TYP_1')),
            typ2: element(by.id('diabetes.diabetesTyp-DIABETES_TYP_2')),
            endastkost: element(by.id('diabetes-kost')),
            tabletter: element(by.id('diabetes-tabletter')),
            insulin: element(by.id('diabetes-insulin'))
        };
        this.neurologiska = {
            aYes: element(by.id('neurologi-neurologiskSjukdomYes')),
            aNo: element(by.id('neurologi-neurologiskSjukdomNo'))
        };
        this.epilepsi = {
            aYes: element(by.id('medvetandestorning-medvetandestorningYes')),
            aText: element(by.id('medvetandestorning-beskrivning')),
            aNo: element(by.id('medvetandestorning-medvetandestorningNo'))
        };
        this.njursjukdom = {
            aYes: element(by.id('njurar-nedsattNjurfunktionYes')),
            aNo: element(by.id('njurar-nedsattNjurfunktionNo'))
        };
        this.kognitivt = {
            aYes: element(by.id('kognitivt-sviktandeKognitivFunktionYes')),
            aNo: element(by.id('kognitivt-sviktandeKognitivFunktionNo'))
        };
        this.somnOchVakenhetsStorningar = {
            JA: element(by.id('somnVakenhet-teckenSomnstorningarYes')),
            NEJ: element(by.id('somnVakenhet-teckenSomnstorningarNo'))
        };
    },
    fillInSynfunktioner: function(utkast) {
        var promiseArr = [];
        if (utkast.synDonder === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.aYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.aNo, protractor.Key.SPACE));
        }
        if (utkast.synNedsattBelysning === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.bYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.bNo, protractor.Key.SPACE));
        }
        if (utkast.synOgonsjukdom === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.cYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.cNo, protractor.Key.SPACE));
        }
        if (utkast.synDubbel === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.dYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.dNo, protractor.Key.SPACE));
        }
        if (utkast.synNystagmus === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.eYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.eNo, protractor.Key.SPACE));
        }
        if (utkast.linser.hoger === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.kontaktlins.hoger, protractor.Key.SPACE));

        }
        if (utkast.linser.vanster === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.kontaktlins.vanster, protractor.Key.SPACE));
        }

        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.hoger.utan, utkast.styrkor.houk));
        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.vanster.utan, utkast.styrkor.vouk));
        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.binokulart.utan, utkast.styrkor.buk));

        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.hoger.med, utkast.styrkor.homk));
        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.vanster.med, utkast.styrkor.vomk));
        promiseArr.push(pageHelpers.moveAndSendKeys(this.syn.binokulart.med, utkast.styrkor.bmk));

        return Promise.all(promiseArr);
    },
    fillInYrsel: function(yrsel) {
        if (yrsel === 'Ja') {
            return pageHelpers.moveAndSendKeys(this.horselBalans.aYes, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.horselBalans.aNo, protractor.Key.SPACE);
        }
    },
    fillInHorselOchBalanssinne: function(horselObj) {
        var horselBalansEl = this.horselBalans;
        return this.fillInYrsel(horselObj.yrsel)
            .then(function() {
                if (horselObj.samtal === 'Ja') {
                    return pageHelpers.moveAndSendKeys(horselBalansEl.bYes, protractor.Key.SPACE);
                } else if (horselObj.samtal === 'Nej') {
                    return pageHelpers.moveAndSendKeys(horselBalansEl.bNo, protractor.Key.SPACE);
                }
            });
    },

    fillInRorelseNedsattning: function(nedsattning, beskrivning) {
        var nedsattningEl = this.funktionsnedsattning;

        if (nedsattning === 'Ja') {
            return pageHelpers.moveAndSendKeys(nedsattningEl.aYes, protractor.Key.SPACE)
                .then(function() {
                    return pageHelpers.moveAndSendKeys(nedsattningEl.aText, beskrivning ? beskrivning : 'Nedsattning text');
                });

        } else if (nedsattning === 'Nej') {
            return pageHelpers.moveAndSendKeys(nedsattningEl.aNo, protractor.Key.SPACE);
        }
    },
    fillInRorelseorganensFunktioner: function(rorelseorganensFunktionerObj) {
        var nedsattningEl = this.funktionsnedsattning;
        var inUtUrFordon = rorelseorganensFunktionerObj.inUtUrFordon;

        return this.fillInRorelseNedsattning(rorelseorganensFunktionerObj.nedsattning, rorelseorganensFunktionerObj.nedsattningBeskrivning)
            .then(function() {
                if (inUtUrFordon === 'Ja') {
                    return pageHelpers.moveAndSendKeys(nedsattningEl.bYes, protractor.Key.SPACE);
                } else if (inUtUrFordon === 'Nej') {
                    return pageHelpers.moveAndSendKeys(nedsattningEl.bNo, protractor.Key.SPACE);
                }
            });
    },

    fillInHjartOchKarlsjukdomar: function(utkast) {
        var promiseArr = [];
        var hjartKarlCEl = this.hjartKarl;

        if (utkast.hjartHjarna === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.aYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.aNo, protractor.Key.SPACE));
        }
        if (utkast.hjartSkada === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.bYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.bNo, protractor.Key.SPACE));
        }
        if (utkast.hjartRisk === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.cYes, protractor.Key.SPACE)
                .then(function() {
                    return browser.sleep(1000); // Testar att vänta på animering eller nästa tick
                })
                .then(function() {
                    return pageHelpers.moveAndSendKeys(hjartKarlCEl.cText, utkast.hjartRiskBeskrivning);
                }));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(hjartKarlCEl.cNo, protractor.Key.SPACE));
        }
        return Promise.all(promiseArr);
    },
    fillInDiabetes: function(diabetesObj) {
        var diabetes = this.diabetes;
        if (diabetesObj.hasDiabetes === 'Ja') {
            return pageHelpers.moveAndSendKeys(diabetes.aYes, protractor.Key.SPACE).then(function() {

                if (diabetesObj.typ === 'Typ 1') {
                    return pageHelpers.moveAndSendKeys(diabetes.typ1, protractor.Key.SPACE);
                } else {
                    return pageHelpers.moveAndSendKeys(diabetes.typ2, protractor.Key.SPACE)
                        .then(function() {
                            // Ange behandlingstyp 
                            var promiseArr = [];
                            var typ = diabetesObj.behandlingsTyper;
                            if (typ.indexOf('Endast kost') > -1) {
                                promiseArr.push(pageHelpers.moveAndSendKeys(diabetes.endastkost, protractor.Key.SPACE));
                            }
                            if (typ.indexOf('Tabletter') > -1) {
                                promiseArr.push(pageHelpers.moveAndSendKeys(diabetes.tabletter, protractor.Key.SPACE));
                            }
                            if (typ.indexOf('Insulin') > -1) {
                                promiseArr.push(pageHelpers.moveAndSendKeys(diabetes.insulin, protractor.Key.SPACE));
                            }
                            return Promise.all(promiseArr);
                        });
                }
            });
        } else {
            return pageHelpers.moveAndSendKeys(diabetes.aNo, protractor.Key.SPACE);
        }

    },
    fillInNeurologiskaSjukdomar: function(utkast) {
        if (utkast.neurologiska === 'Ja') {
            return pageHelpers.moveAndSendKeys(this.neurologiska.aYes, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.neurologiska.aNo, protractor.Key.SPACE);
        }
    },
    fillInEpilepsi: function(utkast) {
        var promiseArr = [];
        if (utkast.epilepsi === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.epilepsi.aYes, protractor.Key.SPACE));
            promiseArr.push(pageHelpers.moveAndSendKeys(this.epilepsi.aText, utkast.epilepsiBeskrivning));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.epilepsi.aNo, protractor.Key.SPACE));
        }
        return Promise.all(promiseArr);
    },
    fillInNjursjukdomar: function(utkast) {
        if (utkast.njursjukdom === 'Ja') {
            return pageHelpers.moveAndSendKeys(this.njursjukdom.aYes, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.njursjukdom.aNo, protractor.Key.SPACE);
        }
    },
    fillInDemens: function(utkast) {
        if (utkast.demens === 'Ja') {
            return pageHelpers.moveAndSendKeys(this.kognitivt.aYes, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.kognitivt.aNo, protractor.Key.SPACE);
        }
    },
    fillInSomnOchVakenhet: function(utkast) {
        if (utkast.somnVakenhet === 'Ja') {
            return pageHelpers.moveAndSendKeys(this.somnOchVakenhetsStorningar.JA, protractor.Key.SPACE);
        } else {
            return pageHelpers.moveAndSendKeys(this.somnOchVakenhetsStorningar.NEJ, protractor.Key.SPACE);
        }
    },
    fillInAlkoholNarkotikaLakemedel: function(utkast) {
        var promiseArr = [];

        promiseArr.push(element.all(by.css('[name="narkotikaLakemedel.teckenMissbruk"]')).then(function(elm) {
            if (utkast.alkoholMissbruk === 'Ja') {
                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        }));
        promiseArr.push(element.all(by.css('[name="narkotikaLakemedel.foremalForVardinsats"]')).then(function(elm) {
            if (utkast.alkoholVard === 'Ja') {
                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        }));

        promiseArr.push(element.all(by.css('[name="narkotikaLakemedel.lakarordineratLakemedelsbruk"]')).then(function(elm) {
            if (utkast.alkoholLakemedel === 'Ja') {
                return Promise.all([
                    pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE),
                    pageHelpers.moveAndSendKeys(element(by.id('narkotikaLakemedel-lakemedelOchDos')), utkast.alkoholLakemedelBeskrivning)
                ]);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        }));

        promiseArr.push(element.all(by.css('[name="narkotikaLakemedel.provtagningBehovs"]')).then(function(elm) {
            if (utkast.alkoholMissbruk === 'Ja' || utkast.alkoholVard === 'Ja') {
                if (utkast.alkoholProvtagning === 'Ja') {
                    return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
                } else {
                    return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
                }
            }
        }));

        return Promise.all(promiseArr);

    },
    fillInPsykiska: function(utkast) {
        return element.all(by.css('[name="psykiskt.psykiskSjukdom"]')).then(function(elm) {
            if (utkast.psykiskSjukdom === 'Ja') {
                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        });
    },
    fillInAdhd: function(utkast) {
        var promiseArr = [];

        var a = element.all(by.css('[name="utvecklingsstorning.psykiskUtvecklingsstorning"]')).then(function(elm) {
            if (utkast.adhdPsykisk === 'Ja') {
                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        });

        promiseArr.push(a);

        var b = element.all(by.css('[name="utvecklingsstorning.harSyndrom"]')).then(function(elm) {
            if (utkast.adhdSyndrom === 'Ja') {
                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        });

        promiseArr.push(b);

        return Promise.all(promiseArr);
    },
    fillInSjukhusvard: function(utkast) {
        return element.all(by.css('[name="sjukhusvard.sjukhusEllerLakarkontakt"]')).then(function(elm) {
            if (utkast.sjukhusvard === 'Ja') {

                return pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE)
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(element(by.id('sjukhusvard-tidpunkt')), utkast.sjukhusvardTidPunkt);
                    })
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(element(by.id('sjukhusvard-vardinrattning')), utkast.sjukhusvardInrattning);
                    })
                    .then(function() {
                        return pageHelpers.moveAndSendKeys(element(by.id('sjukhusvard-anledning')), utkast.sjukhusvardAnledning);
                    });
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        });
    },
    fillInOvrigMedicinering: function(utkast) {
        return element.all(by.css('[name="medicinering.stadigvarandeMedicinering"]')).then(function(elm) {
            if (utkast.ovrigMedicin === 'Ja') {
                return Promise.all([
                    pageHelpers.moveAndSendKeys(elm[0], protractor.Key.SPACE),
                    pageHelpers.moveAndSendKeys(element(by.id('medicinering-beskrivning')), utkast.ovrigMedicinBeskrivning)
                ]);
            } else {
                return pageHelpers.moveAndSendKeys(elm[1], protractor.Key.SPACE);
            }
        });
    }
});

module.exports = new TsBasUtkast();
