<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="bodyId" required="false" type="java.lang.String" %>
<%@ attribute name="head" required="false" fragment="true" %>
<%@ attribute name="scripts" required="false" fragment="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:url var="apiClientJsUrl" value="/resources/js/api-client.js" />
<c:url var="csrfUrl" value="/api/csrf" />
<c:url var="usersUrl" value="/api/users" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <title><c:out value="${title}" /></title>
    <jsp:invoke fragment="head" />
</head>
<body<c:if test="${not empty bodyId}"> id="${fn:escapeXml(bodyId)}"</c:if>>
<div id="app-config"
     hidden
     data-csrf-url="${csrfUrl}"
     data-users-url="${usersUrl}"></div>
<jsp:doBody />
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<script src="${apiClientJsUrl}"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
<jsp:invoke fragment="scripts" />
</body>
</html>
