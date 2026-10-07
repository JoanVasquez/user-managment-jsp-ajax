<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<c:url var="sessionCssUrl" value="/resources/css/session.css" />
<c:url var="sessionJsUrl" value="/resources/js/session.js" />
<c:url var="sessionsUrl" value="/api/sessions" />
<c:url var="passwordResetRequestsUrl" value="/api/password-reset-requests" />
<c:url var="welcomeUrl" value="/welcome" />
<layout:page title="${pageTitle}" bodyId="main">
<jsp:attribute name="head">
    <link rel="stylesheet" href="${sessionCssUrl}">
</jsp:attribute>
<jsp:attribute name="scripts">
    <script src="${sessionJsUrl}"></script>
</jsp:attribute>
<jsp:body>
<div id="page-config"
     hidden
     data-sessions-url="${sessionsUrl}"
     data-password-reset-requests-url="${passwordResetRequestsUrl}"
     data-welcome-url="${welcomeUrl}"></div>
<div class="container">
    <div class="alert alert-dismissible" id="alert" role="alert"></div>
    <div class="row">
        <div class="col-md-4"></div>
        <div class="col-md-4">
            <div class="flip">
                <div class="card">
                    <div class="face front">
                        <div class="panel panel-default">
                            <form class="form-horizontal" id="login-form">
                                <h1 class="text-center">SIGN IN</h1>
                                <input type="email" class="form-control" name="email" placeholder="Email" maxlength="254" required>
                                <input type="password" class="form-control" name="password" placeholder="Password" minlength="12" maxlength="128" required>
                                <p class="text-right"><a href="#" id="forgotten-password">Forgot Password</a></p>
                                <button class="btn btn-success btn-block">LOG IN</button>
                                <hr>
                                <p class="text-center"><a href="#" class="fliper-btn">Create new account?</a></p>
                            </form>
                        </div>
                    </div>
                    <div class="face back">
                        <div class="panel panel-default">
                            <form id="register-form" class="form-horizontal">
                                <h1 class="text-center">SIGN UP</h1>
                                <label>Basic Information</label>
                                <input type="text" class="form-control" name="firstname" placeholder="First Name" maxlength="100" required>
                                <input type="text" class="form-control" name="lastname" placeholder="Last Name" maxlength="100" required>
                                <input type="email" class="form-control" name="email" placeholder="Email" maxlength="254" required>
                                <label>Private Information</label>
                                <input type="password" class="form-control" name="password" placeholder="Password (12+ characters)" minlength="12" maxlength="128" required>
                                <input type="password" class="form-control" name="repeatpassword" placeholder="Repeat Password" minlength="12" maxlength="128" required>
                                <input type="submit" class="btn btn-primary btn-block" value="SIGN UP">
                                <p class="text-center"><a href="#" class="fliper-btn">Already have an account?</a></p>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4"></div>
    </div>
</div>
</jsp:body>
</layout:page>
