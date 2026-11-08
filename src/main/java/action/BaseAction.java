package action;

import com.opensymphony.xwork2.ActionSupport;

import model.ApiResponse;
import model.ServiceResult;
import utils.Json;
import utils.RequestContext;

/**
 * Base for all actions. Every action writes a JSON ApiResponse and returns NONE, so no
 * Struts result page is involved. Validation errors are handled by the global "input"
 * result (results.JsonInputResult).
 */
public abstract class BaseAction extends ActionSupport {

	private static final long serialVersionUID = 1L;

	protected Long currentUserId() {
		return RequestContext.currentUserId();
	}

	protected boolean hasCapability(String code) {
		return RequestContext.hasCapability(code);
	}

	protected String respond(ServiceResult result) {
		return write(ApiResponse.of(result.isSuccess(), result.getMessage(), result.getStatus(), result.getData()));
	}

	protected String ok(String message) {
		return write(ApiResponse.ok(message));
	}

	protected String ok(String message, Object data) {
		return write(ApiResponse.ok(message, data));
	}

	protected String badRequest(String message) {
		return write(ApiResponse.badRequest(message));
	}

	protected String unauthorized(String message) {
		return write(ApiResponse.unauthorized(message));
	}

	protected String forbidden(String message) {
		return write(ApiResponse.forbidden(message));
	}

	protected String notFound(String entity) {
		return write(ApiResponse.notFound(entity));
	}

	protected String conflict(String message) {
		return write(ApiResponse.conflict(message));
	}

	protected String serverError() {
		return write(ApiResponse.serverError());
	}

	protected static boolean blank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private String write(ApiResponse response) {
		Json.write(response);
		return NONE;
	}
}
