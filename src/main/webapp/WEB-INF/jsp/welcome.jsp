<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<c:url var="mainCssUrl" value="/resources/css/main.css" />
<c:url var="mainJsUrl" value="/resources/js/main.js" />
<c:url var="currentSessionUrl" value="/api/sessions/current" />
<c:url var="indexUrl" value="/" />
<layout:page title="${pageTitle}">
<jsp:attribute name="head">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.8/css/dataTables.bootstrap.min.css">
    <link rel="stylesheet" href="${mainCssUrl}">
</jsp:attribute>
<jsp:attribute name="scripts">
    <script src="https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.8/js/dataTables.bootstrap.min.js"></script>
    <script src="${mainJsUrl}"></script>
</jsp:attribute>
<jsp:body>
<div id="page-config"
     hidden
     data-current-session-url="${currentSessionUrl}"
     data-index-url="${indexUrl}"></div>
<div class="container">
    <div class="row">
        <div class="alert alert-dismissible" id="panel-alert" role="alert"></div>
        <div class="col-md-10 col-md-offset-1">
            <div class="panel panel-default panel-table">
                <div class="panel-heading"><div class="row">
                    <div class="col col-xs-3"><h3 class="panel-title">Your account</h3></div>
                    <div class="col col-xs-8 text-right">
                        <c:if test="${authenticated}">
                            <button class="btn btn-success" id="btn-logout">Logout</button>
                        </c:if>
                    </div>
                </div></div>
                <div class="panel-body"><table id="tbl-user" class="table table-striped table-bordered" width="100%"></table></div>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="updateModal" role="dialog"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><button type="button" class="close" data-dismiss="modal">&times;</button><h4>Edit account</h4></div>
    <div class="modal-body"><form class="form-horizontal" id="update-user">
        <div class="alert alert-dismissible" id="modal-alert" role="alert"></div>
        <input type="hidden" name="iduser">
        <input type="text" class="form-control" name="firstname" placeholder="First Name" maxlength="100" required>
        <input type="text" class="form-control" name="lastname" placeholder="Last Name" maxlength="100" required>
        <input type="email" class="form-control" name="email" disabled>
        <input type="password" class="form-control" name="password" placeholder="New password (12+ characters)" minlength="12" maxlength="128" required>
        <input type="password" class="form-control" name="repeat-password" placeholder="Repeat new password" minlength="12" maxlength="128" required>
        <input type="submit" class="btn btn-primary btn-block" value="Update">
    </form></div>
</div></div></div>
<div class="modal fade" id="deleteModal" role="dialog"><div class="modal-dialog modal-sm"><div class="modal-content">
    <div class="modal-header"><button type="button" class="close" data-dismiss="modal">&times;</button><h4>Delete</h4></div>
    <div class="modal-body"><p>Do you really want to delete this account?</p></div>
    <div class="modal-footer">
        <button type="button" class="btn btn-danger" id="btn-delete-user">Delete</button>
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
    </div>
</div></div></div>
</jsp:body>
</layout:page>
