package userprocesses.controller;

import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import userprocesses.models.Error;

//VALIDATION CLASS
public class Validations {

	//VALIDATE CRUD PARAMETERS
	public static boolean crudParams(HttpServletRequest request) {

		boolean requiredParams = true; //INDICATE WHETHER PARAMETERS ARE PRESENT OR NOT

		if (request.getParameter("idUser") == null)
			requiredParams = false;
		else if (request.getParameter("idUser").isEmpty())
			requiredParams = false;

		if (request.getParameter("firstName") == null)
			requiredParams = false;
		else if (request.getParameter("firstName").isEmpty())
			requiredParams = false;

		if (request.getParameter("lastName") == null)
			requiredParams = false;
		else if (request.getParameter("lastName").isEmpty())
			requiredParams = false;

		if (request.getParameter("email") == null)
			requiredParams = false;
		else if (request.getParameter("email").isEmpty())
			requiredParams = false;

		if (request.getParameter("password") == null)
			requiredParams = false;
		else if (request.getParameter("password").isEmpty())
			requiredParams = false;

		if (request.getParameter("action") == null)
			requiredParams = false;
		else if (request.getParameter("action").isEmpty())
			requiredParams = false;

		return requiredParams;
	}

	//VALIDATE SESSION PARAMETERS
	public static boolean sessionParams(HttpServletRequest request) {

		boolean requiredParams = true; //INDICATE WHETHER PARAMETERS ARE PRESENT OR NOT

		if (request.getParameter("email") == null)
			requiredParams = false;
		else if (request.getParameter("email").isEmpty())
			requiredParams = false;

		if (request.getParameter("password") == null)
			requiredParams = false;
		else if (request.getParameter("password").isEmpty())
			requiredParams = false;

		if (request.getParameter("action") == null)
			requiredParams = false;
		else if (request.getParameter("action").isEmpty())
			requiredParams = false;

		return requiredParams;
	}

	//VALIDATE USER DATA
	public static HashMap<String, Error> userValidation(String firstName, String lastName, String email,
			String password) {

		Map<String, Error> errors;

		errors = new HashMap<>();
		errors.clear();
		if (!firstName.matches("^[a-zA-z ]*$")) {
			Error error = new Error();
			error.setMessage("The First Name must be only letters!");
			errors.put("firstNameError", error);
		}

		if (!lastName.matches("^[a-zA-z ]*$")) {
			Error error = new Error();
			error.setMessage("The Last Name must be only letters!");
			errors.put("lastNameError", error);
		}

		try {
			InternetAddress internetAddress = new InternetAddress(email);
			internetAddress.validate();
		} catch (AddressException ae) {
			Error error = new Error();
			error.setMessage("Wrong email format!");
			errors.put("emailError", error);
		}

		return (HashMap<String, Error>) errors;
	}

	//VALIDATE SESSION DATA
	public static HashMap<String, Error> sessionValidation(String email, String password) {

		Map<String, Error> errors = new HashMap<>();

		try {
			InternetAddress internetAddress = new InternetAddress(email);
			internetAddress.validate();
		} catch (AddressException ae) {
			Error error = new Error();
			error.setMessage("Wrong email format!");
			errors.put("emailError", error);
		}

		return (HashMap<String, Error>) errors;
	}

}
