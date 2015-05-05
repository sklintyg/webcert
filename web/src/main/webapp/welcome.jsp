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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv">
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
    </style>

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
                "lakare": false,
                "forskrivarKod": "2481632"
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

            // Vård och Behandling Webcert Enhet 2 (Webcert Vårdgivare 2)
            {
                "fornamn": "Lena",
                "efternamn": "Karlsson",
                "hsaId": "IFV1239877878-104N",
                "enhetId": "IFV1239877878-1045",
                "lakare": false,
                "forskrivarKod": "2481632"
            },

            //Markus Gran Testanvändare  @ VårdEnhet1A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103F",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            //Markus Gran Testanvändare  @ VärdEnhet2A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103H",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },
            //Markus Gran Testanvändare  @ VärdEnhetA
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103D",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },

            //Läkare med flera enheter&mottagningar
            {
                "fornamn" : "Eva",
                "efternamn" : "Holgersson",
                "hsaId" : "eva",
                "enhetId" : "centrum-vast",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },
            //Läkare med massor av enheter&mottagningar
            {
                "fornamn" : "Staffan",
                "efternamn" : "Stafett",
                "hsaId" : "staffan",
                "enhetId" : "linkoping",
                "lakare" : true,
                "forskrivarKod": "2481632"
            },//Läkare för test av djupintegration
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
            //Admin personal med flera enheter&mottagningar
            {
                "fornamn" : "Adam",
                "efternamn" : "Admin",
                "hsaId" : "adam",
                "enhetId" : "centrum-vast",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },
            //Admin personal med 3 enheter och mottagningar
            {
                "fornamn" : "Adamo",
                "efternamn" : "Admin",
                "hsaId" : "adamo",
                "enhetId" : "centrum-vast",
                "lakare" : false,
                "forskrivarKod": "2481632"
            },
            //Admin personal med 3 enheter och mottagningar
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
            //FitNesse Admin personal med 1 enhet
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
            }
        ];

        function updateJsonInput() {
            var jsonEl = document.getElementById("userJson");
            var jsonElView = document.getElementById("userJsonDisplay");
            var selector = document.getElementById("jsonSelect");
            //jsonEl.value = escape(JSON.stringify(loginArr[selector.selectedIndex], undefined, 2));
            jsonElView.value = JSON.stringify(loginArr[selector.selectedIndex], undefined, 1);
            jsonEl.value = escape(JSON.stringify(loginArr[selector.selectedIndex], undefined, 1));
        };

        window.doneLoading = true;
        window.dialogDoneLoading = true;
        window.rendered = true;
    </script>
</head>
<body onLoad="updateJsonInput()">
<form id="loginForm" action="/fake" method="POST" class="form-inline" accept-charset="UTF-8">
    <div class="container">

        <div id="content-container">
            <div class="content row">


                <h1>Testinloggningar Webcert</h1>

                <p class="well">Templatelista till vänster - Manuella ändringar kan göras i jsonstrukturen - detta
                    omvandlas till inloggad userContext</p>

                <div class="form-group col-xs-8">

                    <h4>Mallar</h4>
                    <select id="jsonSelect" name="jsonSelect" onChange="updateJsonInput()" size="15" style="width: 100%" class="form-control">
                        <option value="0" id="IFV1239877878-1049_IFV1239877878-1042" selected>Jan Nilsson - WebCert Enhet 1 (Läkare)(Vård och Behandling + Admin)</option>
                        <option value="1" id="IFV1239877878-104B_IFV1239877878-1042">Åsa Andersson - WebCert Enhet 1 (Läkare)(Vård och Behandling)</option>
                        <option value="2" id="IFV1239877878-104B_IFV1239877878-1045">Åsa Andersson - WebCert Enhet 2 + 2UE (Läkare)(Vård och Behandling)</option>
                        <option value="3" id="IFV1239877878-104B_IFV1239877878-1046">Åsa Andersson - WebCert Enhet 2 - Underenhet 1 (Läkare)(Vård och Behandling)</option>
                        <option value="4" id="IFV1239877878-104B_IFV1239877878-104C">Åsa Andersson - WebCert Enhet 2 - Underenhet 2  (Läkare)(Vård och Behandling)</option>
                        <option value="5" id="IFV1239877878-104B_IFV1239877878-104D">Åsa Andersson - WebCert Enhet 3 (Läkare)(Admin)</option>
                        <option value="6" id="IFV1239877878-104K_IFV1239877878-1045">Lars Andersson - WebCert Enhet 2 + 2UE (Läkare)(Vård och Behandling)</option>
                        <option value="6" id="IFV1239877878-104K_IFV1239877878-104D">Lars Andersson - WebCert Enhet 3 (Läkare)(Vård och Behandling)</option>
                        <option value="7" id="IFV1239877878-104L_IFV1239877878-104D">Anna Persson - WebCert Enhet 3 (Statistik)</option>
                        <option value="8" id="IFV1239877878-104M_IFV1239877878-1045">Anders Larsson WebCert Enhet 2 + 2UE (AT Läkare)(Vård och Behandling)</option>
                        <option value="9" id="IFV1239877878-104N_IFV1239877878-1045">Lena Karlsson - WebCert Enhet 2 + 2UE (Vård och Behandling)</option>
                        <option value="10" id="TST5565594230-106J_IFV1239877878-103F">Markus Gran (Läkare VårdEnhet1A)</option>
                        <option value="11" id="TST5565594230-106J_IFV1239877878-103H">Markus Gran (Läkare VårdEnhet2A)</option>
                        <option value="12" id="TST5565594230-106J_IFV1239877878-103D">Markus Gran (Läkare VårdEnhetA)</option>
                        <option value="13" id="eva_centrum-vast">Eva Holgersson (Läkare Centrum Väst)</option>
                        <option value="14">Staffan Stafett (Läkare Centrum Väst, Linköping, Norrköping)</option>
                        <option value="15" id ="SE4815162344-1B02_SE4815162344-1A03">Journa La System (Läkare WebCert-Integration Enhet 2)</option>
                        <option value="16" id="SE4815162344-1B01_SE4815162344-1A02">Ivar Integration (Läkare WebCert-Integration Enhet 1)</option>
                        <option value="17">Adam Admin (Administratör Centrum Väst)</option>
                        <option value="18">Adamo Admin (Administratör Centrum Väst)</option>
                        <option value="19">Adamo Admin (Administratör Centrum Väst)</option>
                        <option value="20" id="fitnesse-admin1">Fitnesse Admin (Administratör Vardenhet Fitnesse 1)</option>
                        <option value="21" id="fitnesse-admin2">Fitnesse Admin-1 (Administratör Vardenhet Fitnesse 2)</option>
                        <option value="21">Han Solo (Administratör, Centrum Norr)</option>
                    </select>
                    <input id="loginBtn" type="submit" value="Logga in" class="btn btn-primary"
                           style="margin-top: 20px;width: 100%">

                </div>

                <div class="form-group col-xs-4">
                    <p>
                    <h4>Inloggningsprofil</h4>
                    <input type="hidden" id="userJson" name="userjson"/>
                    <textarea id="userJsonDisplay" name="userJsonDisplay" class="field form-control"
                              style="height: 200px;width: 100%;">
                    </textarea>
                </div>


            </div>
        </div>
    </div>


</form>

</body>
</html>
