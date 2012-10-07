create function music.get_lastfmuser_id(user_name text) returns int as $$
declare
	lastfmuserid int;	
begin

	select id into lastfmuserid
	from music.lastfmuser where lastfm_user = upper(user_name);
	
	if (lastfmuserid is not null) then
		return lastfmuserid;
	end if;

	insert into music.lastfmuser (lastfm_user, lastfm_user_capitalization) 
	values (upper(user_name), user_name);

	return currval('music.lastfmuser_id_seq');
	
end;
$$ language plpgsql;