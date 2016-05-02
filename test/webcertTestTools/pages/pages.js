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

/**
 * Created by BESA on 2015-11-17.
 * Holds paths to page files for easy inclusion and intellisense support in specs.
 */
'use strict';

var intygPath = './intyg/'; // should point to intyg folder
var WebcertBasePage = require('./webcert.base.page.js');
var BaseUtkastPage = require(intygPath + 'base.utkast.page.js');

module.exports = {
    'webcertBase': new WebcertBasePage(),
    'welcome': require('./welcome.page.js'),
    'sokSkrivIntyg': {
        'pickPatient': require('./sokSkrivIntyg/sokSkrivIntyg.page.js'),
        'valjUtkastType': require('./sokSkrivIntyg/sokSkrivValjUtkastType.page.js'),
        'visaIntyg': require('./sokSkrivIntyg/sokSkrivValjIntyg.page.js')
    },
    'intyg': {
        fk: {
            '7263': {
                utkast: require(intygPath + 'fk.7263.utkast.page.js'),
                intyg: require(intygPath + 'fk.7263.intyg.page.js')
            }
        },
        base: {
            intyg: require(intygPath + 'base.intyg.page.js'),
            utkast: new BaseUtkastPage()
        },
        luse: {
            utkast: require(intygPath + 'luse.utkast.page.js'),
            intyg: require(intygPath + 'luse.intyg.page.js')
        },
        lisu: {
            utkast: require(intygPath + 'lisu.utkast.page.js')
                //intyg: require(intygPath + 'lisu.intyg.page.js')
        },
        luaeFS: {
            utkast: require(intygPath + 'luae_fs.utkast.page.js'),
            intyg: require(intygPath + 'luae_fs.intyg.page.js')
        },
        luae_na: {
            utkast: require(intygPath + 'luae_na.utkast.page.js'),
            intyg: require(intygPath + 'luae_na.intyg.page.js')
        },
        ts: {
            diabetes: {
                utkast: require(intygPath + 'tsDiabetes.utkast.page.js'),
                intyg: require(intygPath + 'tsDiabetes.intyg.page.js')
            },
            bas: {
                utkast: require(intygPath + 'tsBas.utkast.page.js'),
                intyg: require(intygPath + 'tsBas.intyg.page.js')
            }
        }
    },
    'unsignedPage': require('./unsignedPage.js')
};
