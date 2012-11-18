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
	GET_LASTFMUSER_ID("music", "get_lastfmuser_id", "sql/music/get-lastfmuser-id.sql"),
	GET_LASTFMGROUP_ID("music", "get_lastfmgroup_id", "sql/music/get-lastfmgroup-id.sql"),
	UPDATE_USER_TOP_ARTISTS("music", "update_usertopartists", 
		"sql/music/update-usertopartists.sql"),
	UPDATE_USER_RECOMMENDED_ARTISTS("music", "update_userrecartists", 
		"sql/music/update-userrecommendedartists.sql"),
	UPDATE_LASTFMGROUP("music", "update_lastfmgroup", "sql/music/update-lastfmgroup.sql"),
	UPDATE_TRACKRELATION("music", "update_trackrelation", 
		"sql/music/update-trackrelation.sql"),
	UPDATE_ARTISTINFO("music", "update_artistinfo",
		"sql/music/update-artistinfo.sql"),
	UPDATE_ALBUMINFO("music", "update_albuminfo",
		"sql/music/update-albuminfo.sql"),
	UPDATE_ARTISTRELATION("music", "update_artistrelation", 
		"sql/music/update-artistrelation.sql"),
	UPDATE_ARTISTTOPTRACK("music", "update_artisttoptrack",
		"sql/music/update-artisttoptrack.sql"),
	UPDATE_ARTISTTOPTAG("music", "update_artisttoptag",
		"sql/music/update-artisttoptag.sql"),
	UPDATE_TAGINFO("music", "update_taginfo",
		"sql/music/update-taginfo.sql"),
	UPDATE_TAG_TOP_ARTISTS("music", "update_tagtopartists", 
		"sql/music/update-tagtopartists.sql"),
	UPDATE_GROUP_WEEKLY_ARTIST_CHART("music", "update_groupartistchart", 
		"sql/music/update-groupweeklyartistchart.sql"),
	UPDATE_MB_ARTIST("music", "update_mbartist",
		"sql/music/update-mbartist.sql"),
	UPDATE_MB_ALBUM("music", "update_mbalbum",
		"sql/music/update-mbalbum.sql"),

	/* Library schema */
	UPDATE_TRACKPLAYCOUNT("library", "update_trackplaycount", 
		"sql/library/update-trackplaycount.sql"),
		
	ADD_TO_LIBRARY("library", "add_to_library",
		"sql/library/add-to-library.sql"),
	DELETE_FROM_LIBRARY("library", "delete_from_library",
		"sql/library/delete-from-library.sql"),
	UPDATE_STATISTICS("library", "update_statistics",
		"sql/library/update-statistics.sql"),
	BLOCK_WEBSERVICE("library", "block_webservice",
		"sql/library/block-webservice.sql");

	
	private final String schema;
	private final String functionName;
	private final String URI;
	
	PostgreSQLFunction(String schema, String functionName, String URI) {
		if (schema.length() + 1 + functionName.length() >= 32) {
			throw new IllegalArgumentException("Function name too long!");
		}
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