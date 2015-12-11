'use strict';
// exports = {
// 	hej:'hej',
// 	testdata:require('./testdata/testdata.js'),
// 	pages: require('./pages.js'),
//     helpers: require('./helpers.js')
// };


// module.exports = function() {
// 	var test = 'hehe';
// 	this.pages = require('./pages.js');
// //     helpers: require('./helpers.js')

// };

exports.pages = require('./pages.js');
exports.testdata = require('./testdata/testdata.js');
exports.helpers = require('./helpers.js');

if(global.envConfig){
	exports.envConfig = global.envConfig;
}
else{
	exports.envConfig = require('./envConfig.json').dev;
}

