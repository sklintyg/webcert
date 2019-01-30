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

/*global testdata, logger*/

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
var utbDataJaneEwery = require('./utbDataLisjp_JE_LOR.js');
var utbDataMarcusGran = require('./utbDataLisjp_MG_LOR.js');
/*
 *	Stödfunktioner
 *
 */

function getDemoData(intyg, index, patient) {

    if (intyg.typ === 'Transportstyrelsens läkarintyg högre körkortsbehörighet') {
        return testdata.ts.bas.getRandom(intyg.id, patient);
    } else if (intyg.typ === 'Transportstyrelsens läkarintyg diabetes') {
        return testdata.ts.diabetes.getRandom(intyg.id, patient);
    } else if (intyg.typ === 'Läkarintyg FK 7263') {
        return testdata.fk['7263'].getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
        return testdata.fk.LUSE.getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarintyg för sjukpenning') {
        //Hårdkodad demo data för lisjp
        return demoDataLisjp.get(index, intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
        return testdata.fk.LUAE_NA.getRandom(intyg.id);
    } else if (intyg.typ === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
        return testdata.fk.LUAE_FS.getRandom(intyg.id);
    } else if (intyg.typ === 'Dödsbevis') {
        return testdata.skv.db.getRandom(intyg.id);
    } else if (intyg.typ === 'Dödsorsaksintyg') {
        return testdata.soc.doi.getRandom(intyg.id);
    }
}

/*
 *	Test steg
 *
 */
When(/^jag fyller i alla nödvändiga fält för intyget med demodata "([^"]*)"$/, function(index) {
    if (!this.intyg.typ) {
        throw 'intyg.typ odefinierad.';
    } else {
        this.intyg = getDemoData(this.intyg, index, this.patient);
        logger.silly(this.intyg);
        return fillIn(this);
    }
});

When(/^jag fyller i alla nödvändiga fält för intyget med statistikdata "([^"]*)"$/, function(index) {
    if (!this.intyg.typ) {
        throw 'intyg.typ odefinierad';
    } else {
        this.intyg = statistikData.get(index, this.intyg.id);
        logger.silly(this.intyg);
        return fillIn(this);
    }
});


// Data för utbildningsmiljön. Ett step def per läkare.
When(/^jag fyller i alla nödvändiga fält för intyget med utbdata för Jane Ewery "([^"]*)"$/, function(index) {
    if (!this.intyg.typ) {
        throw 'intyg.typ odefinierad.';
    } else {
        this.intyg = utbDataJaneEwery.get(index, this.intyg.id);
        logger.silly(this.intyg);
        return fillIn(this);
    }
});


When(/^jag fyller i alla nödvändiga fält för intyget med utbdata för Marcus Gran "([^"]*)"$/, function(index) {
    if (!this.intyg.typ) {
        throw 'intyg.typ odefinierad.';
    } else {
        this.intyg = utbDataMarcusGran.get(index, this.intyg.id);
        logger.silly(this.intyg);
        return fillIn(this);
    }
});