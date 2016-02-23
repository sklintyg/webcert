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
/* globals logg */

'use strict';

var helpers = require('./helpers.js');
var intygPage = pages.intyg.fk['7263'].intyg;

function checkSmitta(isSmittskydd, cb) {
  var smitta = helpers.boolTillJaNej(isSmittskydd);
  expect(intygPage.field1.text.getText()).to.eventually.equal(smitta).then(function (value) {
    logg('OK - SMITTA = ' + value);
  }, function (reason) {
    cb('FEL, SMITTA,' + reason);
  });
}

function checkDiagnos(diagnos, cb) {
  var field2 = intygPage.field2;
  if (diagnos) {
    expect(field2.diagnoskod.getText()).to.eventually.equal(diagnos.diagnoser[0].ICD10).then(
      function (value) {
        logg('OK - Diagnoskod = ' + value);
      },
      function (reason) {
        cb('FEL, Diagnoskod,' + reason);
      }
    );

    expect(field2.diagnosBeskrivning.getText()).to.eventually.equal(diagnos.fortydligande).then(
      function (value) {
        logg('OK - Diagnos förtydligande = ' + value);
      },
      function (reason) {
        cb('FEL, Diagnos förtydligande,' + reason);
      }
    );
  }
}

function checkBaserasPa(baserasPa, cb) {
  if (baserasPa.minUndersokning) {
    expect(intygPage.field4b.undersokningAvPatienten.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.minUndersokning.datum)).then(
      function (value) {
        logg('OK - Undersokning av patienten baseras på min Undersokning = ' + value);
      },
      function (reason) {
        cb('FEL, Undersokning av patienten baseras på min Undersokning, ' + reason);
      }
    );
  }
  if (baserasPa.minTelefonkontakt) {
    expect(intygPage.field4b.telefonKontakt.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.minTelefonkontakt.datum)).then(
      function (value) {
        logg('OK - Undersokning av patienten baseras på min Telefonkontakt = ' + value);
      },
      function (reason) {
        cb('FEL, Undersokning av patienten baseras på min Telefonkontakt, ' + reason);
      }
    );
  }
  if (baserasPa.journaluppgifter) {
    expect(intygPage.field4b.journaluppgifter.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.journaluppgifter.datum)).then(
      function (value) {
        logg('OK - Undersokning av patienten baseras på journaluppgifter = ' + value);
      },
      function (reason) {
        cb('FEL, Undersokning av patienten baseras på journaluppgifter, ' + reason);
      }
    );
  }
  if (baserasPa.annat) {
    expect(intygPage.field4b.annat.getText()).to.eventually.equal(helpers.getDateForAssertion(baserasPa.annat.datum)).then(
      function (value) {
        logg('OK - Undersokning av patienten baseras på annat = ' + value);
      },
      function (reason) {
        cb('FEL, Undersokning av patienten baseras på annat, ' + reason);
      }
    );
    console.log('TODO: Fix check for undersökning annat text');
    // expect(intygPage.field4b.annanReferensBeskrivning.getText()).to.eventually.contain(baserasPa.annat.text).then(
    //   function (value) {
    //     logg('OK - Undersokning av patienten baseras på annat text = ' + value);
    //   },
    //   function (reason) {
    //     cb('FEL, Undersokning av patienten baseras på annat text, ' + reason);
    //   }
    // );
  }
}

