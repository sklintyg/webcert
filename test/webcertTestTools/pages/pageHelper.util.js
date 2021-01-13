/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
/*globals protractor, Promise, logger, browser */
'use strict';

var testTools = require('common-testtools');
testTools.protractorHelpers.init('certificate-content-container');

var moveAndSendKeys;

if (testTools.protractorHelpers) {
  moveAndSendKeys = testTools.protractorHelpers.moveAndSendKeys;
} else {
  moveAndSendKeys = function(elm, keys, description) {
    return elm.sendKeys(keys).then(function() {
      return logger.silly('sendKeys OK - ' + description);
    }, function(reason) {
      console.trace(reason);
      throw ('FEL, ' + description + ', ' + reason);
    });
  };
}

module.exports = {
  moveAndSendKeys: moveAndSendKeys,
  clickAll: function(elementArray, elementTextsArray) {
    if (!elementTextsArray) {
      return Promise.resolve();

    }
    return elementArray.filter(function(elem) {

      return elem.getText().then(function(text) {
        return (elementTextsArray.indexOf(text) >= 0);
      });
    }).then(function(filteredElements) {

      return filteredElements.forEach(function(element, i) {
        filteredElements[i].getText().then(function(description) {

          moveAndSendKeys(filteredElements[i], protractor.Key.SPACE, description);
        });
      });
    });
  },
  selectAllCheckBoxes: function(elementArray, elementTextsArray) {
    if (!elementTextsArray) {
      return Promise.resolve();

    }
    return elementArray.filter(function(elem) {

      return elem.getText().then(function(text) {
        return (elementTextsArray.indexOf(text) >= 0);
      });
    }).then(function(filteredElements) {

      return filteredElements.forEach(function(element, i) {
        filteredElements[i].getText().then(function(description) {

          filteredElements[i].click();
        });
      });
    });
  },
  selectCheckBoxesById: function(elementIds) {
    if (!elementIds) {
      return Promise.resolve();

    }
    var promiseArr = [];
    elementIds.forEach(function(elementId, i) {
      promiseArr.push(moveAndSendKeys(element(by.id(elementId)), protractor.Key.SPACE,
          'Checking checkbox (' + i + '/' + elementIds.length + ') with id "' + elementId + '"'));
    });

    return Promise.all(promiseArr);
  },

  hasHogreKorkortsbehorigheter: function(korkortstyper) {
    function findArrayElementsInArray(targetArray, compareArray) {
      // find all elements in targetArray matching any elements in compareArray
      var result = targetArray.filter(function(element) {
        return (compareArray.indexOf(element) >= 0);
      });

      return result;
    }

    var td = require('./../testdata/testvalues.js').ts;
    var foundHogreBehorigheter = findArrayElementsInArray(korkortstyper, td.korkortstyperHogreBehorighet);
    return foundHogreBehorigheter.length > 0;
  },
  smallDelay: function() {
    return browser.sleep(100);
  },
  mediumDelay: function() {
    return browser.sleep(500);
  },
  largeDelay: function() {
    return browser.sleep(1000);
  }
};
