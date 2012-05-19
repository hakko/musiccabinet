package com.github.hakko.musiccabinet.dao.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.core.Logger;
import org.postgresql.core.v3.ConnectionFactoryImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.DatabaseAdministrationDao;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/*
 * Handles database status/initialization.
 * 
 * Exposes methods to check
 * - if a RDBMS is running
 * - if our database has been created
 * - if we have credentials for logging on to the database
 * - versioned updates of schemas/tables/indexes/etc within the database.
 * 
 * This implementation uses the postgresql specific idea of "template1", a default
 * database that ships with the RDBMS, and which subsequent databases are based on.
 * 
 * The code uses classes from the postgresql jdbc driver, to keep the database interaction
 * as minimal as possible. Going through the data source would add some overhead when
 * checking if the database is alive and whether we have a valid user account or not,
 * as the data source is configured to sleep/retry a few times. We do want that behavior
 * normally, but not in this particular use-case.
 */
public class JdbcDatabaseAdministrationDao implements DatabaseAdministrationDao, JdbcTemplateDao {

	// template pointing at postgresql default database, which we'll use
	// as a base when creating the musiccabinet database.
	private JdbcTemplate initialJdbcTemplate;
	
	// template pointing at musiccabinet database (or musiccabinet-test, if unit test)
	private JdbcTemplate jdbcTemplate;
	
	// these are parsed from jdbc url
	private String host;
	private int port;
	private String database;

	private static final com.github.hakko.musiccabinet.log.Logger LOG = 
			com.github.hakko.musiccabinet.log.Logger.getLogger(JdbcDatabaseAdministrationDao.class);
	
	/*
	 * Verify that a postgresql server is running.
	 * 
	 * Try sending what pgAdmin would send when connecting to a postgresql server,
	 * and verify that we get an 'R' back as first character from the database
	 * ('R' = authentication request).
	 * 
	 * Iff we get that, we decide that we have a postgresql server at hand.
	 * 
	 * TODO : it would be good to check version of postgresql server, but that isn't
	 * officially available until we have a user account. Checking it indirectly by
	 * looking at line numbers in error message (those change between postgresql
	 * releases) seems too ugly/error prone.
	 */
	@Override
	public boolean isRDBMSRunning() {
		boolean running = false;
		LOG.debug("Assume database is down.");
		try {
			LOG.debug("Connect socket to host" + host + ", port " + port + ".");
			Socket socket = new Socket(host, port);
			LOG.debug("Open socket output stream.");
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			LOG.debug("Print 0 0 0 42 0 3 0 0 u s e r 0 p o s t g r es 0 etc.");
			pw.print(new char[]{
					(char) 0, (char) 0, (char) 0, (char) 42, (char) 0, (char) 3, (char) 0, (char) 0, 
					'u', 's', 'e', 'r', (char) 0, 
					'p', 'o', 's', 't', 'g', 'r', 'e', 's', (char) 0, 
					'd', 'a', 't', 'a', 'b', 'a', 's', 'e', (char) 0, 
					't', 'e', 'm', 'p', 'l', 'a', 't', 'e', '1', (char) 0, (char) 0});
			pw.flush();
			LOG.debug("Open socket input stream.");
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			LOG.debug("Read one character.");
			char response = (char) isr.read();
			LOG.debug("Character returned was " + response + ".");
			running = response == 'R';
			socket.close();
			LOG.debug("Close socket, running = " + running);
		} catch (IOException e) {
			LOG.warn("Couldn't connect to Postgres service!", e);
			// expected if database is down, or we've connected to something that's not postgre
		}
		return running;
	}
	
	/*
	 * Assuming that the RDBMS is running, check if database has been created.
	 */
	@Override
	public boolean isDatabaseCreated() {
		boolean databaseCreated;
		try {
			databaseCreated = jdbcTemplate.queryForInt("select 1") == 1;
		} catch (CannotGetJdbcConnectionException e) {
			databaseCreated = false;
		}
		return databaseCreated;
	}

