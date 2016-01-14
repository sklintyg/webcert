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

/* global pages, browser, protractor, logg */

'use strict';
var soap = require('soap');


function stripTrailingSlash(str) {
    if(str.substr(-1) === '/') {
        return str.substr(0, str.length - 1);
    }
    return str;
}

function stringStartWith (string, prefix) {
    return string.slice(0, prefix.length) === prefix;
}

function getDraftBody(personId, doctorHsa, doctorName, unitHsa, unitName) {
    var body = '<urn1:CreateDraftCertificate ' +
        'xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:riv:itintegration:registry:1" ' +
        'xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:1" ' +
        'xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:1">' +
        '<urn1:utlatande>' + 
        '<urn1:typAvUtlatande code="fk7263" codeSystem="f6fb361a-e31d-48b8-8657-99b63912dd9b" ' +
        'codeSystemName="kv_utlåtandetyp_intyg" codeSystemVersion="?" displayName="Tjolahopp" ' +
        'originalText="?"/>' + 
        '<urn1:patient>' + 
        '<urn1:person-id root="1.2.752.129.2.1.3.1" extension="' + personId + '" identifierName="X"/>' + 
        '<urn1:fornamn>Lars</urn1:fornamn>' + 
        '<urn1:efternamn>Persson</urn1:efternamn>' + 
        '</urn1:patient>' + 
        '<urn1:skapadAv>' + 
        '<urn1:personal-id root="1.2.752.129.2.1.4.1" extension="' + doctorHsa + '" identifierName="Y"/>' + 
        '<urn1:fullstandigtNamn>' + doctorName + '</urn1:fullstandigtNamn>' + 
        '<urn1:enhet>' + 
        '<urn1:enhets-id root="1.2.752.129.2.1.4.1" extension="' + unitHsa + '" identifierName="Z"/>' + 
        '<urn1:enhetsnamn>' + unitName + '</urn1:enhetsnamn>' + 
        '</urn1:enhet>' + 
        '</urn1:skapadAv>' + 
        '</urn1:utlatande>' + 
        '</urn1:CreateDraftCertificate>';

    return body;
}

function assertDraftWithStatus(personId, intygsId, status, callback) {
    var mysql = require('mysql');

    var connection = mysql.createConnection({
        host  :     process.env.DATABASE_HOST,
        user  :     process.env.DATABASE_USER,
        password  : 'b4pelsin',
        database  : process.env.DATABASE_NAME
    });

    var databaseTable = process.env.DATABASE_NAME + '.INTYG';
    
    connection.connect();

    var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
        databaseTable + '.PATIENT_PERSONNUMMER="' + personId + '" AND ' +
        databaseTable + '.STATUS="' + status + '" AND ' +
        databaseTable + '.INTYGS_ID="' + intygsId + '" ;';

    console.log('QUERY: ' + query);
    
    var correctStatus = false;
    connection.query(query,
                     function(err, rows, fields) {
                         if (err) throw err;
                         
                         console.log('ROW0: ' + rows[0].Counter); 
                         if (rows[0].Counter !== 1) {
                             throw new Error('Bad status on on draft');
                         }
                     });

    connection.end();

    callback();
}

function assertNumberOfEvents(intygsId, event, numEvents, callback) {
    var mysql = require('mysql');

    console.log('Asserting number of events: ' + event);
    
    var connection = mysql.createConnection({
        host  :     process.env.DATABASE_HOST,
        user  :     process.env.DATABASE_USER,
        password  : 'b4pelsin',
        database  : process.env.DATABASE_NAME
    });

    var databaseTable = 'webcert_requests.requests';
    
    connection.connect();

    var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
        databaseTable + '.handelseKod = "' + event + '" AND ' +
        databaseTable + '.utlatandeExtension="' + intygsId + '" ;';

    console.log('QUERY: ' + query);

    connection.query(query,
                     function(err, rows, fields) {
                         if (err) throw err;
                         console.log(rows);
                         if (rows[0].Counter !== numEvents) {
                             console.log('Throwing error...');
                             throw new Error('Bad number of events: ' + rows[0].Counter);
                         } 
                     });    


    console.log('Asserting number of events done');
    connection.end();
    callback();
}


