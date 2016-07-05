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

/* globals pages*/
/* globals logger, Promise */

'use strict';

var helpers = require('./helpers.js');
var intygPage = pages.intyg.fk['7263'].intyg;

var field5 = intygPage.field5.aktivitetsbegransning;
var field6a = intygPage.field6a;

function checkSmitta(isSmittskydd) {
    var smitta = helpers.boolTillJaNej(isSmittskydd);
    expect(intygPage.field1.text.getText()).to.eventually.equal(smitta).then(function(value) {
        logger.info('OK - SMITTA = ' + value);
    }, function(reason) {
        throw ('FEL, SMITTA,' + reason);
    });
}

function checkDiagnos(diagnos) {
    var field2 = intygPage.field2;
    if (diagnos) {
        expect(field2.diagnoskod.getText()).to.eventually.equal(diagnos.diagnoser[0].ICD10).then(
            function(value) {
                logger.info('OK - Diagnoskod = ' + value);
            },
            function(reason) {
                throw ('FEL, Diagnoskod,' + reason);
            }
        );

        expect(field2.diagnosBeskrivning.getText()).to.eventually.equal(diagnos.fortydligande).then(
            function(value) {
                logger.info('OK - Diagnos förtydligande = ' + value);
            },
            function(reason) {
                throw ('FEL, Diagnos förtydligande,' + reason);
            }
        );
    }
}

function checkBaserasPa(baserasPa) {
    if (baserasPa.minUndersokning) {
        expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.minUndersokning.datum)).then(
            function(value) {
                logger.info('OK - Undersokning av patienten baseras på min Undersokning = ' + value);
            },
            function(reason) {
                throw ('FEL, Undersokning av patienten baseras på min Undersokning, ' + reason);
            }
        );
    }
    if (baserasPa.minTelefonkontakt) {
        expect(intygPage.field4b.telefonKontakt.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.minTelefonkontakt.datum)).then(
            function(value) {
                logger.info('OK - Undersokning av patienten baseras på min Telefonkontakt = ' + value);
            },
            function(reason) {
                throw ('FEL, Undersokning av patienten baseras på min Telefonkontakt, ' + reason);
            }
        );
    }
    if (baserasPa.journaluppgifter) {
        expect(intygPage.field4b.journaluppgifter.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.journaluppgifter.datum)).then(
            function(value) {
                logger.info('OK - Undersokning av patienten baseras på journaluppgifter = ' + value);
            },
            function(reason) {
                throw ('FEL, Undersokning av patienten baseras på journaluppgifter, ' + reason);
            }
        );
    }
    if (baserasPa.annat) {
        expect(intygPage.field4b.annat.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.annat.datum)).then(
            function(value) {
                logger.info('OK - Undersokning av patienten baseras på annat = ' + value);
            },
            function(reason) {
                throw ('FEL, Undersokning av patienten baseras på annat, ' + reason);
            }
        );
        logger.debug('TODO: Fix check for undersökning annat text');
        // expect(intygPage.field4b.annanReferensBeskrivning.getText()).to.eventually.contain(baserasPa.annat.text).then(
        //   function (value) {
        //     logger.info('OK - Undersokning av patienten baseras på annat text = ' + value);
        //   },
        //   function (reason) {
        //     throw('FEL, Undersokning av patienten baseras på annat text, ' + reason);
        //   }
        // );
    }
}

function checkArbetsformaga(arbetsformaga) {
    if (arbetsformaga.nedsattMed25) {
        expect(intygPage.field8b.nedsat25.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed25.from)).then(
            function(value) {
                logger.info('OK - Nedsatt med 20% from = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 20% from,' + reason);
            });
        expect(intygPage.field8b.nedsat25.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed25.tom)).then(
            function(value) {
                logger.info('OK - Nedsatt med 20% tom = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 20% tom,' + reason);
            });
    }
    if (arbetsformaga.nedsattMed50) {
        expect(intygPage.field8b.nedsat50.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed50.from)).then(
            function(value) {
                logger.info('OK - Nedsatt med 50% from = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 50% from,' + reason);
            });
        expect(intygPage.field8b.nedsat50.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed50.tom)).then(
            function(value) {
                logger.info('OK - Nedsatt med 50% tom = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 50% tom,' + reason);
            });
    }
    if (arbetsformaga.nedsattMed75) {
        expect(intygPage.field8b.nedsat75.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed75.from)).then(
            function(value) {
                logger.info('OK - Nedsatt med 75% from = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 75% from,' + reason);
            });
        expect(intygPage.field8b.nedsat75.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed75.tom)).then(
            function(value) {
                logger.info('OK - Nedsatt med 75% tom = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 75% tom,' + reason);
            });
    }
    if (arbetsformaga.nedsattMed100) {
        expect(intygPage.field8b.nedsat100.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed100.from)).then(
            function(value) {
                logger.info('OK - Nedsatt med 100% from = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 100% from,' + reason);
            });
        expect(intygPage.field8b.nedsat100.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed100.tom)).then(
            function(value) {
                logger.info('OK - Nedsatt med 100% tom = ' + value);
            },
            function(reason) {
                throw ('FEL, Nedsatt med 100% tom,' + reason);
            });
    }
}

function checkOvrigtRekommendation(rek) {
    if (rek) {
        return expect(field6a.ovrigt.getText()).to.eventually.equal(rek).then(
            function(value) {
                logger.info('OK - Övrig rekommendation= ' + value);
            },
            function(reason) {
                throw ('FEL, Övrig rekommendation,' + reason);
            }
        );
    } else {
        return Promise.resolve();
    }
}

