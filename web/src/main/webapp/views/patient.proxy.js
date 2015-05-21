angular.module('webcert').factory('webcert.PatientProxy',
    [ '$http', '$stateParams', '$log', 'webcert.PatientModel',
        function($http, $stateParams, $log, PatientModel) {
            'use strict';

            /**
             * getNameAndAddress
             * @param personnummer
             * @param onSuccess
             * @param onNotFound
             * @param onError
             */
            function _getPatient(personnummer, onSuccess, onNotFound, onError) {
                $log.debug('getNameAndAddress');

                var that = PatientModel;
                that.personnummer = personnummer;

                var restPath = '/api/person/' + personnummer;
                $http.get(restPath).success(function(data) {
                    $log.debug(data);

                    if (data.status === 'FOUND' && data.person) {
                        that.fornamn = data.person.fornamn;
                        that.mellannamn = data.person.mellannamn;
                        that.efternamn = data.person.efternamn;
                        that.postadress = data.person.postadress;
                        that.postnummer = data.person.postnummer;
                        that.postort = data.person.postort;
                        onSuccess();
                    } else if (data.status === 'ERROR') {
                        $log.warn('PU-tjänsten kunde inte kontaktas, manuell inmatning krävs');
                        onError();
                    } else {
                        $log.debug('Personen hittades inte i PU-tjänsten, manuell inmatning krävs');
                        onNotFound();
                    }

                }).error(function() {
                    $log.warn('PU-tjänsten kunde inte kontaktas, manuell inmatning krävs');
                    onError();
                });
            }

             // Return public API for the service
            return {
                getPatient: _getPatient
            };
        }]);
