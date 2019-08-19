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

/*global testdata, logger, pages, Promise, browser, protractor */
'use strict';
// var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkLusePage = pages.intyg.luse.intyg;
var pool = require('./dbActions').dbPool;
var EC = protractor.ExpectedConditions;

function sh(value) {
  return (value.search(/\s-\s/g) !== -1) ? value.split(/\s-\s/g)[0].replace('Ämne: ', '') : value.split(/\n/g)[0].replace('Ämne: ', '');
}

const commonTools = require('common-testtools');
const moveAndSendKeys = commonTools.protractorHelpers.moveAndSendKeys;

module.exports = {
  elementIsUsable: function(elm) {
    return elm.isDisplayed().then(function(val) {
      //OK
      return val;
    }, function(val) {
      // Fånga fel om elementet inte finns. Kontrollera om det finns.
      return elm.isPresent();
    });
  },
  getUrl: function(url) {
    var largeDelay = this.largeDelay;
    var hugeDelay = this.hugeDelay;
    var removeAlerts = this.removeAlerts;
    logger.info('Går till url:' + url);

    return browser.get(url).then(function() {
      return removeAlerts();
    }).then(function() {
      return largeDelay();
    }).then(function() {
      return browser.getCurrentUrl();
    }).then(function(currentUrl) {
      logger.silly('currentUrl: ' + currentUrl);
      return hugeDelay(); // Vänta på att intyg/utkast laddas in.
    });
  },
  removeAlerts: function() {
    return browser.wait(EC.alertIsPresent(), 1000).then(function() {
      return browser.switchTo().alert().accept();
    }, function() {
      // Ingen dialogruta hittad, allt är frid och fröjd.*/
      return;
    });
  },
  moveAndSendKeys: moveAndSendKeys,
  tinyDelay: function() {
    return browser.sleep(10);
  },
  smallDelay: function() {
    return browser.sleep(100);
  },
  mediumDelay: function() {
    return browser.sleep(500);
  },
  largeDelay: function() {
    return browser.sleep(1000);
  },
  hugeDelay: function() {
    return browser.sleep(3000);
  },
  pageReloadDelay: function() {
    return browser.sleep(5000);
  },
  enter: browser.actions().sendKeys(protractor.Key.ENTER),
  insertDashInPnr: function(pnrString) {
    if (pnrString.indexOf('-') >= 0) {
      return pnrString;
    }
    return pnrString.slice(0, 8) + '-' + pnrString.slice(8);
  },

  intygShortcode: {
    'LISJP': 'Läkarintyg för sjukpenning',
    'LUSE': 'Läkarutlåtande för sjukersättning',
    'LUAE_NA': 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
    'LUAE_FS': 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
    'FK7263': 'Läkarintyg FK 7263',
    'TSTRK1007': 'Transportstyrelsens läkarintyg högre körkortsbehörighet',
    'TSTRK1031': 'Transportstyrelsens läkarintyg diabetes',
    'DB': 'Dödsbevis',
    'DOI': 'Dödsorsaksintyg',
    'AF00213': 'Arbetsförmedlingens medicinska utlåtande'
  },
  internalIntygShortcode: {
    'lisjp': 'Läkarintyg för sjukpenning',
    'luse': 'Läkarutlåtande för sjukersättning',
    'luae_na': 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
    'luae_fs': 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
    'fk7263': 'Läkarintyg FK 7263',
    'ts-bas': 'Transportstyrelsens läkarintyg högre körkortsbehörighet',
    'ts-diabetes': 'Transportstyrelsens läkarintyg diabetes',
    'db': 'Dödsbevis',
    'doi': 'Dödsorsaksintyg',
    'af00213': 'Arbetsförmedlingens medicinska utlåtande'
  },

  //TODO Kan vi hantera detta bättre, Om HSA ändras så behöver vi uppdatera denna data vilket inte är optimalt
  // TSTNMT2321000156-ULLA saknar enhetadress i hsa, dvs behåll tidigare angivet enhetAdress objekt
  updateEnhetAdressForNewIntyg: function(user) {

    if (user.enhetId !== 'TSTNMT2321000156-ULLA') {
      user.enhetsAdress = {
        postnummer: '65340',
        postort: 'Karlstad',
        postadress: 'Bryggaregatan 11',
        telefon: '054100000'
      };

      if (user.enhetId === 'TSTNMT2321000156-107P') {
        user.enhetsAdress.telefon = '054121314';
      }

    }
  },
  generateIntygByType: function(intyg, patient, customFields) {
    if (intyg.typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
      return testdata.ts.bas.getRandom(intyg.id, patient);
    } else if (intyg.typ === 'Transportstyrelsens läkarintyg diabetes') {
      //TODO: V2
      //return testdata.ts.diabetes.v2.get(intyg.id, patient);
      return testdata.ts.diabetes.v3.get(intyg.id, patient);
    } else if (intyg.typ === 'Läkarintyg FK 7263') {
      return testdata.fk['7263'].getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
      return testdata.fk.LUSE.getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarintyg för sjukpenning') {
      return testdata.fk.LISJP.getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
      return testdata.fk.LUAE_NA.getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
      return testdata.fk.LUAE_FS.getRandom(intyg.id);
    } else if (intyg.typ === 'Dödsbevis') {
      return testdata.skv.db.getRandom(intyg.id);
    } else if (intyg.typ === 'Dödsorsaksintyg') {
      return testdata.soc.doi.getRandom(intyg.id, customFields);
    } else if (intyg.typ === 'Arbetsförmedlingens medicinska utlåtande') {
      return testdata.af.af00213.getRandom(intyg.id, customFields);
    } else {
      throw (intyg.typ + ': hittades inte i generateIntygByType');
    }
  },
  fetch: {
    kompletteringar: function() {
      logger.silly('Hämtar meddelande-id:n för kompletteringar');
      var panels = fkLusePage.qaPanels;
      var attributes = panels.kompletteringar.map(function(elm) {
        return Promise.all([
          elm.getAttribute('id'),
          elm.element(by.css('.fraga-status-header')).getText()
        ]);
      });
      return attributes;
    },
    administrativafragor: function() {
      logger.silly('Hämtar meddelande-id:n för administrativafragor');
      var panels = fkLusePage.qaPanels;
      var attributes = panels.kompletteringar.map(function(elm) {
        return Promise.all([
          elm.getAttribute('id'),
          elm.element(by.css('.fraga-status-header')).getText()
        ]);
      });
      return attributes;
    }
  },
  stripTrailingSlash: function(str) {
    if (str.substr(-1) === '/') {
      return str.substr(0, str.length - 1);
    }
    return str;
  },
  getIntFromTxt: function(txt) {
    var n;
    switch (txt) {
    case 'första':
      n = 0;
      break;
    case 'andra':
      n = 1;
      break;
    case 'tredje':
      n = 2;
      break;
    case 'fjärde':
      n = 3;
      break;
    case 'femte':
      n = 4;
      break;
    case 'sjätte':
      n = 5;
      break;
    case 'sjunde':
      n = 6;
      break;

    default:
      throw ('Lägg till fler getIntFromTxt alternativ');
    }
    return n;
  },
  getIntyg: function(intygsTyp, patient, makulerad) {
    var intygShortCode = this.getInternShortcode(intygsTyp);
    var insertDashInPnr = this.insertDashInPnr;
    return new Promise(function(resolve, reject) {
      return pool.getConnection().then(function(connection) {

        patient.id = insertDashInPnr(patient.id);

        var query = 'SELECT INTYGS_ID, ENHETS_ID, SKAPAD_AV_HSAID, STATUS, STATE';
        query += ' FROM ' + process.env.DATABASE_NAME + '.INTYG ';
        query += ' INNER JOIN ' + process.env.INTYGTJANST_DATABASE_NAME + '.CERTIFICATE_STATE ON ';
        query +=
            process.env.DATABASE_NAME + '.INTYG.INTYGS_ID = ' + process.env.INTYGTJANST_DATABASE_NAME + '.CERTIFICATE_STATE.CERTIFICATE_ID';
        query += ' WHERE INTYGS_TYP = "' + intygShortCode + '"';
        query += ' AND PATIENT_PERSONNUMMER = "' + patient.id + '"';
        query += ' AND STATUS NOT LIKE "DRAFT%" '; //Utkast är inte intressant för denna funktionen.
        if (makulerad !== true) { //Behöver efter makulerat intyg i IT p.g.a. Makulering kan ske i mina intyg.
          query += ' AND STATE != "CANCELLED"';
        }
        query += ' LIMIT 100';

        logger.info('Hämtar intyg från webcert på patient ' + patient.id);
        logger.silly('query: ');
        logger.silly(query);

        return connection.query(query,
            function(err, rows, fields) {
              connection.release();
              if (err) {
                throw (err);
              }
              resolve(rows);
            });
      });
    });
  },
  getUtkast: function(intygsTyp, patient) {
    var intygShortCode = this.getInternShortcode(intygsTyp);
    var insertDashInPnr = this.insertDashInPnr;
    return new Promise(function(resolve, reject) {
      return pool.getConnection().then(function(connection) {

        patient.id = insertDashInPnr(patient.id);

        var query = 'SELECT INTYGS_ID, ENHETS_ID, SKAPAD_AV_HSAID, STATUS';
        query += ' FROM ' + process.env.DATABASE_NAME + '.INTYG WHERE INTYGS_TYP = "' + intygShortCode + '"';
        query += ' AND PATIENT_PERSONNUMMER = "' + patient.id + '"';
        query += ' AND STATUS LIKE "DRAFT%" ';
        query += ' LIMIT 100';

        logger.info('Hämtar utkast från webcert på patient ' + patient.id);

        logger.silly('query: ');
        logger.silly(query);

        return connection.query(query,
            function(err, rows, fields) {
              connection.release();
              if (err) {
                throw (err);
              }
              resolve(rows);
            });
      });
    });
  },
  getIntygElementRow: function(intygstyp, status, cb) {
    element(by.id('current-list-noResults-unit')).isPresent().then(function(present) {
      if (present) {
        //Finns inga tidigare intyg!
        cb();
      } else {
        let qaTable = element(by.css('.wc-table-striped'));
        pool.getConnection().then(function(connection) {
          qaTable.all(by.cssContainingText('tr', status)).filter(function(elem, index) {
            return elem.all(by.css('td')).get(2).getText().then(function(text) {
              return (text === intygstyp);
            });
          })
          // Kontrollera att intyget ej är ersatt
          // Förhoppningsvis temporärt tills vi har någon label att gå på istället
          .filter(function(el, i) {
            return el.element(by.cssContainingText('button', 'Visa')).getAttribute('id')
            .then(function(id) {
              id = id.replace('showBtn-', '');
              var query = 'SELECT RELATION_KOD FROM ' + process.env.DATABASE_NAME + '.INTYG where RELATION_INTYG_ID = "' + id
                  + '"  AND RELATION_KOD = "ERSATT"';

              return new Promise(function(resolve, reject) {
                connection.query(query,
                    function(err, rows, fields) {
                      if (err) {
                        throw (err);
                      }
                      resolve(rows.length <= 0);
                    });

              });
            });
          })
          .then(function(filteredElements) {
            connection.release();
            cb(filteredElements[0]);
          });

        });

      }
    });
  },
  getAbbrev: function(value) {
    for (var key in this.intygShortcode) {
      if (this.intygShortcode[key] === value) {
        return key.toString();
      }
    }
    return null;
  },
  getInternShortcode: function(value) {
    for (var key in this.internalIntygShortcode) {
      if (this.internalIntygShortcode[key] === value) {
        return key.toString();
      }
    }
    return null;
  },
  isDBDOIIntyg: function(intygsType) {
    var regex = /(Dödsbevis|Dödsorsaksintyg)/g;
    return (intygsType) ? (intygsType.match(regex) ? true : false) : false;
  },
  isSMIIntyg: function(intygsType) {
    var regex = /(Läkarintyg för|Läkarutlåtande för)/g;
    return (intygsType) ? (intygsType.match(regex) ? true : false) : false;
  },
  isTSIntyg: function(intygsType) {
    return intygsType.indexOf('Transportstyrelsen') > -1;
  },
  isAFIntyg: function(intygsType) {
    return intygsType.indexOf('Arbetsförmedlingen') > -1;
  },
  isFK7263Intyg: function(intygsType) {
    return intygsType.indexOf('7263') > -1;
  },
  subjectCodes: {
    'Komplettering': 'KOMPLT',
    'Paminnelse': 'PAMINN',
    'Arbetstidsförläggning': 'ARBTID',
    'Avstämningsmöte': 'AVSTMN',
    'Kontakt': 'KONTKT',
    'Övrigt': 'OVRIGT'
  },
  subjectCodesFK7263: {
    'Avstämningsmöte': 'Avstamningsmote',
    'Kontakt': 'Kontakt',
    'Arbetstidsförläggning': 'Arbetstidsforlaggning',
    'Påminnelse': 'Paminnelse',
    'Komplettering': 'Komplettering_av_lakarintyg'
  },
  getSubjectFromCode: function(value, isFK7263) {
    var subjectCodes = this.subjectCodes;

    if (isFK7263) {
      subjectCodes = this.subjectCodesFK7263;
    }

    for (var key in subjectCodes) {
      if (subjectCodes[key] === value) {
        return key.toString();
      }
    }
    return null;
  },
  splitHeader: function(value) {
    return sh(value);
  },
  randomTextString: function() {
    var text = '';
    var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö0123456789';

    for (var i = 0; i < 16; i++) {
      text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
  },
  randomPageField: function(isSMIIntyg, intygAbbrev) {
    var index = Math.floor(Math.random() * 3);
    if (intygAbbrev === 'TSTRK1007') {
      return this.pageField.TSTRK1007[index];
    } else if (intygAbbrev === 'TSTRK1031') {
      return this.pageField.TSTRK1031[index];
    } else {
      return this.pageField[intygAbbrev][index];
    }

  },
  pageField: {
    'LISJP': ['aktivitetsbegransning', 'sysselsattning', 'funktionsnedsattning'],
    'LUSE': ['aktivitetsbegransning', 'sjukdomsforlopp', 'funktionsnedsattning'],
    'LUAE_NA': ['aktivitetsbegransning', 'sjukdomsforlopp', 'ovrigt'],
    'LUAE_FS': ['funktionsnedsattningDebut', 'funktionsnedsattningPaverkan', 'ovrigt'],
    'TSTRK1007': ['funktionsnedsattning', 'hjartKarlsjukdom', 'utanKorrektion'],
    'TSTRK1031': ['hypoglykemier', 'diabetesBehandling', 'specialist'],
    'DB': ['dodsdatum', 'dodsplats', 'identitetstyrkt'],
    'DOI': ['dodsdatum', 'dodsplats', 'identitetstyrkt'],
    'AF00213': ['funktionsnedsattning', 'utredningBehandling', 'arbetetsPaverkan']
  },
  getUserObj: function(userKey) {
    return this.userObj[userKey];
  },

  diffDays: function(dateFrom, dateTo) {
    var fromEl = dateFrom.split('-');
    var toEl = dateTo.split('-');

    var oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds

    var firstDate = new Date(fromEl[0], fromEl[1], fromEl[2]);
    var secondDate = new Date(toEl[0], toEl[1], toEl[2]);

    return Math.round(Math.abs((firstDate.getTime() - secondDate.getTime()) / (oneDay)));
  },
  injectConsoleTracing: function() {
    return browser.executeScript(
        'window.errs=typeof(errs)=="undefined" ? [] : window.errs; window.console.error = function(msg){window.errs.push(msg); }; ');
  },

  intygURL: function(intyg) {
    var url = '';
    if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
      url = process.env.WEBCERT_URL + '#/intyg/luse/1.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Läkarintyg för sjukpenning') {
      url = process.env.WEBCERT_URL + '#/intyg/lisjp/1.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
      url = process.env.WEBCERT_URL + '#/intyg/luae_fs/1.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
      url = process.env.WEBCERT_URL + '#/intyg/luae_na/1.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Läkarintyg FK 7263') {
      url = process.env.WEBCERT_URL + '#/intyg/fk7263/1.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Transportstyrelsens läkarintyg diabetes') {
      //Old: utan versionshantering
      //url = process.env.WEBCERT_URL + '#/intyg/ts-diabetes/' + intyg.id + '/';

      //TODO: V2 och V3
      //url = process.env.WEBCERT_URL + '#/intyg/ts-diabetes/2.8/' + intyg.id + '/';
      url = process.env.WEBCERT_URL + '#/intyg/ts-diabetes/3.0/' + intyg.id + '/';
    } else if (intyg.typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
      url = process.env.WEBCERT_URL + '#/intyg/ts-bas/6.8/' + intyg.id + '/';
    } else if (intyg.typ === 'Arbetsförmedlingens medicinska utlåtande') {
      url = process.env.WEBCERT_URL + '#/intyg/af00213/1.0/' + intyg.id + '/';
    }
    return url;
  },
  uniqueItemsInArray: function(value, index, self) {
    return self.indexOf(value) === index;
  },
  getCurrentDate: function() {
    return new Date().toISOString().split('T')[0];
  }
};
