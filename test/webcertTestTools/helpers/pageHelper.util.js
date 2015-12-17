/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
 * Created by BESA on 2015-11-25.
 * Holds helper functions for actions that are needed often in pages.
 */
/*globals protractor */
'use strict';
var testdata = require('./../testdata/testdata.js')
module.exports = {
    clickAll: function(elementArray, elementTextsArray) {
        // filter all elemenets matching elementTextsArray
        elementArray.filter(function(elem) {
            return elem.getText().then(function(text) {
                return (elementTextsArray.indexOf(text) >= 0);
            });
        }).then(function(filteredElements) {
            //filteredElements is the list of filtered elements
            for (var i = 0; i < filteredElements.length; i++) {
                filteredElements[i].sendKeys(protractor.Key.SPACE);
            }
        });
    },
    hasHogreKorkortsbehorigheter: function(korkortstyper) {
        function findArrayElementsInArray(targetArray, compareArray) {
            // find all elements in targetArray matching any elements in compareArray
            var result = targetArray.filter(function(element) {
                return (compareArray.indexOf(element) >= 0);
            });

            return result;
        }

        var foundHogreBehorigheter = findArrayElementsInArray(korkortstyper, testdata.korkortstyperHogreBehorighet);
        return foundHogreBehorigheter.length > 0;
    }
};
