'use strict';

var shuffle = require('./../helpers/testdataHelper.js').shuffle;

module.exports = {
    data: {
        smittskydd: [true, false]
    },

    sjukintyg: {
        getRandom: function() {
            return {
                typ:'Läkarintyg FK 7263',
                smittskydd: true,
                arbetsformaga: {
                    nedsattMed50: {
                        from: '2015-12-07',
                        tom: '2016-03-31'
                    }
                }
            };
        }
    }
};