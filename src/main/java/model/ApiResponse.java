package model;

import java.util.Map;

public class ApiResponse {

	private boolean success;
	private String message;
	private int code;
	private Object data;
	private Map<String, String> errors;

	private ApiResponse(boolean success, String message, int code, Object data, Map<String, String> errors) {
		this.success = success;
		this.message = message;
		this.code = code;
		this.data = data;
		this.errors = errors;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public static ApiResponse of(boolean success, String message, int code, Object data) {
		return new ApiResponse(success, message, code, data, null);
	}

	public static ApiResponse forbidden(String message) {
		return new ApiResponse(false, message, 403, null, null);
	}

	public static ApiResponse unauthorized(String message) {
		return new ApiResponse(false, message, 401, null, null);
	}

	public static ApiResponse ok(String message, Object data) {
		return new ApiResponse(true, message, 200, data, null);
	}

	public static ApiResponse ok(String message) {
		return new ApiResponse(true, message, 200, null, null);
	}

	public static ApiResponse badRequest(String message) {
		return new ApiResponse(false, message, 400, null, null);
	}

	public static ApiResponse badRequest(String message, Map<String, String> errors) {
		return new ApiResponse(false, message, 400, null, errors);
	}

	public static ApiResponse unauthorized() {
		return new ApiResponse(false, "Unauthorized", 401, null, null);
	}

	public static ApiResponse forbidden() {
		return new ApiResponse(false, "Forbidden", 403, null, null);
	}

	public static ApiResponse notFound(String entity) {
		return new ApiResponse(false, entity + " not found", 404, null, null);
	}

	public static ApiResponse conflict(String message) {
		return new ApiResponse(false, message, 409, null, null);
	}

	public static ApiResponse serverError() {
		return new ApiResponse(false, "Something went wrong. Please try again.", 500, null, null);
	}

}