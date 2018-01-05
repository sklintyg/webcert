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
/* globals browser,logger, wcTestTools,commonTools, pages */
'use strict';
var loginHelper = require('./login.helpers.js');
var logInAsUserRole = loginHelper.logInAsUserRole;
var logInAsUser = loginHelper.logInAsUser;
var shuffle = wcTestTools.helpers.testdata.shuffle;
var users = commonTools.HSAusers;
var helpers = require('../helpers');
let srsdata = require('../srsdata.js');

module.exports = function() {

    this.Given(/^att jag är inloggad som tandläkare$/, function() {
        var roll = 'Tandläkare';
        return logInAsUserRole(shuffle(users[roll])[0], roll);
    });

    this.Given(/^att jag är inloggad som tandläkare på vårdenhet "([^"]*)"$/, function(ve) {
        var roll = 'Tandläkare';
        var user = shuffle(users[roll])[0];
        user.enhetId = ve;
        return logInAsUserRole(user, roll);
    });
    this.Given(/^att jag är inloggad som läkare utan adress till enheten$/, function() {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-ULLA',
            lakare: 'true',
            origin: 'NORMAL'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });


    this.Given(/^att jag är inloggad som vårdadministratör$/, function() {
        var roll = 'Vårdadministratör';
        return logInAsUserRole(shuffle(users[roll])[0], roll);
    });

    this.Given(/^att jag är inloggad som uthoppad vårdadministratör$/, function() {
        var roll = 'Vårdadministratör';
        var user = shuffle(users[roll])[0];
        user.origin = 'UTHOPP';
        return logInAsUserRole(user, roll);
    });

    this.Given(/^att jag är inloggad i uthoppsläge$/, function() {
        var roll = shuffle(['Läkare', 'Vårdadministratör', 'Tandläkare'])[0];
        var user = shuffle(users[roll])[0];
        logger.info('Loggar in som uthoppad ' + roll);
        user.origin = 'UTHOPP';
        return logInAsUserRole(user, roll);
    });

    this.Given(/^att jag är inloggad som djupintegrerad vårdadministratör$/, function() {
        var roll = 'Vårdadministratör';
        var user = shuffle(users[roll])[0];
        user.origin = 'DJUPINTEGRATION';
        return logInAsUserRole(user, roll);
    });

    this.Given(/^att jag är inloggad som läkare som inte accepterat kakor$/, function() {
        var roll = 'Läkare';
        return logInAsUserRole(shuffle(users[roll])[0], roll, true);
    });

    this.Given(/^att jag är inloggad som läkare( "([^"]*)")?$/, function(hasLakarnamn, lakarNamn) {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-107Q'
        };

        if (lakarNamn) {
            userObj.enhetId = 'TSTNMT2321000156-107Q';
            userObj.forNamn = lakarNamn.split(' ')[0];
            userObj.efterNamn = lakarNamn.split(' ')[1];

            if (lakarNamn && lakarNamn === 'Karin Persson') {
                userObj.hsaId = 'TSTNMT2321000156-107T';

            }
        }

        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är inloggad som läkare utan angiven vårdenhet$/, function() {
        var roll = 'Läkare';
        var user = shuffle(users[roll])[0];
        user.enhetId = '';
        return logInAsUserRole(user, roll);
    });

    this.Given(/^att jag är inloggad som läkare på (vårdenhet|underenhet) "([^"]*)"$/, function(enhettyp, ve) {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: ve,
            'origin': 'NORMAL'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare$/, function() {

        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-INT1',
            origin: 'DJUPINTEGRATION'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare på vårdenhet "([^"]*)"$/, function(enhetHsa) {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: enhetHsa,
            forskrivarKod: '2481632',
            origin: 'DJUPINTEGRATION'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är djupintegrerat inloggad som läkare på vårdenhet "(med SRS|utan SRS)"$/, function(srsStatus) {
        var userObj = srsdata.inloggningar[srsStatus];
        return logInAsUserRole(userObj, 'Läkare');
    });


    this.Given(/^att jag är inloggad som uthoppsläkare$/, function() {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-107Q',
            origin: 'UTHOPP'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag loggar in som läkare utan medarbetaruppdrag$/, function() {
        var userObj = {
            forNamn: 'Johnny',
            efterNamn: 'Drama',
            hsaId: 'TSTNMT2321000156-6789',
            enhetId: 'TSTNMT2321000156-107P'
        };
        browser.ignoreSynchronization = true;
        return pages.welcome.get()
            .then(function() {
                return helpers.mediumDelay();
            })
            .then(function() {
                return pages.welcome.loginByJSON(JSON.stringify(userObj));
            })
            .then(function() {
                return helpers.mediumDelay();
            });
    });

    this.Given(/^att jag är inloggad som( djupintegrerad)? läkare på (underenhet|vårdenhet) "([^"]*)" och inte har uppdrag på "([^"]*)"$/, function(selectedOrigin, typ, harEnhet, harInteEnhet) {
        console.log(selectedOrigin);
        var origin = 'NORMAL';
        if (selectedOrigin === ' djupintegrerad') {
            origin = 'DJUPINTEGRATION';
        }
        var userObj;
        if (harInteEnhet === 'TSTNMT2321000156-107P') {
            userObj = {
                'forNamn': 'Karin',
                'efterNamn': 'Persson',
                'hsaId': 'TSTNMT2321000156-107T',
                'enhetId': harInteEnhet,
                'origin': origin
            };
        } else if (harInteEnhet === 'TSTNMT2321000156-107J') {
            userObj = {
                forNamn: 'Johan',
                efterNamn: 'Johansson',
                hsaId: 'TSTNMT2321000156-107V',
                enhetId: harInteEnhet,
                'origin': origin
            };
        } else if (harInteEnhet === 'TSTNMT2321000156-INT2') {
            userObj = {
                forNamn: 'Jenny',
                efterNamn: 'Larsson',
                hsaId: 'TSTNMT2321000156-1084',
                enhetId: harInteEnhet,
                'origin': origin
            };
        } else {
            throw 'Användare för detta saknas';
        }

        //Kontrollera att inte medarbetaruppdrag finns på den andra enheten
        return logInAsUserRole(userObj, 'Läkare')
            .then(function() {
                    throw ('Lyckades logga in med den enheten som inte ska fungera');
                },
                function(err) {
                    logger.info('FICK FEL: ' + err.message);
                    userObj.enhetId = harEnhet;
                    return logInAsUserRole(userObj, 'Läkare');
                });
    });



    this.Given(/^ska jag ha rollen "([^"]*)"$/, function(roll, callback) {
        checkUserRole().then(function(value) {
            var re = /\[\"(.*)\"\]/;
            value = value.replace(re, '$1');
            expect(value).to.equal(roll);
            callback();
        });
    });

    this.Given(/^jag ska ha origin "([^"]*)"/, function(origin) {
        return expect(checkUserOrigin()).to.eventually.be.equal(origin);
    });
    //För att se vilka felaktigheter som bör finnas i HSA se https://inera-certificate.atlassian.net/wiki/display/IT/Negativa+tester+HSA
    this.Given(/^jag loggar in med felaktig uppgift om telefonuppgift i HSAkatalogen$/, function() {
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-107Q'
        };
        return logInAsUser(userObj);
    });
    this.Given(/^jag loggar in med felaktig uppgift om befattning i HSAkatalogen$/, function() {
        var userObj = {
            forNamn: 'Susanne Gustafsson',
            efterNamn: 'Ericsson',
            hsaId: 'TSTNMT2321000156-107W',
            enhetId: 'TSTNMT2321000156-107P'
        };
        return logInAsUser(userObj);
    });

    this.Given(/^jag loggar in med felaktig uppgift om adress i HSAkatalogen$/, function() {
        var userObj = {
            forNamn: 'Karin',
            efterNamn: 'Persson',
            hsaId: 'TSTNMT2321000156-107T',
            enhetId: 'TSTNMT2321000156-1098'
        };
        return logInAsUser(userObj);
    });
    this.Given(/^ska jag vara inloggad som 'Läkare'$/, function() {
        var wcHeader = element(by.id('wcHeader'));
        return expect(wcHeader.getText()).to.eventually.contain('Läkare');
    });

};

function checkUserRole() {
    return performUserCheck('role');
}

function checkUserOrigin() {
    return performUserCheck('origin');
}

function performUserCheck(userconfig) {
    browser.ignoreSynchronization = true;
    browser.get('testability/user/' + userconfig + '/');
    var attribute = element(by.css('pre')).getText();
    browser.navigate().back();
    browser.sleep(1000);
    browser.ignoreSynchronization = false;
    return attribute;
}
