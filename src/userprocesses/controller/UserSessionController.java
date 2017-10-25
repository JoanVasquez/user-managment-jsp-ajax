package userprocesses.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import userprocesses.db.dao.UserDao;
import userprocesses.models.Error;
import userprocesses.models.User;
import userprocesses.security.AES256;

//SESSION CLASS
@WebServlet(name = "Session", urlPatterns = { "/session.co" })
public class UserSessionController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private User user;
	private UserDao userDao;
	private Map<String, Error> errors;
	private Gson gSon;

	@Override
	public void init() throws ServletException {
		super.init();
		userDao = new UserDao();
		user = new User();
		errors = new HashMap<>();
		gSon = new GsonBuilder().setPrettyPrinting().create();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String result = "{\"success\":true}";// RESULT IN CASE EVERYTHING GOES OK
		PrintWriter out = response.getWriter();// OBJECT TO PRINT RESULTS
		user = new User();

		HttpSession httpSession = request.getSession(false);

		try {

			boolean requiredParam = Validations.sessionParams(request);// VERIFY THAT PARAMETERS ARE PRESENT

			if (requiredParam) {
				// GET THE PARAMETERS
				String action = request.getParameter("action");
				String email = request.getParameter("email");
				String password = request.getParameter("password");

				errors = Validations.sessionValidation(email, password);// VALIDATE THE USER
				if (errors.isEmpty()) {
					//SIGNIN PROCESS
					if (action.equalsIgnoreCase("signin")) {
						user.setEmail(email);
						user.setPassword(AES256.encryption(password));

						user = userDao.signIn(user);

						if (user != null) {
							httpSession.setAttribute("user", user);
							response.setStatus(200);
							out.println(gSon.toJson(result));
						} else {
							if (errors == null)
								errors = new HashMap<>();
							Error error = new Error();
							error.setMessage("User not found!");
							response.setStatus(404);
							errors.put("notfound", error);
						}
					}
					
					//FORGOTTEN PASSWORD PROCESS
					else if (action.equalsIgnoreCase("forgottenpassword")) {

						String pass = userDao.forgottenPassword(email);

						if (pass != null) {
							SendForgottenPassword.sendEmail(email, pass);
							response.setStatus(200);
							out.println(gSon.toJson(result));
						} else {
							if (errors == null)
								errors = new HashMap<>();
							Error error = new Error();
							error.setMessage("Password not found!");
							response.setStatus(404);
							errors.put("notfound", error);
						}

					}
					
					//LOGOUT PROCESS
					else if (action.equalsIgnoreCase("logout") && errors.isEmpty()) {
						if (httpSession != null) {
							httpSession.invalidate();
							out.println(gSon.toJson(result));
						}
					}	
				}
					
			} else {
				if (errors == null)
					errors = new HashMap<>();
				Error error = new Error();
				error.setMessage("Please set the required datas!");
				response.setStatus(400);
				errors.put("paramserror", error);
			}

		} catch (Exception ex) {
			if (errors == null)
				errors = new HashMap<>();
			Error error = new Error();
			error.setMessage(ex.getMessage());
			response.setStatus(500);
			errors.put("paramserror", error);
		}

		if (!errors.isEmpty()) {
			out.println(gSon.toJson(errors));
			errors.clear();
		}
		out.close();

	}

}
