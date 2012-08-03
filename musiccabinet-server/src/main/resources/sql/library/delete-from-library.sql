create function library.delete_from_library() returns int as $$
begin

	with recursive deleted_directories (id) as (
		select id from library.directory
		where path in (select path from library.directory_delete)
			union all
		select d.id from
		deleted_directories dd, library.directory d
		where d.parent_id = dd.id
	)
	update library.directory set deleted = true where id in (
		select id from deleted_directories
	);

	update library.file set deleted = true where directory_id in (
		select id from library.directory where deleted
	);

	update library.file f set deleted = true from library.file_delete fd 
	inner join library.directory d on d.path = fd.path
	where f.directory_id = d.id and f.filename = fd.filename;


	delete from library.file_headertag_import where file_id in (
		select id from library.file where deleted
	);

	delete from library.filetag where file_id in (
		select id from library.file where deleted
	);

	delete from library.fileheader where file_id in (
		select id from library.file where deleted
	);

	update library.album set embeddedcoverartfile_id = null
	where embeddedcoverartfile_id in (
		select id from library.file where deleted
	);
	
	update library.album set coverartfile_id = null
	where coverartfile_id in (
		select id from library.file where deleted
	);


	delete from library.artisttoptrackplaycount where track_id in (
		select id from library.track where file_id in (
			select id from library.file where deleted
		)
	);

	delete from library.track where file_id in (
		select id from library.file where deleted
	);

	delete from library.album where album_id not in (
		select album_id from library.filetag
	);

	delete from library.artist where artist_id not in (
		select coalesce(album_artist_id, artist_id) from library.filetag
	);

	delete from library.artistindex where ascii_code not in (
		select distinct ascii(artist_name) from music.artist ma 
		inner join library.artist la on la.artist_id = ma.id	
	);
	
	delete from library.file where deleted;
	
	delete from library.directory where deleted;
	
	truncate library.file_delete;
	
	truncate library.directory_delete;
	
	perform library.update_statistics();
	
	return 0;

end;
$$ language plpgsql;