'use strict';

var environment = require('./environment.js');
var testdata = require('./testdata/testdata.js');
var utkastTextmap = require('./testdata/utkastTextmap.js');
var intygTemplates = require('./testdata/intygTemplates.js');
var pages = require('./pages.js');
var helpers = require('./helpers.js'); // The order is important. Helpers requires pages.

module.exports = {
    envConfig: environment.envConfig,
    testdata: testdata,
    utkastTextmap: utkastTextmap,
    intygTemplates: intygTemplates,
    pages: pages,
    helpers: helpers // The order is important. Helpers requires pages.
}