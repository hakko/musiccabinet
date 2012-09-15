create function music.update_lastfmgroup() returns int as $$
begin

	insert into music.lastfmgroup (group_name, group_name_capitalization)
	select distinct on (upper(group_name)) upper(group_name), group_name
	from music.lastfmgroup_import gi
	where not exists (
		select 1 from music.lastfmgroup where group_name = upper(gi.group_name)
	);

	update music.lastfmgroup g set enabled = false
	where not exists (
		select 1 from music.lastfmgroup_import gi 
		where upper(gi.group_name) = g.group_name
	) and enabled;

	update music.lastfmgroup g set enabled = true
	where exists (
		select 1 from music.lastfmgroup_import gi 
		where upper(gi.group_name) = g.group_name
	) and not enabled;
	
	return 0;
	
end;
$$ language plpgsql;