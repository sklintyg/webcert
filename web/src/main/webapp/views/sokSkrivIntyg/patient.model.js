angular.module('webcert').factory('webcert.PatientModel',
    [ '$log',
        function($log) {
            'use strict';

            return {
                reset: function() {
                    this.personnummer = null;
                    this.intygId = null;
                    this.intygType = 'default';
                    this.fornamn = null;
                    this.mellannamn = null;
                    this.efternamn = null;
                    this.postadress = null;
                    this.postnummer = null;
                    this.postort = null;
                }
            };
        }]);
