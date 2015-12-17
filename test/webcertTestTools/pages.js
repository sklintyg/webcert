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