function checkArbetesinriktadRehab(rek) {
    if (rek) {
        return expect(intygPage.field7.text.getText()).to.eventually.equal(rek).then(
            function(value) {
                logger.info('OK - Rehabilitering aktuell = ' + value);
            },
            function(reason) {
                throw ('FEL, Rehabilitering aktuell,' + reason);
            }
        );
    } else {
        return Promise.resolve();
    }
}

function checkAktuellaArbetsuppgifter(arb) {
    if (arb) {
        return helpers.genericAssert(arb.nuvarandeArbete.aktuellaArbetsuppgifter, 'nuvarandeArbetsuppgifter');
    } else {
        return Promise.resolve();
    }
}

function checkAktivitetsbegransning(begr) {
    if (begr) {
        return helpers.genericAssert(begr, 'aktivitetsbegransning');
    } else {
        return Promise.resolve();
    }
}



module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Läkarintyg FK 7263 --');
        logger.info('TODO: FIXA FÄLT 10 CHECKAR');

        var kontaktOnskas = helpers.boolTillJaNej(intyg.kontaktOnskasMedFK);

        return Promise.all([
            // Kontrollera FÄLT 1 : Smittskydd
            checkSmitta(intyg.smittskydd),

            //Kontrollera FÄLT 2 : Diagnos
            checkDiagnos(intyg.diagnos),

            //Kontrollera FÄLT 3 : Sjukdomsförlopp
            expect(intygPage.field3.sjukdomsforlopp.getText()).to.eventually.equal(intyg.aktuelltSjukdomsforlopp).then(
                function(value) {
                    logger.info('OK - Sjukdomsförlopp = ' + value);
                },
                function(reason) {
                    throw ('FEL, Sjukdomsförlopp,' + reason);
                }
            ),

            //Kontrollera FÄLT 4 : Funktionsnedsättning
            expect(intygPage.field4.funktionsnedsattning.getText()).to.eventually.equal(intyg.funktionsnedsattning).then(
                function(value) {
                    logger.info('OK - Funktionsnedsättning = ' + value);
                },
                function(reason) {
                    throw ('FEL, Funktionsnedsättning,' + reason);
                }
            ),

            //Kontrollera FÄLT 4b : Intyget baseras på
            checkBaserasPa(intyg.baserasPa),

            //Kontrollera Fält 5 : Aktivitetsbegränsning
            expect(field5.getText()).to.eventually.equal(intyg.aktivitetsBegransning).then(
                function(value) {
                    logger.info('OK - Aktivitetsbegränsning = ' + value);
                },
                function(reason) {
                    throw ('FEL, Aktivitetsbegränsning,' + reason);
                }
            ),

            //Kontrollera FÄLT 6a : Rekommendationer
            //Kontakt med AF
            expect(field6a.kontaktArbetsformedlingen.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedArbetsformedlingen).then(
                function(value) {
                    logger.info('OK - Kontakt med AF = ' + value);
                },
                function(reason) {
                    throw ('FEL, Kontakt med AF,' + reason);
                }
            ),

            //Kontakt med Företagshälsovården
            expect(field6a.kontaktForetagshalsovarden.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedForetagshalsovard).then(
                function(value) {
                    logger.info('OK - Kontakt med Företagshälsovård = ' + value);
                },
                function(reason) {
                    throw ('FEL, Kontakt med Företagshälsovård,' + reason);
                }
            ),

            // Kontrollera FÄLT 8b : Nedsatt arbetsförmåga
            checkArbetsformaga(intyg.arbetsformaga),

            // fält 9
            expect(intygPage.FMBprognos.getText()).to.eventually.equal(intyg.arbetsformagaFMB).then(
                function(value) {
                    logger.info('OK - Arbetsformåga FMB prognos = ' + value);
                },
                function(reason) {
                    throw ('FEL, Arbetsformåga FMB prognos,' + reason);
                }),

            //Övrig rekommendation
            checkOvrigtRekommendation(intyg.rekommendationer.ovrigt),

            // Kontrollera FÄLT 7 : Rehabilitering
            checkArbetesinriktadRehab(intyg.rekommendationer.arbetslivsinriktadRehab),

            //Kontrollera arbetsuppgifter
            checkAktuellaArbetsuppgifter(intyg.arbete),

            // Kontrollera aktivitetsbegränsning
            checkAktivitetsbegransning(intyg.aktivitetsBegransning),


            // Kontrollera FÄLT 11 : Resa till arbete med annat färdsätt
            expect(intygPage.field11.text.getText()).to.eventually.contain(helpers.boolTillJaNej(intyg.rekommendationer.resor)).then(function(value) {
                logger.info('OK - Resor till arbete med annat färdsätt = ' + value);
            }, function(reason) {
                throw ('FEL, Resor till arbete med annat färdsätt,' + reason);
            }),

            // Kontrollera FÄLT 12 : Kontakt önskas med FK
            expect(intygPage.field12.text.getText()).to.eventually.equal(kontaktOnskas).then(function(value) {
                logger.info('OK - Kontakt med FK = ' + value);
            }, function(reason) {
                throw ('FEL, Kontakt med FK,' + reason);
            })

            // TBI! check förskrivarkod
            // expect(intygPage.forsKod.getText()).to.eventually.equal('0000000 - 1234567890123').then(function(value) {
            //     logger.info('OK - Forskrivarkod = ' + value);
            // }, function(reason) {
            //     throw('FEL, Forskrivarkod,' + reason);
            // }).then(throw);
        ]);
    }
};
