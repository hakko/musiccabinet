package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.DirectoryBrowserDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.AlbumRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.DirectoryRowMapper;
import com.github.hakko.musiccabinet.domain.model.library.Directory;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcDirectoryBrowserDao implements DirectoryBrowserDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	private static final Logger LOG = Logger.getLogger(JdbcDirectoryBrowserDao.class);

	@Override
	public Set<Directory> getRootDirectories() {
		String sql = "select id, path from library.directory where parent_id is null";
		
		return new TreeSet<>(jdbcTemplate.query(sql, new DirectoryRowMapper()));
	}

	@Override
	public Directory getDirectory(int directoryId) {
		String sql = "select id, path from library.directory where id = " + directoryId;
		
		return jdbcTemplate.queryForObject(sql, new DirectoryRowMapper());
	}
	
	@Override
	public Set<Directory> getSubDirectories(int directoryId) {
		String sql = "select id, path from library.directory where parent_id = " + directoryId;

		return new TreeSet<>(jdbcTemplate.query(sql, new DirectoryRowMapper()));
	}
	
	@Override
	public int getParentId(int directoryId) {
		String sql = "select coalesce(parent_id, -1) from library.directory"
				+ " where id = " + directoryId;
		
		return jdbcTemplate.queryForInt(sql);
	}
	
	@Override
	public void addDirectory(String path, int parentId) {
		String sql = "insert into library.directory (parent_id, path) values (?,?)";
		
		jdbcTemplate.update(sql, parentId, path);
	}
	
	@Override
	public List<Album> getAlbums(int directoryId, boolean sortAscending) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select distinct lt.album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids " 
				+ " from library.directory d"
				+ " inner join library.file f on f.directory_id = d.id" 
				+ " inner join library.filetag ft on ft.file_id = f.id"
				+ " inner join library.track lt on lt.file_id = f.id"
				+ " where d.id = " + directoryId
				+ " group by lt.album_id) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by a.artist_name_capitalization," 
				+ " la.year " + (sortAscending ? "asc" : "desc");

		return jdbcTemplate.query(sql, new AlbumRowMapper());
	}
	
	@Override
	public List<String> getNonAudioFiles(int directoryId) {
		String sql = "select filename from library.file f where directory_id = " + directoryId 
				+ " and not exists (select 1 from library.filetag ft where file_id = f.id)"
				+ " order by lower(filename)";
		
		return jdbcTemplate.queryForList(sql, String.class);
	}
	
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}