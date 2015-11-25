/**
 * Created by BESA on 2015-11-17.
 */

var appPath = __dirname + '/../../web/src/main/webapp/'; // should be webapp
var intygPath = __dirname + '/../intygpages/'; // should be webapp

var paths = {
    'intygpages': {
        'fkUtkast': intygPath + 'fk.utkast.page.js',
        'fkIntyg': intygPath + 'fk.intyg.page.js'
    },
    'welcome': appPath + 'welcome.page.js',
    'app': {
        'views' : {
            'sokSkrivIntyg': appPath + 'app/views/sokSkrivIntyg/sokSkrivIntyg.page.js'
        }
    }
};

module.exports = paths;