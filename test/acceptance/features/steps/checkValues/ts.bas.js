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

/* globals logger, Promise, pages, wcTestTools*/
'use strict';
var tsBasIntygPage = pages.intyg.ts.bas.intyg;
var helpers = require('./helpers.js');
var testdataHelper = wcTestTools.helpers.testdata;




function checkDiabetes(intyg) {
    var promiseArr = [];

    if (intyg.diabetes.typ === 'Typ 2' && intyg.diabetes.hasDiabetes === 'Ja') {
        var typer = intyg.diabetes.behandlingsTyper;
        promiseArr.push(expect(tsBasIntygPage.diabetesTyp.getText()).to.eventually.equal(intyg.diabetes.typ).then(function(value) {
            logger.info('OK - Patient diabetes typ = ' + value);

        }, function(reason) {
            throw ('FEL - Patient diabetes typ : ' + reason);
        }));
        typer.forEach(function(_typ, index) {
            if (_typ === 'Endast kost') {
                promiseArr.push(expect(tsBasIntygPage.getBehandlingsTyp(index).getText()).to.eventually.equal('Kost').then(function(value) {
                    logger.info('OK - Endast kost = ' + value);
                }, function(reason) {
                    throw ('FEL - Endast kost : ' + reason);
                }));
            } else if (_typ === 'Tabletter') {
                promiseArr.push(expect(tsBasIntygPage.getBehandlingsTyp(index).getText()).to.eventually.equal('Tabletter').then(function(value) {
                    logger.info('OK - Tabletter = ' + value);
                }, function(reason) {
                    throw ('FEL - Tabletter : ' + reason);
                }));
            } else if (_typ === 'Insulin') {
                promiseArr.push(expect(tsBasIntygPage.getBehandlingsTyp(index).getText()).to.eventually.equal('Insulin').then(function(value) {
                    logger.info('OK - Insulin = ' + value);
                }, function(reason) {
                    throw ('FEL - Insulin: ' + reason);
                }));
            }
        });
    } else if (intyg.diabetes.typ === 'Typ 1') {
        promiseArr.push(expect(tsBasIntygPage.kostTabletterInsulin.getText()).to.eventually.equal('Ej angivet').then(function(value) {
            logger.info('OK - Kost/Tabletter/Insulin = \"TOMT\" --> ' + value);
        }, function(reason) {
            throw ('FEL - Kost/Tabletter/Insulin: \"TOMT\" --> ' + reason);
        }));
    }
    return Promise.all(promiseArr);
}

function checkKorrektionsglasensStyrka(styrkor) {
    var _sum = (+styrkor.homk - +styrkor.houk) + (+styrkor.vomk - +styrkor.vouk);
    var overskrider8dioptrier = _sum > 8;

    return expect(tsBasIntygPage.korrektionsglasensStyrka.getText()).to.eventually.equal(helpers.boolTillJaNej(overskrider8dioptrier)).then(function(value) {
        logger.info('OK - Korrektionsglasens styrka (' + _sum + ') överskrider 8 dioptrier = ' + value);
    }, function(reason) {
        throw ('FEL - Korrektionsglasens styrka (' + _sum + ') överskrider 8 dioptrier : ' + reason);
    });
}

