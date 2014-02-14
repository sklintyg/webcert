'use strict';

/* Services */
var services = angular.module('wc.dashboard.services', []);

services.factory('dashBoardService', ['$http', '$log',
    function ($http, $log) {

        /*
         * Load certificate list of all certificates for a person
         */
        function _getCertificatesForPerson (requestConfig, onSuccess, onError) {
            $log.debug("_getCertificatesForPerson type:" + requestConfig);
            var restPath = '/api/intyg/list/' + requestConfig;
            $http.get(restPath).success(function (data) {
                $log.debug("got data:" + data);
                onSuccess(data);
            }).error(function (data, status, headers, config) {
                $log.error("error " + status);
                // Let calling code handle the error of no data response
                onError(status);
            });
        }

        /*
         * Load certificate list of specified type(unsigned, with unanswered
         * questions and ready to mass sign) TODO: add optionally
         * careWard +
         */

        function _getCertificates (requestConfig, callback) {
            $log.debug("_getCertificates type:" + requestConfig.type);
            // var restPath = '/api/certificates/' + dataType;
            var restPath = '/jsonmocks/' + requestConfig.type;
            $http.get(restPath).success(function (data) {
                $log.debug("got data:" + data);
                callback(data);
            }).error(function (data, status, headers, config) {
                $log.error("error " + status);
                // Let calling code handle the error of no data response
                callback(null);
            });
        }

        /*
         * Load questions and answers data for
         */
        function _getQA (callback) {
            $log.debug("_getQA");
            var restPath = '/api/fragasvar';
            $http.get(restPath).success(function (data) {
                $log.debug("got data:" + data);
                callback(data);
            }).error(function (data, status, headers, config) {
                $log.error("error " + status);
                // Let calling code handle the error of no data response
                callback(null);
            });

        }

        /*
         * Load questions and answers data for
         */
        function _getQAByQuery (qp, onSuccess, onError) {
            $log.debug("_getQAByQuery");
            var restPath = '/api/fragasvar/query';
            $http.put(restPath, qp).success(function (data) {
                $log.debug("_getQAByQuery got data:" + data);
                onSuccess(data);
            }).error(function (data, status, headers, config) {
                $log.error("_getQAByQuery error " + status);
                // Let calling code handle the error of no data response
                onError(data);
            });

        }

        /*
         * Load questions and answers data for
         */
        function _getQAByQueryFetchMore (qp, onSuccess, onError) {
            $log.debug("_getQAByQueryFetchMore");
            var restPath = '/api/fragasvar/query/paging';
            $http.put(restPath, qp).success(function (data) {
                $log.debug("_getQAByQueryFetchMore got data:" + data);
                onSuccess(data);
            }).error(function (data, status, headers, config) {
                $log.error("_getQAByQueryFetchMore error " + status);
                // Let calling code handle the error of no data response
                onError(data);
            });

        }

        function _getDoctorList (enhetsId, onSuccess, onError) {
            $log.debug("_getDoctorList: " + enhetsId);
            var restPath = '/api/fragasvar/mdlist/' + enhetsId;
            $http.get(restPath).success(function (data) {
                $log.debug("_getDoctorList got data:" + data);
                onSuccess(data);
            }).error(function (data, status, headers, config) {
                $log.error("_getDoctorList error " + status);
                // Let calling code handle the error of no data response
                onError(data);
            });

        }


        // Return public API for the service
        return {
            getCertificatesForPerson : _getCertificatesForPerson,
            getCertificates : _getCertificates,
            getQA : _getQA,
            getQAByQuery : _getQAByQuery,
            getQAByQueryFetchMore : _getQAByQueryFetchMore,
            getDoctorList : _getDoctorList
        }
    } ]);

services.factory('CertificateDraft', [ '$http', '$log',
    function ($http, $log) {
        return {

            reset : function () {
                this.personnummer = null;
                this.intygType = 'default';
                this.name = null;
                this.address = null;
                this.vardEnhetHsaId = null;
                this.vardEnhetNamn = null;
                this.vardGivareHsaId = null;
                this.vardGivareHsaNamn = null;
            },

            create : function (onSuccess) {
                $log.debug('CertificateDraft create');

                var payload = {};
                payload.patientPersonnummer = this.personnummer;
                payload.name = this.name;
                payload.intygType = this.intygType;
                payload.address = this.address;

                var restPath = '/api/intyg/create';
                $http.post(restPath, payload).success(function (data) {
                    $log.debug('got callback data: ' + data);

                    // This services has fulfilled it's task, clear all data.
                    // TODO: Clear all data on the service.

                    onSuccess(data);

                }).error(function (data, status) {
                    $log.error('error ' + status);
                });
            },

            getNameAndAddress : function (personnummer, onSuccess) {
                $log.debug('CertificateDraft getNameAndAddress');

                this.personnummer = personnummer;

                if (this.personnummer === '19121212-1212' || this.personnummer === '20121212-1212') {
                    this.name = 'Test Testsson';
                    this.address = 'Storgatan 23';
                } else {
                    this.name = null;
                    this.address = null;

                }
                onSuccess();
            },

            getCertTypes : function () {
                this.intygType = 'default';
                return [
                    {id : 'default', name : 'Välj intygstyp'},
                    {id : 'fk7263', name : 'Läkarintyg FK 7263'},
                    {id : 'ts-bas', name : 'Läkarintyg Transportstyrelsen Bas'}
                ];
            },

            createDraft : function (onSuccess) {
                $log.debug('_createDraft');

                var payload = {};
                payload.patientPersonnummer = this.personnummer;
                var nameParts = this.name.split(' ');
                payload.patientFornamn = (nameParts.length > 0) ? nameParts[0] : null;
                payload.patientEfternamn = (nameParts.length > 1) ? nameParts[1] : null;
                payload.intygType = this.intygType;
                payload.address = this.address;
                payload.vardEnhetHsdId = this.vardEnhetHsaId;
                payload.vardEnhetNamn = this.vardEnhetNamn;
                payload.vardGivareHsdId = this.vardGivareHsaId;
                payload.vardGivareNamn = this.vardGivareNamn;

                var restPath = '/api/intyg/create';
                $http.post(restPath, payload).success(function (data) {
                    $log.debug('got callback data: ' + data);
                    onSuccess(data);

                }).error(function (data, status) {
                    $log.error('error ' + status);
                });
            }
        };
    }]);
