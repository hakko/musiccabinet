create function music.get_lastfmgroup_id(groupname text) returns int as $$
declare
	lastfmgroupid int;	
begin

	select id into lastfmgroupid
	from music.lastfmgroup where group_name = upper(groupname);
	
	if (lastfmgroupid is not null) then
		return lastfmgroupid;
	end if;

	insert into music.lastfmgroup (group_name, group_name_capitalization) 
	values (upper(groupname), groupname);

	return currval('music.lastfmgroup_id_seq');
	
end;
$$ language plpgsql;