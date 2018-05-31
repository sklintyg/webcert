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

/*global intyg, testdata, person, logger*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


/*jshint maxcomplexity:false */
var fillIn = require('../fillIn').fillIn;
var demoDataLisjp = require('./demoDataLisjp.js');
var statistikData = require('./statistikData.js');
/*
 *	Stödfunktioner
 *
 */

function getDemoData(typ, index) {

    /*if (!intyg.id) {
    	global.intyg.id = testdataHelper.generateTestGuid();
    }*/
    var id = intyg.id;

    if (typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
        return testdata.ts.bas.getRandom(id, person);
    } else if (typ === 'Transportstyrelsens läkarintyg diabetes') {
        return testdata.ts.diabetes.getRandom(id, person);
    } else if (typ === 'Läkarintyg FK 7263') {
        return testdata.fk['7263'].getRandom(id);
    } else if (typ === 'Läkarutlåtande för sjukersättning') {
        return testdata.fk.LUSE.getRandom(id);
    } else if (typ === 'Läkarintyg för sjukpenning') {
        //Hårdkodad demo data för lisjp
        return demoDataLisjp.get(index, id);
    } else if (typ === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
        return testdata.fk.LUAE_NA.getRandom(id);
    } else if (typ === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
        return testdata.fk.LUAE_FS.getRandom(id);
    } else if (typ === 'Dödsbevis') {
        return testdata.skv.db.getRandom(id);
    } else if (typ === 'Dödsorsaksintyg') {
        return testdata.soc.doi.getRandom(id);
    }
}

function getStatistikData(typ, index) {
    var id = intyg.id;
    return statistikData.get(index, id);
}

/*
 *	Test steg
 *
 */
Given(/^jag fyller i alla nödvändiga fält för intyget med demodata "([^"]*)"$/, function(index) {
    if (!intyg.typ) {
        throw 'intyg.typ odefinierad.';
    } else {
        global.intyg = getDemoData(intyg.typ, index);
        logger.silly(intyg);
        return fillIn(global.intyg);
    }
});

Given(/^jag fyller i alla nödvändiga fält för intyget med statistikdata "([^"]*)"$/, function(index) {
    if (!intyg.typ) {
        throw 'intyg.typ odefinierad';
    } else {
        global.intyg = getStatistikData(intyg.typ, index);
        logger.silly(intyg);
        return fillIn(global.intyg);
    }
});
