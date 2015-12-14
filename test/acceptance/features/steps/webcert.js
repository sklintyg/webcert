/* global pages, browser, protractor, logg, intyg */

'use strict';

var fkUtkastPage = pages.intygpages.fk7263Utkast;
var fkIntygPage = pages.intygpages.fkIntyg;

module.exports = function () {

    this.Given(/^fyller i alla nödvändiga fält för intyget$/, function (callback) {
        fkUtkastPage.smittskyddCheckboxClick();
        fkUtkastPage.nedsattMed25CheckboxClick();
        callback();
    });

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
        fkIntygPage.radera.knapp.click();
        fkIntygPage.radera.radera.click()
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

  this.Given(/^kollar i databasen att "([^"]*)" är borttagen$/, function (pers_nummer, callback) {
    var mysql = require('mysql');

    var connection = mysql.createConnection({
      host  : "10.1.0.66",
      user  : "nmt_test",
      password  : "Saetter01",
      database  : "webcert_ip40"
    });
    connection.connect();
    connection.query("SELECT COUNT(*) AS Counter FROM webcert_ip40.INTYG WHERE webcert_ip40.INTYG.PATIENT_PERSONNUMMER = \""+pers_nummer+"\" AND webcert_ip40.INTYG.STATUS = \"DRAFT_INCOMPLETE\";", function(err, rows, fields) {
      if(rows != null)
        expect(rows[0].Counter).should.equal('0').then(callback);
    //     if(rows[0].Counter == '0'){
    //       callback();
    //     }
    //   else if (rows[0].Counter != '0')
    //     callback.fail();
    // }
    // else
    //   throw err;
  });
    connection.end();
});

};
