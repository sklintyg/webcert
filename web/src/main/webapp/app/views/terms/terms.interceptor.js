/**
 * Created by stephenwhite on 26/08/15.
 */
angular.module('webcert').service('webcert.TermsInterceptor', ['$rootScope','webcert.TermsState', function($rootScope, TermsState) {

    var service = this;

    service.request = function(config) {
        if(!TermsState.termsAccepted){
            config.headers.terms = "true";
        }
        return config;
    };

    service.response = function(response) {
        return response;
    };
}]);