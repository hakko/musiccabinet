create function music.get_album_id(artistname text, albumname text) returns int as $$
declare
	artistid int;
	albumid int;
begin

	select music.get_artist_id(artistname) into artistid;

	select id into albumid
	from music.album where artist_id = artistid and album_name = upper(albumname);
	
	if (albumid is not null) then
		return albumid;
	end if;

	insert into music.album (artist_id, album_name, album_name_capitalization) 
	values (artistid, upper(albumname), albumname);

	return currval('music.album_id_seq');
	
end;
$$ language plpgsql;