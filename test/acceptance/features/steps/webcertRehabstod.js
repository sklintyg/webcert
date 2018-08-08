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

/*global browser, logger, pages, wcTestTools, Promise, protractor */
'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)

/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

var helpers = require('./helpers');
var fillIn = require('./fillIn/').fillIn;
var loginHelper = require('./inloggning/login.helpers.js');
var loginHelperRehabstod = require('./inloggning/login.helpers.rehabstod.js');
var logInAsUserRole = loginHelper.logInAsUserRole;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var logInAsUserRoleRehabstod = loginHelperRehabstod.logInAsUserRoleRehabstod;
var testdataHelpers = wcTestTools.helpers.testdata;


/*
 *	Stödfunktioner
 *
 */

// match personNr, startDate, endDate, noOfIntyg
var TABLEROW_REGEX = /.*(\d{8}\-\d{4}).*(\d{4}\-\d{2}\-\d{2})\s(\d{4}\-\d{2}\-\d{2}).*(dagar \d{1,3}).*/g;
var TABLEROW_SUBST = '\$1, \$2, \$3, \$4';

var getObjFromList;


//TODO ändra från "user" till "patient" i funktioner för readablity
function createUser(id) {
    var date = new Date();
    var startDate = createDateString(date);
    var endDate = createDateString(date, 30);
    return createObj('19121212-1212,' + startDate + ',' + endDate + ',0');
}

function createObj(row) {
    logger.silly('row:');
    logger.silly(row);
    var elements = row.split(',');
    var ssn = elements[0].trim();
    logger.silly('ssn: ' + ssn);
    var startDate = elements[1].trim();
    logger.silly('startDate: ' + startDate);
    var endDate = elements[2].trim();
    logger.silly('endDate: ' + endDate);
    var noOfIntyg = extractDigit(elements[3]);
    logger.silly('noOfIntyg: ' + noOfIntyg);
    var obj = {};
    obj.ssn = ssn;
    obj.startDate = startDate;
    obj.endDate = endDate;
    obj.noOfIntyg = noOfIntyg;
    return obj;
}

function extractDigit(intyg) {
    var regex = /dagar (\d{1,2})/g;
    var subst = '\$1';
    var result = intyg.replace(regex, subst).trim();

    return parseInt(result, 10);
}

function gotoPatient(patient, user) { //förutsätter  att personen finns i PU-tjänsten
    if (user.origin !== 'DJUPINTEGRATION') {
        element(by.id('menu-skrivintyg')).click().then(function() {
            return helpers.pageReloadDelay();
        });
    }
    return sokSkrivIntygPage.selectPersonnummer(patient.id).then(function() {
            return helpers.pageReloadDelay();
        })
        .then(function() {
            logger.info('Går in på patient ' + patient.id);
            //Patientuppgifter visas
            var patientUppgifter = sokSkrivIntygPage.patientNamn;
            return expect(patientUppgifter.getText()).to.eventually.contain(helpers.insertDashInPnr(patient.id)).then(function() {
                return helpers.smallDelay();
            });
        });
}

/*function sattNySjukskrivningsPeriod(intyg) {
    var newStartDate = createDateString(global.rehabstod.user.endDate, 3, true);
    intyg.arbetsformaga.nedsattMed25.from = newStartDate;
    intyg.arbetsformaga.nedsattMed25.tom = createDateString(newStartDate, 4, false);
    intyg.arbetsformaga.nedsattMed50.from = createDateString(newStartDate, 5, false);
    intyg.arbetsformaga.nedsattMed50.tom = createDateString(newStartDate, 12, false);
    intyg.arbetsformaga.nedsattMed75.from = createDateString(newStartDate, 13, false);
    intyg.arbetsformaga.nedsattMed75.tom = createDateString(newStartDate, 20, false);
    intyg.arbetsformaga.nedsattMed100.from = createDateString(newStartDate, 21, false);
    intyg.arbetsformaga.nedsattMed100.tom = createDateString(newStartDate, 28, false);
}*/

function createDateString(date, daysToAdd, subtraction) {
    var tmpDate = new Date(date);
    if (daysToAdd) {
        var modifiedTmpDate = (subtraction) ? new Date(tmpDate).getDate() - daysToAdd : new Date(tmpDate).getDate() + daysToAdd;
        tmpDate.setDate(modifiedTmpDate);
    }
    var newDateString = tmpDate.getFullYear() + '-' + ('0' + (tmpDate.getMonth() + 1)).slice(-2) + '-' + ('0' + tmpDate.getDate()).slice(-2);
    return newDateString;
}

function createUserArr(getObjFromList) {
    var personArr = [];
    return element.all(by.css('.rhs-table-row')).getText().then(function(tableRows) {
        tableRows.forEach(function(row) {
            if (getObjFromList) {
                var savedObj = getObjFromList();
                var newObj = createObj(row.replace(TABLEROW_REGEX, TABLEROW_SUBST));
                if (savedObj.ssn === newObj.ssn) {
                    personArr.push(newObj);
                }
                logger.silly(newObj);
            } else {
                var obj = createObj(row.replace(TABLEROW_REGEX, TABLEROW_SUBST));
                personArr.push(obj);
                logger.silly(obj);
            }
        });
    }).then(function() {
        return Promise.resolve(personArr);
    });
}

function objList(arr) {
    return function() {
        return arr.find(findSsn);
    };
}

function findSsn(obj) {
    return obj.ssn === global.rehabstod.user.ssn;
}

/*
 *	Test steg
 *
 */

