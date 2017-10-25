<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/dataTables.bootstrap.min.css"/>
        <link rel="stylesheet" href="resources/css/main.css" />

        <title>Insert title here</title>
    </head>

    <body>

        <div class="container">
            <div class="row">
                <div class="alert alert-dismissible" id="panel-alert" role="alert"></div>
                <div class="col-md-10 col-md-offset-1">

                    <div class="panel panel-default panel-table">
                        <div class="panel-heading">
                            <div class="row">
                                <div class="col col-xs-3">
                                    <h3 class="panel-title">Users</h3>
                                </div>
                                <div class="col col-xs-8 text-right">
                                    <button class="btn btn-success" id="btn-logout">Logout!</button>
                                </div>
                            </div>
                        </div>


                        <div class="panel-body">

                            <div id="scrollable">
                                <table id="tbl-user" class="table table-striped table-bordered" cellspacing="0" width="100%">
                                </table>
                            </div>
                        </div>

</div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="updateModal" role="dialog">
            <div class="modal-dialog">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Modal Header</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="update-user">

                            <div class="alert alert-dismissible" id="modal-alert" role="alert"></div>

                            <br>

                            <h1 class="text-center">EDIT</h1>

                            <br>
                            <label>Basic Information</label>
                            <input type="hidden" name="iduser" />
                            
                            <input type="text" class="form-control" name="firstname" placeholder="First Name" required /><br>
                            <input type="text" class="form-control" name="lastname" placeholder="Last Name" required /><br>
                            <input type="email" class="form-control" name="email" disabled /><br>
                            
                            <label>Private Information</label><br>
                            
                            <input type="password" class="form-control" name="password" placeholder="Password" required /><br>
                            <input type="password" class="form-control" name="repeat-password" placeholder="Password" required /><br>
                            <input type="submit" class="btn btn-primary btn-block" value="update">
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>

            </div>
        </div>

        <!-- Modal -->
        <div class="modal fade" id="deleteModal" role="dialog">
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Delete</h4>
                    </div>
                    <div class="modal-body">
                        <p>Do you really want to delete this user?</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger" id="btn-delete-user">Delete</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
        <script src="https://cdn.datatables.net/v/bs/dt-1.10.16/r-2.2.0/sc-1.4.3/datatables.min.js"></script>
        <script src="resources/js/main.js"></script>
    </body>

    </html>