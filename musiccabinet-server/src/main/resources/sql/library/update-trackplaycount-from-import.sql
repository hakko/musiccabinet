create function library.update_trackplaycount_from_import() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(artist_name), artist_name from library.trackplaycount_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(library.trackplaycount_import.artist_name));

	-- update all import rows to correct artist id
	update library.trackplaycount_import set artist_id = music.artist.id
	from music.artist
		where upper(library.trackplaycount_import.artist_name) = music.artist.artist_name;

	-- create missing track(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct artist_id, upper(track_name), track_name from library.trackplaycount_import
		where not exists (select 1 from music.track
			where artist_id = library.trackplaycount_import.artist_id and
				  track_name = upper(library.trackplaycount_import.track_name));

	-- update all import rows to correct track id
	update library.trackplaycount_import set track_id = music.track.id
	from music.track
		where library.trackplaycount_import.artist_id = music.track.artist_id and
			  upper(library.trackplaycount_import.track_name) = music.track.track_name;

	-- update changed tracks already in personal library.
	update library.trackplaycount set 
		play_count = library.trackplaycount_import.play_count
	from library.trackplaycount_import
		where library.trackplaycount.track_id = library.trackplaycount_import.track_id and
			  library.trackplaycount.play_count < library.trackplaycount_import.play_count;

	-- add new tracks to personal library.
	insert into library.trackplaycount (track_id, play_count)
	select track_id, play_count from library.trackplaycount_import
		where not exists (select 1 from library.trackplaycount
			where track_id = library.trackplaycount_import.track_id);

	return 0;

end;
$$ language plpgsql;