Given(/^jag går in på Rehabstöd$/, function() {
    var url = process.env.REHABSTOD_URL + 'welcome.html';
    return helpers.getUrl(url).then(function() {
        logger.info('Går till url: ' + url);
    });
});

Given(/^jag väljer enhet "([^"]*)"$/, function(enhet) {
    let elementId = 'rhs-vardenhet-selector-select-active-unit-' + enhet + '-link';
    let user = this.user;
    let headerboxUser = element(by.css('.header-user'));

    return element(by.id(elementId)).click().then(function() {
        return browser.sleep(2000);
    }).then(function() {
        return headerboxUser.getText();
    }).then(function(txt) {
        if (user.roleName !== 'rehabkoordinator') {
            expect(txt).to.contain(user.roleName);
        }
        expect(txt).to.contain(user.forNamn);
        expect(txt).to.contain(user.efterNamn);
    }).then(function() {
        return element(by.id('verksamhetsNameLabel')).getText();
    }).then(function(txt) {
        logger.info('Inloggad på: ');
        logger.info(txt);
    });
});

When(/^jag går till pågående sjukfall i Rehabstöd$/, function() {
    return element(by.id('navbar-link-sjukfall')).click().then(function() {
        return element(by.id('rhs-pdlconsent-modal-checkbox-label')).isPresent().then(function(isPresent) {
            if (isPresent) {
                return element(by.id('rhs-pdlconsent-modal-give-consent-checkbox')).sendKeys(protractor.Key.SPACE).then(function() {
                    return element(by.id('rhs-pdlconsent-modal-give-consent-btn')).sendKeys(protractor.Key.SPACE);
                });
            }
        });
    });
});
When(/^ska jag inte se patientens personnummer bland pågående sjukfall$/, function() {
    let patient = this.patient;
    return element.all(by.css('.rhs-table-row')).getText().then(function(tableRows) {
        return tableRows.forEach(function(row) {
            row = row.replace('-', '');

            logger.info('letar efter "' + patient.id + '" i :');
            logger.debug(row);

            return expect(row).to.not.contain(patient.id);
        });



    });
});


Given(/^jag söker efter slumpvald patient och sparar antal intyg$/, function(callback) {
    createUserArr().then(function(personArr) {
        getObjFromList = objList(personArr);
        var usrObj = testdataHelpers.shuffle(personArr)[0];
        global.rehabstod = {};
        if (usrObj) {
            global.rehabstod.user = usrObj;
        } else {
            global.rehabstod.user = createUser();
        }
        logger.info('Saved rehab user ( ssn: ' + global.rehabstod.user.ssn + ', noOfIntyg: ' + global.rehabstod.user.noOfIntyg + '). Saved for next steps.');
    }).then(callback);
});

Given(/^jag går in på en patient som sparats från Rehabstöd$/, function() {
    this.patient = {
        id: global.rehabstod.user.ssn.replace('-', '')
    };

    return gotoPatient(this.patient, this.user);
});

Given(/^jag är inloggad som läkare i Rehabstöd$/, function() {
    // Setting rehabstod to new bas url
    browser.baseUrl = process.env.REHABSTOD_URL;
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-107P'
    };

    return logInAsUserRoleRehabstod(this.user, 'Läkare', true);
});

Given(/^jag är inloggad som rehabkoordinator$/, function() {
    // Setting rehabstod to new bas url
    browser.baseUrl = process.env.REHABSTOD_URL;
    this.user = {
        forNamn: 'Automatkoordinator',
        efterNamn: 'Rehab',
        hsaId: 'TSTNMT2321000156-REKO',
        enhetId: 'TSTNMT2321000156-107Q'
    };
    return logInAsUserRoleRehabstod(this.user, 'rehabkoordinator', true);
});

Given(/^jag är inloggad som läkare i Webcert med enhet "([^"]*)"$/, function(enhetsId) {
    // Setting webcert to new bas url
    browser.baseUrl = process.env.WEBCERT_URL;
    var userObj = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: enhetsId,
        lakare: true
    };

    return logInAsUserRole(userObj, 'Läkare', true);
});

Given(/^jag fyller i ett "([^"]*)" intyg som inte är smitta med ny sjukskrivningsperiod$/, function(intygsTyp) {
    this.intyg.typ = intygsTyp;
    global.rehabstod.user.intygId = this.intyg.id;
    //sattNySjukskrivningsPeriod(this.intyg);
    logger.info(this.intyg);
    return fillIn(this);
});

Given(/^ska antalet intyg ökat med (\d+) på patient som sparats från Rehabstöd$/, function(antal) {
    return createUserArr(getObjFromList).then(function(personArr) {
        logger.info('Rehabpatient: ( ssn: ' + global.rehabstod.user.ssn + ', Antal intyg: ' + personArr[0].noOfIntyg + ').');

        logger.info('Förväntar oss att global.rehabstod.user.noOfIntyg + ' + antal + ' => ');
        logger.info(global.rehabstod.user.noOfIntyg + parseInt(antal, 10));
        logger.info('Ska vara lika mycket som personArr[0].noOfIntyg => ');
        logger.info(personArr[0].noOfIntyg);

        return expect(global.rehabstod.user.noOfIntyg + parseInt(antal, 10)).to.equal(personArr[0].noOfIntyg);
    });
});

Given(/^jag går in på intyget som tidigare skapats$/, function() {
    var url;
    if (global.rehabstod) {
        url = process.env.WEBCERT_URL + '#/intyg/lisjp/' + this.intyg.id + '/';
    } else if (global.statistik) {
        url = process.env.WEBCERT_URL + '#/intyg/lisjp/' + global.statistik.intygsId + '/';
    }

    return helpers.getUrl(url);
});
