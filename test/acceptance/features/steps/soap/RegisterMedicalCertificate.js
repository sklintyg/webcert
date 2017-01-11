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
module.exports.RegisterMedicalCertificate = function(personId, personNamn, personEfternamn, doctorHsa, doctorName, unitHsa, unitName, intygsId) {


    return '    <ns3:RegisterMedicalCertificate\n' +
        ' xmlns="urn:riv:insuranceprocess:healthreporting:mu7263:3"\n' +
        ' xmlns:ns2="urn:riv:insuranceprocess:healthreporting:2"\n' +
        ' xmlns:ns3="urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3"\n' +
        ' xmlns:ns4="http://www.w3.org/2005/08/addressing">\n' +
        '    <ns3:lakarutlatande>\n' +
        '        <lakarutlatande-id>' + intygsId + '</lakarutlatande-id>\n' +
        '        <typAvUtlatande>Läkarintyg enligt 3 kap, 8 § lagen (1962:381) om allmän försäkring</typAvUtlatande>\n' +
        '        <kommentar>Prognosen för patienten är god. Han kommer att kunna återgå till sitt arbete efter genomförd behandling.</kommentar>\n' +
        '        <signeringsdatum>2016-09-08T10:00:00</signeringsdatum>\n' +
        '        <skickatDatum>2016-09-08T10:29:15</skickatDatum>\n' +
        '        <patient>\n' +
        '            <ns2:person-id root="1.2.752.129.2.1.3.1" extension="' + personId + '"/>\n' +
        '            <ns2:fullstandigtNamn>' + personNamn + ' ' + personEfternamn + '</ns2:fullstandigtNamn>\n' +
        '        </patient>\n' +
        '        <skapadAvHosPersonal>\n' +
        '            <ns2:personal-id root="1.2.752.129.2.1.4.1" extension="' + doctorHsa + '"/>\n' +
        '            <ns2:fullstandigtNamn>' + doctorName + '</ns2:fullstandigtNamn>\n' +
        '            <ns2:forskrivarkod>1234567</ns2:forskrivarkod>\n' +
        '            <ns2:enhet>\n' +
        '                <ns2:enhets-id root="1.2.752.129.2.1.4.1" extension="' + unitHsa + '"/>\n' +
        '                <ns2:arbetsplatskod root="1.2.752.29.4.71" extension="123456789011"/>\n' +
        '                <ns2:enhetsnamn>Enhetnamn</ns2:enhetsnamn>\n' +
        '                <ns2:postadress>Lasarettsvägen 13</ns2:postadress>\n' +
        '                <ns2:postnummer>85150</ns2:postnummer>\n' +
        '                <ns2:postort>Sundsvall</ns2:postort>\n' +
        '                <ns2:telefonnummer>060-1818000</ns2:telefonnummer>\n' +
        '                <ns2:vardgivare>\n' +
        '                    <ns2:vardgivare-id root="1.2.752.129.2.1.4.1" extension="' + 'TSTNMT2321000156-1002' + '"/>\n' +
        '                    <ns2:vardgivarnamn>Nordic Medtest</ns2:vardgivarnamn>\n' +
        '                </ns2:vardgivare>\n' +
        '            </ns2:enhet>\n' +
        '        </skapadAvHosPersonal>\n' +
        '        <vardkontakt>\n' +
        '            <vardkontakttyp>Min_undersokning_av_patienten</vardkontakttyp>\n' +
        '            <vardkontaktstid>2015-01-01</vardkontaktstid>\n' +
        '        </vardkontakt>\n' +
        '        <vardkontakt>\n' +
        '            <vardkontakttyp>Min_telefonkontakt_med_patienten</vardkontakttyp>\n' +
        '            <vardkontaktstid>2015-01-01</vardkontaktstid>\n' +
        '        </vardkontakt>\n' +
        '        <referens>\n' +
        '            <referenstyp>Journaluppgifter</referenstyp>\n' +
        '            <datum>2015-01-01</datum>\n' +
        '        </referens>\n' +
        '        <referens>\n' +
        '            <referenstyp>Annat</referenstyp>\n' +
        '            <datum>2015-01-01</datum>\n' +
        '        </referens>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Patienten_behover_fa_kontakt_med_Arbetsformedlingen</aktivitetskod>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Patienten _behover_fa_kontakt_med_foretagshalsovarden</aktivitetskod>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Ovrigt</aktivitetskod>\n' +
        '            <beskrivning>När skadan förbättrats  rekommenderas muskeluppbyggande sjukgymnastik</beskrivning>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Planerad_eller_pagaende_behandling_eller_atgard_inom_sju kvarden</aktivitetskod>\n' +
        '            <beskrivning>Utreds om operation är nödvändig</beskrivning>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Planerad_eller_pagaende_annan_atga rd</aktivitetskod>\n' +
        '            <beskrivning>Patienten ansvarar för att armen hålls i stillhet</beskrivning>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Gar_ej_att_bedomma_om_ arbetslivsinriktad_rehabilitering_ar_aktuell</aktivitetskod>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Forandrat_ressatt_till_arbetsplatsen_ar_ej_aktuellt</aktivitetskod>\n' +
        '        </aktivitet>\n' +
        '        <aktivitet>\n' +
        '            <aktivitetskod>Kontakt_med_Forsakringskassan_ar_aktuell</aktivitetskod>\n' +
        '        </aktivitet>\n' +
        '        <bedomtTillstand>\n' +
        '            <beskrivning>Patient en klämde höger överarm vid olycka i hemmet. Problemen har pågått en längre tid.</beskrivning>\n' +
        '        </bedomtTillstand>\n' +
        '        <medicinsktTillstand>\n' +
        '            <beskrivning>Klämskada på överarm</beskrivning>\n' +
        '            <tillstandskod code="S47" codeSystemName="ICD-10"/>\n' +
        '        </medicinsktTillstand>\n' +
        '        <funktionstillstand>\n' +
        '            <beskrivning>Kraftigt nedsatt rörlighet i överarmen pga skadan.</beskrivning>\n' +
        '            <typAvFunktionstillstand>Kroppsfunktion</typAvFunktionstillstand>\n' +
        '        </funktionstillstand>\n' +
        '        <funktionstillstand>\n' +
        '            <beskrivning>Patienten bör/kan inte  använda armen förrän skadan läkt.</beskrivning>\n' +
        '            <typAvFunktionstillstand>Aktivitet</typAvFunktionstillstand>\n' +
        '            <arbetsformaga>\n' +
        '                <motivering>Skadan har förvärrats vid varje tillfälle patienten använt armen.</motivering>\n' +
        '                <prognosangivelse>Det_gar_inte_att_bedomma</prognosangivelse>\n' +
        '                <arbetsuppgift>\n' +
        '                    <typAvArbetsuppgift>Dirigent. Dirigerar en större orkester på deltid</typAvArbetsuppgift>\n' +
        '                </arbetsuppgift>\n' +
        '                <arbetsformagaNedsattning>\n' +
        '                    <varaktighetFrom>2015-05-25</varaktighetFrom>\n' +
        '                    <varaktighetTom>2015-06-05</varaktighetTom>\n' +
        '                    <nedsattningsgrad>Nedsatt_med_1/4</nedsattningsgrad>\n' +
        '                </arbetsformagaNedsattning>\n' +
        '                <arbetsformagaNedsattning>\n' +
        '                    <varaktighetFrom>2015-06-05</varaktighetFrom>\n' +
        '                    <varaktighetTom>2015-06-15</varaktighetTom>\n' +
        '                    <nedsattningsgrad>Nedsatt_med_1/2</nedsattningsgrad>\n' +
        '                </arbetsformagaNedsattning>\n' +
        '                <arbetsformagaNedsattning>\n' +
        '                    <varaktighetFrom>2015-06-15</varaktighetFrom>\n' +
        '                    <varaktighetTom>2015-06-25</varaktighetTom>\n' +
        '                    <nedsattningsgrad>Nedsatt_med_3/4</nedsattningsgrad>\n' +
        '                </arbetsformagaNedsattning>\n' +
        '               <arbetsformagaNedsattning>\n' +
        '                    <varaktighetFrom>2015-06-25</varaktighetFrom>\n' +
        '                    <varaktighetTom>2015-08-25</varaktighetTom>\n' +
        '                    <nedsattningsgrad>Helt_nedsatt</nedsattningsgrad>\n' +
        '                </arbetsformagaNedsattning>\n' +
        '                <sysselsattning>\n' +
        '                    <typAvSysselsattning>Nuvarande_arbete</typAvSysselsattning>\n' +
        '                </sysselsattning>\n' +
        '                <sysselsattning>\n' +
        '                    <typAvSysselsattning>Arbetsloshet</typAvSysselsattning>\n' +
        '                </sysselsattning>\n' +
        '                <sysselsattning>\n' +
        '                    <typAvSysselsattning>Foraldraledighet</typAvSysselsattning>\n' +
        '                </sysselsattning>\n' +
        '            </arbetsformaga>\n' +
        '        </funktionstillstand>\n' +
        '    </ns3:lakarutlatande>' +
        '</ns3:RegisterMedicalCertificate>';

};
