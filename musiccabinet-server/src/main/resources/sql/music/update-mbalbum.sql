create function music.update_mbalbum() returns int as $$
begin

	-- create new format(s)
	insert into music.mb_format (description)
	select distinct format from music.mb_album_import i where format is not null and
	not exists (select 1 from music.mb_format where upper(description) = upper(i.format));

	-- update format ids
	update music.mb_album_import i set format_id = f.id from music.mb_format f
	where upper(i.format) = upper(f.description);

	-- create new label(s)
	insert into music.mb_label (mbid, name)
	select distinct label_mbid, label_name from music.mb_album_import i 
	where label_mbid is not null and label_name is not null and
	not exists (select 1 from music.mb_label where mbid = i.label_mbid);
	
	-- update label ids
	update music.mb_album_import i set label_id = l.id from music.mb_label l
	where label_mbid = l.mbid;

	-- delete duplicate releases (only save earliest, duplicates may still exists)
	delete from music.mb_album_import d where exists (
		select 1 from music.mb_album_import i where 
			d.release_group_mbid = i.release_group_mbid and
			(d.release_year > i.release_year or d.format_id > i.format_id));

	-- add new music.albums
	insert into music.album (artist_id, album_name, album_name_capitalization)
	select distinct on (artist_id, upper(title)) artist_id, upper(title), title
	from music.mb_album_import i where not exists (select 1 from music.album 
		where artist_id = i.artist_id and album_name = upper(i.title));

	-- update all import rows to correct album id
	update music.mb_album_import i set album_id = a.id from music.album a 
	where upper(i.title) = a.album_name and i.artist_id = a.artist_id;

	-- add new music.mb_albums.
	insert into music.mb_album (album_id, mbid, type_id, label_id, format_id, first_release_year)
	select distinct on (release_group_mbid) 
	album_id, release_group_mbid, type_id, label_id, format_id, release_year
	from music.mb_album_import i
		where not exists (select 1 from music.mb_album 
			where release_group_mbid = i.release_group_mbid);

	return 0;

end;
$$ language plpgsql;