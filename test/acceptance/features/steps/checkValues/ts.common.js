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

/* globals logger, pages, Promise */

'use strict';

var tsBasIntygPage = pages.intyg.ts.bas.intyg;


// Vi behöver inte kontrollera address (adress i PU prioriteras)
/*function checkPatientadress(adressObj) {
    return Promise.all([
        expect(tsBasIntygPage.patientAdress.postadress.getText()).to.eventually.contain(adressObj.postadress),
        expect(tsBasIntygPage.patientAdress.postnummer.getText()).to.eventually.contain(adressObj.postnummer),
        expect(tsBasIntygPage.patientAdress.postort.getText()).to.eventually.contain(adressObj.postort)
    ]);
}*/
module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Transportstyrelsens läkarintyg diabetes & Transportstyrelsens läkarintyg högre körkortsbehörighet (gemensama fält) --');

        var promiseArr = [];

        var selectedTypes = intyg.korkortstyper.sort(function(a, b) {
            var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'];
            return allTypes.indexOf(a) - allTypes.indexOf(b);
        });
        selectedTypes = selectedTypes.join(', ');



        // if (intyg.isKopia) {
        //logger.info('Kontrollerar inte angiven patientadress pga att intyget är en kopia och kan ha automatiskt uppdaterad adress');

        logger.info('Kontrollera inte address då denna är hämtad från PU och inget testfallet angett');
        /* Kontrollera inte address då denna är hämtad från PU och inget testfallet angett */
        /*promiseArr.push(checkPatientadress(person.adress).then(function(value) {
            logger.info('OK - checkPatientadress = ' + value);
        }, function(reason) {
            throw ('FEL - checkPatientadress: ' + reason);
        }));*/
        // }

        promiseArr.push(expect(tsBasIntygPage.intygetAvser.getText()).to.eventually.contain(selectedTypes).then(function(value) {
            logger.info('OK - Körkortstyper = ' + value);
        }, function(reason) {
            throw ('FEL - Körkortstyper: ' + reason);
        }));

        if (intyg.identitetStyrktGenom.indexOf('Försäkran enligt 18 kap') > -1) {
            var txt = 'Försäkran enligt 18 kap 4 §';
            promiseArr.push(expect(tsBasIntygPage.idStarktGenom.getText()).to.eventually.contain(txt).then(function(value) {
                logger.info('OK - Identitet styrkt genom = ' + value);
            }, function(reason) {
                throw ('FEL - Identitet styrkt genom: ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.idStarktGenom.getText()).to.eventually.contain(intyg.identitetStyrktGenom).then(function(value) {
                logger.info('OK - Identitet styrkt genom = ' + value);
            }, function(reason) {
                throw ('FEL - Identitet styrkt genom: ' + reason);
            }));
        }

        //Bedömning
        if (intyg.bedomning.stallningstagande !== 'Kan inte ta ställning') {
            promiseArr.push(expect(tsBasIntygPage.falt1.bedomning.getText()).to.eventually.contain(selectedTypes).then(function(value) {
                logger.info('OK - Bedömningen avser körkortstyper = ' + value);
            }, function(reason) {
                throw ('FEL - Bedömningen avser körkortstyper: ' + reason);
            }));
        } else {
            promiseArr.push(expect(tsBasIntygPage.falt1.bedomning.getText()).to.eventually.contain(intyg.bedomning.stallningstagande).then(function() {
                logger.info('OK bedömning -' + intyg.bedomning.stallningstagande);
            }, function(reason) {
                throw ('FEL bedömning- ' + intyg.bedomning.stallningstagande + ' ' + reason);
            }));
        }

        return Promise.all(promiseArr);
    }
};
