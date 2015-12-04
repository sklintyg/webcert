'use strict';

module.exports = function() {
    this.setDefaultTimeout(100 * 1000);

    //Before scenario
    this.Before(function (scenario) {
    	console.log('before');
	});

	//After scenario
	this.After(function (scenario) {
    	console.log('after');
	});
};
