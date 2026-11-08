package model;

/** Outcome of a service call: success flag, HTTP-style status, message and optional data. */
public class ServiceResult {

	private final boolean success;
	private final int status;
	private final String message;
	private final Object data;

	private ServiceResult(boolean success, int status, String message, Object data) {
		this.success = success;
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String getErrorMessage() {
		return success ? null : message;
	}

	public Object getData() {
		return data;
	}

	public static ServiceResult ok() {
		return new ServiceResult(true, 200, "Success", null);
	}

	public static ServiceResult ok(String message) {
		return new ServiceResult(true, 200, message, null);
	}

	public static ServiceResult ok(String message, Object data) {
		return new ServiceResult(true, 200, message, data);
	}

	public static ServiceResult fail(String message) {
		return badRequest(message);
	}

	public static ServiceResult badRequest(String message) {
		return new ServiceResult(false, 400, message, null);
	}

	public static ServiceResult forbidden(String message) {
		return new ServiceResult(false, 403, message, null);
	}

	public static ServiceResult notFound(String entity) {
		return new ServiceResult(false, 404, entity + " not found", null);
	}

	public static ServiceResult conflict(String message) {
		return new ServiceResult(false, 409, message, null);
	}

	public static ServiceResult error() {
		return new ServiceResult(false, 500, "Something went wrong. Please try again.", null);
	}
}
