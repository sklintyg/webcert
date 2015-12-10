/**
 * Created by BESA on 2015-11-25.
 * Holds helper functions for actions that are needed often in pages.
 */
/*globals protractor */
'use strict';

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