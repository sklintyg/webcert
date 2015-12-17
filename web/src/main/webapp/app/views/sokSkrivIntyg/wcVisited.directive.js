/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/**
 * Directive to keep track of when the user has visited a field in order to show validation messages
 * only after the user have had the opportunity to enter some information.
 */
angular.module('webcert').directive('wcVisited',
    function() {
        'use strict';

        return {

            restrict: 'A',
            require: 'ngModel',

            link: function(scope, element, attrs, ctrl) {
                ctrl.$visited = false;
                element.one('blur', function() {
                    element.addClass('wc-visited');
                    scope.$apply(function() {
                        ctrl.$visited = true;
                    });
                });
            }
        };
    });
