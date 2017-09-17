<!--<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

    <!DOCTYPE html>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
        <link rel="stylesheet" href="resources/css/session.css" />
        <title>User Processes</title>
    </head>

    <body id="main">

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
                                        <br>

                                        <h1 class="text-center">SIGN IN</h1>

                                        <br>
                                        <input type="email" class="form-control" name="email" placeholder="Email" required/>
                                        <input type="password" class="form-control" name="password" placeholder="Password" required/>
                                        <p class="text-right">
                                            <a href="#" id="forgotten-password">Forgot Password</a>
                                        </p>
                                        <button class="btn btn-success btn-block">LOG IN</button>

                                        <hr>
                                        <p class="text-center">
                                            <a href="#" class="fliper-btn">Create new account?</a>
                                        </p>
                                    </form>

                                </div>

                            </div>
                            <div class="face back">
                                <div class="panel panel-default">

                                    <form id="register-form" class="form-horizontal">

                                        <br>

                                        <h1 class="text-center">SIGN UP</h1>

                                        <br>
                                        <label>Basic Information</label>
                                        <input type="text" class="form-control" name="firstname" placeholder="First Name" required/>
                                        <input type="text" class="form-control" name="lastname" placeholder="Last Name" required/>
                                        <input type="email" class="form-control" name="email" placeholder="Email" required/>

                                        <label>Private Information</label>
                                        <input type="password" class="form-control" name="password" placeholder="Password" required/>
                                        <input type="password" class="form-control" name="repeatpassword" placeholder="Repeat Password" required/>
                                        
                                        <input type="submit" class="btn btn-primary btn-block" value="SIGN UP">

                                        <p class="text-center">
                                            <a href="#" class="fliper-btn">Already have an account?</a>
                                        </p>

                                    </form>

                                </div>
                            </div>
                        </div>
                    </div>

                </div>
                <div class="col-md-4"></div>

            </div>

        </div>
        <!-- /.container -->

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="resources/js/session.js"></script>

    </body>

    </html>