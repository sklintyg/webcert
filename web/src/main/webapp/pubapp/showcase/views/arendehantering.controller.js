angular.module('showcase').controller('showcase.ArendeCtrl',
    [ '$scope', 'common.ArendenViewStateService', '$httpBackend',
        function($scope, ArendenViewStateService, $httpBackend) {
            'use strict';



            $scope.arendeList = [
                {
                    "answerDisabled":false,
                    "svaraMedNyttIntygDisabled":false,
                    "svaraMedNyttIntygDisabledReason":"",
                    "atgardMessageId":"svarfranvarden",
                    "arende":{
                        "paminnelser":[

                        ],
                        "senasteHandelse":"2016-06-08T15:50:02.596",
                        "fraga":{
                            "kompletteringar":[

                            ],
                            "internReferens":"6",
                            "status":"PENDING_INTERNAL_ACTION",
                            "amne":"OVRIGT",
                            "meddelandeRubrik":"En rubrik",
                            "vidarebefordrad":false,
                            "frageStallare":"FK",
                            "externaKontakter":[

                            ],
                            "meddelande":"Hej, hur står det till idag?",
                            "signeratAv":"Jan Nilsson",
                            "svarSkickadDatum":"2014-11-22T14:53:00.000",
                            "intygId":"421f06cd-a6d1-4bcb-a42b-570c6047f030",
                            "enhetsnamn":"WebCert-Enhet1",
                            "vardgivarnamn":"WebCert-Vårdgivare1",
                            "timestamp":"2016-06-08T15:50:02.596",
                            "arendeType":"FRAGA"
                        }
                    },
                    "kompletteringar":[

                    ]
                },
                {
                    "answerDisabled":false,
                    "svaraMedNyttIntygDisabled":false,
                    "svaraMedNyttIntygDisabledReason":"",
                    "atgardMessageId":"svarfranfk",
                    "arende":{
                        "paminnelser":[

                        ],
                        "senasteHandelse":"2016-06-08T15:50:02.596",
                        "fraga":{
                            "kompletteringar":[

                            ],
                            "internReferens":"7",
                            "status":"PENDING_EXTERNAL_ACTION",
                            "amne":"AVSTMN",
                            "meddelandeRubrik":"En rubrik",
                            "vidarebefordrad":false,
                            "frageStallare":"WC",
                            "externaKontakter":[

                            ],
                            "meddelande":"Hej, hur står det till idag?",
                            "signeratAv":"Jan Nilsson",
                            "svarSkickadDatum":"2014-11-22T14:53:00.000",
                            "intygId":"421f06cd-a6d1-4bcb-a42b-570c6047f030",
                            "enhetsnamn":"WebCert-Enhet1",
                            "vardgivarnamn":"WebCert-Vårdgivare1",
                            "timestamp":"2016-06-08T15:50:02.596",
                            "arendeType":"FRAGA"
                        }
                    },
                    "kompletteringar":[

                    ]
                }
            ];
            //Mocka response för när man klickar på vidarebefordrad
            $httpBackend.whenPUT(/^\/moduleapi\/arende\/.+\/vidarebefordrad*/).respond(
                {"fraga":{
                "kompletteringar":[

                ],
                "internReferens":"6",
                "status":"PENDING_INTERNAL_ACTION",
                "amne":"OVRIGT",
                "meddelandeRubrik":"En rubrik",
                "vidarebefordrad":true,
                "frageStallare":"FK",
                "externaKontakter":[

                ],
                "meddelande":"Hej, hur står det till idag?",
                "signeratAv":"Jan Nilsson",
                "svarSkickadDatum":"2014-11-22T14:53:00.000",
                "intygId":"421f06cd-a6d1-4bcb-a42b-570c6047f030",
                "enhetsnamn":"WebCert-Enhet1",
                "vardgivarnamn":"WebCert-Vårdgivare1",
                "timestamp":"2016-06-08T15:50:02.596",
                "arendeType":"FRAGA"
            }});

            ArendenViewStateService.intyg = {
                "typ": "luae_fs",
                "id": "0000001",
                "textVersion": "1",
                "grundData": {
                    "patient": {
                        "samordningsNummer": false,
                        "efternamn": "Olsson",
                        "fornamn": "Olivia",
                        "fullstandigtNamn": "Olivia Olsson",
                        "personId": "192703104321"
                    },
                    "skapadAv": {
                        "vardenhet": {
                            "arbetsplatsKod": "45312",
                            "vardgivare": {
                                "vardgivarnamn": "VG1",
                                "vardgivarid": "12345678"
                            },

                            "enhetsnamn": "VE1",
                            "enhetsid": "123456789"
                        },
                        "specialiteter": [
                            "Kirurg"
                        ],
                        "befattningar": [
                            "Klinikchef",
                            "Forskningsledare"
                        ],
                        "forskrivarKod": "09874321",
                        "fullstandigtNamn": "Karl Karlsson",
                        "personId": "19650708-1234"
                    },
                    "signeringsdatum": "2015-12-07T15:48:05.000"
                }
            };

            $scope.viewState = ArendenViewStateService;
        }]);
