/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
/* globals logger */
'use strict';

// These are pushed to webcert using restDataHelper.createArende
var templateFragasvar = require('webcert-testtools/testdata/fragasvar.template.json');

module.exports = {

    /*
     [{
     "internReferens" : 1,
     "amne": "KOMPLETTERING_AV_LAKARINTYG",
     "frageText": "",
     "externaKontakter": [],
     "intygsReferens": {
         "intygsId": "",
         "intygsTyp":  "fk7263",
         "patientId": "",
         "patientNamn": "",
         "signeringsDatum": "2016-09-16T13:37:00"
     },
     "kompletteringar": [{
     "falt": "",
     "text": ""
     }],
     "status":"",
     "vidarebefordrad": false,
     "senasteHandelse": "",
     "senasteHandelseDatum":  "2016-09-16T13:37:00",
     "frageSkickadDatum":  "2016-09-16T13:37:00",
     "meddelandeRubrik": "",
     "frageStallare": "",
     "frageSigneringsDatum":  "2016-09-16T13:37:00",
     "vardAktorNamn": "",
     "vardAktorHsaId": "",
     "externReferens": "",
     "svarsText": "",
     "svarSigneringsDatum":  "2016-09-16T13:37:00",
     "svarSkickadDatum":  "2016-09-16T13:37:00",
     "vardperson": {
     "hsaId": "",
     "namn": "",
     "enhetsId": ""
     }
     }]

     "required": [
     "internReferens",
     "amne",
     "frageText",
     "externaKontakter",
     "intygsReferens",
     "kompletteringar",
     "status",
     "vidarebefordrad"
     ]
     */
    get: function(fragaSvarOptions) {
        var fragaSvar = Object.assign({}, templateFragasvar); // Create clone so not all callers use the same instance

        fragaSvar.amne = fragaSvarOptions.amne;
        fragaSvar.internReferens = fragaSvarOptions.internReferens;
        var ir = {
            abc: '123',
            'intygsTyp': fragaSvarOptions.intygsType || 'tom',
            'intygsId': fragaSvarOptions.intygsId,
            'patientId': fragaSvarOptions.patientId
        };
        fragaSvar.intygsReferens = ir;
        fragaSvar.frageText = fragaSvarOptions.frageText;
        fragaSvar.svarsText = fragaSvarOptions.svarsText;
        fragaSvar.meddelandeRubrik = fragaSvarOptions.meddelandeRubrik;
        fragaSvar.status = fragaSvarOptions.status;

        fragaSvar.vardperson = fragaSvarOptions.vardperson;
        fragaSvar.externaKontakter = [];
        fragaSvar.vidarebefordrad = fragaSvarOptions.vidarebefordrad;

        if (fragaSvarOptions.kompletteringar.length > 0) {
            fragaSvar.kompletteringar.push(fragaSvarOptions.kompletteringar[0]);
        }
        logger.debug("FRAGA SVAR: " + JSON.stringify(fragaSvar) + " ir is: " + JSON.stringify(ir));
        return fragaSvar;
    }
};
