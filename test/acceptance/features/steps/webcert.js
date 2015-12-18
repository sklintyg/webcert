/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/* global pages, browser, protractor, logg, intyg, should */

'use strict';

var fkUtkastPage = pages.intygpages.fk7263Utkast;
var fkIntygPage = pages.intygpages.fkIntyg;
var mysql = require('mysql');

module.exports = function () {

    this.Given(/^signerar FK7263-intyget$/, function (callback) {

        fkUtkastPage.whenSigneraButtonIsEnabled().then(function () {
            fkUtkastPage.signeraButtonClick();
        });

        browser.getCurrentUrl().then(function (text) {
            global.intyg.id = text.split('/').slice(-1)[0];
            global.intyg.id = global.intyg.id.replace('?signed', '');
        });

        callback();
    });


    this.Given(/^jag makulerar intyget$/, function (callback) {
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-1)[0];
            intyg.id = intyg.id.replace('?signed', '');
        });

        fkIntygPage.makulera.btn.click();
        fkIntygPage.makulera.dialogAterta.click();
        fkIntygPage.makulera.kvittensOKBtn.click()
        .then(callback);
    });

    this.Given(/^jag raderar utkastet$/, function (callback) {
        // browser.wait(EC.elementToBeClickable($('#makuleraBtn')), 10000);
        fkUtkastPage.radera.knapp.click();
        fkUtkastPage.radera.bekrafta.click()
        .then(callback);
    });

    this.Given(/^jag går tillbaka till start$/, function (callback) {
        element(by.id('tillbakaButton')).click()
        .then(callback);
    });

    this.Given(/^ska intyget visa varningen "([^"]*)"$/, function (arg1, callback) {
        expect(element(by.id('certificate-is-revoked-message-text')).getText())
            .to.eventually.contain(arg1).and.notify(callback);
    });

    this.Given(/^ska intyget "([^"]*)" med status "([^"]*)" inte synas mer$/, function (intyg, status, callback) {
      var qaTable = element(by.css('table.table-qa'));

      qaTable.all(by.cssContainingText('tr', intyg)).filter(function(elem, index) {
          return elem.getText().then(function(text) {
              return (text.indexOf(status) > -1);
          });
      }).then(function(filteredElements) {
          expect(element(by.cssContainingText('button', 'Kopiera')).isPresent()).to.become(false).and.notify(callback);
          callback();
      });
  });


  this.Given(/^kollar jag i databasen att intyget är borttaget$/, function (callback) {

    should.exist(process.env.DBUSR);
    should.exist(process.env.DBPW);

    var connection = mysql.createConnection({
      host  : '10.1.0.66',
      user  : process.env.DBUSR,
      password  : process.env.DBPW,
      database  : process.env.DATABASE_NAME
    });
    connection.connect();
    connection.query('SELECT COUNT(*) AS Counter FROM webcert_ip40.INTYG WHERE webcert_ip40.INTYG.INTYGS_ID = \"'+intyg.id+'\";', function(err, rows, fields){
      should.not.exist(err);
      logg('Från databas:');
      logg(JSON.stringify(rows));
      if(rows!==null){
        logg('Antal rader i databasen : ' + rows[0].Counter);
        var radix = 10; //for parseInt
        expect(parseInt(rows[0].Counter,radix)).to.equal(0);
        callback();
      }
    });
    connection.end();
});

};
