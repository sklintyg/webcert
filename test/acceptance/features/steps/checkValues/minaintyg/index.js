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
/* globals logger, Promise, person, wcTestTools */

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;

module.exports = {
    fk: {
        LUSE: function(intyg) {
            return Promise.all([
                expect(element(by.id('patient-crn')).getText()).to.eventually.equal(person.id.replace('-', ''))
                .then(function(value) {
                    logger.info('OK - Personnummer');
                }, function(reason) {
                    return Promise.reject('FEL, Personnummer: ' + reason);
                }),

                //Baserat på
                checkBaseratPa(intyg.baseratPa)
                .then(function(value) {
                    logger.info('OK - Baseras på');
                }, function(reason) {
                    return Promise.reject('FEL, Baseras på: ' + reason);
                }),

                //Medicinska utredningar
                checkMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
                .then(function(value) {
                    logger.info('OK - Medicinska utredningar');
                }, function(reason) {
                    return Promise.reject('FEL, Medicinska utredningar: ' + reason);
                })
            ]);
        }
    }
};



function checkBaseratPa(baseratPa) {
    var minUndersokningText = testdataHelper.dateToText((baseratPa.minUndersokningAvPatienten));
    var journaluppgifterText = testdataHelper.dateToText((baseratPa.journaluppgifter));
    var anhorigBeskrivningText = testdataHelper.dateToText((baseratPa.anhorigsBeskrivning));
    // var annatText = testdataHelper.dateToText((baseratPa.annat));
    // var annatBeskrivningText = ejAngivetIfNull(baseratPa.annatBeskrivning);
    var personligKannedomText = testdataHelper.dateToText((baseratPa.personligKannedom));

    return Promise.all([
        expect(element(by.id('undersokningAvPatienten')).getText()).to.eventually.contain(minUndersokningText),
        expect(element(by.id('journaluppgifter')).getText()).to.eventually.contain(journaluppgifterText),
        expect(element(by.id('anhorigsBeskrivningAvPatienten')).getText()).to.eventually.contain(anhorigBeskrivningText),
        // expect(element(by.id('undersokningAvPatienten')).getText()).to.eventually.contain(annatText),
        // expect(element(by.id('undersokningAvPatienten')).getText()).to.eventually.contain(annatBeskrivningText),
        expect(element(by.id('kannedomOmPatient')).getText()).to.eventually.contain(personligKannedomText)
    ]);
}


function checkMedicinskaUtredningar(andraMedicinskaUtredningar) {
    var field = element(by.cssContainingText('.body-row', 'Är utlåtandet även baserat på andra medicinska utredningar eller underlag?'));

    if (andraMedicinskaUtredningar) {
        var promiseArr = [];

        promiseArr.push(expect(field.getText()).to.eventually.contain('Ja'));

        for (var i = 0; i < andraMedicinskaUtredningar.length; i++) {
            var typEL = element(by.id('underlag_' + i + '_typ'));
            var datumEL = element(by.id('underlag_' + i + '_datum'));
            var infoEL = element(by.id('underlag_' + i + '_hamtasFran'));

            var utredningDatum = testdataHelper.dateToText(andraMedicinskaUtredningar[i].datum);
            promiseArr.push(expect(typEL.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].underlag));
            promiseArr.push(expect(datumEL.getText()).to.eventually.equal(utredningDatum));
            promiseArr.push(expect(infoEL.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].infoOmUtredningen));
        }
        return Promise.all(promiseArr);
    } else if (!andraMedicinskaUtredningar) {
        return expect(field.getText()).to.eventually.contain('Nej');
    }
}
