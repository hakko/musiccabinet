create function music.update_artistrelation_from_import() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct upper(target_artist_name), target_artist_name from music.artistrelation_import
		where not exists (select 1 from music.artist 
			where artist_name = upper(music.artistrelation_import.target_artist_name));

	-- update all import rows to correct artist id
	update music.artistrelation_import set target_id = music.artist.id
	from music.artist
		where upper(music.artistrelation_import.target_artist_name) = music.artist.artist_name;

	-- update existing (changed) artist relations
	update music.artistrelation set 
		weight = music.artistrelation_import.weight
	from music.artistrelation_import
		where music.artistrelation.target_id = music.artistrelation_import.target_id and
			  music.artistrelation.source_id = music.artistrelation_import.source_id and
			  music.artistrelation.weight <> music.artistrelation_import.weight;

	-- add new artist relations.
	-- the max(weight)/group by is there because last.fm have sometimes sent different
	-- weights for identical relations, and that would violate a database index.
	insert into music.artistrelation (source_id, target_id, weight)
	select source_id, target_id, max(weight) from music.artistrelation_import
		where not exists (select 1 from music.artistrelation
			where source_id = music.artistrelation_import.source_id and
				  target_id = music.artistrelation_import.target_id)
		group by target_id, source_id;

	return 0;

end;
$$ language plpgsql;