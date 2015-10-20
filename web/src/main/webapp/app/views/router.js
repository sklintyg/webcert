/**
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    'use strict';

    $stateProvider.

        state('webcert', {
            views: {
                'header': {
                    templateUrl: '/web/webjars/common/webcert/gui/headers/wcHeader.partial.html',
                    controller: 'common.wcHeaderController'
                }
            }
        }).

        state('webcert.create-index', {
            url: '/create/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
                    controller: 'webcert.InitCertCtrl'
                }
            }
        }).
        state('webcert.create-choosepatient-index', {
            url: '/create/choose-patient/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
                    controller: 'webcert.ChoosePatientCtrl'
                }
            }
        }).
        state('webcert.create-edit-patientname', {
            url:'/create/edit-patient-name/:mode',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivPatientName.html',
                    controller: 'webcert.EditPatientNameCtrl'
                }
            }
        }).
        state('webcert.create-choose-certtype-index', {
            url:'/create/choose-cert-type/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivValjUtkastType.html',
                    controller: 'webcert.ChooseCertTypeCtrl'
                }
            }
        }).
        state('webcert.unhandled-qa', {
            url:'/unhandled-qa',
            views: {
                'content@': {
                    templateUrl: '/app/views/fragorOchSvar/fragorOchSvar.html',
                    controller: 'webcert.UnhandledQACtrl'
                }
            }
        }).
        state('webcert.unsigned', {
            url: '/unsigned',
            views: {
                'content@': {
                    templateUrl: '/app/views/ejSigneradeUtkast/ejSigneradeUtkast.html',
                    controller: 'webcert.UnsignedCertCtrl'
                }
            }
        }).
        state('webcert.intyg', {
            abstract : true, // jshint ignore:line
            data: { defaultActive : 'index' },
            views: {
                'content@' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.main.html'
                }
            }

        }).
        state('webcert.intyg.fk', {
            data: { defaultActive : 'index' },
            url:'/intyg/fk7263/:certificateId?:patientId&:hospName&:signed',
            onEnter: function($stateParams){
                $stateParams.certificateType = 'fk7263';
            },
            views: {
                'main@webcert.intyg' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.fk.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'intyg@webcert.intyg.fk' : {
                    templateUrl: '/web/webjars/fk7263/webcert/views/intyg/intyg.html'

                },
                'fragaSvar@webcert.intyg.fk' : {
                    templateUrl: '/web/webjars/fk7263/webcert/views/intyg/fragasvar/fragasvar.html'
                },
                'header@webcert.intyg.fk' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('webcert.intyg.ts', {
            data: { defaultActive : 'index' },
            url:'/intyg/{certificateType:ts.+}/:certificateId?:patientId&:hospName&:signed',

            views: {
                'main@webcert.intyg' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.ts.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'intyg@webcert.intyg.ts' : {
                    templateUrl: function($stateParams){
                        return '/web/webjars/' + $stateParams.certificateType + '/webcert/views/intyg/intyg.html';
                    }
                },
                'header@webcert.intyg.ts' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('webcert.fragasvar', {
            data: { defaultActive : 'unhandled-qa' },
            url: '/fragasvar/:certificateType/:certificateId',
            views: {
                'content@' : {
                    templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'header@webcert.fragasvar' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('webcert.fragasvar-qaonly', {
            data: { defaultActive : 'unhandled-qa' },
            url: '/fragasvar/:certificateType/:certificateId',
            views: {
                'content@' : {
                    templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'header@webcert.fragasvar-qaonly' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('webcert.webcert-about', {
            url: '/webcert/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.webcert.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.terms-about', {
            url: '/terms/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.terms.html',
                    controller: 'webcert.AboutWebcertTermsCtrl'
                }
            }
        }).
        state('webcert.support-about', {
            url: '/support/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.support.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.certificates-about', {
            url: '/certificates/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.certificates.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.faq-about', {
            url: '/faq/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.faq.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.cookies-about', {
            url: '/cookies/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.cookies.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.terms', {
            url: '/terms',
            views: {
                'dialogs@webcert': {
                    templateUrl: '/app/views/terms/terms.main.html',
                    controller: 'webcert.TermsCtrl'
                }
            }
        });

        $urlRouterProvider.when('', '/create/index');

});