define([
], function () {
    'use strict';

    return ['$http', '$log',
        function ($http, $log) {

            /*
             * Load certificate list of all certificates for a person
             */
            function _getCertificatesForPerson (requestConfig, onSuccess, onError) {
                $log.debug('_getCertificatesForPerson type:' + requestConfig);
                var restPath = '/api/intyg/list/' + requestConfig;
                $http.get(restPath).success(function (data) {
                    $log.debug('got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(status);
                });
            }

            /*
             * Load unsigned certificate list for valdVardenhet
             */
            function _getUnsignedCertificates(onSuccess, onError) {
                $log.debug('_getUnsignedCertificates:');
                var restPath = '/api/intyg/unsigned/'; // release version
                //var restPath = '/jsonmocks/intyg_unsigned.json'; // mocked version
                $http.get(restPath).success(function (data) {
                    $log.debug('got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(null);
                });
            }

            /*
             * Load more unsigned certificates by query
             */
            function _getUnsignedCertificatesByQueryFetchMore (query, onSuccess, onError) {
                $log.debug('_getUnsignedCertificatesByQueryFetchMore');
                var restPath = '/api/intyg/unsigned';
                $http.put(restPath, query).success(function (data) {

                /////////////// MOCKED ////////////////
                //var restPath = '/jsonmocks/intyg_unsigned_fetchmore.json'; // mocked version
                //$http.get(restPath).success(function (data) {
                /////////////// MOCKED ////////////////
                    $log.debug('_getUnsignedCertificatesByQueryFetchMore got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('_getUnsignedCertificatesByQueryFetchMore error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            /*
             * Load questions and answers data for
             */
            function _getQA (callback) {
                $log.debug('_getQA');
                var restPath = '/api/fragasvar';
                $http.get(restPath).success(function (data) {
                    $log.debug('got data:' + data);
                    callback(data);
                }).error(function (data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    callback(null);
                });

            }

            /*
             * Load questions and answers data for
             */
            function _getQAByQuery (qp, onSuccess, onError) {
                $log.debug('_getQAByQuery');
                var restPath = '/api/fragasvar/query';
                $http.put(restPath, qp).success(function (data) {
                    $log.debug('_getQAByQuery got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('_getQAByQuery error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            /*
             * Load questions and answers data for
             */
            function _getQAByQueryFetchMore (qp, onSuccess, onError) {
                $log.debug('_getQAByQueryFetchMore');
                var restPath = '/api/fragasvar/query/paging';
                $http.put(restPath, qp).success(function (data) {
                    $log.debug('_getQAByQueryFetchMore got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('_getQAByQueryFetchMore error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });

            }

            function _getDoctorList (enhetsId, onSuccess, onError) {
                $log.debug('_getDoctorList: ' + enhetsId);
                var restPath = '/api/fragasvar/mdlist/' + enhetsId;
                $http.get(restPath).success(function (data) {
                    $log.debug('_getDoctorList got data:' + data);
                    onSuccess(data);
                }).error(function (data, status) {
                    $log.error('_getDoctorList error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            // Return public API for the service
            return {
                getCertificatesForPerson : _getCertificatesForPerson,
                getUnsignedCertificates : _getUnsignedCertificates,
                getUnsignedCertificatesByQueryFetchMore : _getUnsignedCertificatesByQueryFetchMore,
                getQA : _getQA,
                getQAByQuery : _getQAByQuery,
                getQAByQueryFetchMore : _getQAByQueryFetchMore,
                getDoctorList : _getDoctorList
            };
        }];
});
