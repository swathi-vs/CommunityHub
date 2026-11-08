package results;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.interceptor.ValidationAware;

import model.ApiResponse;
import utils.Json;

/** Global "input" result: returns validation and type-conversion errors as JSON (400). */
public class JsonInputResult implements Result {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		Map<String, String> errors = new LinkedHashMap<>();
		Object action = invocation.getAction();
		if (action instanceof ValidationAware) {
			ValidationAware va = (ValidationAware) action;
			Map<String, List<String>> fieldErrors = va.getFieldErrors();
			if (fieldErrors != null) {
				fieldErrors.forEach((field, msgs) -> {
					if (msgs != null && !msgs.isEmpty())
						errors.put(field, msgs.get(0));
				});
			}
			if (va.getActionErrors() != null && !va.getActionErrors().isEmpty())
				errors.put("general", va.getActionErrors().iterator().next());
		}
		Json.write(ApiResponse.badRequest("Validation failed", errors));
	}
}
