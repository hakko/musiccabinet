create function music.update_tagtopartists() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name from music.tagtopartist_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(music.tagtopartist_import.artist_name));

	-- update all import rows to correct tag id
	update music.tagtopartist_import ti set tag_id = t.id
	from music.tag t
		where lower(ti.tag_name) = t.tag_name;

	-- update all import rows to correct artist id
	update music.tagtopartist_import ti set artist_id = a.id
	from music.artist a
		where upper(ti.artist_name) = a.artist_name;

	-- delete previous top artists for tag
	delete from music.tagtopartist where tag_id in
		(select distinct tag_id from music.tagtopartist_import);

	-- add new top artists for tag
	insert into music.tagtopartist (tag_id, artist_id, rank)
	select tag_id, artist_id, rank from music.tagtopartist_import;

	return 0;

end;
$$ language plpgsql;