<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>Login Page</title>
  <link rel="stylesheet" href="<c:url value="/css/bootstrap.css"/>">
  <style>
    .errorblock {
      color: #ff0000;
      background-color: #ffEEEE;
      border: 3px solid #ff0000;
      padding: 8px;
      margin: 16px;
    }
  </style>
</head>
<body onload='document.f.j_username.focus();'>
<c:if test="${not empty error}">
  <div class="errorblock">
    Your login attempt was not successful, try again.<br/> Caused :
      ${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
  </div>
</c:if>

<div style="padding-left: 20px;">
  <form name='f' action="<c:url value='j_spring_security_check' />"
        method='POST' class="form-horizontal">
    <fieldset>
      <legend>Inloggning</legend>
      <div class="control-group">
        User:
        <input type='text' name='j_username' value=''>
      </div>
      <div class="control-group">
        Password:
        <input type='password' name='j_password'/>
      </div>
      <div class="control-group">
        <td colspan='2'><input name="submit" type="submit"
                               value="Logga in" class="btn"/>
      </div>
    </fieldset>
  </form>
</div>
</body>
</html>