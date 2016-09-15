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

angular.module('webcert').controller('integration.EnhetsvalHeaderCtrl',
    [ '$scope', 'common.UserModel', function($scope, UserModel) {
    'use strict';

    //Expose 'now' as a model property for the template to render as todays date
    $scope.today = new Date();
    $scope.user = angular.copy(UserModel.user);

    //Pretend that the user has not selected a unit
    $scope.user.valdVardgivare = null;
    $scope.user.valdVardenhet = null;

    $scope.menuDefs = [];
    $scope.stat = {
        fragaSvarValdEnhet: 0,
        fragaSvarAndraEnheter: 0,
        intygValdEnhet: 0,
        intygAndraEnheter: 0,
        vardgivare: []
    };

} ]);
