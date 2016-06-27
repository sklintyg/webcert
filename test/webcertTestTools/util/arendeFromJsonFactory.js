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

/**
 * Created by eriklupander on 2016-04-29.
 */

'use strict';

// These are pushed to webcert using restDataHelper.createArende
var templateArende = require('webcert-testtools/testdata/arende.template.json');

module.exports = {

    /**
     * Example:
     {
         "timestamp": "2016-05-01T12:02:27.200",
         "meddelandeId": "1",
         "referensId": "fkid",
         "skickatTidpunkt": "2016-03-31T14:42:27.849",
         "intygsId": "-",
         "patientPersonId": "191212121212",
         "amne": "KOMPLT",
         "rubrik": "rubrik",
         "skickatAv": "fk",
         "meddelande": "Här kommer ett meddeleande från era vänner på Försäkringskassan.",
         "sistaDatumForSvar": "2016-04-04",
         "intygTyp": "luse",
         "status": "PENDING_INTERNAL_ACTION",
         "vidarebefordrad": false,
         "signeratAv": "TSTNMT2321000156-103F",
         "signeratAvName": "Leonie Koehl",
         "enhetId": "TSTNMT2321000156-1039",
         "senasteHandelse": "",
         "vardaktorName": "",
         "kontaktInfo": [
             "test",
             "adress",
             "telefon"
         ],
        "paminnelseMeddelandeId": "",
        "svarPaId": "",
        "svarPaReferens": "",
        "komplettering": "",
     }

     Ämnen:
     ARBTID("Arbetstidsförläggning"),
     AVSTMN("Avstämningsmöte"),
     KONTKT("Kontakt"),
     OVRIGT("Övrigt"),
     PAMINN("Påminnelse"),
     KOMPLT("Komplettering");

     Status:
     PENDING_INTERNAL_ACTION    // The FragaSvar or Arende has been received from an external entity and needs to be answered.
     PENDING_EXTERNAL_ACTION    // The FragaSvar or Arende has been sent to an external entity and awaits an answer.
     ANSWERED                   // The FragaSvar or Arende has received an answer from the external entity.
     CLOSED                     // The FragaSvar or Arende has been handled.

     *
     * @param arendeOptions
     * @returns {*|exports}
     */

    get: function(arendeOptions) {
        var arende = Object.assign({}, templateArende); // Create clone so not all callers use the same instance

        arende.intygTyp = arendeOptions.intygType;
        arende.intygsId = arendeOptions.intygId;
        arende.meddelandeId = arendeOptions.meddelandeId;
        arende.paminnelseMeddelandeId = typeof arendeOptions.paminnelseMeddelandeId !== 'undefined' ? arendeOptions.paminnelseMeddelandeId : undefined;
        arende.meddelande = arendeOptions.meddelande;
        arende.amne = arendeOptions.amne;
        arende.status = arendeOptions.status;
        arende.komplettering = arendeOptions.kompletteringar;
        return arende;
    }
};
