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


'use strict';
//var testdataHelper = wcTestTools.helpers.testdata;
//var helpers = require('../helpers');
module.exports.SendMedicalCertificate = function(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId) {
    return '<urn:SendMedicalCertificateRequest\n' +
        '   xmlns:urn="urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateResponder:1"\n' +
        '   xmlns:urn1="urn:riv:insuranceprocess:healthreporting:medcertqa:1"\n' +
        '   xmlns:urn2="urn:riv:insuranceprocess:healthreporting:2">\n' +
        '         <urn:send>\n' +
        '            <urn:vardReferens-id>123565</urn:vardReferens-id>\n' +
        '            <urn:avsantTidpunkt>2016-10-05T10:05:53</urn:avsantTidpunkt>\n' +
        '            <urn:adressVard>\n' +
        '               <urn1:hosPersonal>\n' +
        '                  <urn2:personal-id root="1.2.752.129.2.1.4.1" extension="' + doctorHsa + '"/>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn2:fullstandigtNamn>' + doctorName + '</urn2:fullstandigtNamn>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn2:forskrivarkod>1234</urn2:forskrivarkod>\n' +
        '                  <urn2:enhet>\n' +
        '                     <urn2:enhets-id extension="' + unitHsa + '" root="1.2.752.129.2.1.4.1"/>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:arbetsplatskod root="1.2.752.29.4.71" extension="123456789011"/>\n' +
        '                     <urn2:enhetsnamn>' + unitName + '</urn2:enhetsnamn>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:postadress>Hemvägen 1</urn2:postadress>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:postnummer> </urn2:postnummer>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:postort> </urn2:postort>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:telefonnummer> </urn2:telefonnummer>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:epost> </urn2:epost>\n' +
        '                     <!--Optional:-->\n' +
        '                     <urn2:vardgivare>\n' +
        '                        <urn2:vardgivare-id extension="' + 'TSTNMT2321000156-1002' + '" root="1.2.752.129.2.1.4.1"/>\n' +
        '                        <urn2:vardgivarnamn>nmt_vg1</urn2:vardgivarnamn>\n' +
        '                        <!--You may enter ANY elements at this point-->\n' +
        '                     </urn2:vardgivare>\n' +
        '                     <!--You may enter ANY elements at this point-->\n' +
        '                  </urn2:enhet>\n' +
        '                  <!--You may enter ANY elements at this point-->\n' +
        '               </urn1:hosPersonal>\n' +
        '               <!--You may enter ANY elements at this point-->\n' +
        '            </urn:adressVard>\n' +
        '            <urn:lakarutlatande>\n' +
        '               <urn1:lakarutlatande-id>' + intygsId + '</urn1:lakarutlatande-id>\n' +
        '               <urn1:signeringsTidpunkt>2016-10-04T10:05:53</urn1:signeringsTidpunkt>\n' +
        '               <urn1:patient>\n' +
        '                  <urn2:person-id extension="' + personId + '" root="1.2.752.129.2.1.3.1"/>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn2:fullstandigtNamn> </urn2:fullstandigtNamn>\n' +
        '                  <!--You may enter ANY elements at this point-->\n' +
        '               </urn1:patient>\n' +
        '               <!--You may enter ANY elements at this point-->\n' +
        '            </urn:lakarutlatande>\n' +
        '            <!--You may enter ANY elements at this point-->\n' +
        '         </urn:send>\n' +
        '         <!--You may enter ANY elements at this point-->\n' +
        '      </urn:SendMedicalCertificateRequest> \n';
};
