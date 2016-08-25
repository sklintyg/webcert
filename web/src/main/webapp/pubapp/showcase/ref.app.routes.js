
angular.module('showcase').config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    'use strict';

    var templateRoot = '/pubapp/showcase/';
    $stateProvider.

    state('showcase', {
        views: {
            'header@': {
                templateUrl: templateRoot +'header.html'
            }
        }
    }).

    state('showcase.bootstrap', {
        url: '/bootstrap-components',
        views: {
            'content@': {
                templateUrl: templateRoot + 'views/bootstrap.html',
                controller: 'showcase.BootstrapCtrl'
            }
        }
    }).
    state('showcase.arendehantering', {
        url: '/arendehantering',
        views: {
            'content@': {
                templateUrl: templateRoot + 'views/arendehantering.html',
                controller: 'showcase.ArendeCtrl'
            }
        }
    }).
    state('showcase.utkast', {
        url: '/utkast',
        views: {
            'content@': {
                templateUrl: templateRoot + 'views/utkast.html',
                controller: 'showcase.UtkastCtrl'
            }
        }
    }).
    state('showcase.navigation', {
        url: '/navigation',
        views: {
            'content@': {
                templateUrl: templateRoot + 'views/navigation.html',
                controller: 'showcase.navigationCtrl'
            }
        }
    });

    $urlRouterProvider.when('', '/bootstrap-components');

});
