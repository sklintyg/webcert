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

'use strict';
var environment = require('./environment.js');
var testdata = require('./testdata');
var utkastTextmap = require('./testdata/utkastTextmap.js');
var intygTemplates = require('./testdata/intygTemplates.js');
var pages = require('./pages/pages.js');
var helpers = require('./helpers/helpers.js'); // The order is important. Helpers requires pages.
var arendeFromJsonFactory = require('./util/arendeFromJsonFactory.js');
var intygFromJsonFactory = require('./util/intygFromJsonFactory.js');
var intygGenerator = require('./util/intygGenerator.util.js');
var restUtil = require('./util/rest.util.js');
module.exports = {
    envConfig: environment.envConfig,
    testdata: testdata,
    utkastTextmap: utkastTextmap,
    intygTemplates: intygTemplates,
    pages: pages,
    helpers: helpers,
    arendeFromJsonFactory: arendeFromJsonFactory,
    intygFromJsonFactory: intygFromJsonFactory,
    intygGenerator: intygGenerator,
    restUtil: restUtil
};
