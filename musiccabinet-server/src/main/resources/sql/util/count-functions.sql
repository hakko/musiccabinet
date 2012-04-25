create function util.count_functions() returns int as $$
declare
	nr_of_functions int;
begin

	select count(*) into nr_of_functions from pg_proc;
	return nr_of_functions;

end;
$$ language plpgsql;
