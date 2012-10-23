create function library.add_to_library() returns int as $$
begin

	-- add missing parent directories
	insert into library.directory (path)
	select distinct parent_path from library.directory_import di
		where parent_path is not null and not exists
		(select 1 from library.directory d where d.path = di.parent_path);

	-- add missing directories
	insert into library.directory (path)
	select distinct path from library.directory_import di
		where not exists
		(select 1 from library.directory d where d.path = di.path);

	-- set correct parent directory ids
	update library.directory_import di
		set parent_id = d.id
	from library.directory d where d.path = di.parent_path;

	-- set correct directory ids
	update library.directory d
		set parent_id = di.parent_id
	from library.directory_import di where di.path = d.path;
	
	truncate library.directory_import;

	
	-- update file import to correct directory id
	update library.file_import fi
		set directory_id = d.id
	from library.directory d where d.path = fi.path;

	-- add new files
	insert into library.file (directory_id, filename, modified, size)
	select directory_id, filename, modified, size from
	library.file_import;


	-- metadata:
	-- set correct file ids
	update library.file_headertag_import fht
		set file_id = f.id
	from library.file f inner join library.directory d on f.directory_id = d.id
	where d.path = fht.path and f.filename = fht.filename;

	-- set correct extension type
	update library.file_headertag_import fht
		set type_id = t.id
	from library.fileheader_type t where fht.extension = t.extension;
	
	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name 
	from library.file_headertag_import fht
		where not exists (select 1 from music.artist 
			where artist_name = upper(fht.artist_name));

	-- update all import rows to correct artist id
	update library.file_headertag_import fht
		set artist_id = a.id
	from music.artist a where upper(fht.artist_name) = a.artist_name;

	-- update preferred capitalization of all artists in library, if new/changed
	update music.artist a set artist_name_capitalization = fht.artist_name
	from library.file_headertag_import fht
		where a.id = artist_id and (artist_name_capitalization is null 
		or artist_name_capitalization != fht.artist_name);

	-- create missing album artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(album_artist_name)) upper(album_artist_name), album_artist_name 
	from library.file_headertag_import fht
		where album_artist_name is not null and not exists (select 1 from music.artist 
			where artist_name = upper(fht.album_artist_name));

	-- update all import rows to correct album artist id
	update library.file_headertag_import fht
		set album_artist_id = a.id
	from music.artist a where upper(fht.album_artist_name) = a.artist_name;

	-- update preferred capitalization of all album artists in library, if new/changed
	update music.artist a set artist_name_capitalization = fht.album_artist_name
	from library.file_headertag_import fht
		where a.id = album_artist_id and (artist_name_capitalization is null 
		or artist_name_capitalization != fht.album_artist_name);

	-- create missing sort artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artistsort_name)) upper(artistsort_name), artistsort_name 
	from library.file_headertag_import fht
		where artistsort_name is not null and not exists (select 1 from music.artist 
			where artist_name = upper(fht.artistsort_name));

	-- update preferred capitalization of all sort artists in library, if new/changed
	update music.artist a set artist_name_capitalization = fht.artistsort_name
	from library.file_headertag_import fht
		where a.artist_name = upper(artistsort_name) and 
		artist_name_capitalization != fht.artistsort_name;

	-- create missing sort album artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(albumartistsort_name)) upper(albumartistsort_name), albumartistsort_name 
	from library.file_headertag_import fht
		where albumartistsort_name is not null and not exists (select 1 from music.artist 
			where artist_name = upper(fht.albumartistsort_name));

	-- update preferred capitalization of all sort album artists in library, if new/changed
	update music.artist a set artist_name_capitalization = fht.albumartistsort_name
	from library.file_headertag_import fht
		where a.artist_name = upper(albumartistsort_name) and 
		artist_name_capitalization != fht.albumartistsort_name;

	-- create missing composer(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(composer_name)) upper(composer_name), composer_name 
	from library.file_headertag_import fht
		where composer_name is not null and not exists (select 1 from music.artist 
			where artist_name = upper(fht.composer_name));

	-- update all import rows to correct composer id
	update library.file_headertag_import fht
		set composer_id = a.id
	from music.artist a where upper(fht.composer_name) = a.artist_name;

	-- update preferred capitalization of all composers in library, if new/changed
	update music.artist a set artist_name_capitalization = fht.composer_name
	from library.file_headertag_import fht
		where a.id = composer_id and
		artist_name_capitalization != fht.composer_name;

	-- create missing album(s)
	insert into music.album (artist_id, album_name, album_name_capitalization)
	select distinct on (coalesce(album_artist_id, artist_id), upper(album_name)) 
		coalesce(album_artist_id, artist_id), upper(album_name), album_name 
	from library.file_headertag_import fht
		where fht.album_name is not null
			and not exists (select 1 from music.album
			where artist_id = coalesce(fht.album_artist_id, fht.artist_id) and
				album_name = upper(fht.album_name));

	--update all import rows to correct album id
	update library.file_headertag_import fht
		set album_id = a.id
	from music.album a
		where a.album_name = upper(fht.album_name)
		and a.artist_id = coalesce(fht.album_artist_id, fht.artist_id);

	-- create missing track(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct on (artist_id, upper(track_name)) artist_id, upper(track_name), track_name 
	from library.file_headertag_import fht
		where not exists (select 1 from music.track
			where artist_id = fht.artist_id and
				  track_name = upper(fht.track_name));

	-- update all import rows to correct track id
	update library.file_headertag_import fht set track_id = t.id
	from music.track t
		where fht.artist_id = t.artist_id and
			  upper(fht.track_name) = t.track_name;

	-- update preferred capitalization of all tracks in library, if new/changed
	update music.track t set track_name_capitalization = fht.track_name
	from library.file_headertag_import fht
		where t.id = track_id and track_name_capitalization != fht.track_name;

	-- create missing tag(s)
	insert into music.tag (tag_name)
	select distinct lower(tag_name)
	from library.file_headertag_import fht	
		where tag_name is not null and not exists (select 1 from music.tag
			where tag_name = lower(fht.tag_name));

	-- update all import rows to correct tag id
	update library.file_headertag_import fht set tag_id = t.id
	from music.tag t
		where lower(fht.tag_name) = t.tag_name;
	
	-- update file header (info that never changes)
	insert into library.fileheader (file_id, type_id, bitrate, vbr, duration)
	select file_id, type_id, bitrate, vbr, duration
		from library.file_headertag_import;

	-- update file tag
	insert into library.filetag (file_id, artist_id, album_artist_id, composer_id, album_id, track_id, track_nr, track_nrs, disc_nr, disc_nrs, year, tag_id, coverart, lyrics)
	select file_id, artist_id, album_artist_id, composer_id, album_id, track_id, track_nr, track_nrs, disc_nr, disc_nrs, year, tag_id, coverart, lyrics
		from library.file_headertag_import;

	insert into library.artist (artist_id)
	select distinct artist_id from library.filetag ft
	where not exists (
		select 1 from library.artist where artist_id = ft.artist_id
	);

	insert into library.artist (artist_id)
	select distinct album_artist_id from library.filetag ft
	where album_artist_id is not null and not exists (
		select 1 from library.artist where artist_id = ft.album_artist_id
	);

	insert into library.album (album_id)
	select distinct album_id from library.filetag ft
	where not exists (
		select 1 from library.album where album_id = ft.album_id
	);

	update library.artist art
		set hasalbums = true
	from library.album la 
	inner join music.album ma on la.album_id = ma.id 
	where ma.artist_id = art.artist_id and not art.hasalbums;
	
	insert into library.track (track_id, album_id, file_id)
	select distinct on (coalesce(disc_nr, 0), coalesce(track_nr, 0), track_id, album_id) 
		track_id, album_id, file_id from library.filetag ft
	where not exists (
		select 1 from library.track ext 
		inner join library.filetag exft on exft.file_id = ext.file_id
		where ext.track_id = ft.track_id and ext.album_id = ft.album_id
			and coalesce(exft.track_nr, 0) = coalesce(ft.track_nr, 0)
			and coalesce(exft.disc_nr, 0) = coalesce(ft.disc_nr, 0)
	);

	-- update search tables text search index
	update library.artist la
		set artist_name_search = array_to_string(array(select unnest(string_to_array(artist_name, ' ')) order by 1), ' ')
	from music.artist ma
	where ma.id = la.artist_id and la.artist_name_search is null;

	update library.album la
		set album_name_search = array_to_string(array(select unnest(string_to_array(artist_name || ' ' || album_name, ' ')) order by 1), ' ')
	from music.album malb
	inner join music.artist mart on malb.artist_id = mart.id
	where malb.id = la.album_id and la.album_name_search is null;

	update library.track lt
		set track_name_search = array_to_string(array(select unnest(string_to_array(artist_name || ' ' || album_name || ' ' || track_name, ' ')) order by 1), ' ')
	from music.album malb, music.track mt 
	inner join music.artist mart on mt.artist_id = mart.id
	where mt.id = lt.track_id and malb.id = lt.album_id and lt.track_name_search is null;

	-- set album year from file metadata
	update library.album a set year = ft.year
	from library.filetag ft where a.album_id = ft.album_id and ft.year is not null
		and (a.year is null or a.year != ft.year);

	-- set album embedded cover art from file metadata
	update library.album a set embeddedcoverartfile_id = ft.file_id 
	from library.filetag ft where a.album_id = ft.album_id and ft.coverart
		and a.embeddedcoverartfile_id is null;

	-- set album cover art from found image files. we need to:
	-- * find most prioritized image per folder (in case of multiple cover images
	-- * create a mapping from directory to album (done via filetag)
	-- in case an album exists in multiple directories with different artwork,
	-- no guarantees are given on which one is chosen
	update library.album a
		set coverartfile_id = f.id
	from library.file f 
	inner join (
		select directory_id, min(priority) as priority from library.file f 
		inner join library.coverartfilename c on upper(f.filename) = upper(c.filename) group by directory_id
	) mp on f.directory_id = mp.directory_id
	inner join library.coverartfilename c on upper(f.filename) = upper(c.filename) and mp.priority = c.priority
	inner join (
		select distinct f.directory_id, ft.album_id from library.filetag ft
		inner join library.file f on ft.file_id = f.id
	) da on f.directory_id = da.directory_id
	where da.album_id = a.album_id and a.coverartfile_id is null;

	--  add artist sort for artist
	insert into library.artistsort (artist_id, artistsort_id)
	select distinct on (fht.artist_id) fht.artist_id, a.id from library.file_headertag_import fht
	inner join music.artist a on a.artist_name = upper(fht.artistsort_name)
	where not exists (select 1 from library.artistsort where artistsort_id = fht.artist_id);

	--  add artist sort for album artist
	insert into library.artistsort (artist_id, artistsort_id)
	select distinct on (fht.artist_id) fht.artist_id, a.id from library.file_headertag_import fht
	inner join music.artist a on a.artist_name = upper(fht.albumartistsort_name)
	where not exists (select 1 from library.artistsort where artistsort_id = fht.artist_id);
	
	truncate library.file_headertag_import;
	truncate library.file_import;
	truncate library.directory_import;
	
	-- create set of unique first letters from artist names
	truncate library.artistindex;

	insert into library.artistindex (ascii_code)
	select distinct ascii(artist_name) from music.artist ma 
	inner join library.artist la on la.artist_id = ma.id
	where ascii(artist_name) <= 90;
	
	insert into library.artistindex (ascii_code)
	select ascii('#') from music.artist ma 
	inner join library.artist la on la.artist_id = ma.id
	where ascii(artist_name) > 90 limit 1;
	
	perform library.update_statistics();
	
	return 0;

end;
$$ language plpgsql;