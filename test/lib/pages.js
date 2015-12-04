/**
 * Created by BESA on 2015-11-17.
 */
'use strict';

var appPath = __dirname + '/../../web/src/main/webapp/'; // should be webapp
var intygPath = __dirname + '/../intygpages/'; // should be webapp

var pages = {
    'intygpages': {
        'fkUtkast': require(intygPath + 'fk.utkast.page.js'),
        'fkIntyg': require(intygPath + 'fk.intyg.page.js')
    },
    'welcome': require(appPath + 'welcome.page.js'),
    'app': {
        'views' : {
            'sokSkrivIntyg': require(appPath + 'app/views/sokSkrivIntyg/sokSkrivIntyg.page.js')
        }
    }
};

module.exports = pages;