module.exports = {
    checkValues: function(intyg, callback) {
        logger.info('-- Kontrollerar Transportstyrelsens läkarintyg högre körkortsbehörighet --');
        var promiseArr = [];

        promiseArr.push(expect(tsBasIntygPage.synfaltsdefekter.getText()).to.eventually.equal(intyg.synDonder).then(function(value) {
            logger.info('OK - Synfältsdefekter = ' + value);
        }, function(reason) {
            throw ('FEL - Synfältsdefekter : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.nattblindhet.getText()).to.eventually.equal(intyg.synNedsattBelysning).then(function(value) {
            logger.info('OK - Nattblindhet = ' + value);
        }, function(reason) {
            throw ('FEL - Nattblindhet : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.progressivOgonsjukdom.getText()).to.eventually.equal(intyg.synOgonsjukdom).then(function(value) {
            logger.info('OK - Progressiv ögonsjukdom = ' + value);
        }, function(reason) {
            throw ('FEL - Progressiv ögonsjukdom : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.diplopi.getText()).to.eventually.equal(intyg.synDubbel).then(function(value) {
            logger.info('OK - Diplopi = ' + value);
        }, function(reason) {
            throw ('FEL - Diplopi : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.nystagmus.getText()).to.eventually.equal(intyg.synNystagmus).then(function(value) {
            logger.info('OK - Nystagmus = ' + value);
        }, function(reason) {
            throw ('FEL - Nystagmus : ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.hogerOgautanKorrektion.getText()).to.eventually.equal(intyg.styrkor.houk.toString().replace('.', ',')).then(function(value) {
                logger.info('OK - Höger Öga utan korrektion = ' + value);
            },
            function(reason) {
                throw ('FEL - Höger Öga utan korrektion : ' + reason);
            }));
        promiseArr.push(expect(tsBasIntygPage.hogerOgamedKorrektion.getText()).to.eventually.equal(intyg.styrkor.homk.toString().replace('.', ',')).then(function(value) {

            logger.info('OK - Höger Öga med korrektion = ' + value);
        }, function(reason) {
            throw ('FEL - Höger Öga med korrektion : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.vansterOgautanKorrektion.getText()).to.eventually.equal(intyg.styrkor.vouk.toString().replace('.', ',')).then(function(value) {
            logger.info('OK - Vänster öga utan korrektion = ' + value);
        }, function(reason) {
            throw ('FEL - Vänster öga utan korrektion : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.vansterOgamedKorrektion.getText()).to.eventually.equal(intyg.styrkor.vomk.toString().replace('.', ',')).then(function(value) {
            logger.info('OK - Vänster Öga med korrektion = ' + value);
        }, function(reason) {
            throw ('FEL - Vänster Öga med korrektion: ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.binokulartutanKorrektion.getText()).to.eventually.equal(intyg.styrkor.buk.toString().replace('.', ',')).then(function(value) {
            logger.info('OK - Binokulärt utan klorrektion = ' + value);
        }, function(reason) {
            throw ('FEL - Binokulärt utan klorrektion : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.binokulartmedKorrektion.getText()).to.eventually.equal(intyg.styrkor.bmk.toString().replace('.', ',')).then(function(value) {
            logger.info('OK - Binokulärt med klorrektion = ' + value);
        }, function(reason) {
            throw ('FEL - Binokulärt med klorrektion : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.vansterOgakontaktlins.getText()).to.eventually.equal(intyg.linser.vanster).then(function(value) {
            logger.info('OK - Vänster öga kontaktlins = ' + value);
        }, function(reason) {
            throw ('FEL - Vänster öga kontaktlins : ' + reason);
        }));
        promiseArr.push(expect(tsBasIntygPage.hogerOgakontaktlins.getText()).to.eventually.equal(intyg.linser.hoger).then(function(value) {
            logger.info('OK - Höger öga kontaktlins = ' + value);
        }, function(reason) {
            throw ('FEL - Höger öga kontaktlins : ' + reason);
        }));

        //Kontrollera Fält 1 : Korrektionsglasens styrka
        promiseArr.push(checkKorrektionsglasensStyrka(intyg.styrkor));

        promiseArr.push(expect(tsBasIntygPage.horselBalansbalansrubbningar.getText()).to.eventually.equal(intyg.horsel.yrsel).then(function(value) {
            logger.info('OK - Hörsel- och balansrubbningar = ' + value);
        }, function(reason) {
            throw ('FEL - Hörsel- och balansrubbningar : ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.funktionsnedsattning.getText()).to.eventually.equal(intyg.rorelseorganensFunktioner.nedsattning).then(function(value) {
            logger.info('OK - Rörelsehinder = ' + value);
        }, function(reason) {
            throw ('FEL - Rörelsehinder : ' + reason);
        }));

        if (intyg.rorelseorganensFunktioner.nedsattning === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.funktionsnedsattningbeskrivning.getText()).to.eventually.equal(intyg.rorelseorganensFunktioner.nedsattningBeskrivning).then(function(value) {
                logger.info('OK - Rörelsehinder kommentar = ' + value);
            }, function(reason) {
                throw ('FEL - Rörelsehinder kommentar : ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.funktionsnedsattningbeskrivning.getText()).to.eventually.equal('Ej angivet').then(function(value) {
                logger.info('OK - Rörelsehinder kommentar är tom = ' + value);
            }, function(reason) {
                throw ('FEL - Rörelsehinder kommentar är tom : ' + reason);
            }));
        }

        var rorelseFormaga = testdataHelper.ejAngivetIfNull(intyg.rorelseorganensFunktioner.inUtUrFordon);
        promiseArr.push(expect(tsBasIntygPage.funktionsnedsRorelseformaga.getText()).to.eventually.equal(rorelseFormaga).then(function(value) {
            logger.info('OK - Rörelseförmågan = ' + value);
        }, function(reason) {
            throw ('FEL - Rörelseförmågan : ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.hjartKarlSjukdom.getText()).to.eventually.equal(intyg.hjartHjarna).then(function(value) {
            logger.info('OK - Hjart kärl sjukdom = ' + value);
        }, function(reason) {
            throw ('FEL - Hjart kärl sjukdom : ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.hjarnskadaEfterTrauma.getText()).to.eventually.equal(intyg.hjartSkada).then(function(value) {
            logger.info('OK - Hjärnskada efter trauma = ' + value);
        }, function(reason) {
            throw ('FEL - Hjärnskada efter trauma : ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.riskfaktorerStroke.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull(intyg.hjartRisk)).then(function(value) {
            logger.info('OK - Riskfaktorer för stroke = ' + value);
        }, function(reason) {
            throw ('FEL - Riskfaktorer för stroke : ' + reason);
        }));

        if (intyg.hjartRisk === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.beskrivningRiskfaktorer.getText()).to.eventually.equal('TIA och förmaksflimmer.').then(function(value) {
                logger.info('OK - Riskfaktorer för stroke (Kommentar) = ' + value);
            }, function(reason) {
                throw ('FEL - Riskfaktorer för stroke (Kommentar) : ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.beskrivningRiskfaktorer.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull('')).then(function(value) {
                logger.info('OK - Riskfaktorer för stroke (Kommentar) = \"TOMT\"');
            }, function(reason) {
                throw ('FEL - Riskfaktorer för stroke (Kommentar) : ' + reason);
            }));
        }

        promiseArr.push(expect(tsBasIntygPage.harDiabetes.getText()).to.eventually.equal(intyg.diabetes.hasDiabetes).then(function(value) {
            logger.info('OK - Patient har Diabetes = ' + value);
        }, function(reason) {
            throw ('FEL - Patient har Diabetes : ' + reason);
        }));

        //kontrollera diabetes
        promiseArr.push(checkDiabetes(intyg));

        promiseArr.push(expect(tsBasIntygPage.neurologiskSjukdom.getText()).to.eventually.equal(intyg.neurologiska).then(function(value) {
            logger.info('OK - Neurologiska sjukdomar = ' + value);
        }, function(reason) {
            throw ('FEL - Neurologiska sjukdomar: ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.medvetandestorning.getText()).to.eventually.equal(intyg.epilepsi).then(function(value) {
            logger.info('OK - Patienten har eller har patienten haft epilepsi = ' + value);
        }, function(reason) {
            throw ('FEL - Patienten har eller har patienten haft epilepsi: ' + reason);
        }));
        if (intyg.epilepsi === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.medvetandestorningbeskrivning.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull(intyg.epilepsiBeskrivning)).then(function(value) {
                logger.info('OK - epilepsiBeskrivning Kommentar: ' + value);
            }, function(reason) {
                throw ('FEL - epilepsiBeskrivning Kommentar: -> ' + reason);
            }));
        }

        promiseArr.push(expect(tsBasIntygPage.nedsattNjurfunktion.getText()).to.eventually.equal(intyg.njursjukdom).then(function(value) {
            logger.info('OK - Njurfunktion = ' + value);
        }, function(reason) {
            throw ('FEL - Njurfunktion = ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.sviktandeKognitivFunktion.getText()).to.eventually.equal(intyg.demens).then(function(value) {
            logger.info('OK - Kognitiv funktion = ' + value);
        }, function(reason) {
            throw ('FEL - Kognitiv funktion = ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.teckenSomnstorningar.getText()).to.eventually.equal(intyg.somnVakenhet).then(function(value) {
            logger.info('OK - Tecken sömnstörningar = ' + value);
        }, function(reason) {
            throw ('FEL - Tecken sömnstörningar = ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.teckenMissbruk.getText()).to.eventually.equal(intyg.alkoholMissbruk).then(function(value) {
            logger.info('OK - Missbruk eller beroende = ' + value);
        }, function(reason) {
            throw ('FEL - Missbruk eller beroende = ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.foremalForVardinsats.getText()).to.eventually.equal(intyg.alkoholVard).then(function(value) {
            logger.info('OK - Alkohol vård = ' + value);
        }, function(reason) {
            throw ('FEL - Alkohol vård = ' + reason);
        }));

        if (intyg.alkoholMissbruk === 'Ja' || intyg.alkoholVard === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.provtagningBehovs.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull(intyg.alkoholProvtagning)).then(function(value) {
                logger.info('OK - Alkohol provtagning = ' + value);
            }, function(reason) {
                throw ('FEL - Alkohol provtagning = ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.provtagningBehovs.getText()).to.eventually.equal(testdataHelper.ejAngivetIfNull('')).then(function(value) {
                logger.info('OK - Alkohol provtagning = ' + value);
            }, function(reason) {
                throw ('FEL - Alkohol provtagning = ' + reason);
            }));
        }
        promiseArr.push(expect(tsBasIntygPage.lakarordineratLakemedelsbruk.getText()).to.eventually.equal(intyg.alkoholLakemedel).then(function(value) {
            logger.info('OK - Alkohol läkemedel = ' + value);
        }, function(reason) {
            throw ('FEL - Alkohol läkemedel = ' + reason);
        }));

        if (intyg.alkoholLakemedel === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.lakemedelOchDos.getText()).to.eventually.equal('2 liter metadon.').then(function(value) {
                logger.info('OK - Kommentar innehåller = \" 2 liter metadon.\"');
            }, function(reason) {
                throw ('FEL - Kommentar innehåller: \" 2 liter metadon.\" ->' + reason);
            }));
        }

        promiseArr.push(expect(tsBasIntygPage.psykiskSjukdom.getText()).to.eventually.equal(intyg.psykiskSjukdom).then(function(value) {
            logger.info('OK - Psykisk sjukdom = ' + value);
        }, function(reason) {
            throw ('FEL - Psykisk sjukdom: ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.psykiskUtvecklingsstorning.getText()).to.eventually.equal(intyg.adhdPsykisk).then(function(value) {
            logger.info('OK - ADHD psykisk = ' + value);
        }, function(reason) {
            throw ('FEL - ADHD psykisk: ' + reason);
        }));

        promiseArr.push(expect(tsBasIntygPage.harSyndrom.getText()).to.eventually.equal(intyg.adhdSyndrom).then(function(value) {
            logger.info('OK - ADHD syndrom = ' + value);
        }, function(reason) {
            throw ('FEL - ADHD syndrom: ' + reason);
        }));

        if (intyg.sjukhusvard === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.tidpunkt.getText()).to.eventually.equal(intyg.sjukhusvardTidPunkt).then(function(value) {
                logger.info('OK - Tidpunkt = ' + value);
            }, function(reason) {
                throw ('FEL - Tidpunkt: ' + reason);
            }));

            promiseArr.push(expect(tsBasIntygPage.vardinrattning.getText()).to.eventually.equal(intyg.sjukhusvardInrattning).then(function(value) {
                logger.info('OK - Vårdinrättning = ' + value);
            }, function(reason) {
                throw ('FEL - Vårdinrättning: ' + reason);
            }));

            promiseArr.push(expect(tsBasIntygPage.sjukhusvardanledning.getText()).to.eventually.equal(intyg.sjukhusvardAnledning).then(function(value) {
                logger.info('OK - Sjukhusvårdanledning = ' + value);
            }, function(reason) {
                throw ('FEL - Sjukhusvårdanledning: ' + reason);
            }));

            promiseArr.push(expect(tsBasIntygPage.sjukhusEllerLakarkontakt.getText()).to.eventually.contain('Ja').then(function(value) {
                logger.info('OK - Sjukhus Eller Läkarkontakt = ' + value);
            }, function(reason) {
                throw ('FEL - Sjukhus Eller Läkarkontakt: ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.sjukhusEllerLakarkontakt.getText()).to.eventually.equal('Nej').then(function(value) {
                logger.info('OK - Sjukhus Eller Läkarkontakt = ' + value);
            }, function(reason) {
                throw ('FEL - Sjukhus Eller Läkarkontakt: ' + reason);
            }));
        }

        if (intyg.ovrigMedicin === 'Ja') {
            promiseArr.push(expect(tsBasIntygPage.medicineringbeskrivning.getText()).to.eventually.equal(intyg.ovrigMedicinBeskrivning).then(function(value) {
                logger.info('OK - Stadigvarande medicinering = ' + value);
            }, function(reason) {
                throw ('FEL - Stadigvarande medicinering: ' + reason);
            }));

        } else if (intyg.ovrigMedicin === 'Nej') {
            promiseArr.push(expect(tsBasIntygPage.stadigvarandeMedicinering.getText()).to.eventually.equal('Nej').then(function(value) {
                logger.info('OK - Stadigvarande medicinering = ' + value);
            }, function(reason) {
                throw ('FEL - Stadigvarande medicinering: ' + reason);
            }));
        }

        return Promise.all(promiseArr);
    }
};
