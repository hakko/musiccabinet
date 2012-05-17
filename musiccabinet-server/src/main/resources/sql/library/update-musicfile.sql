create function library.update_musicfile() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(artist_name), artist_name from library.musicfile_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(library.musicfile_import.artist_name));

	-- update all import rows to correct artist id
	update library.musicfile_import set artist_id = music.artist.id
	from music.artist
		where upper(library.musicfile_import.artist_name) = music.artist.artist_name;

	-- update preferred capitalization of all artists in library, if new/changed
	update music.artist set artist_name_capitalization = library.musicfile_import.artist_name
	from library.musicfile_import 
		where music.artist.id = artist_id and (artist_name_capitalization is null 
		or artist_name_capitalization != library.musicfile_import.artist_name);

	-- create missing track(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct artist_id, upper(track_name), track_name from library.musicfile_import
		where not exists (select 1 from music.track
			where artist_id = library.musicfile_import.artist_id and
				  track_name = upper(library.musicfile_import.track_name));

	-- update all import rows to correct track id
	update library.musicfile_import set track_id = music.track.id
	from music.track
		where library.musicfile_import.artist_id = music.track.artist_id and
			  upper(library.musicfile_import.track_name) = music.track.track_name;

	-- delete tracks previously found in personal library, that have now been removed/renamed.
	-- start off by removing them from search index.
	delete from library.artisttoptrackplaycount where music_file_id in ( 
		select id from library.musicfile
			where not exists (select 1 from library.musicfile_import
				where library.musicfile_import.path = library.musicfile.path));

	delete from library.musicfile
		where not exists (select 1 from library.musicfile_import
			where library.musicfile_import.path = library.musicfile.path);

	-- update changed tracks already in personal library.
	update library.musicfile set 
		path = library.musicfile_import.path,
		created = library.musicfile_import.created,
		last_modified = library.musicfile_import.last_modified
	from library.musicfile_import
		where library.musicfile.path = library.musicfile_import.path;
		
	-- add new tracks to personal library.
	-- (distinct, as media folder may appear twice in index file)
	insert into library.musicfile (track_id, path, created, last_modified)
	select distinct track_id, path, created, last_modified from library.musicfile_import
		where not exists (select 1 from library.musicfile
			where path = library.musicfile_import.path);

	return 0;

end;
$$ language plpgsql;