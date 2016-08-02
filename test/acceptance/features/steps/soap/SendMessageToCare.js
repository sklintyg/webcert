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
/*globals wcTestTools*/

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('../helpers');
var helpers = helpers;

function addDays(date, days) {
    date.setDate(date.getDate() + days);
    return date;
}

module.exports.SendMessageToCare = function(user, person, intyg, message, amneCode) {
    var amneDisplayName = helpers.getSubjectFromCode(amneCode);
    var messageID = testdataHelper.generateTestGuid();
    var skickatTidpunkt = new Date();
    var sistaDatumForSvar = addDays(skickatTidpunkt, 5);

    var paminnelseMeddelandeId;
    if (global.previousGuid && amneCode === 'PAMINN') {
        paminnelseMeddelandeId = '<paminnelseMeddelande-id>' + global.previousGuid + '</paminnelseMeddelande-id>';
    }

    var kompletteringar = [];
    if (amneCode === 'KOMPLT') {
        global.previousGuid = messageID;

        for (var k = 1; k <= 26; k++) {
            if (k === 24) {
                continue;
            } // Frage-id 24 finns inte
            kompletteringar.push(
                '<komplettering>' +
                '<frage-id>' + k + '</frage-id>' +
                '<text>Kompletterning #' + k + '</text>' +
                '</komplettering>'
            );
        }
    }

    console.log('global.previousGuid: ' + global.previousGuid);

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
        ((paminnelseMeddelandeId) ? paminnelseMeddelandeId : '') +
        '<skickatAv>' +
        '<part>' +
        '<types:code>FKASSA</types:code>' +
        '<types:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</types:codeSystem>' +
        '<types:displayName>Försäkringskassan</types:displayName>' +
        '</part>' +
        '<kontaktInfo>Kontaktinfo</kontaktInfo>' +
        '</skickatAv>' +
        ((kompletteringar.length > 0) ? kompletteringar.join('\n') : '') +
        '<sistaDatumForSvar>' + testdataHelper.dateFormat(sistaDatumForSvar) + '</sistaDatumForSvar>' +
        '</SendMessageToCare>';
};
