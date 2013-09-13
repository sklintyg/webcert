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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html lang="sv">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="ROBOTS" content="nofollow, noindex" />

<title>WebCert test inloggning</title>

<link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon" />

<link rel="stylesheet" href="<c:url value="/css/bootstrap/2.3.2/bootstrap.css"/>">
<link rel="stylesheet" href="<c:url value="/css/inera.css"/>">
<style type="text/css">
textarea {
	font-family: Consolas, Lucida Console, monospace;
	font-size: 0.7em;
}
</style>

<script type="text/javascript">
    //Lägg till fler templates i arrayen + i options för att utöka antalet inloggingar 

    var loginArr = [
            //Läkare med flera enheter&mottagningar
            '{\n' + '    "namn": "Eva Holgersson",\n' + '   "lakare": true,\n' + '    "vardgivare": {\n' + '        "id": "vardgivar-id",\n' + '        "namn": "Vård i Väst",\n'
                    + '        "vardenheter": [{\n' + '            "id": "vardenhets-id",\n' + '            "namn": "Vårdcentrum i väst",\n' + '            "mottagningar": [{\n'
                    + '                "id": "mottagnings-id",\n' + '                "namn": "KirMott"\n' + '            }]\n' + '        }]\n' + '    }\n' + '}\n',
            //Admin personal med 1 enheter utan mottagningar
            '{\n' + '    "namn": "Adam Admin",\n' + '    "lakare": false,\n' + '   "vardgivare": {\n' + '       "id": "vardgivar-id",\n' + '       "namn": "Vårdgivare i Väst",\n'
                    + '       "vardenheter": [{\n' + '           "id": "vardenhets-id",\n' + '           "namn": "Väntrummets enhet"\n' + '       }]\n' + '   }\n' + '}\n',
            //Admin personal med 2 enheter och mottagningar
            '{\n' + '    "namn": "Adamo Admin",\n' + '    "lakare": false,\n' + '   "vardgivare": {\n' + '       "id": "vardgivar-id",\n' + '       "namn": "Vårdgivare i Väst",\n'
                    + '        "vardenheter": [{\n' + '            "id": "vardenhets-id",\n' + '            "namn": "Vårdcentrum i väst",\n' + '            "mottagningar": [{\n'
                    + '                "id": "mottagnings-id",\n' + '                "namn": "Dialys"\n' + '            },\n' + '            {\n' + '                "id": "mottagnings-id-x",\n'
                    + '                "namn": "Akuten"\n' + '            }]\n' + '        },' + '        {\n' + '            "id": "vardenhets-id-2",\n'
                    + '            "namn": "Vårdcentrum i Öst",\n' + '            "mottagningar": [{\n' + '                "id": "mottagnings-id-2",\n'
                    + '                "namn": "Nagelmottagningen"\n' + '            }]\n' + '        }]\n' + '   }\n' + '}\n' ];

    function updateJsonInput() {
        var jsonEl = document.getElementById("userJson");
        var selector = document.getElementById("jsonSelect");
        jsonEl.value = loginArr[selector.selectedIndex];
    }
</script>
</head>
<body onLoad="updateJsonInput()">
  <form id="loginForm" action="/fake" method="POST" class="form-inline">
    <div class="container">

      <div id="content-container">
        <div class="content row">






          <h1>Testinloggningar WebCert</h1>
          <p class="well">Templatelista till vänster - Manuella ändringar kan göras i jsonstrukturen - detta omvandlas till inloggad userContext</p>

          <div class="form-group span4">
            
            <h4>Mallar</h4>
            <select id="jsonSelect" name="jsonSelect" onChange="updateJsonInput()" size="8" style="width: 100%">
              <option value="0" selected>Eva Holgersson (Läkare)</option>
              <option value="1">Adam Admin (Administratör)</option>
              <option value="2">Adamo Admin (Administratör flera enheter)</option>
            </select>
            </p>
            
              <input type="submit" value="Logga in" class="btn btn-primary btn-default" style="width: 100%">
            
          </div>

          <div class="form-group span8">
            <p>
            <h4>Inloggningsprofil</h4>
            <textarea id="userJson" name="userjson" class="field" style="height: 400px; width: 100%">
              </textarea>
            </p>
          </div>


        </div>
      </div>
    </div>


  </form>

</body>
</html>
