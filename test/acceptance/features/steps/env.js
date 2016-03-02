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

/* globals browser, logger */
'use strict';

module.exports = function() {
    this.setDefaultTimeout(200 * 1000);

    //After scenario
    this.After(function(scenario, callback) {

        //Ska intyg rensas bort efter scenario?
        var rensaBortIntyg = true;
        var tagArr = scenario.getTags();
        for (var i = 0; i < tagArr.length; i++) {
            if (tagArr[i].getName() === '@keepIntyg') {
                rensaBortIntyg = false;
            }
        }

        if (scenario.isFailed()) {
            logger.info('scenario failed');
            browser.takeScreenshot().then(function(png) {
                //var base64Image = new Buffer(png, 'binary').toString('base64');
                var decodedImage = new Buffer(png, 'base64').toString('binary');
                scenario.attach(decodedImage, 'image/png', function(err) {
                    callback(err);
                });
            });

        } else {

            if (process.env.DATABASE_PASSWORD && rensaBortIntyg) {
                callback();
                /*
                Bortkommenterad pga att vi behöver några intyg att arbeta med.
                Vi kan aktivera denna funktion sen när vi löst problemet med att skapa 
                nya intyg då de behövs
                */
                // require('./db_actions/db.js').removeCert(global.intyg.id, callback);
            } else {
                logger.info('Behåller skapat testintyg');
                callback();
            }
        }
    });

    this.Before(function(scenario, callback) {
        global.scenario = scenario;
        callback();
    });

    logger.on('logging', function(transport, level, msg, meta) {
        if (global.scenario) {
            global.scenario.attach(msg);
        }
    });

};
