create function music.update_mbartist() returns int as $$
begin

	-- update all import rows to correct artist id
	update music.mb_artist_import i set artist_id = a.id
	from music.artist a where upper(i.artist_name) = a.artist_name;

	-- delete import rows for wrong artists
	delete from music.mb_artist_import i where artist_id is null or
		not exists (select 1 from library.artist where artist_id = i.artist_id);

	-- update existing (changed) artists
	update music.mb_artist a set
		mbid = i.mbid,
		country_code = i.country_code,
		start_year = i.start_year,
		active = i.active
	from music.mb_artist_import i where a.artist_id = i.artist_id;

	-- add new artists.
	insert into music.mb_artist (artist_id, mbid, country_code, start_year, active)
	select artist_id, mbid, country_code, start_year, active
	from music.mb_artist_import i
		where not exists (select 1 from music.mb_artist where artist_id = i.artist_id);

	return 0;

end;
$$ language plpgsql;