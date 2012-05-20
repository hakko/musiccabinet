create function library.update_usertopartists() returns int as $$
begin

	-- create missing user(s)
	insert into library.lastfmuser (lastfm_user, lastfm_user_capitalization)
	select distinct upper(lastfm_user), lastfm_user from library.usertopartist_import
		where not exists (select 1 from library.lastfmuser
			where lastfm_user = upper(library.usertopartist_import.lastfm_user));

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(artist_name), artist_name from library.usertopartist_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(library.usertopartist_import.artist_name));

	-- update all import rows to correct user id
	update library.usertopartist_import set lastfmuser_id = library.lastfmuser.id
	from library.lastfmuser
		where upper(library.usertopartist_import.lastfm_user) = library.lastfmuser.lastfm_user;

	-- update all import rows to correct artist id
	update library.usertopartist_import set artist_id = music.artist.id
	from music.artist
		where upper(library.usertopartist_import.artist_name) = music.artist.artist_name;

	-- delete previous top artists for user
	delete from library.usertopartist where lastfmuser_id in
		(select distinct lastfmuser_id from library.usertopartist_import);

	-- add new top artists for user
	insert into library.usertopartist (lastfmuser_id, artist_id, rank, days)
	select lastfmuser_id, artist_id, rank, days from library.usertopartist_import;

	return 0;

end;
$$ language plpgsql;