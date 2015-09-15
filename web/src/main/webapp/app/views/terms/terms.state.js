angular.module('webcert').factory('webcert.TermsState',
    function() {
        'use strict';
        return {
            termsAccepted :false,
            transitioning : false,
            reset: function() {
                this.termsAccepted = false;
                this.transitioning = false;
            }
    };
});