angular.module('showcase').controller('showcase.navigationCtrl',
    ['$scope', '$window', 'common.UserModel',
        function($scope, $window, UserModel) {
            'use strict';

            $scope.showCookieBanner = false;
            $scope.doShowCookieBanner = function() {
                $window.localStorage.setItem("wc-cookie-consent-given", "0");
                $scope.showCookieBanner = !$scope.showCookieBanner;
            };

            //Header state
            $scope.today = new Date();
            $scope.user = UserModel.user;
            $scope.stat = {
                fragaSvarValdEnhet: 22,
                fragaSvarAndraEnheter: 33,
                intygValdEnhet: 33,
                intygAndraEnheter: 33,
                vardgivare: []
            };


            $scope.menuDefs = [];
            $scope.menuDefs.push({
                link: '',
                label: 'Menyval1',
                requiresDoctor: false,
                statNumberId: 'stat-unitstat-unhandled-question-count',
                statTooltip: 'not set',
                id: 'menu-unhandled-qa',
                getStat: function() {
                    return '';
                }
            });

            $scope.menuDefs.push({
                link: '',
                label: "Menyval med statistik",
                requiresDoctor: false,
                statNumberId: 'stat-unitstat-unsigned-certs-count',
                statTooltip: 'not set',
                id: 'menu-unsigned',
                getStat: function() {
                    this.statTooltip =
                        'Statistikbubblor kan också ha tooltips';
                    return $scope.stat.intygValdEnhet || '';
                }
            });

            $scope.menuDefs.push({
                link: '',
                label: 'En aktiv meny',
                requiresDoctor: false,
                id: 'activeOne',
                getStat: function() {
                    return '';
                }
            });

            $scope.isActive = function(id) {
                return id === "activeOne";
            };



        }]);
