package com.github.hakko.musiccabinet.dao;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public interface DatabaseAdministrationDao {

	boolean isRDBMSRunning(); // checks if postgresql service is running
	boolean isDatabaseCreated();
	boolean isPasswordCorrect(String password);
	void createEmptyDatabase() throws ApplicationException;
	void forcePasswordUpdate(String password) throws ApplicationException;
	
	int getDatabaseVersion();
	void loadDatabaseUpdate(int versionNumber, String statement);
	
}