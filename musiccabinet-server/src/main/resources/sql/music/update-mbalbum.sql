create function music.update_mbalbum() returns int as $$
begin

	-- add new albums
	insert into music.album (artist_id, album_name, album_name_capitalization)
	select distinct on (artist_id, upper(album_name)) artist_id, upper(album_name), album_name
	from music.mb_album_import i where not exists (select 1 from music.album 
		where artist_id = i.artist_id and album_name = upper(i.album_name));

	-- update all import rows to correct album id
	update music.mb_album_import i set album_id = a.id from music.album a 
	where upper(i.album_name) = a.album_name and i.artist_id = a.artist_id;

	-- add new albums.
	insert into music.mb_album (album_id, mbid, type_id, release_year)
	select album_id, mbid, type_id, release_year
	from music.mb_album_import i
		where not exists (select 1 from music.mb_album where album_id = i.album_id);

	return 0;

end;
$$ language plpgsql;