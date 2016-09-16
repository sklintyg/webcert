angular.module('webcert').controller('integration.EnhetsvalPageCtrl',
        [ '$scope', '$window', 'common.UserModel', function($scope, $window, UserModel) {
            'use strict';
            // Construct base destination url
            var baseDestUrl = getParameterByName('destination', $window.location.search) + '&enhet=';

            //Expose scope model
            $scope.user = UserModel.user;

            //on unit selection, redirect widow to destination + &enhet=<selected enhetsid>
            $scope.onUnitSelected = function(enhet) {
                $window.location.replace(baseDestUrl + enhet.id);
            };

            //Util function that parses destination argument from current url
            function getParameterByName(name, url) {
                var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'), results = regex.exec(url);
                if (!results || !results[2]){
                    return '';
                }
                return decodeURIComponent(results[2].replace(/\+/g, ' '));
            }
        } ]);
