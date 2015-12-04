'use strict';

module.exports = function() {
    this.setDefaultTimeout(60 * 1000);

    //Before scenario
    this.Before(function (scenario) {
    	console.log('before');
	});

	//After scenario
	this.Before(function (scenario) {
    	console.log('after');
	});
};
