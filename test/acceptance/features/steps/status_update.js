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

/* global pages, browser, protractor, intyg, logg */

'use strict';
var soap = require('soap');
var sleep = require('sleep');
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var db = require('./db.js');

function stripTrailingSlash(str) {
    if(str.substr(-1) === '/') {
        return str.substr(0, str.length - 1);
    }
    return str;
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

function getQuestionBody(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId, amne) {
    // Komplettering_av_lakarintyg, Makulering_av_lakarintyg, Avstamningsmote, Kontakt, Arbetstidsforlaggning, Paminnelse, Ovrigt

    var body ='<urn:ReceiveMedicalCertificateQuestion ' +
        'xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" ' + 
        'xmlns:add="http://www.w3.org/2005/08/addressing"  ' + 
        'xmlns:urn="urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1"  ' + 
        'xmlns:urn1="urn:riv:insuranceprocess:healthreporting:medcertqa:1"  ' + 
        'xmlns:urn2="urn:riv:insuranceprocess:healthreporting:2"         ' + 
        '>' + 
        '  <urn:Question>' + 
        '    <urn:fkReferens-id>8e048a89</urn:fkReferens-id>' + 
        '    <urn:amne>' + amne + '</urn:amne>' + 
        '    <urn:fraga>' + 
        '      <urn1:meddelandeText>Fråga RT simulerar FK</urn1:meddelandeText>' + 
        '      <urn1:signeringsTidpunkt>2014-11-28T10:18:10</urn1:signeringsTidpunkt>' + 
        '    </urn:fraga>' + 
        '    <urn:avsantTidpunkt>2014-11-28T10:18:10</urn:avsantTidpunkt>' + 
        '    <urn:fkKontaktInfo>' + 
        '      <urn1:kontakt>NMT RT</urn1:kontakt>' + 
        '    </urn:fkKontaktInfo>' + 
        '    <urn:adressVard>' + 
        '      <urn1:hosPersonal>' + 
        '        <urn2:personal-id root="1.2.752.129.2.1.4.1" extension="' + doctorHsa + '"/>' + 
        '        <urn2:fullstandigtNamn>' + doctorName + '</urn2:fullstandigtNamn>' + 
        '        <urn2:enhet>' + 
        '          <urn2:enhets-id extension="' + unitHsa + '" root="1.2.752.129.2.1.4.1"/>' + 
        '          <urn2:enhetsnamn>unitName</urn2:enhetsnamn>' + 
        '          <urn2:vardgivare>' + 
        '            <urn2:vardgivare-id extension="' + unitHsa + '" root="1.2.752.129.2.1.4.1"/>' + 
        '            <urn2:vardgivarnamn>NMT</urn2:vardgivarnamn>' + 
        '          </urn2:vardgivare>' + 
        '        </urn2:enhet>' + 
        '      </urn1:hosPersonal>' + 
        '    </urn:adressVard>' + 
        '    <urn:fkMeddelanderubrik>Avstämningsmöte</urn:fkMeddelanderubrik>' + 
        '    <urn:fkKomplettering>' + 
        '      <urn1:falt>Test</urn1:falt>' + 
        '      <urn1:text>Testfråga</urn1:text>' + 
        '    </urn:fkKomplettering>' + 
        '    <urn:fkSistaDatumForSvar>2015-01-28</urn:fkSistaDatumForSvar>' + 
        '    <urn:lakarutlatande>' + 
        '      <urn1:lakarutlatande-id>' + intygsId + '</urn1:lakarutlatande-id>' + 
        '      <urn1:signeringsTidpunkt>2014-11-28T10:18:10</urn1:signeringsTidpunkt>' + 
        '      <urn1:patient>' + 
        '        <urn2:person-id extension="' + personId + '" root="1.2.752.129.2.1.3.1"/>' + 
        '        <urn2:fullstandigtNamn>Lars Persson</urn2:fullstandigtNamn>' + 
        '      </urn1:patient>' + 
        '    </urn:lakarutlatande>' + 
        '  </urn:Question>' + 
        '</urn:ReceiveMedicalCertificateQuestion>'; 

    return body;
}

function getAnswerBody(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId, fragaId) {
    var body = '<urn:ReceiveMedicalCertificateAnswer' +
        '    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"' +
        '    xmlns:add="http://www.w3.org/2005/08/addressing"' +
        '    xmlns:urn="urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1"' +
        '    xmlns:urn1="urn:riv:insuranceprocess:healthreporting:medcertqa:1"' +
        '    xmlns:urn2="urn:riv:insuranceprocess:healthreporting:2">' +
        '  <urn:Answer>' +
        '    <urn:vardReferens-id>' + fragaId + '</urn:vardReferens-id>' +
        '    <urn:fkReferens-id>626251</urn:fkReferens-id>' +
        '    <urn:amne>Arbetstidsforlaggning</urn:amne>' +
        '    <urn:fraga>' +
        '      <urn1:meddelandeText>Fråga TF 1.5</urn1:meddelandeText>' +
        '      <urn1:signeringsTidpunkt>2015-08-28T09:05:21</urn1:signeringsTidpunkt>' +
        '    </urn:fraga>' +
        '    <urn:svar>' +
        '      <urn1:meddelandeText>Här kommer ett svar!, tf1.5</urn1:meddelandeText>' +
        '      <urn1:signeringsTidpunkt>2015-08-28T09:05:21</urn1:signeringsTidpunkt>' +
        '    </urn:svar>' +
        '    <urn:avsantTidpunkt>2015-08-28T09:05:21</urn:avsantTidpunkt>' +
        '    <urn:fkKontaktInfo>' +
        '      <urn1:kontakt>Sim FK-kontaktinfo Anton (NMT)</urn1:kontakt>' +
        '    </urn:fkKontaktInfo>' +
        '    <urn:adressVard>' +
        '      <urn1:hosPersonal>' +
        '        <urn2:personal-id root="1.2.752.129.2.1.4.1" extension="' + doctorHsa + '"/>' +
        '        <urn2:fullstandigtNamn>' + doctorName + '</urn2:fullstandigtNamn>' +
        '        <urn2:enhet>' +
        '          <urn2:enhets-id root="1.2.752.129.2.1.4.1" extension="' + unitHsa + '"/>' +
        '          <urn2:enhetsnamn>' + unitName + '</urn2:enhetsnamn>' +
        '          <urn2:vardgivare>' +
        '            <urn2:vardgivare-id root="1.2.752.129.2.1.4.1" extension="' + unitHsa + '"/>' +
        '            <urn2:vardgivarnamn>Norrbottens läns landsting - NPÖ</urn2:vardgivarnamn>' +
        '          </urn2:vardgivare>' +
        '        </urn2:enhet>' +
        '      </urn1:hosPersonal>' +
        '    </urn:adressVard>' +
        '    <urn:lakarutlatande>' +
        '      <urn1:lakarutlatande-id>' + intygsId + '</urn1:lakarutlatande-id>' +
        '      <urn1:signeringsTidpunkt>2015-08-28T09:05:21</urn1:signeringsTidpunkt>' +
        '      <urn1:patient>' +
        '        <urn2:person-id root="1.2.752.129.2.1.3.1" extension="' + personId + '"/>' +
        '        <urn2:fullstandigtNamn>Lars Persson</urn2:fullstandigtNamn>' +
        '      </urn1:patient>' +
        '    </urn:lakarutlatande>' +
        '  </urn:Answer>' +
        '</urn:ReceiveMedicalCertificateAnswer>';
    return body;
}


function assertDraftWithStatus(personId, intygsId, status, cb) {
    sleep.sleep(5);

    var databaseTable = process.env.DATABASE_NAME + '.INTYG';
    var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
        databaseTable + '.PATIENT_PERSONNUMMER="' + personId + '" AND ' +
        databaseTable + '.STATUS="' + status + '" AND ' +
        databaseTable + '.INTYGS_ID="' + intygsId + '" ;';

    assertNumberOfEvents(query, 1, cb);
}

function assertDatabaseContents(intygsId, column, value, cb) {
    sleep.sleep(10);

    var databaseTable = process.env.DATABASE_NAME + '.INTYG';
    var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
        databaseTable + '.INTYGS_ID="' + intygsId + '" AND ' +  
        databaseTable + '.' + column + '="' + value + '";';

    assertNumberOfEvents(query, 1, cb);
}

function assertEvents(intygsId, event, numEvents, cb) {
    sleep.sleep(5);
    var databaseTable = 'webcert_requests.requests';
    var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
        databaseTable + '.handelseKod = "' + event + '" AND ' +
        databaseTable + '.utlatandeExtension="' + intygsId + '" ;';

    assertNumberOfEvents(query, numEvents, cb);
}

function assertNumberOfEvents(query, numEvents, cb) {
    // console.log('Assert number of events. Query: ' + query);
    var conn = db.makeConnection();
    conn.connect();
    conn.query(query,
     function(err, rows, fields) {
        conn.end();
        if (err) {  
            cb(err); 
        }
        else if (rows[0].Counter !== numEvents) {
         cb('FEL, Antal händelser i db: ' + rows[0].Counter + ' (' + numEvents + ')');
        } 
        else {
         logg('OK - Antal händelser i db '+ rows[0].Counter + '(' + numEvents+')');
         cb();
        }
     });    
}

module.exports = function () {

    this.Given(/^att vårdsystemet skickat ett intygsutkast$/, function (callback) {
        global.person.id = '19121212-1212';
        
        var body = getDraftBody(global.person.id, 'IFV1239877878-1049', 'Jan Nilsson',
                                'IFV1239877878-1042', 'WebCert Enhet 1');

        var url = stripTrailingSlash(process.env.WEBCERT_URL) + ':8080/services/create-draft-certificate/v1.0?wsdl';
        url = url.replace('https', 'http');
               
        soap.createClient(url, function(err, client) {
            
            client.CreateDraftCertificate(body, function(err, result, body) {
                if (result.result.resultCode !== 'OK') {
                    callback('CreateDraftCertificate failed!');
                }
                global.intyg.id = result['utlatande-id'].attributes.extension;
            });
        });
        
        callback();
    });
    
    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk$/, function (callback) {
        global.intyg.typ = 'Läkarintyg FK 7263';
        
        var url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id;
        
        browser.get(url).then(function () {
            fkIntygPage.qaPanel.isPresent().then(function (isVisible) {
                if (isVisible) {
                    fkIntygPage.qaPanel.getAttribute('id').then(function (result) {
                        global.intyg.fragaId = result.split('-')[1];
                        console.log('Question ID: ' + global.intyg.fragaId);
                        callback();
                    });
                } else {
                    callback();
                }
            });
        });
    });

    this.Then(/^ska intygsutkastets status vara "([^"]*)"$/, function (statustext, callback) {
        expect(element(by.id('intyget-sparat-och-ej-komplett-meddelande')).getText()).to.eventually.contain(statustext).and.notify(callback);
    });
    
    this.Given(/^när jag fyller i fältet "([^"]*)"$/, function (arg1, callback) {

        if (arg1 === 'Min undersökning av patienten') {
            fk7263Utkast.minUndersokning.sendKeys(protractor.Key.SPACE)
                .then(function () {
                    fk7263Utkast.diagnosKod.sendKeys('A00').then(callback);
                });
        }
        else if (arg1 === 'Arbetsförmåga') {
            fk7263Utkast.faktiskTjanstgoring.sendKeys('40')
                .then(function () {
                    fk7263Utkast.nedsattMed25Checkbox.sendKeys(protractor.Key.SPACE).then(callback);
                });
        } else {
            callback();
        }
    });

    this.Given(/^när jag fyller i resten av de nödvändiga fälten\.$/, function (callback) {
        fk7263Utkast.funktionsNedsattning.sendKeys('Halt och lytt').then(function () {
            fk7263Utkast.aktivitetsBegransning.sendKeys('Orkar inget').then(function () {
                fk7263Utkast.nuvarandeArbete.sendKeys('Stuveriarbetare').then(callback);
            });
        });
    });

    this.Given(/^ska statusuppdatering "([^"]*)" skickas till vårdsystemet\. Totalt: "([^"]*)"$/, function (arg1, arg2,callback) {
        assertEvents(global.intyg.id, arg1, parseInt(arg2, 10), callback);
    });


    this.Given(/^är intygets status "([^"]*)"$/, function (arg1, callback) {
        assertDraftWithStatus(global.person.id, global.intyg.id, arg1, callback);
    });

    this.Given(/^när jag skickar intyget till Försäkringskassan$/, function (callback) {

        var fkIntygPage = pages.intyg.fk['7263'].intyg;

        fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE).then(function () {
            fkIntygPage.skicka.samtyckeCheckbox.sendKeys(protractor.Key.SPACE).then(function () {
                fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE).then(callback);
            });
        });
    });
    
    this.Given(/^är innehåller databasfältet "([^"]*)" värdet "([^"]*)"$/, function (arg1, arg2, callback) {
        assertDatabaseContents(global.intyg.id, arg1, arg2, callback);
    });

    this.Given(/^när jag makulerar intyget$/, function (callback) {

        var fkIntygPage = pages.intyg.fk['7263'].intyg;
        fkIntygPage.makulera.btn.sendKeys(protractor.Key.SPACE).then(function () {
            fkIntygPage.makulera.dialogAterta.sendKeys(protractor.Key.SPACE).then(function () {
                fkIntygPage.makulera.kvittensOKBtn.sendKeys(protractor.Key.SPACE).then(callback);
            });
        });
    });

    this.Given(/^när jag raderar intyget$/, function (callback) {
        fk7263Utkast.radera.knapp.click().then(function () {
            fk7263Utkast.radera.bekrafta.click().then(callback);
        });
    });

    this.Given(/^när jag svarar på frågan$/, function (callback) {
        fkIntygPage.answer.text.sendKeys('Ett litet svar.').then(function () {
            fkIntygPage.answer.sendButton.sendKeys(protractor.Key.SPACE).then(callback);
        });
    });

    this.Given(/^när jag fyller i en ny fråga till Försäkringskassan$/, function (callback) {
        fkIntygPage.question.newQuestionButton.sendKeys(protractor.Key.SPACE).then(function () {
            fkIntygPage.question.text.sendKeys('En liten fråga...').then(function () {
                fkIntygPage.question.kontakt.sendKeys(protractor.Key.SPACE).then(callback);
            });
        });
    });
    
    this.Given(/^sedan klickar på skicka$/, function (callback) {
        fkIntygPage.question.sendButton.sendKeys(protractor.Key.SPACE).then(function () {
             fkIntygPage.qaPanel.getAttribute('id').then(function (result) {
                 global.intyg.fragaId = result.split('-')[1];
                 console.log('Question ID: ' + global.intyg.fragaId);
                 callback();
            });
        });
    });

    this.Given(/^när jag markerar frågan som hanterad$/, function (callback) {
        element(by.id('markAsHandledWcOriginBtn-' + global.intyg.fragaId)).sendKeys(protractor.Key.SPACE).then(callback);
    });

    this.Given(/^när jag markerar frågan från Försäkringskassan som hanterad$/, function (callback) {
        element(by.id('markAsHandledFkOriginBtn-' + global.intyg.fragaId)).sendKeys(protractor.Key.SPACE).then(callback);
    });
    
    this.Given(/^när Försäkringskassan ställer en fråga om intyget$/, function (callback) {
        var url = stripTrailingSlash(process.env.WEBCERT_URL) + ':8080/services/receive-question/v1.0?wsdl';
        url = url.replace('https', 'http');

        global.person.id = '19121212-1212';

        var body = getQuestionBody(global.person.id, 'IFV1239877878-1049', 'Jan Nilsson',
                                   'IFV1239877878-1042', 'WebCert Enhet 1', global.intyg.id, 'Ovrigt');

        soap.createClient(url, function(err, client) {
            if (err) {
                callback(err);
            }
            
            client.ReceiveMedicalCertificateQuestion(body, function(err, result, body) {
                callback();
            });
        });
    });
    
    this.Given(/^när Försäkringskassan skickar ett svar$/, function (callback) {

        var url = stripTrailingSlash(process.env.WEBCERT_URL) + ':8080/services/receive-answer/v1.0?wsdl';
        url = url.replace('https', 'http');
        
        soap.createClient(url, function(err, client) {

            if (err) {
                console.log('HEHHEHEHEHEHEHHEHE');
                callback(err);
            }
            else{
                var body = getAnswerBody(
                    global.person.id,
                    'IFV1239877878-1049',
                    'Jan Nilsson',                  
                    'IFV1239877878-1042',
                    'WebCert Enhet 1', 
                    intyg.id,
                    intyg.fragaId
                    );
                
                client.ReceiveMedicalCertificateAnswer(body, function(err, result, body) {
                    callback(err);
                });
            }

        });
    });

    this.Given(/^när Försäkringskassan ställer en fråga om intyget \- "([^"]*)"$/, function (arg1, callback) {
        var url = stripTrailingSlash(process.env.WEBCERT_URL) + ':8080/services/receive-question/v1.0?wsdl';
        url = url.replace('https', 'http');

        global.person.id = '19121212-1212';

        var body = getQuestionBody(global.person.id, 'IFV1239877878-1049', 'Jan Nilsson',
                                  'IFV1239877878-1042', 'WebCert Enhet 1', global.intyg.id, arg1);
        soap.createClient(url, function(err, client) {
            if (err) {
                callback(err);
            }
            client.ReceiveMedicalCertificateQuestion(body, function(err, result, body) {
                console.log(body);
                console.log(result);
                callback();
            });
        });
    });

    this.Given(/^när jag skickat ett signerat intyg till Försäkringskassan$/, function (callback) {
        fk7263Utkast.minUndersokning.sendKeys(protractor.Key.SPACE)
            .then(function () {
                fk7263Utkast.diagnosKod.sendKeys('A00');
            })
            .then(function () {
                fk7263Utkast.faktiskTjanstgoring.sendKeys('40');
            })
            .then(function () {
                fk7263Utkast.nedsattMed25Checkbox.sendKeys(protractor.Key.SPACE);
            })
            .then(function () {
                fk7263Utkast.funktionsNedsattning.sendKeys('Halt och lytt');
            })
            .then(function () {
                fk7263Utkast.aktivitetsBegransning.sendKeys('Orkar inget');
            }).then(function () {
                fk7263Utkast.nuvarandeArbete.sendKeys('Stuveriarbetare');
            }).then(function () {
                element(by.id('signera-utkast-button')).sendKeys(protractor.Key.SPACE);
            }).then(function () {
                fkIntygPage.skicka.knapp.click();
            }).then(function () {
                fkIntygPage.skicka.samtyckeCheckbox.click();
            }).then(function () {
                fkIntygPage.skicka.dialogKnapp.click();
            }).then(callback);
    });
};
