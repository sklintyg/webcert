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

<title><spring:message code="application.name" /></title>

<link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon" />

<link rel="stylesheet" href="<c:url value="/css/bootstrap/2.3.2/bootstrap.css"/>">
<link rel="stylesheet" href="<c:url value="/css/inera.css"/>">
</head>
<body>

  <div class="container">

    <div id="content-container">
      <div class="content">

        <div class="row-fluid">
          <div id="content-body" class="span12" style="padding-top: 25px;">
            <pre>Detta är en startsida som inte skall finns tillgänglig i en produktionsmiljö!</pre>
            <h1>Testinloggningar</h1>



            <h2>Logga in</h2>

            <form id="customguidform" class="navbar-form pull-left" action="/fake" method="POST">

              <textarea name="userjson" rows="30" cols="100">
                {
                    "namn" : "Demon Stration",
                    "lakare" : true,
                    "vardgivare" : {
                        "id" : "vardgivar-id",
                        "namn" : "Vård i Väst",

                        "vardenheter" : [
                            {
                                "id" : "vardenhets-id",
                                "namn" : "Vårdcentrum i väst",
                                "mottagningar" : [
                                    {
                                        "id" : "mottagnings-id",
                                        "namn" : "KirMott"
                                    }
                                ]
                            }
                        ]
                    }
                }
              </textarea>

              <input type="submit">

            </form>
          </div>
        </div>
      </div>

    </div>
  </div>
</body>
</html>
