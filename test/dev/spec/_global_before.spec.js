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
 * Adds a custom matcher that can be used in all jasmine specs to expect the (eventual) invisibility of an element.
 * Will wait up to 5000 ms before giving up.
 *
 * Usage: expect(<elementlocator>).toDisappear();
 */
beforeEach(function() {
  jasmine.addMatchers({

    toDisappear: function(util, customEqualityTesters) {
      return {
        compare: function(actual, expected) {
          return {
            pass: browser.wait(protractor.ExpectedConditions.invisibilityOf(actual), 5000).then(function() {
              return true
            }, function() {
              return false
            }),
            message: 'Expected element ' + actual.parentElementArrayFinder.locator_.toString() + ' to be invisible!'
          };
        }
      };
    }

  })
});
