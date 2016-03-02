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

/* globals browser */

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
