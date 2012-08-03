package com.github.hakko.musiccabinet.dao.util;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.DROP_FUNCTION;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcTemplateDao;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

/**
 * Utility tool for tasks specific for PostgreSQL.
 * 
 * @author haer
 */
public class PostgreSQLUtil {
	
	private PostgreSQLUtil() {}
	
	/**
	 * Load a named SQL function to database.
	 * 
	 * If a previous version with the same name exists, it is dropped by a call to
	 * function drop_procedure (creation of this function is part of database set-up).
	 * 
	 * @param rc			RequestContext for call
	 * @param functionName	Name of function
	 * @param functionBody	Body of function
	 * @throws ApplicationException On failure
	 */
	public static void loadFunction(JdbcTemplateDao dao, PostgreSQLFunction dbFunction) throws ApplicationException {
		JdbcTemplate jdbcTemplate = dao.getJdbcTemplate();

		// initialize drop procedure, if database has been dropped
		jdbcTemplate.execute(new ResourceUtil(DROP_FUNCTION.getURI()).getContent());

		// drop previous function version, if necessary
		jdbcTemplate.queryForInt("select util.drop_function( ?,? )", 
				dbFunction.getSchema(), dbFunction.getFunctionName());

		// load new function body
		jdbcTemplate.execute(new ResourceUtil(dbFunction.getURI()).getContent());
	}

	public static void loadAllFunctions(JdbcTemplateDao dao) throws ApplicationException {
		for (PostgreSQLFunction dbFunction : PostgreSQLFunction.values()) {
			loadFunction(dao, dbFunction);
		}
	}
	
	public static void truncateTables(JdbcTemplateDao dao) throws ApplicationException {
		JdbcTemplate jdbcTemplate = dao.getJdbcTemplate();
		jdbcTemplate.execute("truncate music.artist cascade");
		jdbcTemplate.execute("truncate library.file cascade");
		
		/*
		 * If we really wanted to truncate all tables, we could do: 
		 * loadFunction(dao, PostgreSQLFunction.TRUNCATE_ALL_TABLES);
		 * jdbcTemplate.execute("select util.truncate_all_tables()");
		 * 
		 * Truncating music.artist and library.file reaches pretty much
		 * everything and is by far faster, though.
		 */
	}
	
	/*
	 * Returns a comma-separated list of parameters, to be used in a
	 * prepared statement where the number of arguments is variable.
	 */
	public static String getParameters(int nrOfArgs) {
		char[] chars = new char[nrOfArgs * 2 - 1];
		for (int i = 0; i < nrOfArgs; i++) {
			chars[i * 2] = '?';
		}
		for (int i = 1; i < nrOfArgs; i++) {
			chars[i * 2 - 1] = ',';
		}
		return new String(chars);
	}
	
	public static String getIdParameters(List<Integer> ids) {
		StringBuilder sb = new StringBuilder();
		if (ids.size() > 0) {
			sb.append(ids.get(0));
		}
		for (int i = 1; i < ids.size(); i++) {
			sb.append(",").append(ids.get(i));
		}
		return sb.toString();
	}
	
}