'use strict';

var testdataHelper = wcTestTools.helpers.testdata;

describe('Clean utkast via rest', function() {

    it('should login rest client and clean all utkast', function() {
        testdataHelper.deleteAllUtkast();
    });
});