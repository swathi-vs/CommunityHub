package results;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

import model.ApiResponse;
import utils.Json;
import utils.Log;

/** Global result for unhandled exceptions: logs the exception and returns a JSON 500. */
public class JsonErrorResult implements Result {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		Object exception = invocation.getStack().findValue("exception");
		if (exception instanceof Throwable)
			Log.error((Throwable) exception);
		Json.write(ApiResponse.serverError());
	}
}
