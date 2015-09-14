angular.module('webcert').directive('wcAbout', ['$location', 'common.UserModel',
    function($location, UserModel) {
        'use strict';

        return {
            restrict: 'A',
            transclude: true,
            replace: true,
            scope: {
                menuDefsAbout: '@'
            },
            templateUrl: '/app/views/omWebcert/wcAbout.directive.html',
            controller: function($scope) {
                // Expose "now" as a model property for the template to render as todays date
                $scope.today = new Date();
                $scope.menuItems = [];

                $scope.menuItems.push({
                    id: 'about-webcert',
                    link: '/web/dashboard#/webcert/about',
                    label: 'Om Webcert'
                });
                if (UserModel.isPrivatLakare()) {
                    $scope.menuItems.push({
                        id: 'about-pp-terms',
                        link: '/web/dashboard#/terms/about',
                        label: 'Användarvillkor'
                    });
                }
                $scope.menuItems.push({
                    id: 'about-support',
                    link: '/web/dashboard#/support/about',
                    label: 'Support och kontaktinformation'
                });
                $scope.menuItems.push({
                    id: 'about-intyg',
                    link: '/web/dashboard#/certificates/about',
                    label: 'Intyg som stöds i Webcert'
                });
                $scope.menuItems.push({
                    id: 'about-faq',
                    link: '/web/dashboard#/faq/about',
                    label: 'Vanliga frågor'
                });
                $scope.menuItems.push({
                    id: 'about-cookies',
                    link: '/web/dashboard#/cookies/about',
                    label: 'Om kakor (cookies)'
                });


                function getSubMenuName(path) {
                    path = path.substring(0, path.lastIndexOf('/'));
                    return path.substring(path.lastIndexOf('/') + 1);
                }

                var currentSubMenuName = getSubMenuName($location.path()) || 'index';
                $scope.currentSubMenuLabel = '';

                // set header label based on menu items label
                angular.forEach($scope.menuItems, function(menu) {
                    var page = getSubMenuName(menu.link);
                    if (page === currentSubMenuName) {
                        $scope.currentSubMenuLabel = menu.label;
                    }
                });

                $scope.isActive = function(page) {
                    page = getSubMenuName(page);
                    return page === currentSubMenuName;
                };
            }
        };
    }]);
