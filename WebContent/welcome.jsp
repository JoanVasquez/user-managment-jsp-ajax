<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
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
                                <div class="col-xs-6">
                                    <input type="text" class="form-control" id="search" placeholder="Search for...">
                                </div>
                                <div class="col col-xs-3 text-right">
                                    <button class="btn btn-success" id="btn-logout">Logout!</button>
                                </div>
                            </div>
                        </div>


                        <div class="panel-body">

                            <div id="scrollable">
                                <table class="table table-fixed table-striped table-bordered table-list">
                                    <thead>
                                        <tr>
                                            <th>Action</th>
                                            <th class="hidden-xs">ID</th>
                                            <th>First Name</th>
                                            <th>Last Name</th>
                                            <th>Email</th>
                                        </tr>
                                    </thead>

                                    <tbody id="tbl-users">

                                    </tbody>

                                </table>
                            </div>
                        </div>

                        <div class="panel-footer">
                            <div class="row">
                                <div class="col col-xs-4"></div>
                                <div class="col col-xs-8">
                                    <ul class="pagination pull-right" id="pagination">
                                    </ul>
                                </div>
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
        <script type="text/javascript" src="resources/js/main.js"></script>
    </body>

    </html>