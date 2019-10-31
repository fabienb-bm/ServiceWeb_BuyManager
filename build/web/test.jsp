<%-- 
    Document   : test
    Created on : 18 juin 2014, 11:36:52
    Author     : Stagiaire
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<sql:query var="rs" dataSource="jdbc/TestDB">
select clientID from client
</sql:query>
<!DOCTYPE html>
<html>
  <head>
    <title>DB Test</title>
  </head>
  <body>

  <h2>Results</h2>

<c:forEach var="row" items="${rs.rows}">
    ClientID ${row.clientID}<br/>
</c:forEach>

  </body>
</html>
