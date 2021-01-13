/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
/*globals wcTestTools, JSON, logger*/

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;

function addDays(date, days) {
  date.setDate(date.getDate() + days);
  return date;
}

module.exports.SendMessageToCare = function(user, person, intyg, message, testString, amneCode) {
  var messageID = testdataHelper.generateTestGuid();
  var skickatTidpunkt = new Date();

  if (!intyg.messages) {
    intyg.messages = [];
  }

  var svarPa = '';
  var sistaDatumForSvar = '<urn1:sistaDatumForSvar>' + testdataHelper.dateFormat(addDays(skickatTidpunkt, 5)) + '</urn1:sistaDatumForSvar>';

  if (amneCode) {
    intyg.messages.unshift({
      id: messageID,
      typ: 'Fråga',
      amne: amneCode,
      testString: testString
    });
  } else {
    // Om ämne inte skickas med till funktionen så behandlar vi det som
    // ett svarsmeddelande och kopierar ämne från tidigare
    amneCode = intyg.messages[0].amne;
    svarPa = '<urn1:svarPa>' + '<urn3:meddelande-id>' + intyg.messages[0].id + '</urn3:meddelande-id>' + '</urn1:svarPa>';
    sistaDatumForSvar = '';

    intyg.messages.unshift({
      id: messageID,
      typ: 'Svar',
      amne: amneCode,
      testString: testString
    });

  }

  logger.silly('this.intyg.messages: ' + JSON.stringify(intyg.messages));

  var kompletteringar = '';
  var paminnelseMeddelandeId = '';
  if (intyg.messages[0].id && amneCode === 'PAMINN') {
    paminnelseMeddelandeId = '<urn1:paminnelseMeddelande-id>' + intyg.messages[1].id + '</urn1:paminnelseMeddelande-id>';
  } else if (amneCode === 'KOMPLT') {

    kompletteringar = [];
    for (var k = 1; k <= 26; k++) {
      if (k === 24) {
        continue; // Frage-id 24 finns inte
      }
      kompletteringar.push(
          '<urn1:komplettering>' +
          '<urn1:frage-id>' + k + '</urn1:frage-id>' +
          '<urn1:text>Komplettering #' + k + '</urn1:text>' +
          '</urn1:komplettering>'
      );
    }
    kompletteringar = kompletteringar.join('\n');
  }

  return '<urn1:SendMessageToCare' +
      ' xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"' +
      ' xmlns:urn="urn:riv:itintegration:registry:1"' +
      ' xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2"' +
      ' xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3"' +
      ' xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3"' +
      '>' +
      '   <urn1:meddelande-id>' + messageID + '</urn1:meddelande-id>' +
      '   <urn1:skickatTidpunkt>' + skickatTidpunkt.toISOString().slice(0, -5) + '</urn1:skickatTidpunkt>' +
      '   <urn1:intygs-id>' +
      '      <urn2:root>' + user.enhetId + '</urn2:root>' +
      '      <urn2:extension>' + intyg.id + '</urn2:extension>' +
      '   </urn1:intygs-id>' +
      '   <urn1:patientPerson-id>' +
      '      <urn2:root>1.2.752.129.2.1.3.1</urn2:root>' +
      '      <urn2:extension>' + person.id.replace('-', '') + '</urn2:extension>' +
      '   </urn1:patientPerson-id>' +
      '   <urn1:logiskAdressMottagare>' + 'nmtWebcert' + process.env.environmentName + '</urn1:logiskAdressMottagare>' +
      '   <urn1:amne>' +
      '      <urn2:code>' + amneCode + '</urn2:code>' +
      '      <urn2:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</urn2:codeSystem>' +
      '   </urn1:amne>' +
      '   <urn1:meddelande>' + message + ' ' + testString + '</urn1:meddelande>' +
      paminnelseMeddelandeId +
      svarPa +
      '   <urn1:skickatAv>' +
      '      <urn1:part>' +
      '         <urn2:code>FKASSA</urn2:code>' +
      '         <urn2:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</urn2:codeSystem>' +
      '      </urn1:part>' +
      '   </urn1:skickatAv>' +
      kompletteringar +
      sistaDatumForSvar +
      '</urn1:SendMessageToCare>';
};
