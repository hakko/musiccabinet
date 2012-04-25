create function music.get_track_id(artistname text, trackname text) returns int as $$
declare
	artistid int;
	trackid int;
begin

	select music.get_artist_id(artistname) into artistid;

	select id into trackid
	from music.track where artist_id = artistid and track_name = upper(trackname);
	
	if (trackid is not null) then
		return trackid;
	end if;

	insert into music.track (artist_id, track_name, track_name_capitalization) 
	values (artistid, upper(trackname), trackname);

	return currval('music.track_id_seq');
	
end;
$$ language plpgsql;