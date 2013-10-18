'use strict';

/*
 * Dashboard Services
 */
angular.module('dashboard.services', []);
angular.module('dashboard.services').factory('dashBoardService', [ '$http', '$log', '$modal', function($http, $log, $modal) {

    /*
     * Load certificate list of specified type(unsigned, with unanswered
     * questions and ready to mass sign) TODO: Add careUnit and optionally
     * careWard +
     */

    function _getCertificates(requestConfig, callback) {
        $log.debug("_getCertificates type:" + requestConfig.type);
        // var restPath = '/api/certificates/' + dataType;
        var restPath = '/jsonmocks/' + requestConfig.type;
        $http.get(restPath).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });
    }

    /*
     * Load questions and answers data for
     */
    function _getQA(callback) {
        $log.debug("_getQA");
        var restPath = '/api/fragasvar';
        $http.get(restPath).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });

    }

    /*
     * Toggle vidarebefordrad state
     */
    function _setVidareBefordradState(id, isVidareBefordrad, callback) {
        $log.debug("_setVidareBefordradState");
        var restPath = '/moduleapi/fragasvar/' + id + "/setDispatchState";
        $http.put(restPath, isVidareBefordrad.toString()).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
            //callback(null); // error test 
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });
    }

    function _showErrorMessageDialog(message, callback) {
    	
      var msgbox = $modal.open({
            templateUrl: '/views/partials/error-dialog.html',
            controller: ErrorMessageDialogInstanceCtrl,
            resolve: { bodyText: function() { return angular.copy(message);}}
      });

      msgbox.result.then(function(result) {
      	if (callback) {
      		callback(result)
        }
      }, function() {});
    }

    function _showDialog(title, bodyText, yesCallback, noCallback, noDontAskCallback, callback) {
    	
      var msgbox = $modal.open({
            templateUrl: '/views/partials/general-dialog.html',
            controller: DialogInstanceCtrl,
            resolve: {
            	title: function() { return angular.copy(title); },
            	bodyText: function() { return angular.copy(bodyText); },
            	yesCallback: function() { return yesCallback; },
            	noCallback: function() { return noCallback; },
            	noDontAskCallback: function() { return noDontAskCallback; }
            }
      });

      msgbox.result.then(function(result) {
      	if (callback) {
      		callback(result)
        }
      }, function() {});
    }
    
    // Return public API for the service
    return {
        getCertificates : _getCertificates,
        getQA : _getQA,
        setVidareBefordradState : _setVidareBefordradState,
        showErrorMessageDialog : _showErrorMessageDialog,
        showDialog : _showDialog
    }
} ]);


var ErrorMessageDialogInstanceCtrl = function ($scope, $modalInstance, bodyText) {
	$scope.bodyText = bodyText;
};

var DialogInstanceCtrl = function ($scope, $modalInstance, title, bodyText, yesCallback, noCallback, noDontAskCallback) {
	$scope.title = title;
	$scope.bodyText = bodyText;
	$scope.noDontAskVisible = noDontAskCallback != undefined;
	$scope.yes = function(result) { yesCallback(); $modalInstance.close(result) };
	$scope.no = function(result) { noCallback(); $modalInstance.dismiss('cancel') };
	$scope.noDontAsk = function(result) { noDontAskCallback(); $modalInstance.dismiss('cancel') };
};
