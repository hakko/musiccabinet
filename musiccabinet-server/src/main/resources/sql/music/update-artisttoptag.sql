create function music.update_artisttoptag() returns int as $$
declare
	artistid int;
begin

	update music.artisttoptag_import set tag_name = lower(tag_name);

	select artist_id into artistid from music.artisttoptag_import limit 1;

	-- create missing tag(s)
	insert into music.tag (tag_name)
	select distinct tag_name from music.artisttoptag_import imp
		where not exists (select 1 from music.tag t 
			where imp.tag_name = t.tag_name);

	-- update all import rows to correct tag id
	update music.artisttoptag_import imp set tag_id = t.id
	from music.tag t where imp.tag_name = t.tag_name;

	-- clear previous tags for artist
	delete from music.artisttoptag where artist_id = artistid;

	-- add new top tags (import file have shown to contain duplicates).
	insert into music.artisttoptag (artist_id, tag_id, tag_count)
	select distinct artist_id, tag_id, tag_count from music.artisttoptag_import;

	return 0;

end;
$$ language plpgsql;