function checkArbetsformaga(arbetsformaga, cb) {
  if (arbetsformaga.nedsattMed25) {
    expect(intygPage.field8b.nedsat25.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed25.from)).then(
      function (value) {
        logg('OK - Nedsatt med 20% from = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 20% from,' + reason);
      });
    expect(intygPage.field8b.nedsat25.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed25.tom)).then(
      function (value) {
        logg('OK - Nedsatt med 20% tom = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 20% tom,' + reason);
      });
  }
  if (arbetsformaga.nedsattMed50) {
    expect(intygPage.field8b.nedsat50.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed50.from)).then(
      function (value) {
        logg('OK - Nedsatt med 50% from = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 50% from,' + reason);
      });
    expect(intygPage.field8b.nedsat50.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed50.tom)).then(
      function (value) {
        logg('OK - Nedsatt med 50% tom = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 50% tom,' + reason);
      });
  }
  if (arbetsformaga.nedsattMed75) {
    expect(intygPage.field8b.nedsat75.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed75.from)).then(
      function (value) {
        logg('OK - Nedsatt med 75% from = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 75% from,' + reason);
      });
    expect(intygPage.field8b.nedsat75.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed75.tom)).then(
      function (value) {
        logg('OK - Nedsatt med 75% tom = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 75% tom,' + reason);
      });
  }
  if (arbetsformaga.nedsattMed100) {
    expect(intygPage.field8b.nedsat100.from.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed100.from)).then(
      function (value) {
        logg('OK - Nedsatt med 100% from = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 100% from,' + reason);
      });
    expect(intygPage.field8b.nedsat100.tom.getText()).to.eventually.equal(helpers.getDateForAssertion(arbetsformaga.nedsattMed100.tom)).then(
      function (value) {
        logg('OK - Nedsatt med 100% tom = ' + value);
      },
      function (reason) {
        cb('FEL, Nedsatt med 100% tom,' + reason);
      });
  }
}

