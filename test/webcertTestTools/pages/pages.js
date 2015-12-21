/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
 * Created by BESA on 2015-11-17.
 * Holds paths to page files for easy inclusion and intellisense support in specs.
 */
'use strict';

var intygPath = './intyg/'; // should point to intyg folder

var webcertBase = require('./webcert.base.page.js');

module.exports = {
    'webcertBase': webcertBase,
    'welcome': require('./welcome.page.js'),
    'sokSkrivIntyg' : {
        'pickPatient': require('./sokSkrivIntyg/sokSkrivIntyg.page.js'),
        'valjUtkastType': require('./sokSkrivIntyg/sokSkrivValjUtkastType.page.js')
    },
    'intyg': {
        fk:{
            '7263':{
                utkast:require(intygPath + 'fk.utkast.page.js'),
                intyg:require(intygPath + 'fk.intyg.page.js')
            }
        },
        ts:{
            diabetes:{
                utkast:require(intygPath + 'tsDiabetes.utkast.page.js'),
                intyg: require(intygPath + 'tsDiabetes.intyg.page.js')
            },
            bas:{
                utkast:require(intygPath + 'tsBas.utkast.page.js'),
                intyg: require(intygPath + 'tsBas.intyg.page.js')
            }
        }
    }
};
