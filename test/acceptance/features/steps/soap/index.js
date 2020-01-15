/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

'use strict';
// var testdataHelper = wcTestTools.helpers.testdata;
var today = new Date();

module.exports = {
  CreateDraftCertificate: function(doctorHsa, doctorName, unitHsa, unitName, personId) {
    return '<urn1:CreateDraftCertificate ' +
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
  },
  GetCertificate: function() {
    //TODO
  },
  ListCertificatesForCitizen: function(personId, intygsTyper) {
    var string = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" ' +
        'xmlns:urn="urn:riv:itintegration:registry:1" ' +
        'xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCitizenResponder:3" ' +
        'xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3"> ' +
        '<soapenv:Header> ' +
        '<urn:LogicalAddress>?</urn:LogicalAddress> ' +
        '</soapenv:Header> ' +
        '<soapenv:Body> ' +
        '<urn1:ListCertificatesForCitizen> ' +
        '<urn1:person-id> ' +
        '<urn2:root>1.2.752.129.2.1.3.1</urn2:root> ' +
        '<urn2:extension>' + personId + '</urn2:extension> ' +
        '</urn1:person-id> ' +
        '<!--Zero or more repetitions:--> ';

    for (var i = 0; i < intygsTyper.length; i++) {
      string += '<urn1:intygTyp> ' +
          '<urn2:code>' + intygsTyper[i] + '</urn2:code> ' +
          '<urn2:codeSystem>c2362fcd-eda0-4f9a-bd13-b3bbaf7f214</urn2:codeSystem> ' +
          '<!--Optional:--> ' +
          '<urn2:displayName></urn2:displayName> ' +
          '</urn1:intygTyp> ';
    }

    string += '<urn1:arkiverade>true</urn1:arkiverade> ' +
        '<!--FKASSA, HSVARD, INVANA, och TRANSP --> ' +
        '<urn1:part> ' +
        '<urn2:code>FKASSA</urn2:code> ' +
        '<urn2:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</urn2:codeSystem> ' +
        '<!--Optional:--> ' +
        '<urn2:displayName>FÖRSÄKRINGSKASSAN</urn2:displayName> ' +
        '</urn1:part> ' +
        '<!--You may enter ANY elements at this point--> ' +
        '</urn1:ListCertificatesForCitizen> ' +
        '</soapenv:Body> ' +
        '</soapenv:Envelope> ';
  },
  ReceiveMedicalCertificateQuestion: function(personId, user, unitName, intygsId, amne, meddelande) {

    if (typeof meddelande === 'undefined') {
      meddelande = 'Testfråga';
    }

    return '<urn:ReceiveMedicalCertificateQuestion ' +
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
        '      <urn1:meddelandeText>NMT simulerar FK</urn1:meddelandeText>' +
        '      <urn1:signeringsTidpunkt>' + today.toISOString() + '</urn1:signeringsTidpunkt>' +
        '    </urn:fraga>' +
        '    <urn:avsantTidpunkt>' + today.toISOString() + '</urn:avsantTidpunkt>' +
        '    <urn:fkKontaktInfo>' +
        '      <urn1:kontakt>Automatiska tester</urn1:kontakt>' +
        '    </urn:fkKontaktInfo>' +
        '    <urn:adressVard>' +
        '      <urn1:hosPersonal>' +
        '        <urn2:personal-id root="1.2.752.129.2.1.4.1" extension="' + user.hsaId + '"/>' +
        '        <urn2:fullstandigtNamn>' + user.forNamn + ' ' + user.efterNamn + '</urn2:fullstandigtNamn>' +
        '        <urn2:enhet>' +
        '          <urn2:enhets-id extension="' + user.enhetId + '" root="1.2.752.129.2.1.4.1"/>' +
        '          <urn2:enhetsnamn>unitName</urn2:enhetsnamn>' +
        '          <urn2:vardgivare>' +
        '            <urn2:vardgivare-id extension="' + user.enhetId + '" root="1.2.752.129.2.1.4.1"/>' +
        '            <urn2:vardgivarnamn>NMT</urn2:vardgivarnamn>' +
        '          </urn2:vardgivare>' +
        '        </urn2:enhet>' +
        '      </urn1:hosPersonal>' +
        '    </urn:adressVard>' +
        '    <urn:fkMeddelanderubrik>' + amne + '</urn:fkMeddelanderubrik>' +
        '    <urn:fkKomplettering>' +
        '      <urn1:falt>Test</urn1:falt>' +
        '      <urn1:text>' + meddelande + '</urn1:text>' +
        '    </urn:fkKomplettering>' +
        '    <urn:fkSistaDatumForSvar>2017-01-28</urn:fkSistaDatumForSvar>' +
        '    <urn:lakarutlatande>' +
        '      <urn1:lakarutlatande-id>' + intygsId + '</urn1:lakarutlatande-id>' +
        '      <urn1:signeringsTidpunkt>' + today.toISOString() + '</urn1:signeringsTidpunkt>' +
        '      <urn1:patient>' +
        '        <urn2:person-id extension="' + personId + '" root="1.2.752.129.2.1.3.1"/>' +
        '        <urn2:fullstandigtNamn>Lars Persson</urn2:fullstandigtNamn>' +
        '      </urn1:patient>' +
        '    </urn:lakarutlatande>' +
        '  </urn:Question>' +
        '</urn:ReceiveMedicalCertificateQuestion>';
  },

  ReceiveMedicalCertificateAnswer: function(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId, fragaId) {
    return '<urn:ReceiveMedicalCertificateAnswer' +
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
        '      <urn1:meddelandeText>Fråga</urn1:meddelandeText>' +
        '      <urn1:signeringsTidpunkt>2015-08-28T09:05:21</urn1:signeringsTidpunkt>' +
        '    </urn:fraga>' +
        '    <urn:svar>' +
        '      <urn1:meddelandeText>Här kommer ett svar!</urn1:meddelandeText>' +
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
  },
  SendMessageToCare: require('./SendMessageToCare').SendMessageToCare,
  CreateDraftCertificateV3: require('./CreateDraftV3').CreateDraftCertificateV3,
  RegisterMedicalCertificate: require('./RegisterMedicalCertificate').RegisterMedicalCertificate,
  SendMedicalCertificate: require('./SendMedicalCertificate').SendMedicalCertificate,
  RegisterCertificate: require('./RegisterCertificate').RegisterCertificate,
  ListCertificatesForCareWithQA: require('./ListCertificatesForCareWithQA3')
};
