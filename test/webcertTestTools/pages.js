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

var appPath = __dirname + '/../../web/src/main/webapp/'; // should point to webapp
var intygPath = __dirname + '/../intygpages/'; // should point to intygpages folder

module.exports = {
    'intygpages': {
        'fk7263Utkast': require(intygPath + 'fk.utkast.page.js'),
        'fkIntyg': require(intygPath + 'fk.intyg.page.js'),
        'ts-diabetesUtkast': require(intygPath + 'tsDiabetes.utkast.page.js'),
        'tsDiabetesIntyg': require(intygPath + 'tsDiabetes.intyg.page.js'),
        'ts-basUtkast': require(intygPath + 'tsBas.utkast.page.js'),
        'tsBasIntyg': require(intygPath + 'tsBas.intyg.page.js')
    },
    'welcome': require(appPath + 'welcome.page.js'),
    'app': {
        'views' : {
            'sokSkrivIntyg': require(appPath + 'app/views/sokSkrivIntyg/sokSkrivIntyg.page.js')
        }
    }
};
