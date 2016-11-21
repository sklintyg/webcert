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

describe(
        'IntegrationEnhetsvalPageCtrlSpec',
        function() {
            'use strict';

            var $controller;
            var $scope;
            var $uibModal;
            var $window;
            var UserModel;

            beforeEach(function() {

                module('webcertTest');
                module('webcert', [ '$provide', function($provide) {

                    $provide.value('common.UserModel', {});
                    $uibModal = jasmine.createSpyObj('$uibModal', [ 'open' ]);

                    var windowObj = {
                        location: {
                            search: '',
                            replace: function(uri) {
                                return;
                            }
                        }

                    };

                    $provide.value('$window', windowObj);
                } ]);

                inject([ '$rootScope', '$window', '$uibModal', 'common.UserModel', '$controller',
                        function($rootScope, _$window_, _$uibModal_, _UserModel_, _$controller_) {
                            $scope = $rootScope.$new();
                            $window = _$window_;
                            UserModel = _UserModel_;
                            $controller = _$controller_;

                        } ]);
                spyOn($window.location, 'replace').and.callThrough('');
            });

            describe('when ADDING "enhet" parameter', function() {

                        beforeEach(function() {
                            $window.location.search = '?destination=http%3A%2F%2Flocalhost%3A9088%2Fvisa%2Fintyg%2F438fef62-1bde-4d00-9ae5-b29a95a8f077%3Ffornamn%3Dtest#/integration-enhetsval';
                            $controller('integration.EnhetsvalPageCtrl', {
                                $scope: $scope
                            });
                        });

                        it('selecting a unit should add "enhet" query param in destination redirect url', function() {
                            $scope.onUnitSelected({
                                id: '1234'
                            });

                            expect($window.location.replace).toHaveBeenCalledWith('http://localhost:9088/visa/intyg/438fef62-1bde-4d00-9ae5-b29a95a8f077?fornamn=test&enhet=1234');
                        });

            });

            describe('when REPLACING existing "enhet" parameter', function() {

                beforeEach(function() {
                    $window.location.search = '?destination=http%3A%2F%2Flocalhost%3A9088%2Fvisa%2Fintyg%2F438fef62-1bde-4d00-9ae5-b29a95a8f077%3Ffornamn%3Dtest&enhet=01#/integration-enhetsval';

                    $controller('integration.EnhetsvalPageCtrl', {
                        $scope: $scope
                    });
                });

                it('selecting a unit should replace "enhet" query param in destination redirect url', function() {
                    $scope.onUnitSelected({
                        id: '1234'
                    });

                    expect($window.location.replace).toHaveBeenCalledWith('http://localhost:9088/visa/intyg/438fef62-1bde-4d00-9ae5-b29a95a8f077?fornamn=test&enhet=1234');
                });

            });

            describe('when NO existing parameters exist in destination', function() {

                beforeEach(function() {
                    $window.location.search = '?destination=http%3A%2F%2Flocalhost%3A9088%2Fvisa%2Fintyg%2F438fef62-1bde-4d00-9ae5-b29a95a8f077#/integration-enhetsval';

                    $controller('integration.EnhetsvalPageCtrl', {
                        $scope: $scope
                    });
                });

                it('selecting a unit should add "enhet" query param in destination redirect url', function() {
                    $scope.onUnitSelected({
                        id: '1234'
                    });

                    expect($window.location.replace).toHaveBeenCalledWith('http://localhost:9088/visa/intyg/438fef62-1bde-4d00-9ae5-b29a95a8f077?enhet=1234');
                });

            });

        });
