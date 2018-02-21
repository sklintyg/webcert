/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
 * Helper for asserting dynamic text values.
 * Created by marced on 2016-04-29.
 */
/*globals protractor, process, logger */
'use strict';

var fs = require('fs');
var path = require('path');
var xml2js = require('xml2js');

module.exports = {

    /**
     *  Loads and parses texts from the specified xml and promises to return a flat object hash consisting of key:value
     * @param textXmlFile
     * @returns {IPromise<T>}
     */
    readTextsFromFkTextFile: function(textXmlFile) {
        var deferred = protractor.promise.defer();

        var parser = new xml2js.Parser();
        //cwd is expected to be webcert/test
        var fullPath = path.join(process.cwd(), '../src/main/resources/texts/' + textXmlFile);
        logger.debug('About to load fk xml text file:' + fullPath);
        fs.readFile(fullPath, function(err, data) {

            if (err) {
                logger.error('Error while reading file ' + err);
                deferred.reject(err);
                return;
            }

            parser.parseString(data, function(err, result) {
                if (err) {
                    logger.error('Error while parsing data ' + err);
                    deferred.reject(null);
                    return;
                }

                //xml2js converts the xml into an array of nested objects.
                //We return a simplified flattened structure to make it easy too look up a text
                //value in specs based on the id, such as texts['text.kod.1']
                var textArray = result.texter.text.reduce(function(acc, curr) {
                    acc[curr.$.id] = curr._;
                    return acc;
                }, {});

                //Handle tillaggsfragor also, they are under a different structure..
                if (result.texter.tillagg) {
                    result.texter.tillagg.forEach(function(tillaggsfraga) {
                        tillaggsfraga.tillaggsfraga.forEach(function(texter) {
                            texter.text.forEach(function(text) {
                                textArray[text.$.id] = text._;
                            });
                        });
                    });
                }

                logger.debug('Successfully parsed ' + Object.keys(textArray).length + ' texts from file ' + textXmlFile);
                deferred.fulfill(textArray);
            });
        });
        return deferred.promise;

    }
};
