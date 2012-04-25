create function music.get_artist_id(artistname text) returns int as $$
declare
	artistid int;	
begin

	select id into artistid
	from music.artist where artist_name = upper(artistname);
	
	if (artistid is not null) then
		return artistid;
	end if;

	insert into music.artist (artist_name, artist_name_capitalization) 
	values (upper(artistname), artistname);

	return currval('music.artist_id_seq');
	
end;
$$ language plpgsql;