create function music.update_usertopartists() returns int as $$
begin

	-- create missing user(s)
	insert into music.lastfmuser (lastfm_user, lastfm_user_capitalization)
	select distinct on (upper(lastfm_user)) upper(lastfm_user), lastfm_user from music.usertopartist_import ui
		where not exists (select 1 from music.lastfmuser u
			where u.lastfm_user = upper(ui.lastfm_user));

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name from music.usertopartist_import ui
		where not exists (select 1 from music.artist a
			where a.artist_name = upper(ui.artist_name));

	-- update all import rows to correct user id
	update music.usertopartist_import ui set lastfmuser_id = u.id
	from music.lastfmuser u
		where upper(ui.lastfm_user) = u.lastfm_user;

	-- update all import rows to correct artist id
	update music.usertopartist_import ui set artist_id = a.id
	from music.artist a
		where upper(ui.artist_name) = a.artist_name;

	-- delete previous top artists for user
	delete from music.usertopartist uta where exists
		(select 1 from music.usertopartist_import where lastfmuser_id = uta.lastfmuser_id);

	-- add new top artists for user
	insert into music.usertopartist (lastfmuser_id, artist_id, rank, days)
	select lastfmuser_id, artist_id, rank, days from music.usertopartist_import;

	return 0;

end;
$$ language plpgsql;