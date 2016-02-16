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
 * Created by BESA on 2015-11-17.
 */

var intygTemplates = {
  'fkMax': {
    personnr: '19121212-1212',
    patientNamn: 'Tolvan Tolvansson',
    //issuerId : '',
    issuer: 'IFV1239877878-104B',
    issued: '2013-04-01',
    validFrom: '2013-04-01',
    validTo: '2013-04-11',
    enhetId: 'IFV1239877878-1042',
    vardgivarId: 'IFV1239877878-1041',
    intygType: 'fk7263',
    intygId: 'fk-ptor-max',
    sent: false,
    revoked: false
  },
  'ts-bas': {
    //=================
    //=== Temp data ===
    //=================
    patientNamn: 'Tolvan Tolvansson',
    issuer: 'IFV1239877878-104B',
    issued: '2013-04-01',
    validFrom: '2013-04-01',
    validTo: '2013-04-11',
    enhetId: 'IFV1239877878-1042',
    vardgivarId: 'IFV1239877878-1041',
    intygType: 'ts-bas',
    intygId: 'fk-ptor-max',
    sent: false,
    revoked: false,
    //========================
    //=== END of temp data ===
    //========================

    grundData: {
      signeringsdatum: '2013-08-12T15:57:00.000',
      skapadAv: {
        personId: 'SE0000000000-1333',
        fullstandigtNamn: 'Doktor Thompson',
        specialiteter: ['SPECIALITET'],
        vardenhet: {
          enhetsid: 'SE0000000000-1337',
          enhetsnamn: 'Vårdenhet Väst',
          postadress: 'Enhetsvägen 12',
          postnummer: '54321',
          postort: 'Tumba',
          telefonnummer: '08-1337',
          vardgivare: {
            vardgivarid: 'SE0000000000-HAHAHHSAA',
            vardgivarnamn: 'Vårdgivarnamn'
          }
        },
        befattningar: []
      },
      patient: {
        personId: '19121212-1212',
        fullstandigtNamn: 'Johnny Appleseed',
        fornamn: 'Johnny',
        efternamn: 'Appleseed',
        postadress: 'Testvägen 12',
        postnummer: '12345',
        postort: 'Testort'
      }
    },
    vardkontakt: {
      typ: '5880005',
      idkontroll: 'PASS'
    },
    intygAvser: {
      korkortstyp: [{
        type: 'C1',
        selected: false
      }, {
        type: 'C1E',
        selected: false
      }, {
        type: 'C',
        selected: true
      }, {
        type: 'CE',
        selected: false
      }, {
        type: 'D1',
        selected: false
      }, {
        type: 'D1E',
        selected: false
      }, {
        type: 'D',
        selected: false
      }, {
        type: 'DE',
        selected: false
      }, {
        type: 'TAXI',
        selected: false
      }],
      syn: {
        synfaltsdefekter: false,
        nattblindhet: false,
        progressivOgonsjukdom: false,
        diplopi: false,
        nystagmus: false,
        hogerOga: {
          utanKorrektion: 0.0,
          kontaktlins: false
        },

        vansterOga: {
          utanKorrektion: 0.0,
          kontaktlins: false
        },

        binokulart: {
          utanKorrektion: 0.0
        }
      },

      horselBalans: {
        balansrubbningar: false
      },

      funktionsnedsattning: {
        funktionsnedsattning: false
      },

      hjartKarl: {
        hjartKarlSjukdom: false,
        hjarnskadaEfterTrauma: false,
        riskfaktorerStroke: false
      },

      diabetes: {
        harDiabetes: false
      },

      neurologi: {
        neurologiskSjukdom: false
      },

      medvetandestorning: {
        medvetandestorning: false
      },

      njurar: {
        nedsattNjurfunktion: false
      },

      kognitivt: {
        sviktandeKognitivFunktion: false
      },

      somnVakenhet: {
        teckenSomnstorningar: false
      },

      narkotikaLakemedel: {
        teckenMissbruk: false,
        foremalForVardinsats: false,
        lakarordineratLakemedelsbruk: false
      },

      psykiskt: {
        psykiskSjukdom: false
      },

      utvecklingsstorning: {
        psykiskUtvecklingsstorning: false,
        harSyndrom: false
      },

      sjukhusvard: {
        sjukhusEllerLakarkontakt: false
      },

      medicinering: {
        stadigvarandeMedicinering: false
      },

      bedomning: {
        korkortstyp: [{
          type: 'C1',
          selected: false
        }, {
          type: 'C1E',
          selected: false
        }, {
          type: 'C',
          selected: true
        }, {
          type: 'CE',
          selected: false
        }, {
          type: 'D1',
          selected: false
        }, {
          type: 'D1E',
          selected: false
        }, {
          type: 'D',
          selected: false
        }, {
          type: 'DE',
          selected: false
        }, {
          type: 'TAXI',
          selected: false
        }, {
          type: 'ANNAT',
          selected: false
        }]
      }

    }

  }
};

module.exports = intygTemplates;
