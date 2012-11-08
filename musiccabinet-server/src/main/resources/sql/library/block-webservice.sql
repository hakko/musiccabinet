create function library.block_webservice(artistid int, calltypeid int) returns int as $$
begin

	if not exists (select 1 from library.webservice_history where artist_id = artistid 
		and calltype_id = calltypeid and invocation_time = 'infinity') then
		
		delete from library.webservice_history 
			where artist_id = artistid and calltype_id = calltypeid;
			
		insert into library.webservice_history (artist_id, calltype_id, invocation_time) 
		values (artistid, calltypeid, 'infinity');
		
	end if;
	
	return 0;

end;
$$ language plpgsql;