package dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import utils.Log;

/**
 * Connection source. Uses the pooled JNDI DataSource "java:comp/env/jdbc/CommunityHub"
 * when the server defines one; otherwise falls back to DriverManager with settings from
 * environment variables (DB_URL, DB_USER, DB_PASSWORD) or db.properties on the classpath.
 */
public class DBConnection {

	private static final String JNDI_NAME = "java:comp/env/jdbc/CommunityHub";

	private static volatile boolean initialised;
	private static DataSource dataSource;
	private static String url;
	private static String user;
	private static String password;

	private static synchronized void init() {
		if (initialised)
			return;
		try {
			dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
		} catch (Exception e) {
			dataSource = null;
		}
		Properties props = new Properties();
		try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in != null)
				props.load(in);
		} catch (Exception e) {
			Log.error("Could not read db.properties", e);
		}
		url = setting("DB_URL", props, "db.url", "jdbc:mysql://localhost:3306/CommunityHub");
		user = setting("DB_USER", props, "db.user", "root");
		password = setting("DB_PASSWORD", props, "db.password", "");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			Log.error("MySQL driver not found", e);
		}
		initialised = true;
	}

	private static String setting(String env, Properties props, String key, String fallback) {
		String value = System.getenv(env);
		if (value != null && !value.isEmpty())
			return value;
		return props.getProperty(key, fallback);
	}

	public static Connection getConnection() throws SQLException {
		if (!initialised)
			init();
		if (dataSource != null)
			return dataSource.getConnection();
		return DriverManager.getConnection(url, user, password);
	}
}
