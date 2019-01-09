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
/* globals browser,logger, wcTestTools, pages */
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

const commonTools = require('common-testtools');
const loginHelper = require('./login.helpers.js');
const logInAsUserRole = loginHelper.logInAsUserRole;
const logInAsUser = loginHelper.logInAsUser;
const shuffle = wcTestTools.helpers.testdata.shuffle;
const users = commonTools.HSAusers;
const helpers = require('../helpers');



/*
 *	Stödfunktioner
 *
 */


function checkUserRole() {
    return performUserCheck('role');
}

function checkUserOrigin() {
    return performUserCheck('origin');
}

function performUserCheck(userconfig) {
    browser.ignoreSynchronization = true;
    let attributes;

    return helpers.getUrl('testability/user/' + userconfig + '/').then(function() {
        attributes = element(by.css('pre')).getText();
        return;
    }).then(function() {
        return browser.navigate().back();
    }).then(function() {
        browser.ignoreSynchronization = false;
        return helpers.largeDelay();
    }).then(function() {
        return attributes;
    });
}

/*
 *	Test steg
 *
 */

Given(/^att jag är inloggad som tandläkare$/, function() {
    var roll = 'Tandläkare';
    this.user = shuffle(users[roll])[0];

    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad som tandläkare på vårdenhet "([^"]*)"$/, function(ve) {
    var roll = 'Tandläkare';
    this.user = shuffle(users[roll])[0];
    this.user.enhetId = ve;
    return logInAsUserRole(this.user, roll);
});
Given(/^att jag är inloggad som läkare utan adress till enheten$/, function() {
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-ULLA',
        lakare: true,
        origin: 'NORMAL'
    };
    return logInAsUserRole(this.user, 'Läkare');
});


Given(/^att jag är inloggad som vårdadministratör$/, function() {
    var roll = 'Vårdadministratör';
    this.user = shuffle(users[roll])[0];
    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad som uthoppad vårdadministratör$/, function() {
    var roll = 'Vårdadministratör';
    this.user = shuffle(users[roll])[0];
    this.user.origin = 'UTHOPP';
    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad i uthoppsläge$/, function() {
    var roll = shuffle(['Läkare', 'Vårdadministratör', 'Tandläkare'])[0];
    this.user = shuffle(users[roll])[0];
    logger.info('Loggar in som uthoppad ' + roll);
    this.user.origin = 'UTHOPP';
    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad som djupintegrerad vårdadministratör$/, function() {
    var roll = 'Vårdadministratör';
    this.user = shuffle(users[roll])[0];
    this.user.origin = 'DJUPINTEGRATION';
    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad som läkare som inte accepterat kakor$/, function() {
    var roll = 'Läkare';
    this.user = shuffle(users[roll])[0];
    return logInAsUserRole(this.user, roll, true);
});

Given(/^att jag är inloggad som läkare(?: "([^"]*)")?$/, function(lakarNamn) {
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-107Q',
        lakare: true
    };

    if (lakarNamn) {
        this.user.enhetId = 'TSTNMT2321000156-107Q';
        this.user.forNamn = lakarNamn.split(' ')[0];
        this.user.efterNamn = lakarNamn.split(' ')[1];

        if (lakarNamn && lakarNamn === 'Karin Persson') {
            this.user.hsaId = 'TSTNMT2321000156-107T';

        } else if (lakarNamn && lakarNamn === 'Ingrid Nilsson Olsson') {
            this.user.hsaId = 'TSTNMT2321000156-105T';
            this.user.enhetId = 'TSTNMT2321000156-105P';
            this.user.forNamn = lakarNamn.split(' ')[0];
            this.user.efterNamn = lakarNamn.split(' ')[1] + ' ' + lakarNamn.split(' ')[2];

        } else if (lakarNamn && lakarNamn === 'Lennart Johansson Persson') {
            this.user.hsaId = 'TSTNMT2321000156-1016';
            this.user.enhetId = 'TSTNMT2321000156-1013';
            this.user.forNamn = lakarNamn.split(' ')[0];
            this.user.efterNamn = lakarNamn.split(' ')[1] + ' ' + lakarNamn.split(' ')[2];
        } else if (lakarNamn && lakarNamn === 'Karl Johansson') {
            this.user.hsaId = 'TSTNMT2321000156-1014';
            this.user.enhetId = 'TSTNMT2321000156-1013';
            this.user.forNamn = lakarNamn.split(' ')[0];
            this.user.efterNamn = lakarNamn.split(' ')[1];

        }
    }
    return logInAsUserRole(this.user, 'Läkare');
});

Given(/^att jag är inloggad som läkare utan angiven vårdenhet$/, function() {
    var roll = 'Läkare';
    this.user = shuffle(users[roll])[0];
    this.user.enhetId = '';
    return logInAsUserRole(this.user, roll);
});

Given(/^att jag är inloggad som läkare på (vårdenhet|underenhet) "([^"]*)"$/, function(enhettyp, ve) {
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: ve,
        origin: 'NORMAL',
        lakare: true
    };
    return logInAsUserRole(this.user, 'Läkare');
});

