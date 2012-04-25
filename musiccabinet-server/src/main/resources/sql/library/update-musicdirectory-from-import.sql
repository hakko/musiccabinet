create function library.update_musicdirectory_from_import() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(artist_name), artist_name from library.musicdirectory_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(library.musicdirectory_import.artist_name));

	-- update all import rows to correct artist id
	update library.musicdirectory_import set artist_id = music.artist.id
	from music.artist
		where music.artist.artist_name = upper(library.musicdirectory_import.artist_name);

	-- create missing album(s)
	insert into music.album (artist_id, album_name, album_name_capitalization)
	select distinct artist_id, upper(album_name), album_name from library.musicdirectory_import
		where library.musicdirectory_import.album_name is not null
			and not exists (select 1 from music.album
			where album_name = upper(library.musicdirectory_import.album_name));

	--update all import rows to correct album id
	update library.musicdirectory_import set album_id = music.album.id
	from music.album
		where music.album.album_name = upper(library.musicdirectory_import.album_name)
		and music.album.artist_id = library.musicdirectory_import.artist_id;

	-- update preferred capitalization of all artists in library, if new/changed
	update music.artist set artist_name_capitalization = library.musicdirectory_import.artist_name
	from library.musicdirectory_import 
		where music.artist.id = artist_id 
		and (artist_name_capitalization is null or artist_name_capitalization != library.musicdirectory_import.artist_name);

	-- update preferred capitalization of all albums in library, if new/changed
	update music.album set album_name_capitalization = library.musicdirectory_import.album_name
	from library.musicdirectory_import
		where library.musicdirectory_import.album_name is not null and music.album.id = album_id
		and (album_name_capitalization is null or album_name_capitalization != library.musicdirectory_import.album_name);

	-- delete paths previously found in personal library, that have now been removed/renamed
	delete from library.musicdirectory
		where not exists (select 1 from library.musicdirectory_import
			where path = library.musicdirectory.path);

	-- update changed directories already in personal library.
	update library.musicdirectory set 
		artist_id = library.musicdirectory_import.artist_id,
		album_id = library.musicdirectory_import.album_id
	from library.musicdirectory_import
		where library.musicdirectory.path = library.musicdirectory_import.path;

	-- add new directories to personal library.
	insert into library.musicdirectory (artist_id, album_id, path)
	select artist_id, album_id, path from library.musicdirectory_import
		where not exists (select 1 from library.musicdirectory
			where path = library.musicdirectory_import.path);

	return 0;

end;
$$ language plpgsql;