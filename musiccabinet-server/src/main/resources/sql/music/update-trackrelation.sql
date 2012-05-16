create function music.update_trackrelation() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(target_artist_name), target_artist_name from music.trackrelation_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(music.trackrelation_import.target_artist_name));

	-- update all import rows to correct artist id
	update music.trackrelation_import set target_artist_id = music.artist.id
	from music.artist
		where upper(music.trackrelation_import.target_artist_name) = music.artist.artist_name;

	-- create missing track(s)
	insert into music.track (artist_id, track_name, track_name_capitalization)
	select distinct target_artist_id, upper(target_track_name), target_track_name
	from music.trackrelation_import
		where not exists (select 1 from music.track
			where artist_id = music.trackrelation_import.target_artist_id and
				  track_name = upper(music.trackrelation_import.target_track_name));

	-- update all import rows to correct track id
	update music.trackrelation_import set target_id = music.track.id
	from music.track
		where music.trackrelation_import.target_artist_id = music.track.artist_id and
			  upper(music.trackrelation_import.target_track_name) = music.track.track_name;

	-- update existing (changed) track relations
	update music.trackrelation set 
		weight = music.trackrelation_import.weight
	from music.trackrelation_import
		where music.trackrelation.target_id = music.trackrelation_import.target_id and
			  music.trackrelation.source_id = music.trackrelation_import.source_id and
			  music.trackrelation.weight <> music.trackrelation_import.weight;

	-- add new track relations.
	-- the max(weight)/group by is there because last.fm have sometimes sent different
	-- weights for identical relations, and that would violate a database index.
	insert into music.trackrelation (source_id, target_id, weight)
	select source_id, target_id, max(weight) from music.trackrelation_import
		where not exists (select 1 from music.trackrelation
			where source_id = music.trackrelation_import.source_id and
				  target_id = music.trackrelation_import.target_id)
  		group by target_id, source_id;

	return 0;

end;
$$ language plpgsql;