Given(/^att jag är inloggad som djupintegrerad läkare$/, function() {

    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-INT1',
        origin: 'DJUPINTEGRATION',
        lakare: true
    };
    return logInAsUserRole(this.user, 'Läkare');
});

Given(/^att jag är inloggad som djupintegrerad läkare på vårdenhet "([^"]*)"$/, function(enhetHsa) {
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: enhetHsa,
        forskrivarKod: '2481632',
        origin: 'DJUPINTEGRATION',
        lakare: true
    };
    return logInAsUserRole(this.user, 'Läkare');
});


Given(/^att jag är inloggad som uthoppsläkare$/, function() {
    this.user = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-107Q',
        origin: 'UTHOPP',
        lakare: true
    };
    return logInAsUserRole(this.user, 'Läkare');
});

Given(/^att jag loggar in som läkare utan medarbetaruppdrag$/, function() {
    this.user = {
        forNamn: 'Johnny',
        efterNamn: 'Drama',
        hsaId: 'TSTNMT2321000156-6789',
        enhetId: 'TSTNMT2321000156-107P'
    };
    let user = this.user;

    browser.ignoreSynchronization = true;
    return pages.welcome.get()
        .then(function() {
            return helpers.removeAlerts();
        })
        .then(function() {
            return helpers.mediumDelay();
        })
        .then(function() {
            return pages.welcome.loginByJSON(JSON.stringify(user));
        })
        .then(function() {
            return helpers.mediumDelay();
        });
});

Given(/^att jag är inloggad som( djupintegrerad)? läkare på (underenhet|vårdenhet) "([^"]*)" och inte har uppdrag på "([^"]*)"$/, function(selectedOrigin, typ, harEnhet, harInteEnhet) {
    logger.silly(selectedOrigin);
    var origin = 'NORMAL';
    if (selectedOrigin === ' djupintegrerad') {
        origin = 'DJUPINTEGRATION';
    }
    if (harInteEnhet === 'TSTNMT2321000156-107P') {
        this.user = {
            'forNamn': 'Karin',
            'efterNamn': 'Persson',
            'hsaId': 'TSTNMT2321000156-107T',
            'enhetId': harInteEnhet,
            'origin': origin
        };
    } else if (harInteEnhet === 'TSTNMT2321000156-107J') {
        this.user = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: harInteEnhet,
            'origin': origin,
            lakare: true
        };
    } else if (harInteEnhet === 'TSTNMT2321000156-INT2') {
        this.user = {
            forNamn: 'Jenny',
            efterNamn: 'Larsson',
            hsaId: 'TSTNMT2321000156-1084',
            enhetId: harInteEnhet,
            'origin': origin
        };
    } else {
        throw 'Användare som ska sakna medarbetaruppdrag på ' + harInteEnhet + ' hittades inte';
    }

    let user = this.user;
    //Kontrollera att inte medarbetaruppdrag finns på den andra enheten
    return logInAsUserRole(user, 'Läkare')
        .then(function() {
                throw ('Lyckades logga in med den enheten som inte ska fungera');
            },
            function(err) {
                logger.info('FICK FEL: ' + err.message);
                user.enhetId = harEnhet;
                return logInAsUserRole(user, 'Läkare');
            });
});



Then(/^ska jag ha rollen "([^"]*)"$/, function(roll) {
    return checkUserRole().then(function(value) {
        var re = /\[\"(.*)\"\]/;
        value = value.replace(re, '$1');
        return expect(value).to.equal(roll);
    });
});

When(/^jag ska ha origin "([^"]*)"/, function(origin) {
    return expect(checkUserOrigin()).to.eventually.be.equal(origin);
});
//För att se vilka felaktigheter som bör finnas i HSA se https://inera-certificate.atlassian.net/wiki/display/IT/Negativa+tester+HSA
When(/^jag loggar in med felaktig uppgift om telefonuppgift i HSAkatalogen$/, function() {
    var userObj = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: 'TSTNMT2321000156-107Q',
        lakare: true
    };
    return logInAsUser(userObj);
});
When(/^jag loggar in med felaktig uppgift om befattning i HSAkatalogen$/, function() {
    var userObj = {
        forNamn: 'Susanne Gustafsson',
        efterNamn: 'Ericsson',
        hsaId: 'TSTNMT2321000156-107W',
        enhetId: 'TSTNMT2321000156-107P'
    };
    return logInAsUser(userObj);
});

When(/^jag loggar in med felaktig uppgift om adress i HSAkatalogen$/, function() {
    var userObj = {
        forNamn: 'Karin',
        efterNamn: 'Persson',
        hsaId: 'TSTNMT2321000156-107T',
        enhetId: 'TSTNMT2321000156-1098'
    };
    return logInAsUser(userObj);
});
Then(/^ska jag vara inloggad som 'Läkare'$/, function() {
    var wcHeader = element(by.id('wcHeader'));
    return expect(wcHeader.getText()).to.eventually.contain('Läkare');
});
