create function music.update_groupartistchart() returns int as $$
begin

	-- create missing group(s)
	insert into music.lastfmgroup (group_name, group_name_capitalization)
	select distinct on (upper(lastfmgroup_name)) upper(lastfmgroup_name), lastfmgroup_name 
	from music.groupweeklyartistchart_import i
		where not exists (select 1 from music.lastfmgroup
			where group_name = upper(i.lastfmgroup_name));

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name 
	from music.groupweeklyartistchart_import i
		where not exists (select 1 from music.artist 
			where artist_name = upper(i.artist_name));

	-- update all import rows to correct group id
	update music.groupweeklyartistchart_import i
		set lastfmgroup_id = g.id
	from music.lastfmgroup g
		where upper(i.lastfmgroup_name) = g.group_name;

	-- update all import rows to correct artist id
	update music.groupweeklyartistchart_import i
		set artist_id = a.id
	from music.artist a
		where upper(i.artist_name) = a.artist_name;

	-- delete previous chart artists for group
	delete from music.groupweeklyartistchart where lastfmgroup_id in
		(select distinct lastfmgroup_id from music.groupweeklyartistchart_import);

	-- add new chart artists
	insert into music.groupweeklyartistchart (lastfmgroup_id, artist_id, playcount)
	select lastfmgroup_id, artist_id, playcount from music.groupweeklyartistchart_import;

	return 0;

end;
$$ language plpgsql;