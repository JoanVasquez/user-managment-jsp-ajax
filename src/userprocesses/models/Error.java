package userprocesses.models;

//ERROR ENTITY CLASS
public class Error {

	private String code;// STATUS CODE
	private String message;// MESSAGE
	private String description;// DESCRIPTION

	public Error() {
	}

	public Error(String code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
