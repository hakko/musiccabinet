create function music.update_userlovedtracks() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name 
	from music.lovedtrack_import lt
		where not exists (select 1 from music.artist 
			where artist_name = upper(lt.artist_name));

	-- update all import rows to correct artist id
	update music.lovedtrack_import lt
		set artist_id = a.id
	from music.artist a where upper(lt.artist_name) = a.artist_name;

	-- update preferred capitalization of all artists in library, if new/changed
	update music.artist a set artist_name_capitalization = lt.artist_name
	from music.lovedtrack_import lt
		where a.id = artist_id and (artist_name_capitalization is null 
		or artist_name_capitalization != lt.artist_name);

	-- create missing track(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct on (artist_id, upper(track_name)) artist_id, upper(track_name), track_name 
	from music.lovedtrack_import lt
		where not exists (select 1 from music.track
			where artist_id = lt.artist_id and track_name = upper(lt.track_name));

	-- update all import rows to correct track id
	update music.lovedtrack_import lt set track_id = t.id
	from music.track t
		where lt.artist_id = t.artist_id and
			  upper(lt.track_name) = t.track_name;

	-- update preferred capitalization of all tracks in library, if new/changed
	update music.track t set track_name_capitalization = lt.track_name
	from music.lovedtrack_import lt
		where t.id = track_id and track_name_capitalization != lt.track_name;

	-- update all import rows to correct user id
	update music.lovedtrack_import lt
		set lastfmuser_id = u.id
	from music.lastfmuser u
		where upper(lt.lastfm_user) = u.lastfm_user;

	-- add new loved tracks from import as starred
	insert into library.starredtrack (lastfmuser_id, album_id, track_id)
	select lti.lastfmuser_id, t.album_id, lti.track_id
	from music.lovedtrack_import lti
	inner join library.track t on lti.track_id = t.track_id
	where not exists (select 1 from library.starredtrack st
		where st.lastfmuser_id = lti.lastfmuser_id and st.track_id = lti.track_id);

	-- remove unloved tracks (those found in loved tracks, but not in import), from starred
	delete from library.starredtrack st
	where exists (select 1 from music.lovedtrack lt 
		where st.lastfmuser_id = lt.lastfmuser_id and st.track_id = lt.track_id)
	and not exists (select 1 from music.lovedtrack_import lti
		where st.lastfmuser_id = lti.lastfmuser_id and st.track_id = lti.track_id);

	-- add new loved tracks from import
	insert into music.lovedtrack (lastfmuser_id, track_id)
	select lastfmuser_id, track_id from music.lovedtrack_import lti
	where not exists (select 1 from music.lovedtrack lt
		where lt.lastfmuser_id = lti.lastfmuser_id and lt.track_id = lti.track_id);
	
	-- removed unloved (those found in loved tracks, but not in import)
	delete from music.lovedtrack lt
	where not exists (select 1 from music.lovedtrack_import lti
		where lt.lastfmuser_id = lti.lastfmuser_id and lt.track_id = lti.track_id);
	
	return 0;
	
end;
$$ language plpgsql;