create function music.update_userrecartists() returns int as $$
begin

	-- create missing user(s)
	insert into music.lastfmuser (lastfm_user, lastfm_user_capitalization)
	select distinct on (upper(lastfm_user)) upper(lastfm_user), lastfm_user 
	from music.userrecommendedartist_import ui
		where not exists (select 1 from music.lastfmuser
			where lastfm_user = upper(ui.lastfm_user));

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name 
	from music.userrecommendedartist_import ui
		where not exists (select 1 from music.artist 
			where artist_name = upper(ui.artist_name));

	-- create missing context artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(contextartist1_name)) upper(contextartist1_name), contextartist1_name 
	from music.userrecommendedartist_import ui
		where not exists (select 1 from music.artist 
			where artist_name = upper(ui.contextartist1_name));

	-- create missing context artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(contextartist2_name)) upper(contextartist2_name), contextartist2_name 
	from music.userrecommendedartist_import ui
		where not exists (select 1 from music.artist 
			where artist_name = upper(ui.contextartist2_name));


	-- update all import rows to correct user id
	update music.userrecommendedartist_import ui 
		set lastfmuser_id = lfu.id
	from music.lastfmuser lfu
		where upper(ui.lastfm_user) = lfu.lastfm_user;

	-- update all import rows to correct artist id
	update music.userrecommendedartist_import ui
		set artist_id = ma.id
	from music.artist ma
		where upper(ui.artist_name) = ma.artist_name;

	-- update all import rows to correct context artist id
	update music.userrecommendedartist_import ui
		set contextartist1_id = ma.id
	from music.artist ma
		where upper(ui.contextartist1_name) = ma.artist_name;

	-- update all import rows to correct context artist id
	update music.userrecommendedartist_import ui
		set contextartist2_id = ma.id
	from music.artist ma
		where upper(ui.contextartist2_name) = ma.artist_name;

	-- delete previous top artists for user
	delete from music.userrecommendedartist where lastfmuser_id in
		(select distinct lastfmuser_id from music.userrecommendedartist_import);

	-- add new top artists for user
	insert into music.userrecommendedartist 
	(lastfmuser_id, artist_id, rank, contextartist1_id, contextartist2_id)
	select lastfmuser_id, artist_id, rank, contextartist1_id, contextartist2_id 
	from music.userrecommendedartist_import;

	return 0;

end;
$$ language plpgsql;