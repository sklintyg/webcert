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
/*globals wcTestTools, JSON*/

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('../helpers');

function addDays(date, days) {
    date.setDate(date.getDate() + days);
    return date;
}

module.exports.SendMessageToCare = function(user, person, intyg, message, amneCode) {
    var amneDisplayName = helpers.getSubjectFromCode(amneCode);
    var messageID = testdataHelper.generateTestGuid();
    var skickatTidpunkt = new Date();



    var svarPa = '';
    var sistaDatumForSvar = '<sistaDatumForSvar>' + testdataHelper.dateFormat(addDays(skickatTidpunkt, 5)) + '</sistaDatumForSvar>';

    if (amneCode) {
        global.meddelanden.push({
            id: messageID,
            typ: 'Fråga',
            amne: amneCode
        });
    } else {
        // Om ämne inte skickas med till funktionen så behandlar vi det som 
        // ett svarsmeddelande och kopierar ämne från tidigare
        amneCode = global.meddelanden[0].amne;
        svarPa = '<svarPa>' + '<core:meddelande-id>' + global.meddelanden[0].id + '</core:meddelande-id>' + '</svarPa>';
        sistaDatumForSvar = '';
    }

    console.log('global.meddelanden: ' + JSON.stringify(global.meddelanden));

    var kompletteringar = '';
    var paminnelseMeddelandeId = '';
    if (global.meddelanden[0].id && amneCode === 'PAMINN') {
        paminnelseMeddelandeId = '<paminnelseMeddelande-id>' + global.meddelanden[0].id + '</paminnelseMeddelande-id>';
    } else if (amneCode === 'KOMPLT') {

        kompletteringar = [];
        for (var k = 1; k <= 26; k++) {
            if (k === 24) {
                continue; // Frage-id 24 finns inte
            }
            kompletteringar.push(
                '<komplettering>' +
                '<frage-id>' + k + '</frage-id>' +
                '<text>Kompletterning #' + k + '</text>' +
                '</komplettering>'
            );
        }
        kompletteringar = kompletteringar.join('\n');
    }

    return '<SendMessageToCare' +
        ' xmlns="urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1"' +
        ' xmlns:core="urn:riv:clinicalprocess:healthcond:certificate:2"' +
        ' xmlns:types="urn:riv:clinicalprocess:healthcond:certificate:types:2">' +
        '<meddelande-id>' + messageID + '</meddelande-id>' +
        '<referens-id>160500047253</referens-id>' +
        '<skickatTidpunkt>' + skickatTidpunkt.toISOString().slice(0, -5) + '</skickatTidpunkt>' +
        '<intygs-id>' +
        '<types:root>' + user.enhetId + '</types:root>' +
        '<types:extension>' + intyg.id + '</types:extension>' +
        '</intygs-id>' +
        '<patientPerson-id>' +
        '<types:root>1.2.752.129.2.1.3.1</types:root>' +
        '<types:extension>' + person.id.replace('-', '') + '</types:extension>' +
        '</patientPerson-id>' +
        '<logiskAdressMottagare>' + user.enhetId + '</logiskAdressMottagare>' +
        '<amne>' +
        '<types:code>' + amneCode + '</types:code>' +
        '<types:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</types:codeSystem>' +
        '<types:displayName>' + amneDisplayName + '</types:displayName>' +
        '</amne>' +
        '<rubrik>' + amneCode + '</rubrik>' +
        '<meddelande>' + message + '</meddelande>' +
        paminnelseMeddelandeId +
        svarPa +
        '<skickatAv>' +
        '<part>' +
        '<types:code>FKASSA</types:code>' +
        '<types:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</types:codeSystem>' +
        '<types:displayName>Försäkringskassan</types:displayName>' +
        '</part>' +
        '<kontaktInfo>Kontaktinfo</kontaktInfo>' +
        '</skickatAv>' +
        kompletteringar +
        sistaDatumForSvar +
        '</SendMessageToCare>';
};
