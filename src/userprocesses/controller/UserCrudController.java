package userprocesses.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

//CRUD CLASS
@WebServlet(name = "User", urlPatterns = { "/user.co" })
public class UserCrudController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private User user;
	private ArrayList<User> users;
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

	// METHOD TO PROCESS EVERY REQUEST AND RESPONSE
	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws SecurityException, IOException {

		response.setContentType("application/json");

		String result = "{\"success\":true}"; // RESULT IN CASE EVERYTHING GOES OK
		PrintWriter out = response.getWriter(); // OBJECT TO PRINT RESULTS
		
		try {

			// CHECK IF METHOD IS GET OR POST
			switch (request.getMethod()) {
			case "GET":
				users = (ArrayList<User>) userDao.entities(); // GET ALL OF THE USERS
				out.println(gSon.toJson(users)); // PRINT AN ARRAY OF JSON WITHE ALL OF THE USERS
				break;

			case "POST":
				boolean requiredParam = Validations.crudParams(request); // VERIFY THAT PARAMETERS ARE PRESENT
				
				if (requiredParam) {
					// GET THE PARAMETERS
					int idUser = Integer.parseInt(request.getParameter("idUser"));
					String action = request.getParameter("action");
					String firstName = request.getParameter("firstName");
					String lastName = request.getParameter("lastName");
					String email = request.getParameter("email");
					String password = request.getParameter("password");

					errors = Validations.userValidation(firstName, lastName, email, password); // VALIDATE THE USER
					if (errors.isEmpty()) {
						// SAVE THE USER
						if (action.equalsIgnoreCase("save")) {
							user.setFirstName(firstName);
							user.setLastName(lastName);
							user.setEmail(email);
							user.setPassword(AES256.encryption(password));

							if (userDao.insertEntity(user)) {
								response.setStatus(200);
								out.println(gSon.toJson(result));
							}
						}

						// UPDATE THE USER
						else if (action.equalsIgnoreCase("update")) {
							if (idUser > 0) {
								user.setIdUser(idUser);
								user.setFirstName(firstName);
								user.setLastName(lastName);
								user.setPassword(AES256.encryption(password));

								if (userDao.updateEntity(user)) {
									response.setStatus(200);
									out.println(gSon.toJson(result));
								}
							} else {
								if (errors == null)
									errors = new HashMap<>();
								Error error = new Error();
								error.setMessage("User not found!");
								response.setStatus(404);
								errors.put("notfound", error);
							}
						}

						// DELETE THE USER
						else if (action.equalsIgnoreCase("delete")) {
							HttpSession httpSession = request.getSession(false);
							user = (User) httpSession.getAttribute("user");

							if (userDao.deleteEntity(idUser)) {
								response.setStatus(200);
								if (user.getIdUser() == idUser) {
									String currentSessionDelete = "{\"redirect\":true}";
									httpSession.invalidate();
									out.println(gSon.toJson(currentSessionDelete));
								} else {
									out.println(gSon.toJson(result));
								}
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
				break;

			}
		} catch (Exception ex) {
			if (errors == null)
				errors = new HashMap<>();
			Error error = new Error();
			response.setStatus(500);
			error.setMessage(ex.getMessage());
			errors.put("paramserror", error);
		}

		if (!errors.isEmpty()) {
			out.println(gSon.toJson(errors));
			errors.clear();
		}
		out.close();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public void destroy() {
		super.destroy();
		userDao.closeConnection();
	}
}
