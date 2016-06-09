
angular.module('showcase').config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    'use strict';


    $stateProvider.

    state('showcase', {
        views: {
            'header@': {
                templateUrl: '/showcase/header.html'
            }
        }
    }).

    state('showcase.bootstrap', {
        url: '/bootstrap-components',
        views: {
            'content@': {
                templateUrl: '/showcase/views/bootstrap.html',
                controller: 'showcase.BootstrapCtrl'
            }
        }
    }).
    state('showcase.arendehantering', {
        url: '/arendehantering',
        views: {
            'content@': {
                templateUrl: '/showcase/views/arendehantering.html',
                controller: 'showcase.ArendeCtrl'
            }
        }
    }).
    state('showcase.navigation', {
        url: '/navigation',
        views: {
            'content@': {
                templateUrl: '/showcase/views/navigation.html',
                controller: 'showcase.navigationCtrl'
            }
        }
    });

    $urlRouterProvider.when('', '/bootstrap-components');

});
