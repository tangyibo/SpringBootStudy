package com.weishao.migration.utils;

public class JdbcUrlUtil {
	
	public static String findDataBaseNameByUrl(String jdbcUrl) {
		String database = null;
		int pos, pos1;
		String connUri;

		jdbcUrl = jdbcUrl.toLowerCase();

		if (jdbcUrl.startsWith("jdbc:impala")) {
			jdbcUrl = jdbcUrl.replace(":impala", "");
		}

		if (!jdbcUrl.startsWith("jdbc:") || (pos1 = jdbcUrl.indexOf(':', 5)) == -1) {
			throw new IllegalArgumentException("Invalid JDBC url.");
		}

		connUri = jdbcUrl.substring(pos1 + 1);

		if (connUri.startsWith("//")) {
			if ((pos = connUri.indexOf('/', 2)) != -1) {
				database = connUri.substring(pos + 1);
			}
		} else {
			database = connUri;
		}

		if (database.contains("?")) {
			database = database.substring(0, database.indexOf("?"));
		}

		if (database.contains(";")) {
			database = database.substring(0, database.indexOf(";"));
		}

		return database;
	}
}
