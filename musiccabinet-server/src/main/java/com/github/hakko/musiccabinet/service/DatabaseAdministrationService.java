package com.github.hakko.musiccabinet.service;

import static org.apache.commons.lang.math.NumberUtils.toInt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.hakko.musiccabinet.dao.DatabaseAdministrationDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcDatabaseAdministrationDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.util.ResourceUtil;

/*
 * Contains database administrative tasks.
 * 
 * Exposed methods are:
 * - check if a database management system (postgresql service) is running on configured port
 * - check database version (which files updating schemas/tables/indexes etc have been run)
 * - run missing database update files, chronologically.
 * 
 * This class also comes with a main class, to allow new developers to install postgresql,
 * check out this code, and then run a single maven command to set up a test database that
 * is up to date with the code base.
 * 
 * As a side-note, the reason for having it this way rather than mocking database calls
 * with an in-memory database like hsqldb is that they currently lack some analysis
 * functionality, like [dense_rank() over (partition by... ) ]. It's also a syntax matter
 * for database functions etc. Therefore, a real test database comes into play already at
 * unit test level, not at integration test level.
 * 
 * TODO : mock a database and test just this class.
 */
public class DatabaseAdministrationService {

	private DatabaseAdministrationDao dbAdmDao;
	
	private static final String VERSION_PROPERTIES = 
		"sql/setup/musiccabinet_version.properties";
	
	private static final String UPDATE_FILE =
		"sql/setup/${update}.sql";
	private static final String UPDATE_KEY = 
		"${update}";
	
	private static final Logger LOG = Logger.getLogger(DatabaseAdministrationService.class);

	/*
	 * Check if we can connect to a database management system (in our case, postgresql).
	 */
    public boolean isRDBMSRunning() {
    	return dbAdmDao.isRDBMSRunning();
    }

    /*
     * Assuming that the database management system is running, check if supplied password
     * allows user 'postgres' to login.
     */
    public boolean isPasswordCorrect(String password) {
    	return dbAdmDao.isPasswordCorrect(password);
    }
    
    /*
     * Check if database version (an integer kept in a table in the database itself)
     * maps to the largest update number defined in the code base.
     */
    public boolean isDatabaseUpdated() {
    	List<Integer> updates = getDatabaseUpdates();
    	return dbAdmDao.getDatabaseVersion() ==
    		updates.get(updates.size() - 1);
    }

    /*
     * 
     */
    public void forcePasswordUpdate(String password) throws ApplicationException {
    	dbAdmDao.forcePasswordUpdate(password);
    }
    
    /*
     * Create database if necessary, load database updates and functions.
     */
    public void loadNewDatabasUpdates() throws ApplicationException {
		if (!dbAdmDao.isDatabaseCreated()) {
			dbAdmDao.createEmptyDatabase();
		}

    	int currentVersion = dbAdmDao.getDatabaseVersion();
    	
    	for (int update : getDatabaseUpdates()) {
    		if (update > currentVersion) {
    			dbAdmDao.loadDatabaseUpdate(update, getStatements(update));
    		}
    	}
    	
    	PostgreSQLUtil.loadAllFunctions((JdbcDatabaseAdministrationDao) dbAdmDao);
    }

    /*
     * Load the list of database updates, defined in properties file.
     * An "update" is an integer number, that maps to a file consisting DDL statements.
     * Updates are always run chronologically, as one update may depend on (or alter) a
     * previous one (relying on a schema, adding/removing a column of a table etc).
     */
    protected List<Integer> getDatabaseUpdates() {
    	List<Integer> updates = new ArrayList<>();
    	Properties props = new Properties();
    	try (ResourceUtil resourceUtil = new ResourceUtil(VERSION_PROPERTIES)) {
    		props.load(resourceUtil.getInputStream());
    	} catch (IOException e) {
    		LOG.warn("Could not load database updates list!", e);
    	}
    	for (Object key : props.keySet()) {
    		updates.add(toInt(key.toString()));
    	}
    	Collections.sort(updates);
    	return updates;
    }

    /*
     * Get DDL statements that a certain update maps to.
     */
    private String getStatements(int update) {
    	String fileUrl = StringUtils.replace(UPDATE_FILE, UPDATE_KEY, ""+update);
    	return new ResourceUtil(fileUrl).getContent();
    }
    
    // Spring setters
    
    public void setDatabaseAdministrationDao(DatabaseAdministrationDao dbAdmDao) {
    	this.dbAdmDao = dbAdmDao;
    }

    // main class, allowing for a single call to setup external database before 
    // running the build process.
    //
    // mvn exec:java -Dexec.mainClass=com.github.hakko.musiccabinet.service.DatabaseAdministrationService
    public static void main(String[] args) throws ApplicationException {
    	ApplicationContext context = new ClassPathXmlApplicationContext(
    			"applicationContext.xml");
    	DatabaseAdministrationService service = 
    		context.getBean(DatabaseAdministrationService.class);
    	if (service.isRDBMSRunning()) {
    		service.loadNewDatabasUpdates();
    	} else {
    		System.out.println("\n[WARN] It doesn't seem like Postgresql is running.\n");
    		System.exit(1);
    	}
    }
    
}