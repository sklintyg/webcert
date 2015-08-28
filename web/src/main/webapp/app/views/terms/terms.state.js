/**
 * Created by stephenwhite on 27/08/15.
 */
angular.module('webcert').service('webcert.TermsState',['common.UserModel',
    function(UserModel) {
        'use strict';

        this.reset = function() {
            this.termsAccepted = false;
            this.transitioning = UserModel.user ? UserModel.user.privatLakareAvtalGodkand : false;
        };

        this.reset();
    }
]);
