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
