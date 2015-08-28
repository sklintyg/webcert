/**
 * Created by stephenwhite on 27/08/15.
 */
angular.module('webcert').service('webcert.TermsState',
    function(UserModel) {
        'use strict';

        this.reset = function() {
            this.termsAccepted = false;
            this.transitioning = false;
        };

        this.reset();
    }
);
