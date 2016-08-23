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

/* global wcMessages, console */

(function() {
    'use strict';


    // --- end test hooks

    // Globally configure jquery not to cache ajax requests.
    // Our other angular $http service driven requests have their own solution (using an interceptor)

    $.ajaxSetup({cache: false});

    //Mock user object: Kanske skall vi ha möjlighet att växla mellan några olika users, typ en vårdadmin, privatläkare etc?
    var user = {
        "privatLakareAvtalGodkand":true,
        "hsaId":"000000000",
        "namn":"Kom Ponent",
        "titel":"",
        "forskrivarkod":"0000000",
        "authenticationScheme":"urn:inera:webcert:siths:fake",
        "vardgivare":[
            {
                "@class":"se.inera.intyg.common.integration.hsa.model.Vardgivare",
                "id":"IFV1239877878-1041",
                "namn":"WebCert-komponent-Vårdgivare1",
                "vardenheter":[
                    {
                        "@class":"se.inera.intyg.common.integration.hsa.model.Vardenhet",
                        "id":"0000000-1042",
                        "namn":"WebCert-Enhet1",
                        "epost":"enhet1@webcert.invalid.se",
                        "postadress":"Storgatan 1",
                        "postnummer":"12345",
                        "postort":"Småmåla",
                        "telefonnummer":"0101234567890",
                        "arbetsplatskod":"1234567890",
                        "agandeForm":"OFFENTLIG",
                        "mottagningar":[

                        ]
                    },
                    {
                        "@class":"se.inera.intyg.common.integration.hsa.model.Vardenhet",
                        "id":"0000000-1043",
                        "namn":"WebCert-Enhet2",
                        "epost":"enhet2@webcert.invalid.se",
                        "postadress":"Storgatan 2",
                        "postnummer":"12345",
                        "postort":"Småmåla2",
                        "telefonnummer":"0101234567890",
                        "arbetsplatskod":"12345678902",
                        "agandeForm":"OFFENTLIG",
                        "mottagningar":[

                        ]
                    }
                ]
            }
        ],
        "specialiseringar":[
            "Kirurgi i käkpartiet"
        ],
        "legitimeradeYrkesgrupper":[
            "Läkare"
        ],
        "valdVardenhet":{
            "@class":"se.inera.intyg.common.integration.hsa.model.Vardenhet",
            "id":"000000-1042",
            "namn":"WebCert-Enhet1",
            "epost":"enhet1@webcert.invalid.se",
            "postadress":"Storgatan 1",
            "postnummer":"12345",
            "postort":"Småmåla",
            "telefonnummer":"0101234567890",
            "arbetsplatskod":"1234567890",
            "agandeForm":"OFFENTLIG",
            "mottagningar":[

            ]
        },
        "valdVardgivare":{
            "@class":"se.inera.intyg.common.integration.hsa.model.Vardgivare",
            "id":"IFV1239877878-1041",
            "namn":"WebCert-Vårdgivare1",
            "vardenheter":[
                {
                    "@class":"se.inera.intyg.common.integration.hsa.model.Vardenhet",
                    "id":"IFV1239877878-1042",
                    "namn":"WebCert-Enhet1",
                    "epost":"enhet1@webcert.invalid.se",
                    "postadress":"Storgatan 1",
                    "postnummer":"12345",
                    "postort":"Småmåla",
                    "telefonnummer":"0101234567890",
                    "arbetsplatskod":"1234567890",
                    "agandeForm":"OFFENTLIG",
                    "mottagningar":[

                    ]
                }
            ]
        },
        "authenticationMethod":"FAKE",
        "features":[
            "arbetsgivarUtskrift",
            "arbetsgivarUtskrift.fk7263",
            "arbetsgivarUtskrift.luae_na",
            "hanteraFragor",
            "hanteraFragor.fk7263",
            "hanteraFragor.lisu",
            "hanteraFragor.luae_fs",
            "hanteraFragor.luae_na",
            "hanteraFragor.luse",
            "hanteraIntygsutkast",
            "hanteraIntygsutkast.fk7263",
            "hanteraIntygsutkast.lisu",
            "hanteraIntygsutkast.luae_fs",
            "hanteraIntygsutkast.luae_na",
            "hanteraIntygsutkast.luse",
            "hanteraIntygsutkast.ts-bas",
            "hanteraIntygsutkast.ts-diabetes",
            "jsLoggning",
            "kopieraIntyg",
            "kopieraIntyg.fk7263",
            "kopieraIntyg.lisu",
            "kopieraIntyg.luae_fs",
            "kopieraIntyg.luae_na",
            "kopieraIntyg.luse",
            "kopieraIntyg.ts-bas",
            "kopieraIntyg.ts-diabetes",
            "makuleraIntyg",
            "makuleraIntyg.fk7263",
            "makuleraIntyg.lisu",
            "makuleraIntyg.luae_fs",
            "makuleraIntyg.luae_na",
            "makuleraIntyg.luse",
            "makuleraIntyg.ts-bas",
            "makuleraIntyg.ts-diabetes",
            "skickaIntyg",
            "skickaIntyg.fk7263",
            "skickaIntyg.lisu",
            "skickaIntyg.luae_fs",
            "skickaIntyg.luae_na",
            "skickaIntyg.luse",
            "skickaIntyg.ts-bas",
            "skickaIntyg.ts-diabetes"
        ],
        "roles":{
            "LAKARE":{
                "name":"LAKARE",
                "desc":"Läkare",
                "privileges":[
                    {
                        "name":"VISA_INTYG",
                        "desc":"Visa intyg",
                        "intygstyper":[

                        ]

                    },
                    {
                        "name":"SKRIVA_INTYG",
                        "desc":"Skriva intyg",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"SIGNERA_INTYG",
                        "desc":"Signera intyg",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"MAKULERA_INTYG",
                        "desc":"Makulera intyg",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"KOPIERA_INTYG",
                        "desc":"Kopiera intyg",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"VIDAREBEFORDRA_UTKAST",
                        "desc":"Vidarebefordra utkast",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"VIDAREBEFORDRA_FRAGASVAR",
                        "desc":"Vidarebefordra frågasvar",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"BESVARA_KOMPLETTERINGSFRAGA",
                        "desc":"Besvara fråga om komplettering",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"FILTRERA_PA_LAKARE",
                        "desc":"Filtrera på annan läkare",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"ATKOMST_ANDRA_ENHETER",
                        "desc":"Åtkomst andra vårdenheter",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"HANTERA_PERSONUPPGIFTER",
                        "desc":"Hantera personuppgifter",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"HANTERA_MAILSVAR",
                        "desc":"Hantera notifieringsmail om frågasvar",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"NAVIGERING",
                        "desc":"Navigera i menyer, på logo, via tillbakaknappar",
                        "intygstyper":[

                        ]
                    },
                    {
                        "name":"SVARA_MED_NYTT_INTYG",
                        "desc":"Svara med nytt intyg",
                        "intygstyper":[

                        ]
                    }
                ]
            }
        },
        "authorities":{
            "FILTRERA_PA_LAKARE":{
                "name":"FILTRERA_PA_LAKARE",
                "desc":"Filtrera på annan läkare",
                "intygstyper":[

                ]
            },
            "VIDAREBEFORDRA_UTKAST":{
                "name":"VIDAREBEFORDRA_UTKAST",
                "desc":"Vidarebefordra utkast",
                "intygstyper":[

                ]
            },
            "HANTERA_MAILSVAR":{
                "name":"HANTERA_MAILSVAR",
                "desc":"Hantera notifieringsmail om frågasvar",
                "intygstyper":[

                ]
            },
            "HANTERA_PERSONUPPGIFTER":{
                "name":"HANTERA_PERSONUPPGIFTER",
                "desc":"Hantera personuppgifter",
                "intygstyper":[

                ]
            },
            "MAKULERA_INTYG":{
                "name":"MAKULERA_INTYG",
                "desc":"Makulera intyg",
                "intygstyper":[

                ]
            },
            "SIGNERA_INTYG":{
                "name":"SIGNERA_INTYG",
                "desc":"Signera intyg",
                "intygstyper":[

                ]
            },
            "BESVARA_KOMPLETTERINGSFRAGA":{
                "name":"BESVARA_KOMPLETTERINGSFRAGA",
                "desc":"Besvara fråga om komplettering",
                "intygstyper":[

                ]
            },
            "NAVIGERING":{
                "name":"NAVIGERING",
                "desc":"Navigera i menyer, på logo, via tillbakaknappar",
                "intygstyper":[

                ]
            },
            "ATKOMST_ANDRA_ENHETER":{
                "name":"ATKOMST_ANDRA_ENHETER",
                "desc":"Åtkomst andra vårdenheter",
                "intygstyper":[

                ]
            },
            "VIDAREBEFORDRA_FRAGASVAR":{
                "name":"VIDAREBEFORDRA_FRAGASVAR",
                "desc":"Vidarebefordra frågasvar",
                "intygstyper":[

                ]
            },
            "VISA_INTYG":{
                "name":"VISA_INTYG",
                "desc":"Visa intyg",
                "intygstyper":[

                ]
            },
            "SKRIVA_INTYG":{
                "name":"SKRIVA_INTYG",
                "desc":"Skriva intyg",
                "intygstyper":[

                ]
            },
            "SVARA_MED_NYTT_INTYG":{
                "name":"SVARA_MED_NYTT_INTYG",
                "desc":"Svara med nytt intyg",
                "intygstyper":[
                    "fk7263",
                    "lisu",
                    "luse",
                    "luae_na",
                    "luae_fs"
                ]
            },
            "KOPIERA_INTYG":{
                "name":"KOPIERA_INTYG",
                "desc":"Kopiera intyg",
                "intygstyper":[

                ]
            }
        },
        "origin":"NORMAL",
        "privatLakare":false,
        "lakare":true,
        "totaltAntalVardenheter": 2,
        "jsMinified":false,
        "tandLakare":false,
        "isLakareOrPrivat":true,
        "role":"Läkare"
    };



    angular.module('webcert',
        ['ui.bootstrap', 'ui.router', 'ngCookies', 'ngSanitize', 'common', 'ngAnimate', 'smoothScroll', 'formly', 'formlyBootstrap']);

    var app = angular.module('showcase',
        ['ui.bootstrap', 'ui.router', 'ngCookies', 'ngSanitize', 'webcert', 'common', 'ngAnimate', 'smoothScroll', 'formly', 'formlyBootstrap', 'ngMockE2E']);

    app.value('networkConfig', {
        defaultTimeout: 30000 // test: 1000
    });



    app.config(['$httpProvider', '$logProvider',
        function($httpProvider, $logProvider) {
            // Add cache buster interceptor
            $httpProvider.interceptors.push('common.httpRequestInterceptorCacheBuster');

            // Enable debug logging
            $logProvider.debugEnabled(true);
        }]);



    // Global config of default date picker config (individual attributes can be
    // overridden per directive usage)
    app.constant('uibDatepickerPopupConfig', {
        altInputFormats: [],
        appendToBody: true,
        clearText: 'Rensa',
        closeOnDateSelection: true,
        closeText: 'OK',
        currentText: 'Idag',
        datepickerPopup: 'yyyy-MM-dd',
        datepickerPopupTemplateUrl: 'uib/template/datepickerPopup/popup.html',
        datepickerTemplateUrl: 'uib/template/datepicker/datepicker.html',
        html5Types: {
            date: 'yyyy-MM-dd',
            'datetime-local': 'yyyy-MM-ddTHH:mm:ss.sss',
            'month': 'yyyy-MM'
        },
        onOpenFocus: true,
        showButtonBar: true,
        placement: 'auto bottom-left'
    });

    // Inject language resources
    app.run(['$log', '$rootScope', '$window', '$location', '$state', '$q', 'common.messageService', 'common.UserModel', 'formlyConfig', '$httpBackend',
        function($log, $rootScope, $window, $location, $state, $q, messageService, UserModel, formlyConfig, $httpBackend) {

            // Configure formly to use default hide directive.
            // must be ng-if or attic won't work because that works by watching when elements are destroyed and created, which only happens with ng-if.
            // With ng-show they are always in DOM and those phases won't happen.
            formlyConfig.extras.defaultHideDirective = 'ng-if';

            $rootScope.lang = 'sv';
            $rootScope.DEFAULT_LANG = 'sv';

            UserModel.setUser(user);
            UserModel.termsAccepted = true;
            UserModel.transitioning = false;

            //Kanske vi kan (i resp controller) sätta upp 'when' mockning så att direktiven kan köra som i en sandbox (Se exempel i arendehantering.controller.js)?
            // Detta kanske gör det möjligt att kunna laborera med ett direktivs alla funktioner som även kräver backendkommunikation.
            $httpBackend.whenGET(/^\/api\/*/).respond(200);
            $httpBackend.whenPOST(/^\/api\/*/).respond(200);
            $httpBackend.whenPUT(/^\/api\/*/).respond(200);

            $httpBackend.whenGET(/^\/moduleapi\/*/).respond(200);
            $httpBackend.whenPOST(/^\/moduleapi\/*/).respond(200);

            //Ev. templates skall få hämtas på riktigt
            $httpBackend.whenGET(/^.+\.html/).passThrough();

        }]);


    // Ladda alla dependencies
    $.get('/api/modules/map').then(function(modules) {


        var modulesIds = [];
        var modulePromises = [];
        //Add wc/common resources
        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/module.min.js'));

        angular.forEach(modules, function(module) {
            modulesIds.push(module.id);
            loadCssFromUrl(module.cssPath);
            modulePromises.push(loadScriptFromUrl(module.scriptPath + '.min.js'));

        });

        // Wait for all modules and module dependency definitions to load.
        $.when.apply(this, modulePromises).then(function() {

            angular.element(document).ready(function() {

                var allModules = [app.name, 'showcase', 'common'].concat(Array.prototype.slice.call(modulesIds, 0));



                // Everything is loaded, bootstrap the application with all dependencies.
                document.documentElement.setAttribute('ng-app', 'showcase');
                angular.bootstrap(document, allModules);

            });
        }).fail(function(error) {
            if (window.console) {
                console.log(error);
            }
        });
    });

    function loadCssFromUrl(url) {


        var link = document.createElement('link');
        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = url;
        document.getElementsByTagName('head')[0].appendChild(link);
    }

    function loadScriptFromUrl(url) {


        var result = $.Deferred();

        var script = document.createElement('script');
        script.async = 'async';
        script.type = 'text/javascript';
        script.src = url;
        script.onload = script.onreadystatechange = function(_, isAbort) {
            if (!script.readyState || /loaded|complete/.test(script.readyState)) {
                if (isAbort) {
                    result.reject();
                } else {
                    result.resolve();
                }
            }
        };
        script.onerror = function() {
            result.reject();
        };
        document.getElementsByTagName('head')[0].appendChild(script);
        return result.promise();
    }



}());
