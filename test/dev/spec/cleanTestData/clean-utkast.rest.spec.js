'use strict';

var testdataHelper = helpers.testdata;

describe('Clean utkast via rest', function() {

    it('should login rest client and clean all utkast', function() {
        testdataHelper.deleteAllUtkast();
    });
});