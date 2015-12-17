var restHelper = wcTestTools.helpers.testdata;

describe('Generate fk utkast', function() {

    it('should generate an fk7263 utkast', function() {
        restHelper.createUtkast('fk7263').then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;
            expect(intygsId).not.toBe(null);
        });
    });
});