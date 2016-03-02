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

/* globals logger */

'use strict';

module.exports = {
    boolTillJaNej: function(val) {
        if (val) {
            return 'Ja';
        } else {
            return 'Nej';
        }
    },
    testElement: function(_typ, _element) {
        var ele = element(by.id(_element));
        if (!_typ) {
            _typ = 'Ej angivet';
        }
        logger.info('Kontrollerar ' + _element + ' : ' + _typ);
        expect(ele.getText()).to.eventually.equal(_typ);
    },

    genericAssert: function(_val, _element) {
        logger.info('genericAssert-function is deprecated, does not fail on error');
        var ele = element(by.id(_element));
        if (_val !== null) {
            // logger.info('Kontrollerar '+_element+' : '+ _val);
            expect(ele.getText()).to.eventually.equal(_val).then(function(value) {
                logger.info('OK - ' + _element + ' = ' + value);
            }, function(reason) {
                logger.info('FEL, ' + _element + ', ' + reason);
            });
        }
    },
    getDateForAssertion: function(_date) {
        var monthNames = ['januari', 'februari', 'mars', 'april', 'maj', 'juni', 'juli', 'augusti', 'september', 'oktober', 'november', 'december'];
        var dateObj, month, day, year;
        var regExp = /^0[0-9].*$/;
        if (typeof _date === 'undefined') {
            dateObj = new Date();
            month = monthNames[dateObj.getUTCMonth()];
            day = dateObj.getUTCDate().toString();
            if (regExp.test(day)) {
                day = day.replace('0', '');
            }
            year = dateObj.getUTCFullYear().toString();
            return day.concat(' ', month, ' ', year);
        } else {
            var _split = _date.split('-');
            month = monthNames[_split[1] - 1];
            day = _split[2];
            if (regExp.test(day)) {
                day = day.replace('0', '');
            }
            year = _split[0];
            return day.concat(' ', month, ' ', year);
        }
    }

};
