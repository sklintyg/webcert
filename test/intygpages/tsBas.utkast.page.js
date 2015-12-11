/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,helpers*/
'use strict';

// NOTE: This file is loaded before helpers in protractor.conf.js onPrepare. Therefore helpers are not available in file scope.

var BaseTsUtkast = require('./ts.base.utkast.page.js');

var TsBasUtkast = BaseTsUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-bas';
        this.at = element(by.id('edit-ts-bas'));
        this.syn = {
            aYes: element(by.id('synay')),
            aNo: element(by.id('synan')),
            bYes: element(by.id('synby')),
            bNo: element(by.id('synbn')),
            cYes: element(by.id('syncy')),
            cNo: element(by.id('syncn')),
            dYes: element(by.id('syndy')),
            dNo: element(by.id('syndn')),
            eYes: element(by.id('syney')),
            eNo: element(by.id('synen')),
            synHogerOgaUtanKorrektion: element(by.id('synHogerOgaUtanKorrektion')),
            synVansterOgaUtanKorrektion: element(by.id('synVansterOgaUtanKorrektion')),
            synBinokulartUtanKorrektion: element(by.id('synBinokulartUtanKorrektion')),
            synHogerOgaMedKorrektion: element(by.id('synHogerOgaMedKorrektion')),
            synVansterOgaMedKorrektion: element(by.id('synVansterOgaMedKorrektion')),
            synBinokulartMedKorrektion: element(by.id('synBinokulartMedKorrektion'))
        }
        this.horselBalans = {
            aYes: element(by.id('horselbalansay')),
            aNo: element(by.id('horselbalansan')),
            bYes: element(by.id('horselbalansby')),
            bNo: element(by.id('horselbalansbn'))
        }
        this.funktionsnedsattning = {
            aYes: element(by.id('funktionsnedsattningay')),
            aNo: element(by.id('funktionsnedsattningan')),
            aText: element(by.id('funktionsnedsattning')),
            bYes: element(by.id('funktionsnedsattningby')),
            bNo: element(by.id('funktionsnedsattningbn'))
        }
        this.hjartKarl = {
            aYes: element(by.id('hjartkarlay')),
            aNo: element(by.id('hjartkarlan')),
            bYes: element(by.id('hjartkarlby')),
            bNo: element(by.id('hjartkarlbn')),
            cYes: element(by.id('hjartkarlcy')),
            cNo: element(by.id('hjartkarlcn')),
            cText: element(by.id('beskrivningRiskfaktorer'))
        }
        this.diabetes = {
            aYes: element(by.id('diabetesay')),
            aNo: element(by.id('diabetesan')),
            typ1: element(by.id('diabetestyp1')),
            typ2: element(by.id('diabetestyp2')),
            endastkost: element(by.id('diabetestreat1')),
            tabletter: element(by.id('diabetestreat2')),
            insulin: element(by.id('diabetestreat3'))
        }
        this.neurologiska = {
            aYes: element(by.id('neurologiay')),
            aNo: element(by.id('neurologian'))
        }
        this.epilepsi = {
            aYes: element(by.id('medvetandestorningay')),
            aText: element(by.id('beskrivningMedvetandestorning')),
            aNo: element(by.id('medvetandestorningan'))
        }
        this.njursjukdom = {
            aYes: element(by.id('njuraray')),
            aNo: element(by.id('njuraran'))
        }
        this.kognitivt = {
            aYes: element(by.id('kognitivtay')),
            aNo: element(by.id('kognitivtan'))
        }
    },
    fillInSynfunktioner: function (utkast) {
        if (utkast.synDonder === 'Ja') {
            this.syn.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.aNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.synNedsattBelysning === 'Ja') {
            this.syn.bYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.bNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.synOgonsjukdom === 'Ja') {
            this.syn.cYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.cNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.synDubbel === 'Ja') {
            this.syn.dYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.dNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.synNystagmus === 'Ja') {
            this.syn.eYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.syn.eNo.sendKeys(protractor.Key.SPACE);
        }

        this.syn.synHogerOgaUtanKorrektion.sendKeys('0,8');
        this.syn.synVansterOgaUtanKorrektion.sendKeys('0,7');
        this.syn.synBinokulartUtanKorrektion.sendKeys('1,0');

        this.syn.synHogerOgaMedKorrektion.sendKeys('1,0');
        this.syn.synVansterOgaMedKorrektion.sendKeys('1,0');
        this.syn.synBinokulartMedKorrektion.sendKeys('1,0');
    },
    fillInHorselOchBalanssinne: function(utkast) {
        if (utkast.horselYrsel === 'Ja') {
            this.horselBalans.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.horselBalans.aNo.sendKeys(protractor.Key.SPACE);
        }

        if (helpers.page.hasHogreKorkortsbehorigheter(utkast.korkortstyper)) {
            if (utkast.horselSamtal === 'Ja') {
                this.horselBalans.bYes.sendKeys(protractor.Key.SPACE);
            } else {
                this.horselBalans.bNo.sendKeys(protractor.Key.SPACE);
            }
        }
    },
    fillInRorelseorganensFunktioner: function(utkast) {
        if (utkast.rorOrgNedsattning === 'Ja') {
            this.funktionsnedsattning.aYes.sendKeys(protractor.Key.SPACE);
            this.funktionsnedsattning.aText.sendKeys('Amputerad under höger knä.');
        } else {
            this.funktionsnedsattning.aNo.sendKeys(protractor.Key.SPACE);
        }

        if (helpers.page.hasHogreKorkortsbehorigheter(utkast.korkortstyper)) {
            if (utkast.rorOrgInUt === 'Ja') {
                this.funktionsnedsattning.bYes.sendKeys(protractor.Key.SPACE);
            } else {
                this.funktionsnedsattning.bNo.sendKeys(protractor.Key.SPACE);
            }
        }
    },
    fillInHjartOchKarlsjukdomar: function(utkast) {
        if (utkast.hjartHjarna === 'Ja') {
            this.hjartKarl.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.hjartKarl.aNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.hjartSkada === 'Ja') {
            this.hjartKarl.bYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.hjartKarl.bNo.sendKeys(protractor.Key.SPACE);
        }
        if (utkast.hjartRisk === 'Ja') {
            this.hjartKarl.cYes.sendKeys(protractor.Key.SPACE);
            this.hjartKarl.cText.sendKeys('TIA och förmaksflimmer.');
        } else {
            this.hjartKarl.cNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInDiabetes: function (utkast) {
        if (utkast.diabetes.has === 'Ja') {
            this.diabetes.aYes.sendKeys(protractor.Key.SPACE);

            if (utkast.diabetes.typ === 'Typ 1') {
                this.diabetes.typ1.sendKeys(protractor.Key.SPACE);
            } else {
                this.diabetes.typ2.sendKeys(protractor.Key.SPACE);
            }

            // Ange behandlingstyp
            var typer = utkast.diabetes.behandling.typer;
            if (typer.indexOf('Endast kost') > -1) {
                this.diabetes.endastkost.sendKeys(protractor.Key.SPACE);
            }
            if (typer.indexOf('Tabletter') > -1) {
                this.diabetes.tabletter.sendKeys(protractor.Key.SPACE);
            }
            if (typer.indexOf('Insulin') > -1) {
                this.diabetes.insulin.sendKeys(protractor.Key.SPACE);
            }
        } else {
            this.diabetes.aNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInNeurologiskaSjukdomar: function(utkast) {
        if (utkast.neurologiska === 'Ja') {
            this.neurologiska.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.neurologiska.aNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInEpilepsi: function (utkast) {
        if (utkast.epilepsi === 'Ja') {
            this.epilepsi.aYes.sendKeys(protractor.Key.SPACE);
            this.epilepsi.aText.sendKeys('Blackout. Midsommarafton.');
        } else {
            this.epilepsi.aNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInNjursjukdomar: function(utkast) {
        if (utkast.njursjukdom === 'Ja') {
            this.njursjukdom.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.njursjukdom.aNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInDemens: function (utkast) {
        if (utkast.demens === 'Ja') {
            this.kognitivt.aYes.sendKeys(protractor.Key.SPACE);
        } else {
            this.kognitivt.aNo.sendKeys(protractor.Key.SPACE);
        }
    },
    fillInSomnOchVakenhet: function (utkast) {
        element.all(by.css('[name="somnvakenheta"]')).then(function (elm) {
            if (utkast.somnVakenhet === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    fillInAlkoholNarkotikaLakemedel: function(utkast) {
        element.all(by.css('[name="narkotikalakemedela"]')).then(function (elm) {
            if (utkast.alkoholMissbruk === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
        element.all(by.css('[name="narkotikalakemedelb"]')).then(function (elm) {
            if (utkast.alkoholVard === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
        element.all(by.css('[name="narkotikalakemedelc"]')).then(function (elm) {
            if (utkast.alkoholLakemedel === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
                element(by.id('beskrivningNarkotikalakemedel')).sendKeys('2 liter metadon.');
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
        element.all(by.css('[name="narkotikalakemedelb2"]')).then(function (elm) {
            if (utkast.alkoholMissbruk === 'Ja' || utkast.alkoholVard === 'Ja') {
                if (utkast.alkoholProvtagning === 'Ja') {
                    elm[0].sendKeys(protractor.Key.SPACE);
                } else {
                    elm[1].sendKeys(protractor.Key.SPACE);
                }
            }
        });
    },
    fillInPsykiska: function(utkast) {
        element.all(by.css('[name="psykiskta"]')).then(function (elm) {
            if (utkast.psykiskSjukdom === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    fillInAdhd: function (utkast) {
        element.all(by.css('[name="utvecklingsstorninga"]')).then(function (elm) {
            if (utkast.adhdPsykisk === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });

        element.all(by.css('[name="utvecklingsstorningb"]')).then(function (elm) {
            if (utkast.adhdSyndrom === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    fillInSjukhusvard: function(utkast) {
        element.all(by.css('[name="sjukhusvarda"]')).then(function (elm) {
            if (utkast.sjukhusvard === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
                element(by.id('tidpunkt')).sendKeys('2015-12-13');
                element(by.id('vardinrattning')).sendKeys('Östra sjukhuset.');
                element(by.id('anledning')).sendKeys('Allmän ysterhet.');
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    fillInOvrigMedicinering: function(utkast) {
        element.all(by.css('[name="medicineringa"]')).then(function (elm) {
            if (utkast.ovrigMedicin === 'Ja') {
                elm[0].sendKeys(protractor.Key.SPACE);
                element(by.id('beskrivningMedicinering')).sendKeys('Xanax FTW');
            } else {
                elm[1].sendKeys(protractor.Key.SPACE);
            }
        });
    },
});

module.exports = new TsBasUtkast();