	@Override
	public boolean isPasswordCorrect(String password) {
		boolean passwordCorrect = false;
		try {
			Logger logger = new Logger();
			Properties info = new Properties();
			info.setProperty("password", password);
			ConnectionFactory connectionFactory = new ConnectionFactoryImpl();
			connectionFactory.openConnectionImpl(
					host, port, "postgres", "template1", info, logger);
			passwordCorrect = true;
		} catch (SQLException e) {
			// expected for wrong password
		}
		return passwordCorrect;
	}
	
	/*
	 * Create actual database (musiccabinet, or musiccabinet-test depending on environment).
	 * template0 is used as template since template1 has a few connected sessions through
	 * the database connection pooling, and postgresql has a lock for reading it when
	 * more than one session is connected.
	 */
	@Override
	public void createEmptyDatabase() {
		String createSql = "create database \"" + database + "\" with"
				+ " owner=postgres"
				+ " template=template0"
				+ " encoding='UTF8'"
				+ " connection limit=-1;";
		initialJdbcTemplate.execute(createSql);
	}

	@Override
	public void forcePasswordUpdate(String password) throws ApplicationException {
		// we know it's ComboPooledDataSource, as we define it in applicationContext.xml
		// this is the primary reason for using C3P0 rather than Apache DBCP, since it
		// doesn't support password updates.
		ComboPooledDataSource dataSource = 
				(ComboPooledDataSource) jdbcTemplate.getDataSource();
		ComboPooledDataSource initialDataSource = 
				(ComboPooledDataSource) initialJdbcTemplate.getDataSource();
		dataSource.setPassword(password);
		initialDataSource.setPassword(password);
		try {
			dataSource.softResetDefaultUser();
			initialDataSource.softResetDefaultUser();
		} catch (SQLException e) {
			throw new ApplicationException("Password update failed!", e);
		}
		try {
			initialJdbcTemplate.execute("select 1");
		} catch (DataAccessException e) {
			throw new ApplicationException("Password update failed!", e);
		}
	}
	
	/*
	 * Return id of latest (=greatest) database update,
	 * or 0 if no updates have been made, or if database isn't even initialized.
	 */
	@Override
	public int getDatabaseVersion() {
		String versionSql = "select max(update_id) from util.musiccabinet_version";
		int version = 0;
		try {
			version = jdbcTemplate.queryForInt(versionSql);
		} catch (DataAccessException e) {
			// this is actually part of normal execution flow, however ugly that may seem.
			// it handles a few possible errors (no database, no util schema, no version table)
			// and it seems most appropriate to return the value zero for all of them.
		}
		return version;
	}

	@Override
	public void loadDatabaseUpdate(int versionNumber, String statements) {
		for (String statement : StringUtils.split(statements, ';')) {
			jdbcTemplate.execute(statement);
		}

		String insertSql = "insert into util.musiccabinet_version (update_id) values (?)";
		jdbcTemplate.update(insertSql, versionNumber);
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		parseJDBCURL();
	}
	
	public void setInitialDataSource(DataSource dataSource) {
		this.initialJdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/*
	 * We know for sure that the DataSource is a ComboPooledDataSource,
	 * since we specified it in applicationContext.xml.
	 * 
	 * Use that fact to parse host name and port number out of jdbc url,
	 * which is on form jdbc:postgresql://localhost:5432/musiccabinet.
	 */
	private void parseJDBCURL() {
		ComboPooledDataSource ds = (ComboPooledDataSource) jdbcTemplate.getDataSource();

		String url = ds.getJdbcUrl();
		int i1 = url.indexOf("://") + 3;
		int i2 = url.indexOf(":", i1);
		int i3 = url.indexOf("/", i2);
		
		host = url.substring(i1, i2);
		port = Integer.parseInt(url.substring(i2 + 1, i3));
		database = url.substring(i3 + 1);
	}

}