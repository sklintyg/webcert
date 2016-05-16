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

function addDays(date, days) {
    date.setDate(date.getDate() + days);
    return date;
}

module.exports.SendMessageToCare = function(user, person, intyg) {
    var amneCode = 'KOMPLT';
    var amneDisplayName = 'Komplettering';
    var messageID = testdataHelper.generateTestGuid();
    var skickatTidpunkt = new Date();
    var sistaDatumForSvar = addDays(skickatTidpunkt, 5);

    var kompletteringar = new Array(26);

    for (var k = 1; k <= 26; k++) {
        kompletteringar.push(
            '<komplettering>' +
            '<frage-id>' + k + '</frage-id>' +
            '<text>Kompletterning #' + k + '/text>' +
            '</komplettering>'
        );
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
        '<rubrik>KOMPLT</rubrik>' +
        '<meddelande>Inledande text för komplettering avseende LUSE</meddelande>' +
        '<skickatAv>' +
        '<part>' +
        '<types:code>FKASSA</types:code>' +
        '<types:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</types:codeSystem>' +
        '<types:displayName>Försäkringskassan</types:displayName>' +
        '</part>' +
        '<kontaktInfo>MAX antal kategorier. Automatiskt test </kontaktInfo>' +
        '</skickatAv>' +
        '<komplettering>' +
        '<frage-id>1</frage-id>' +
        '<text>1a MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>2</frage-id>' +
        '<text>2a. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>3</frage-id>' +
        '<text>3e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>4</frage-id>' +
        '<text>4e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>5</frage-id>' +
        '<text>5e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>6</frage-id>' +
        '<text>6e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>7</frage-id>' +
        '<text>7e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>8</frage-id>' +
        '<text>8e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>9</frage-id>' +
        '<text>9e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>10</frage-id>' +
        '<text>10e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>11</frage-id>' +
        '<text>11e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>12</frage-id>' +
        '<text>12e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>13</frage-id>' +
        '<text>13e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>14</frage-id>' +
        '<text>14e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>17</frage-id>' +
        '<text>17e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>18</frage-id>' +
        '<text>18e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>19</frage-id>' +
        '<text>19e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>20</frage-id>' +
        '<text>20e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>21</frage-id>' +
        '<text>21a. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>22</frage-id>' +
        '<text>22a. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>23</frage-id>' +
        '<text>23e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>25</frage-id>' +
        '<text>25e. MAX</text>' +
        '</komplettering>' +
        '<komplettering>' +
        '<frage-id>26</frage-id>' +
        '<text>26e. MAX</text>' +
        '</komplettering>' +
        '<sistaDatumForSvar>' + testdataHelper.dateFormat(sistaDatumForSvar) + '</sistaDatumForSvar>' +
        '</SendMessageToCare>';
};