module.exports = {
  checkFKValues: function (intyg, callback) {

    // Kontrollera FÄLT 1 : Smittskydd
    checkSmitta(intyg.smittskydd, callback);

    //Kontrollera FÄLT 2 : Diagnos
    checkDiagnos(intyg.diagnos, callback);

    //Kontrollera FÄLT 3 : Sjukdomsförlopp
    expect(intygPage.field3.sjukdomsforlopp.getText()).to.eventually.equal(intyg.aktuelltSjukdomsforlopp).then(
      function (value) {
        logg('OK - Sjukdomsförlopp = ' + value);
      },
      function (reason) {
        callback('FEL, Sjukdomsförlopp,' + reason);
      }
    );

    //Kontrollera FÄLT 4 : Funktionsnedsättning
    expect(intygPage.field4.funktionsnedsattning.getText()).to.eventually.equal(intyg.funktionsnedsattning).then(
      function (value) {
        logg('OK - Funktionsnedsättning = ' + value);
      },
      function (reason) {
        callback('FEL, Funktionsnedsättning,' + reason);
      }
    );

    //Kontrollera FÄLT 4b : Intyget baseras på

    checkBaserasPa(intyg.baserasPa, callback);

    //Kontrollera Fält 5 : Aktivitetsbegränsning
    var field5 = intygPage.field5.aktivitetsbegransning;
    expect(field5.getText()).to.eventually.equal(intyg.aktivitetsBegransning).then(
      function (value) {
        logg('OK - Aktivitetsbegränsning = ' + value);
      },
      function (reason) {
        callback('FEL, Aktivitetsbegränsning,' + reason);
      }
    );

    //Kontrollera FÄLT 6a : Rekommendationer
    var field6a = intygPage.field6a;
    //Kontakt med AF
    expect(field6a.kontaktArbetsformedlingen.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedArbetsformedlingen).then(
      function (value) {
        logg('OK - Kontakt med AF = ' + value);
      },
      function (reason) {
        callback('FEL, Kontakt med AF,' + reason);
      }
    );
    //Kontakt med Företagshälsovården
    expect(field6a.kontaktForetagshalsovarden.isDisplayed()).to.become(intyg.rekommendationer.kontaktMedForetagshalsovard).then(
      function (value) {
        logg('OK - Kontakt med Företagshälsovård = ' + value);
      },
      function (reason) {
        callback('FEL, Kontakt med Företagshälsovård,' + reason);
      }
    );
    //Övrig rekommendation
    if (intyg.rekommendationer.ovrigt) {
      expect(field6a.ovrigt.getText()).to.eventually.equal(intyg.rekommendationer.ovrigt).then(
        function (value) {
          logg('OK - Övrig rekommendation= ' + value);
        },
        function (reason) {
          callback('FEL, Övrig rekommendation,' + reason);
        }
      );
    }

    // Kontrollera FÄLT 7 : Rehabilitering
    if (intyg.rekommendationer.arbetslivsinriktadRehab) {
      expect(intygPage.field7.text.getText()).to.eventually.equal(intyg.rekommendationer.arbetslivsinriktadRehab).then(
        function (value) {
          logg('OK - Rehabilitering aktuell = ' + value);
        },
        function (reason) {
          callback('FEL, Rehabilitering aktuell,' + reason);
        }
      );
    }

    //Kontrollera arbetsuppgifter
    if (intyg.arbete) {
      helpers.genericAssert(intyg.arbete.nuvarandeArbete.aktuellaArbetsuppgifter, 'nuvarandeArbetsuppgifter');
    }

    // Kontrollera aktivitetsbegränsning
    if (intyg.aktivitetsbegränsning) {
      helpers.genericAssert(intyg.aktivitetsBegransning, 'aktivitetsbegransning');
    }

    // Kontrollera FÄLT 8b : Nedsatt arbetsförmåga
    checkArbetsformaga(intyg.arbetsformaga, callback);

    // fält 9
    expect(intygPage.FMBprognos.getText()).to.eventually.equal(intyg.arbetsformagaFMB).then(
      function (value) {
        logg('OK - Arbetsformåga FMB prognos = ' + value);
      },
      function (reason) {
        callback('FEL, Arbetsformåga FMB prognos,' + reason);
      });

    // fält 10
    logg('TODO: FIXA FÄLT 10 CHECKAR');
    // if (!intyg.smittskydd) {
    //     expect(intygPage.prognosJ.getText()).to.eventually.equal(intyg.prognos.val).then(
    //         function(value) {
    //             logg('OK - Arbetsformåga prognos = ' + value);
    //         }, function(reason) {
    //             callback('FEL, Arbetsformåga prognos, ' + reason);
    //         });
    //     if (intyg.prognos.fortydligande) {
    //         expect(intygPage.prognosFortyd.getText()).to.eventually.equal(intyg.prognos.fortydligande).then(
    //             function(value) {
    //                 logg('OK - Arbetsformåga prognos förtydligande = ' + value);
    //             }, function(reason) {
    //                 callback('FEL, Arbetsformåga prognos förtydligande, ' + reason);
    //             });
    //     }

    //     //Kontrollera FÄLT13 : Övriga upplysningar
    //     expect(intygPage.field13.kommentar.getText()).to.eventually.contain(intyg.baserasPa.annat.text).then(function(value) {
    //         logg('OK - Övrig kommentar = ' + value);
    //     }, function(reason) {
    //         callback('FEL, Övrig kommentar,' + reason);
    //     });
    // }

    // Kontrollera FÄLT 11 : Resa till arbete med annat färdsätt
    expect(intygPage.field11.text.getText()).to.eventually.contain(helpers.boolTillJaNej(intyg.rekommendationer.resor)).then(function (value) {
      logg('OK - Resor till arbete med annat färdsätt = ' + value);
    }, function (reason) {
      callback('FEL, Resor till arbete med annat färdsätt,' + reason);
    });

    // Kontrollera FÄLT 12 : Kontakt önskas med FK
    var kontaktOnskas = helpers.boolTillJaNej(intyg.kontaktOnskasMedFK);
    expect(intygPage.field12.text.getText()).to.eventually.equal(kontaktOnskas).then(function (value) {
      logg('OK - Kontakt med FK = ' + value);
    }, function (reason) {
      callback('FEL, Kontakt med FK,' + reason);
    }).then(callback);

    // TBI! check förskrivarkod
    // expect(intygPage.forsKod.getText()).to.eventually.equal('0000000 - 1234567890123').then(function(value) {
    //     logg('OK - Forskrivarkod = ' + value);
    // }, function(reason) {
    //     callback('FEL, Forskrivarkod,' + reason);
    // }).then(callback);
  }
};
