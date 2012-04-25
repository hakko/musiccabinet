package com.github.hakko.musiccabinet.dao.util;

/*
 * Enumeration representing defined PostgreSQL functions in this project.
 */
public enum PostgreSQLFunction {

	/* Util schema */
	DROP_FUNCTION("util", "drop_function", "sql/util/drop-function.sql"),
	COUNT_ALL_FUNCTIONS("util", "count_functions", "sql/util/count-functions.sql"),
	COUNT_NAMED_FUNCTION("util", "count_functions", "sql/util/count-named-function.sql"),
	TRUNCATE_ALL_TABLES("util", "truncate_all_tables", "sql/util/truncate-all-tables.sql"),

	/* Music schema */
	GET_ARTIST_ID("music", "get_artist_id", "sql/music/get-artist-id.sql"),
	GET_ALBUM_ID("music", "get_album_id", "sql/music/get-album-id.sql"),
	GET_TRACK_ID("music", "get_track_id", "sql/music/get-track-id.sql"),
	UPDATE_TRACKRELATION_FROM_IMPORT("music", "update_trackrelation_from_import", 
		"sql/music/update-trackrelation-from-import.sql"),
	UPDATE_ARTISTINFO_FROM_IMPORT("music", "update_artistinfo_from_import",
		"sql/music/update-artistinfo-from-import.sql"),
	UPDATE_ALBUMINFO_FROM_IMPORT("music", "update_albuminfo_from_import",
		"sql/music/update-albuminfo-from-import.sql"),
	UPDATE_ARTISTRELATION_FROM_IMPORT("music", "update_artistrelation_from_import", 
		"sql/music/update-artistrelation-from-import.sql"),
	UPDATE_ARTISTTOPTRACK_FROM_IMPORT("music", "update_artisttoptrack_from_import",
		"sql/music/update-artisttoptrack-from-import.sql"),
	UPDATE_ARTISTTOPTAG_FROM_IMPORT("music", "update_artisttoptag_from_import",
		"sql/music/update-artisttoptag-from-import.sql"),
	UPDATE_TAGINFO_FROM_IMPORT("music", "update_taginfo_from_import",
		"sql/music/update-taginfo-from-import.sql"),
		
	/* Library schema */
	UPDATE_MUSICFILE_FROM_IMPORT("library", "update_musicfile_from_import", 
		"sql/library/update-musicfile-from-import.sql"),
	UPDATE_MUSICFILE_EXTERNAL_IDS("library", "update_musicfile_external_ids", 
		"sql/library/update-musicfile-external-ids.sql"),
	UPDATE_MUSICDIRECTORY_FROM_IMPORT("library", "update_musicdirectory_from_import", 
		"sql/library/update-musicdirectory-from-import.sql"),
	UPDATE_TRACKPLAYCOUNT_FROM_IMPORT("library", "update_trackplaycount_from_import", 
		"sql/library/update-trackplaycount-from-import.sql");

	private final String schema;
	private final String functionName;
	private final String URI;
	
	PostgreSQLFunction(String schema, String functionName, String URI) {
		this.schema = schema;
		this.functionName = functionName;
		this.URI = URI;
	}

	public String getSchema() {
		return schema;
	}
	
	public String getFunctionName() {
		return functionName;
	}

	public String getURI() {
		return URI;
	}

	@Override
	public String toString() {
		return schema + "." + functionName + " [" + URI + "]";
	}

}