/**
 * Created by BESA on 2015-11-25.
 */
'use strict';

var fs = require('fs');

function _writeScreenShot(data, filename) {
    // abstract writing screen shot to a file
    var stream = fs.createWriteStream(filename);
    stream.write(new Buffer(data, 'base64'));
    stream.end();
}

module.exports = {
    takeScreenshots: function _takeScreenshots() {

        // Check our custom property if addExpectationResult has already been overridden
        if (jasmine.Spec.prototype.itrOriginalAddExpectationResult) {
            console.log('takeScreenshots already activated!');
            return;
        }

        // Jasmine 2.1
        jasmine.Spec.prototype.itrOriginalAddExpectationResult = jasmine.Spec.prototype.addExpectationResult;
        jasmine.Spec.prototype.addExpectationResult = function() {
            if (!arguments[0]) {
                // take screenshot
                // this.description and arguments[1].message can be useful to constructing the filename.
                console.log('ERROR! Taking screenshot!');
                console.log(this.description);
                console.log(arguments[1].message);
                browser.takeScreenshot().then(function(png) {
                    _writeScreenShot(png, 'exception.png');
                });
            }
            return jasmine.Spec.prototype.itrOriginalAddExpectationResult.apply(this, arguments);
        };
    },
    printClientLog: function _printClientLog() {
        browser.manage().logs().get('browser').then(function(browserLog) {
            console.log('log: ' + require('util').inspect(browserLog));
        });
    }
};