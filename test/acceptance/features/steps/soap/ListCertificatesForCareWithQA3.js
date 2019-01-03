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

/*global logger*/

'use strict';
module.exports.getBody = function(personID, enhetHSA) {
    var personIDRoot = '1.2.752.129.2.1.3.1';
    var isSamordningsnummer = parseInt(personID.slice(6, 8), 10) > 31;

    logger.silly('isSamordningsnummer : ' + isSamordningsnummer);
    if (isSamordningsnummer) {
        personIDRoot = '1.2.752.129.2.1.3.3';
    }

    return '<urn1:ListCertificatesForCareWithQA' +
        ' xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"' +
        ' xmlns:urn="urn:riv:itintegration:registry:1"' +
        ' xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCareWithQAResponder:3"' +
        ' xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3"' +
        '>' +
        ' <urn1:person-id>' +
        ' <urn2:root>' + personIDRoot + '</urn2:root>' +
        ' <urn2:extension>' + personID.replace('-', '') + '</urn2:extension>' +
        ' </urn1:person-id>' +
        ' <urn1:enhets-id>' +
        ' <urn2:root>1.2.752.129.2.1.4.1</urn2:root>' +
        ' <urn2:extension>' + enhetHSA + '</urn2:extension>' +
        ' </urn1:enhets-id>' +
        ' </urn1:ListCertificatesForCareWithQA>';
};
