define([
], function () {
    'use strict';

    /*
     *  Controller for logic related to displaying the list of a doctors unsigned certificates (mina osignerade intyg)
     */
    return ['$scope', '$window', '$log', '$location',
        function ($scope, $window, $log, $location) {

            $scope.createCert = function () {
                $location.path('/create/index');
            };

            $scope.viewCert = function (item) {
                $log.debug('open ' + item.id);
                //listCertService.selectedCertificate = item;
                $window.location.href = '/m/' + item.typ.toLowerCase() + '/webcert/intyg/' + item.id + '#/view';
            };
        }];
});
