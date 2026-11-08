package utils;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import model.ApiResponse;

/** Shared Gson instance and the single place that writes JSON responses. */
public final class Json {

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(java.sql.Date.class,
					(JsonSerializer<java.sql.Date>) (d, t, c) -> new JsonPrimitive(d.toString()))
			.registerTypeAdapter(java.sql.Time.class,
					(JsonSerializer<java.sql.Time>) (d, t, c) -> new JsonPrimitive(d.toString()))
			.registerTypeAdapter(java.sql.Timestamp.class,
					(JsonSerializer<java.sql.Timestamp>) (d, t, c) -> new JsonPrimitive(
							new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d)))
			.create();

	private Json() {
	}

	public static void write(ApiResponse response) {
		write(ServletActionContext.getResponse(), response);
	}

	public static void write(HttpServletResponse res, ApiResponse response) {
		try {
			res.setStatus(response.getCode());
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			PrintWriter writer = res.getWriter();
			writer.write(GSON.toJson(response));
			writer.flush();
		} catch (Exception e) {
			Log.error(e);
		}
	}
}
