'use strict';
/**
 * Common util components and services for cross cutting concerns in the app.
 * 
 * @author marced
 * 
 */

/**
 * Interceptor that decorates all GET requests made by the $http service. Can be
 * used by the modules as a common component. To hook up the interceptor, simply
 * config the http provider for the app like this (in 1.1.5):
 * 
 * app.config(function ($httpProvider) {
 * $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster'); })
 * 
 */
angular.module('wc.utils', []);
angular.module('wc.utils').factory('httpRequestInterceptorCacheBuster', function($q) {
    return {
        request : function(config) {
            // Don't mess with view loading, ok if cached..
            if (config.url.indexOf(".html") == -1) {
                var sep = config.url.indexOf('?') === -1 ? '?' : '&';
                config.url = config.url + sep + 'cacheSlayer=' + new Date().getTime();
            }
            return config || $q.when(config);
        }
    };
});

/*
 * Generic Dialog services
 */
angular.module('wc.utils').factory(
        'wcDialogService',
        [
                '$http',
                '$log',
                '$modal',
                function($http, $log, $modal) {

                    function _showErrorMessageDialog(message, callback) {

                        var msgbox = $modal.open({
                            template : 
                                ' <div class="modal-header">' 
                                + '<h3>Tekniskt fel</h3>' 
                                + '</div>' 
                                + '<div class="modal-body">' 
                                + ' {{bodyText}}' 
                                + '</div>' 
                                + '<div class="modal-footer">'
                                + ' <button class="btn btn-success" ng-click="$close()">OK</button>' 
                                + '</div>',
                            controller : function($scope, $modalInstance, bodyText) {
                                $scope.bodyText = bodyText;
                            },
                            resolve : {
                                bodyText : function() {
                                    return angular.copy(message);
                                }
                            }
                        });

                        msgbox.result.then(function(result) {
                            if (callback) {
                                callback(result)
                            }
                        }, function() {
                        });
                    }
                    // Return public API for the service
                    return {
                        showErrorMessageDialog : _showErrorMessageDialog
                    }

                } ]);