module.exports = function () {

    this.Given(/^att vårdsystemet skickat ett intygsutkast$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions

        global.person.id = '19121212-1212';
        
        var body = getDraftBody(global.person.id, 'IFV1239877878-1049', 'Jan Nilsson',
                                'IFV1239877878-1042', 'WebCert Enhet 1');

        var url = stripTrailingSlash(process.env.WEBCERT_URL) + ':8080/services/create-draft-certificate/v1.0?wsdl';
        url = url.replace('https', 'http');
        
        console.log('CreateDraftCertificate URL: ' + url);
        
        soap.createClient(url, function(err, client) {
            
            client.CreateDraftCertificate(body, function(err, result, body) {
                if (result.result.resultCode !== 'OK') {
                    throw new Error('CreateDraftCertificate failed!');
                }
                global.intyg.id = result['utlatande-id'].attributes.extension;
                console.log('CreateDraftCertificate Utlåtande ID: ' + global.intyg.id);
            });
        });
        
        callback();
    });
    
    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk$/, function (callback) {
        // FIXME: Temporärt för att aktivera djupintegration...
        //var url = process.env.WEBCERT_URL + 'api/testability/userrole/ROLE_LAKARE_DJUPINTEGRERAD';
        
        var url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id;

        console.log('Loggar in som djupintegrerad: ' + url);
        browser.get(url).then(callback);
    });

    this.Then(/^ska intygsutkastets status vara "([^"]*)"$/, function (statustext, callback) {
        expect(element(by.id('intyget-sparat-och-ej-komplett-meddelande')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });
    
    this.Given(/^när jag fyller i fältet "([^"]*)"$/, function (arg1, callback) {
        var fk7263Utkast = pages.intyg.fk['7263'].utkast;

        if (arg1 === 'Min undersökning av patienten') {
            console.log('Fyller i min undersökning av patienten...');
            fk7263Utkast.minUndersokning.sendKeys(protractor.Key.SPACE)
                .then(function () {
                    console.log('Fyller i diagnoskod...');
                    fk7263Utkast.diagnosKod.sendKeys('A000')
                })
                .then(function () {
                    console.log('Verifierar antalet events...');
                    assertNumberOfEvents(global.intyg.id, 'HAN1', 1, callback);
                });
        }
        else if (arg1 === 'Funktionsnedsättning') {
            fk7263Utkast.funktionsNedsattning.sendKeys('Halt och lytt')
                .then(function () {
                    console.log('Verifierar antalet events...');
                    assertNumberOfEvents(global.intyg.id, 'HAN1', 1, callback);
                });
        }
        else if (arg1 === 'Aktivitetsbegränsning') {
            fk7263Utkast.aktivitetsBegransning.sendKeys('Orkar inget')
                .then(function () {
                    console.log('Verifierar antalet events...');
                    assertNumberOfEvents(global.intyg.id, 'HAN1', 1, callback);
                });
        }
        else if (arg1 === 'Arbete') {
            fk7263Utkast.nuvarandeArbete.sendKeys('Stuveriarbetare')
                .then(function () {
                    console.log('Verifierar antalet events...');
                    assertNumberOfEvents(global.intyg.id, 'HAN1', 1, callback);
                });
        }
        else if (arg1 === 'Arbetsförmåga') {
            fk7263Utkast.faktiskTjanstgoring.sendKeys('40')
                .then(function () {
                    fk7263Utkast.nedsattMed25Checkbox.sendKeys(protractor.Key.SPACE)
                        .then(function () {
                            console.log('Verifierar antalet events...');
                            assertNumberOfEvents(global.intyg.id, 'HAN1', 1, callback);
                        });
                });
        } else {
            callback();
        }
    });

    this.Given(/^ska statusuppdatering "([^"]*)" skickas till vårdsystemet\."$/, function (arg1, callback) {
        assertNumberOfEvents(global.intyg.id, arg1, 1, callback);
    });

    this.Given(/^ska en CreateDraftUpdate skickas till vårdsystemet\.$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^är intygets status "([^"]*)"$/, function (arg1, callback) {
        // Write code here that turns the phrase above into concrete actions
        assertDraftWithStatus(global.person.id, global.intyg.id, arg1, callback);
    });

};
