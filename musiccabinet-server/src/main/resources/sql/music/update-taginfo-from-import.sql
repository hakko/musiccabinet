create function music.update_taginfo_from_import() returns int as $$
begin

	-- update all import rows to correct tag id
	update music.taginfo_import set tag_id = music.tag.id
	from music.tag
		where music.taginfo_import.tag_name = music.tag.tag_name;

	-- add new tag infos.
	insert into music.taginfo (tag_id, summary, content)
	select tag_id, summary, content
	from music.taginfo_import;
	
	-- clear import.
	delete from music.taginfo_import;

	return 0;

end;
$$ language plpgsql;