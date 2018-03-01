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

/*global logger */

'use strict';
/*jshint newcap:false */


/*
 *	Stödlib och ramverk
 *
 */

var testdataHelper = require('common-testtools').testdataHelper;

/*
 *	Stödfunktioner
 *
 */
function idag(modifyer) {
    if (!modifyer) {
        modifyer = 0;
    }

    var datum = new Date();

    datum.setDate(datum.getDate() + modifyer);

    return testdataHelper.dateFormat(datum);
}

/*
 *	Demo Data Lisjp
 *
 */
var demoDataLisjp = [{
    "smittskydd": false,
    "nuvarandeArbeteBeskrivning": "Föräldraledigt för vård av barn",
    "baseratPa": {
        "minUndersokningAvPatienten": idag()
    },
    "sysselsattning": {
        "typ": "FORALDRALEDIG"
    },
    "diagnos": {
        "kod": "J36"
    },
    "funktionsnedsattning": "Personen har nedsatt förmåga att fungera fysiskt och psykiskt",
    "aktivitetsbegransning": "Kan inte lyfta armarna över axelhöjd",
    "arbetsformaga": {
        "nedsattMed50": {
            "from": idag(),
            "tom": idag(14)
        }
    },
    "arbetsformagaFMB": "Prognosen är oklar",
    "arbetstidsforlaggning": {
        "val": "Nej"
    },
    "atgarder": [{
        "namn": "Arbetsträning",
        "beskrivning": "Arbetsträning-beskrivning",
        "key": "ARBETSTRANING"
    }],
    "prognosForArbetsformaga": {
        "name": "Återgång i nuvarande sysselsättning är oklar."
    },
    "ovrigt": "Detta är ett Demo-Intyg"
}];


module.exports = {
    get: function(index, id) {
        var obj = demoDataLisjp[index];
        obj.id = id; /*("id": testdataHelper.generateTestGuid(),)*/
        obj.typ = "Läkarintyg för sjukpenning";
        logger.silly(JSON.stringify(obj));
        return obj;
    }


};
