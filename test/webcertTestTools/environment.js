/**
 * Created by BESA on 2015-11-17.
 */

var envConfig = null;
if(process.env.WEBCERT_URL) {
    envConfig = process.env;
} else {
    envConfig = require('./../webcertTestTools/envConfig.json').dev; // override if not running via grunt ie IDEA.
}

module.exports = {
    'envConfig': envConfig,
};