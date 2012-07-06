create function music.update_artisttoptrack() returns int as $$
declare
	artistid int;
begin

	select artist_id into artistid from music.artisttoptrack_import limit 1;

	-- create missing tracks(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct on (artist_id, upper(track_name)) artist_id, upper(track_name), track_name from music.artisttoptrack_import
		where not exists (select 1 from music.track 
			where upper(music.artisttoptrack_import.track_name) = music.track.track_name
			  and music.artisttoptrack_import.artist_id = music.track.artist_id);

	-- update all import rows to correct track id
	update music.artisttoptrack_import set track_id = music.track.id
	from music.track
		where upper(music.artisttoptrack_import.track_name) = music.track.track_name
		  and music.artisttoptrack_import.artist_id = music.track.artist_id;

	-- clear previous tracks for artist
	delete from music.artisttoptrack where artist_id = artistid;

	-- add new top tracks.
	insert into music.artisttoptrack (artist_id, track_id, rank)
	select artist_id, track_id, rank from music.artisttoptrack_import;

	return 0;

end;
$$ language plpgsql;