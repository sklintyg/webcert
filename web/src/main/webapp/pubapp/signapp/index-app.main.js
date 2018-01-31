/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Register module
angular.module('rhsIndexApp', []);

var testsignatur = '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';

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}

function isDefined(value) {
    return value !== null && typeof value !== 'undefined';
}

function isEmpty(value) {
    return value === null || typeof value === 'undefined' || value === '';
}

function returnJoinedArrayOrNull(value) {
    return value !== null && value !== undefined ? value.join(', ') : null;
}

function valueOrNull(value) {
    return value !== null && value !== undefined ? value : null;
}

function timeInMillis() {
    return new Date().getTime();
}


angular.module('rhsIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';

        $scope.personId = '19121212-1212';
        $scope.selectedEnhet = '';
        $scope.q = {};
        $scope.q.intygsId = '';
        $scope.utkast = null;

        $scope.isNetID = false;
        $scope.isBankID = false;
        $scope.isNias = false;

        $scope.pollHandle = null;

        function cleanup() {
            clearInterval($scope.pollHandle);
            clearInterval($scope.grpPollHandle);
            clearInterval($scope.niasPollHandle);
            $scope.isNetID = false;
            $scope.isBankID = false;
            $scope.isNias = false;
            $scope.ticketId = null;
            $scope.pollHandle = null;
            $scope.q.intygsId = null;
        }

        // - START NetiD Access Server sign (nias)

        $scope.bekraftaSigneringNias = function() {
            // Här måste vi använda ett testbarhets-API för att låtsas vara klara med NIAS-signeringen.
            if (!isDefined($scope.ticketId)) {
                alert('Wait until there is an orderRef!');
                return;
            }
            $http.put('/services/nias-api/status/' + $scope.ticketId, 'COMPLETE', null).then(
                function(response) {
                    // Success
                    console.log("NIAS Signing successful: " + JSON.stringify(response));
                    cleanup();
                },
                function(response) {
                    // Failure
                    console.log("NIAS Signing NOT successful: " + JSON.stringify(response));
                    cleanup();
                }
            )
        };

        $scope.startNiasSign = function() {

            if (!isDefined($scope.utkast)) {
                alert('No utkast selected');
                return;
            }
            clearInterval($scope.pollHandle);
            clearInterval($scope.grpPollHandle);
            clearInterval($scope.niasPollHandle);

            $scope.isBankID = false;
            $scope.isNetID = false;
            $scope.isNias = true;

            // First, do the ugly hack of specifiying a PERSONNUMMER on the user context just to get BankID or NIAS signing to work... for now.
            $http.put('/testability/user/personid', $scope.personId, null).then(
                function(response) {
                    console.log('PersonID setting on User returned ' + response.data);
                    // success callback

                    // Then issue NIAS sign to server and start polling backend.
                    $http.post('/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.utkast.intygsId + '/' +
                        $scope.utkast.version + '/nias/signeraserver', null, null)
                        .then(
                            function(response) {
                                // success callback
                                console.log(JSON.stringify(response));
                                $scope.ticketId = response.data.id;
                                $scope.newVersion = response.data.version;
                                $scope.statusMessage = response.data.status;

                                // Got ticket id. Start poller and activate Confirm / Cancel buttons
                                $scope.pollHandle = setInterval(function() {
                                    $http({
                                        method: 'GET',
                                        url: '/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.ticketId +
                                        '/signeringsstatus'
                                    })
                                        .then(function successCallback(response) {
                                            console.log(JSON.stringify(response.data));
                                            $scope.statusMessage = response.data.status;
                                        }, function errorCallback(response) {
                                            console.log('Error during poll, cancelling interval. Msg: ' + JSON.stringify(response.data));
                                            clearInterval($scope.pollHandle);
                                        });
                                }, 3000);

                                // $scope.niasPollHandle = setInterval(function() {
                                //     $http({
                                //         method: 'GET',
                                //         url: '/services/nias-api/status/' + $scope.ticketId
                                //     })
                                //         .then(function successCallback(response) {
                                //             console.log(JSON.stringify(response.data));
                                //             $scope.niasStatusMessage = response.data;
                                //         }, function errorCallback(response) {
                                //             console.log('Error during NIAS poll, cancelling interval. Msg: ' + JSON.stringify(response.data));
                                //             clearInterval($scope.niasPollHandle);
                                //         });
                                // }, 3000);

                                // Load all ongoing signatures, filter out those for other personnummer


                            },
                            function(response) {
                                // failure callback
                                alert('Failure: ' + JSON.stringify(response));
                            }
                        );
                },
                function(response) {
                    // error callback
                    alert('Error setting personId on session: ' + response.data);
                });
        };


        $scope.signeraAvbrytGrp = function() {
            // Här måste vi använda ett testbarhets-API för att låtsas avbryta GRP-signeringen.
            $http.put('/services/grp-api/cancel/' + $scope.ticketId, null, null).then(
                function(response) {
                    // Success callback
                    console.log('signeraAvbrytGrp success');
                    cleanup();
                },
                function(response) {
                    // Error callback
                    console.log('signeraAvbrytGrp error: ' + JSON.stringify(response.data));
                    cleanup();
                })

        };

        $scope.setGrpStatus = function(status) {
            // Här måste vi använda ett testbarhets-API för att låtsas vara klara med GRP-signeringen. Kanske använda
            // GRP-stubben?
            // /services/grp-api
            $http.put('/services/grp-api/status', {orderRef: $scope.ticketId, status: status}, null).then(
                function(response) {
                    // Success
                    console.log('Set state to ' + status + ' successful: ' + JSON.stringify(response));
                    cleanup();
                },
                function(response) {
                    // Failure

                }
            )
        };

        $scope.bekraftaSigneringGrp = function() {
            // Här måste vi använda ett testbarhets-API för att låtsas vara klara med GRP-signeringen. Kanske använda
            // GRP-stubben?
            // /services/grp-api
            if (!isDefined($scope.orderRef)) {
                alert('Wait until there is an orderRef!');
                return;
            }
            $http.put('/services/grp-api/status', {orderRef: $scope.orderRef, status: 'COMPLETE'}, null).then(
                function(response) {
                    // Success
                    console.log("Signing successful: " + JSON.stringify(response));
                    cleanup();
                },
                function(response) {
                    // Failure
                    console.log("Signing NOT successful: " + JSON.stringify(response));
                }
            )
        };

        $scope.startBankIDSign = function() {
            // /{intygsTyp}/{intygsId}/{version}/grp/signeraserver
            if (!isDefined($scope.utkast)) {
                alert('No utkast selected');
                return;
            }
            clearInterval($scope.pollHandle);
            clearInterval($scope.grpPollHandle);
            clearInterval($scope.niasPollHandle);

            $scope.isBankID = true;
            $scope.isNetID = false;
            $scope.isNias = false;

            $http.put('/testability/user/personid', $scope.personId, null).then(
                function(response) {
                    console.log('PersonID setting on User returned ' + response.data);
                    // success callback
                    // Then issue GRP sign to server and start polling backend.
                    $http.post('/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.utkast.intygsId + '/' +
                        $scope.utkast.version + '/grp/signeraserver', null, null)
                        .then(
                            function(response) {
                                // success callback
                                console.log(JSON.stringify(response));
                                $scope.ticketId = response.data.id;
                                $scope.newVersion = response.data.version;
                                $scope.statusMessage = response.data.status;

                                $http.get('/services/grp-api/orderref/' + $scope.ticketId,  null, null).then(
                                    function(response) {
                                        // Success
                                        $scope.orderRef = response.data;
                                        console.log('Set orderRef: ' + $scope.orderRef);
                                    },
                                    function(response) {
                                        // Error
                                    }
                                )

                                // Got ticket id. Start poller and activate Confirm / Cancel buttons
                                $scope.pollHandle = setInterval(function() {
                                    $http({
                                        method: 'GET',
                                        url: '/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.ticketId +
                                        '/signeringsstatus'
                                    })
                                        .then(function successCallback(response) {
                                            console.log(JSON.stringify(response.data));
                                            $scope.statusMessage = response.data.status;
                                        }, function errorCallback(response) {
                                            console.log('Error during poll, cancelling interval. Msg: ' + JSON.stringify(response.data));
                                            clearInterval($scope.pollHandle);
                                        });
                                }, 3000);

                                $scope.grpPollHandle = setInterval(function() {
                                    $http({
                                        method: 'GET',
                                        url: '/services/grp-api/status/' + $scope.ticketId
                                    })
                                        .then(function successCallback(response) {
                                            console.log(JSON.stringify(response.data));
                                            $scope.grpStatusMessage = response.data.status;
                                        }, function errorCallback(response) {
                                            console.log('Error during poll, cancelling interval. Msg: ' + JSON.stringify(response.data));
                                            clearInterval($scope.grpPollHandle);
                                        });
                                }, 3000);
                            },
                            function(response) {
                                // failure callback
                                alert('Failure: ' + JSON.stringify(response));
                            }
                        );
                },
                function(response) {
                    // error callback
                    alert('Error setting personId on session: ' + response.data);
                });
        };

        $scope.signeraAvbryt = function() {
            cleanup();
        };

        $scope.signeraKlient = function() {
            if (!isDefined($scope.ticketId)) {
                return;
            }


            $http.post('/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.ticketId + '/signeraklient', '{"signatur":"' + testsignatur + '"}',
                null)
                .then(
                    function(response) {
                        console.log("Signing successful: " + JSON.stringify(response));
                        cleanup();
                    },
                    function(response) {
                        // failure callback
                        alert('Failure: ' + JSON.stringify(response.data));
                    }
                )
        };

        $scope.startNetIDSign = function() {
            if (!isDefined($scope.utkast)) {
                alert('No utkast selected');
                return;
            }
            $scope.isNetID = true;
            $scope.isBankID = false;
            $scope.isNias = false;

            // Start by issuing client sign to server and start polling backend.
            $http.post('/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.utkast.intygsId + '/' +
                $scope.utkast.version + '/signeringshash', null, null)
                .then(
                    function(response) {
                        // success callback
                        console.log(JSON.stringify(response));
                        $scope.ticketId = response.data.id;
                        $scope.newVersion = response.data.version;
                        $scope.statusMessage = response.data.status;

                        // Got ticket id. Start poller and activate Confirm / Cancel buttons
                        $scope.pollHandle = setInterval(function() {
                            $http({
                                method: 'GET',
                                url: '/moduleapi/utkast/' + $scope.utkast.intygsTyp + '/' + $scope.ticketId +
                                '/signeringsstatus'
                            })
                                .then(function successCallback(response) {
                                    console.log(JSON.stringify(response.data));
                                    $scope.statusMessage = response.data.status;
                                }, function errorCallback(response) {
                                    console.log('Error during poll, cancelling interval. Msg: ' + JSON.stringify(response.data));
                                    clearInterval($scope.pollHandle);
                                });
                        }, 3000);
                    },
                    function(response) {
                        // failure callback
                        alert('Failure: ' + JSON.stringify(response));
                    }
                );
        };

        $scope.openForm = function(intyg) {
            $scope.q.intygsId = intyg.intygsId;
            $scope.utkast = intyg;
        };

        $scope.loadUtkastReadyToSign = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/' + $scope.selectedEnhet + '/drafts?cachekiller=' + timeInMillis()
            }).then(function successCallback(response) {
                $scope.resultat = '';
                $scope.data = response.data;
            });
        };

        $scope.loadUnits = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/signingunits?cachekiller=' + timeInMillis()
            }).then(function successCallback(response) {
                $scope.units = response.data;
            });
        };

        $scope.loadUnits();

    }]);
