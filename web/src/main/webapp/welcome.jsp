<%--

    Copyright (C) 2013 Inera AB (http://www.inera.se)

    This file is part of Inera Certificate Web (http://code.google.com/p/inera-certificate-web).

    Inera Certificate Web is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Inera Certificate Web is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv" id="ng-app" ng-app="WcWelcomeApp">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="ROBOTS" content="nofollow, noindex"/>

    <title>Webcert test inloggning</title>

    <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon"/>

    <link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css"/>">

    <style type="text/css">
        textarea {
            font-family: Consolas, Lucida Console, monospace;
            font-size: 0.7em;
        }
        .envButtons { margin-right: 0.6em; }
        .envButtons input { margin-right: 0.2em; }
    </style>

    <script type="text/javascript" src="/web/webjars/angularjs/1.4.7/angular.min.js"></script>

    <script type="text/javascript">
        //Lägg till fler templates i arrayen + i options för att utöka antalet inloggingar

        var loginArr = [
            // Läkare, Vård och Behandling Webcert Enhet 1 (Webcert Vårdgivare 1)
            {
                "fornamn": "Jan",
                "efternamn": "Nilsson",
                "hsaId": "IFV1239877878-1049",
                "enhetId": "IFV1239877878-1042",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert-Enhet 1 (Webcert Vårdgivare 1)
            {
                "fornamn": "Åsa",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104B",
                "enhetId": "IFV1239877878-1042",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Åsa",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104B",
                "enhetId": "IFV1239877878-1045",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Åsa",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104B",
                "enhetId": "IFV1239877878-1046",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Åsa",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104B",
                "enhetId": "IFV1239877878-104C",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Statistik Webcert Enhet 3 (Webcert Vårdgivare 2) (Kan inte logga in)
            {
                "fornamn": "Åsa",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104B",
                "enhetId": "IFV1239877878-104D",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Lars",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104K",
                "enhetId": "IFV1239877878-1045",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Läkare, Vård och Behandling Webcert Enhet 3 (Webcert Vårdgivare 2)
            {
                "fornamn": "Lars",
                "efternamn": "Andersson",
                "hsaId": "IFV1239877878-104K",
                "enhetId": "IFV1239877878-104D",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Statistik Webcert Enhet 3 (Webcert Vårdgivare 2)
            {
                "fornamn": "Anna",
                "efternamn": "Persson",
                "hsaId": "IFV1239877878-104L",
                "enhetId": "IFV1239877878-104D",
                "lakare": false
            },

            // AT Läkare Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Anders",
                "efternamn": "Larsson",
                "hsaId": "IFV1239877878-104M",
                "enhetId": "IFV1239877878-1045",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Vårdadministratör Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Lena",
                "efternamn": "Karlsson",
                "hsaId": "IFV1239877878-104N",
                "enhetId": "IFV1239877878-1045",
                "lakare": false,
                "forskrivarKod": "2481632"
            },

            // Markus Gran Testanvändare @ VårdEnhet1A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103F",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            // Markus Gran Testanvändare @ VårdEnhet2A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103H",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },
            // Markus Gran Testanvändare @ VårdEnhetA
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103D",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            // Läkare med flera enheter & mottagningar
            {
                "fornamn" : "Eva",
                "efternamn" : "Holgersson",
                "hsaId" : "eva",
                "enhetId" : "centrum-vast",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            // Läkare med massor av enheter & mottagningar
            {
                "fornamn" : "Staffan",
                "efternamn" : "Stafett",
                "hsaId" : "staffan",
                "enhetId" : "linkoping",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            // Läkare för test av djupintegration
            {
                "fornamn" : "Journa",
                "efternamn" : "La System",
                "hsaId" : "SE4815162344-1B02",
                "enhetId" : "SE4815162344-1A03",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            {
                "fornamn" : "Ivar",
                "efternamn" : "Integration",
                "hsaId" : "SE4815162344-1B01",
                "enhetId" : "SE4815162344-1A02",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },


            // Admin personal med flera enheter & mottagningar
            {
                "fornamn" : "Adam",
                "efternamn" : "Admin",
                "hsaId" : "adam",
                "enhetId" : "centrum-vast",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            // Admin personal med 3 enheter och mottagningar
            {
                "fornamn" : "Adamo",
                "efternamn" : "Admin",
                "hsaId" : "adamo",
                "enhetId" : "centrum-vast",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            // Admin personal med 3 enheter och mottagningar
            {
                "fornamn" : "Adamo",
                "efternamn" : "Admin",
                "hsaId" : "adamo",
                "enhetId" : "centrum-ost",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            {
                "fornamn" : "Test",
                "efternamn" : "Testsson",
                "hsaId" : "fitness1",
                "enhetId" : "vardenhet-fit-1",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            // FitNesse Admin personal med 1 enhet
            {
                "fornamn" : "fit",
                "efternamn" : "nesse",
                "hsaId" : "fitness2",
                "enhetId" : "vardenhet-fit-2",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            {
                "fornamn" : "Han",
                "efternamn" : "Solo",
                "hsaId" : "hansolo",
                "enhetId" : "centrum-norr",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },

            {
                "fornamn" : "Per",
                "efternamn" : "Persson",
                "hsaId" : "perpersson",
                "enhetId" : "anestesikliniken",
                "lakare" : true,
                "forskrivarKod": "1111"
            },

            // Privatläkare
            {
                "firstName": "Tolvan",
                "lastName": "Privatläkarsson",
                "personId" : "19121212-1212",
                "privatLakare" : true
            },

            {
                "firstName": "Nina",
                "lastName": "Greger",
                "personId" : "19730906-9289",
                "privatLakare" : true
            },

            // Tandläkare
            {
                "fornamn" : "Tore",
                "efternamn" : "Tandläkare",
                "hsaId" : "tore-tandlakare",
                "enhetId" : "tandenheten-1",
                "tandlakare" : true,
                "forskrivarKod": "6745341"
            },

            // NMT testperson
            {
                "fornamn": "Åsa",
                "efternamn": "Svensson",
                "hsaId": "TSTNMT2321000156-100L",
                "enhetId": "TSTNMT2321000156-1003",
                "lakare": true,
                "forskrivarKod": "2481632"
            },

            // Tandläkare som ska finnas i demo-miljö och därmed i test-HSA
            {
              "fornamn" : "Louise",
              "efternamn" : "Ericsson",
              "hsaId" : "TSTNMT2321000156-103B",
              "enhetId" : "TSTNMT2321000156-1039",
              "tandlakare" : true
            },

            // Läkare inom EU/ESS/Schweiz som ska finnas i demo-miljö och därmed i test-HSA
            {
                "fornamn": "Leonie",
                "efternamn": "Koehl",
                "hsaId": "TSTNMT2321000156-103F",
                "enhetId": "TSTNMT2321000156-1039",
                "lakare" : true,
                "forskrivarKod": "9300005",
                "befattningsKod": "203090"
            },

            // Läkare utom EU/ESS/Schweiz som ska finnas i demo-miljö och därmed i test-HSA
            {
                "fornamn": "Bill",
                "efternamn": "Smith",
                "hsaId": "TSTNMT2321000156-103G",
                "enhetId": "TSTNMT2321000156-1039",
                "lakare" : true,
                "forskrivarKod": "9400003",
                "befattningsKod": "203090"
            },

            // Vikarierande examinerad läkare som ska finnas i demo-miljö och därmed i test-HSA
            {
                "fornamn": "Martin",
                "efternamn": "Johansson",
                "hsaId": "TSTNMT2321000156-103H",
                "enhetId": "TSTNMT2321000156-1039",
                "lakare" : true,
                "forskrivarKod": "9100009",
                "befattningsKod": "204090"
            }
        ];

        function updateJsonInput() {
            var jsonEl = document.getElementById("userJson");
            var jsonElView = document.getElementById("userJsonDisplay");
            var selector = document.getElementById("jsonSelect");

            jsonElView.value = JSON.stringify(loginArr[selector.selectedIndex], undefined, 1);
            jsonEl.value = escape(JSON.stringify(loginArr[selector.selectedIndex], undefined, 1));
        };

        window.doneLoading = true;
        window.dialogDoneLoading = true;
        window.rendered = true;

        angular.module('WcWelcomeApp', [
          'WcWelcomeApp.controllers'
        ]);

        angular.module('WcWelcomeApp.controllers', []).
            controller('welcomeController', function($scope) {
                $scope.loginModel = loginArr;
                $scope.selectedIndex = '0';
                $scope.$watch('selectedIndex', function(newSelected, oldVal) {
                  var jsonEl = angular.element( document.querySelector( '#userJson' ) );
                  var jsonElView = angular.element( document.querySelector( '#userJsonDisplay' ) );
                  var selector = angular.element( document.querySelector( '#jsonSelect' ) );

                  var loginJson = JSON.stringify($scope.loginModel[newSelected], undefined, 1);
                  jsonElView.text( loginJson );
                  jsonEl.text( escape( loginJson ) );
                });

                $scope.environment = {
                  name: 'all'
                };

                $scope.whichEnv = function(env) {
                  if ($scope.environment.name === 'all') {
                    return true;
                  }
                  if ($scope.environment.name === 'dev' && env === 'dev') {
                    return true;
                  }
                  if ($scope.environment.name === 'demo' && env === 'demo') {
                    return true;
                  }

                  return false;
                };

                $scope.data = {
                  repeatSelect: null,
                  availableOptions: [
                    {id:0,  hsaId: 'IFV1239877878-1049_IFV1239877878-1042',       env: 'dev',  name: 'Jan Nilsson - WebCert Enhet 1 (Läkare / Vård och Behandling + Admin)'},
                    {id:1,  hsaId: 'IFV1239877878-104B_IFV1239877878-1042',       env: 'dev',  name: 'Åsa Andersson - WebCert Enhet 1 (Läkare / Vård och Behandling)'},
                    {id:2,  hsaId: 'IFV1239877878-104B_IFV1239877878-1045',       env: 'dev',  name: 'Åsa Andersson - WebCert Enhet 2 + 2UE (Läkare / Vård och Behandling)'},
                    {id:3,  hsaId: 'IFV1239877878-104B_IFV1239877878-1046',       env: 'dev',  name: 'Åsa Andersson - WebCert Enhet 2 - Underenhet 1 (Läkare / Vård och Behandling)'},
                    {id:4,  hsaId: 'IFV1239877878-104B_IFV1239877878-104C',       env: 'dev',  name: 'Åsa Andersson - WebCert Enhet 2 - Underenhet 2  (Läkare / Vård och Behandling)'},
                    {id:5,  hsaId: 'IFV1239877878-104B_IFV1239877878-104D',       env: 'dev',  name: 'Åsa Andersson - WebCert Enhet 3 (Läkare+Vårdadmin)'},
                    {id:6,  hsaId: 'IFV1239877878-104K_IFV1239877878-1045',       env: 'dev',  name: 'Lars Andersson - WebCert Enhet 2 + 2UE (Läkare / Vård och Behandling)'},
                    {id:7,  hsaId: 'IFV1239877878-104K_IFV1239877878-104D',       env: 'dev',  name: 'Lars Andersson - WebCert Enhet 3 (Läkare / Vård och Behandling)'},
                    {id:8,  hsaId: 'IFV1239877878-104L_IFV1239877878-104D',       env: 'dev',  name: 'Anna Persson - WebCert Enhet 3 (Statistik)'},
                    {id:9,  hsaId: 'IFV1239877878-104M_IFV1239877878-1045',       env: 'dev',  name: 'Anders Larsson WebCert Enhet 2 + 2UE (AT Läkare / Vård och Behandling)'},
                    {id:10, hsaId: 'IFV1239877878-104N_IFV1239877878-1045',       env: 'dev',  name: 'Lena Karlsson - WebCert Enhet 2 + 2UE (Vårdadmin / Vård och Behandling)'},
                    {id:11, hsaId: 'TST5565594230-106J_IFV1239877878-103F',       env: 'dev',  name: 'Markus Gran (Läkare / VårdEnhet1A)'},
                    {id:12, hsaId: 'TST5565594230-106J_IFV1239877878-103H',       env: 'dev',  name: 'Markus Gran (Läkare / VårdEnhet2A)'},
                    {id:13, hsaId: 'TST5565594230-106J_IFV1239877878-103D',       env: 'dev',  name: 'Markus Gran (Läkare / VårdEnhetA)'},
                    {id:14, hsaId: 'eva_centrum-vast',                            env: 'dev',  name: 'Eva Holgersson (Läkare / Centrum Väst)'},
                    {id:15, hsaId: 'staffan',                                     env: 'dev',  name: 'Staffan Stafett (Läkare / Centrum Väst, Linköping, Norrköping)'},
                    {id:16, hsaId: 'SE4815162344-1B02_SE4815162344-1A03',         env: 'dev',  name: 'Journa La System (Läkare, djupintegration / WebCert-Integration Enhet 2)'},
                    {id:17, hsaId: 'SE4815162344-1B01_SE4815162344-1A02',         env: 'dev',  name: 'Ivar Integration (Läkare, djupintegration / WebCert-Integration Enhet 1)'},
                    {id:18, hsaId: 'adam_centrum-vast',                           env: 'dev',  name: 'Adam Admin (Vårdadmin / Centrum Väst)'},
                    {id:19, hsaId: 'adamo_centrum-vast',                          env: 'dev',  name: 'Adamo Admin (Vårdadmin / Centrum Väst)'},
                    {id:20, hsaId: 'adamo_centrum-ost',                           env: 'dev',  name: 'Adamo Admin (Vårdadmin / Centrum Öst)'},
                    {id:21, hsaId: 'fitnesse-admin1',                             env: 'dev',  name: 'Fitnesse Admin (Vårdadmin / Vardenhet Fitnesse 1)'},
                    {id:22, hsaId: 'fitnesse-admin2',                             env: 'dev',  name: 'Fitnesse Admin-1 (Vårdadmin / Vardenhet Fitnesse 2)'},
                    {id:23, hsaId: 'hansolo_centrum-norr',                        env: 'dev',  name: 'Han Solo (Vårdadmin / Centrum Norr)'},
                    {id:24, hsaId: 'perpersson_anestesikliniken',                 env: 'dev',  name: 'Per Persson (Läkare / Anestesikliniken)'},
                    {id:25, hsaId: 'private-practitioner-1',                      env: 'dev',  name: 'Tolvan Privatläkarsson (Privatläkare, godkänd)'},
                    {id:26, hsaId: 'private-practitioner-2',                      env: 'dev',  name: 'Nina Greger (Privatläkare, ej godkänd)'},
                    {id:27, hsaId: 'tore-tandlakare',                             env: 'dev',  name: 'Tore Tandläkare (Tandläkare)'},
                    {id:28, hsaId: 'TSTNMT2321000156-100L_TSTNMT2321000156-1003', env: 'demo', name: 'Åsa Svensson (NMT variabel testperson)'},
                    {id:29, hsaId: 'TSTNMT2321000156-100B_TSTNMT2321000156-1039', env: 'demo', name: 'Louise Ericsson (Tandläkare / Vård och Behandling)'},
                    {id:30, hsaId: 'TSTNMT2321000156-100F_TSTNMT2321000156-1039', env: 'demo', name: 'Leonie Koehl (Läkare inom EU/ESS/Schweiz / Vård och Behandling)'},
                    {id:31, hsaId: 'TSTNMT2321000156-100G_TSTNMT2321000156-1039', env: 'demo', name: 'Bill Smith (Läkare utom EU/ESS/Schweiz / Vård och Behandling)'},
                    {id:32, hsaId: 'TSTNMT2321000156-100H_TSTNMT2321000156-1039', env: 'demo', name: 'Martin Johansson (Vikarierande examinerad läkare / Vård och Behandling)'}
                  ]
                };

         });
    </script>
</head>
<body ng-controller="welcomeController">
<form id="loginForm" action="/fake" method="POST" class="form-inline" accept-charset="UTF-8">
    <div class="container">

        <div id="content-container">
            <div class="content row">

                <h1>Testinloggningar Webcert</h1>

                <p class="well">Templatelista till vänster - Manuella ändringar kan göras i jsonstrukturen - detta
                    omvandlas till inloggad userContext</p>

                <div class="form-group col-xs-8">
                    <h4>Mallar</h4>
                    <select name="jsonSelect" id="jsonSelect" ng-model="selectedIndex" size="15" class="form-control" style="width: 100%">
                      <option ng-repeat="option in data.availableOptions" ng-if="whichEnv(option.env)" id="{{option.hsaId}}" value="{{option.id}}">{{option.name}}</option>
                    </select>
                    <input id="loginBtn" type="submit" value="Logga in" class="btn btn-primary" style="margin-top: 20px; width: 100%">
                </div>

                <div class="form-group col-xs-4">
                    <div>
                        <h4>Miljö</h4>
                        <span class="envButtons"><input type="radio" ng-model="environment.name" value="all" />All</span>
                        <span class="envButtons"><input type="radio" ng-model="environment.name" value="dev" />Dev</span>
                        <span class="envButtons"><input type="radio" ng-model="environment.name" value="demo" />Demo</span>
                    </div>
                    <div style="padding-top: 0.6em;">
                        <h4>Inloggningsprofil</h4>
                        <input type="hidden" id="userJson" name="userjson"/>
                        <textarea id="userJsonDisplay" name="userJsonDisplay" class="field form-control" style="height: 200px; width: 100%;">
                        </textarea>
                    </div>
                </div>

            </div>
          <div class="content row"><hr style="padding-top:16px;"/></div>
            <div class="content row">
                <h1>Hjälplänkar</h1>
                <p class="well">Nedan finns ett antal snabblänkar till hjälpfunktioner för utvecklings- och teständamål.</p>
                <a href="/version.jsp" target="_blank">Versions- och bygginformation</a><br/>
                <a href="/healthcheck.jsp" target="_blank">Healthcheck</a><br/>
                <a href="/swagger-ui/index.html" target="_blank">REST-dokumentation</a><br/>
            </div>
        </div>
    </div>


</form>

</body>
</html>
