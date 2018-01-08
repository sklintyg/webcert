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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luaeFS.intyg;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var restUtil = wcTestTools.restUtil;

// used for some assertions of non-transformed values.
var luaefsTemplate = require('webcert-testtools/testdata/intyg.luae_fs.minimal.json');

describe('Validera visning av Intyg', function() {

    var intygsId;

    var data = {
        baseratPa: {
            minUndersokningAvPatienten: '2015-09-09',
            journaluppgifter: '2015-09-10',
            anhorigsBeskrivning: '2015-09-11',
            annat: '2015-09-12',
            annatBeskrivning: luaefsTemplate.annatGrundForMUBeskrivning,
            personligKannedom: '2015-01-07'
        },
        andraMedicinskaUtredningar: [{
            datum: '2015-09-03',
            underlag: 'Underlag från psykolog',
            infoOmUtredningen: 'Skickas med posten'
        }, {
            datum: '2015-09-04',
            underlag: 'Underlag från habiliteringen',
            infoOmUtredningen: 'Arkivet'
        }],
        funktionsnedsattning: {
            debut: 'Skoldansen',
            paverkan: 'Haltar när han dansar'
        },
        diagnos: {
            diagnoser: [{
                kod: 'S47',
                beskrivning: 'Klämskada skuldra'
            },{
                kod: 'J22',
                beskrivning: 'Icke specificerad akut infektion i nedre luftvägarna'
            }]
        },
        ovrigt: 'Detta skulle kunna innebära sämre möjlighet att få ställa upp i danstävlingar',
        kontaktMedFk: true,
        kontaktAnledning: 'Vill stämma av ersättningen',
        tillaggsfragor: [{
            id: 9001,
            svar: 'Tämligen'
        }, {
            id: 9002,
            svar: 'Minst 3 fot'
        }]
    };

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Something...', function() {
            var intyg = intygFromJsonFactory.defaultLuaefs();
            intygsId = intyg.id;
            restUtil.createIntyg(intyg);
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Verifiera intyget', function() {
           IntygPage.verify(data);
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
    });

});
