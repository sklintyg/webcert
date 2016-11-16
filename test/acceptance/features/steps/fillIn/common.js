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

/* globals pages, logger, JSON, Promise */

'use strict';
var utkastPage;
module.exports = {
    fillInEnhetAdress: function() {
        return utkastPage.angeEnhetAdress(global.user.enhetsAdress).then(function() {
            logger.info('OK - angeEnhetAdress :' + JSON.stringify(global.user.enhetsAdress));
        }, function(reason) {
            throw ('FEL, angeEnhetAdress,' + reason);
        });


    },
    setPatientAdressIfNotGiven: function() {
        var isFk7263 = global.intyg.typ.indexOf('7263') >= 0;

        if (global.person.adress && !isFk7263 && global.user.origin !== 'DJUPINTEGRATION') {
            return utkastPage.angePatientAdress(global.person.adress).then(function() {
                logger.info('OK - setPatientAdress :' + JSON.stringify(global.person.adress));
            }, function(reason) {
                throw ('FEL, setPatientAdress,' + reason);
            });
        } else {
            logger.info('Ingen adress ändras');
            return Promise.resolve();
        }

    },
    fillIn: function(intyg) {
        utkastPage = pages.getUtkastPageByType(intyg.typ);
        return Promise.all([this.setPatientAdressIfNotGiven(), this.fillInEnhetAdress()]);
    }

};
